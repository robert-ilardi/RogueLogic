/**
 * Created Nov 19, 2006
 */
package com.roguelogic.roguenet;

import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_MESSAGE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_DATA_SEGMENT;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_STATUS_COMPLETED;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_STATUS_ERROR;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_STATUS_PROP;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class DownloadFileSender {

  private static DownloadFileSender FSInstance = null;

  private ArrayList<FileTransferRequest> activeFileReqQueue;
  private Object afrqLock;
  private int queuePos;

  private byte[] segBuf;

  private Thread senderThread;
  private boolean running;
  private Object fstCntrlLock;
  private boolean doSending;

  private PlugInManager piManager;

  private int maxSegmentLen;
  private int sendCycleSleepInterval;

  private HashMap<String, DownloadFile> downloadTrackingMap;
  private Object dtmLock;

  //public static final int MAX_SEGMENT_LEN = 16384; //10240; //16384; //8192; //4096;
  public static final String MESSAGE_FILE_TRANSFER_ERROR = "File Transfer Exception occurred! Transfer Aborted by the Server!";

  public static final String PROP_MAX_SEGMENT_LEN = "FileSenderMaxSegmentLen";
  public static final String PROP_SEND_CYCLE_SLEEP_INTERVAL = "FileSenderSendCycleSleepInterval";

  private DownloadFileSender() {
    activeFileReqQueue = new ArrayList<FileTransferRequest>();
    afrqLock = new Object();
    running = false;
    fstCntrlLock = new Object();
    doSending = false;
    queuePos = 0;
    downloadTrackingMap = new HashMap<String, DownloadFile>();
    dtmLock = new Object();
  }

  public static synchronized DownloadFileSender GetInstance() {
    if (FSInstance == null) {
      FSInstance = new DownloadFileSender();
    }

    return FSInstance;
  }

  private Runnable fsRunnable = new Runnable() {
    public void run() {
      FileTransferRequest curReq;

      try {
        synchronized (fstCntrlLock) {
          running = true;
          fstCntrlLock.notifyAll();
        }

        while (doSending) {
          curReq = getNextRequest(); //Will block until a request is available or if doSending is false, will wake up and return null

          if (curReq != null) {
            synchronized (curReq) {
              try {
                sendNextSegment(curReq);
              }
              catch (Exception e) {
                RNALogger.GetLogger().log(e);
                try {
                  abortFileTransfer(curReq); //Error occurred on this transfer request. Abort the transfer!
                  sendErrorMesg(curReq);
                }
                catch (Exception e2) {
                  RNALogger.GetLogger().log(e2);
                }
              }
            } //End synchronized block
          } //End curReq null check 
        } //End while doSending;
      } //End try block
      catch (Exception e) {
        RNALogger.GetLogger().log(e);
      }
      finally {
        synchronized (fstCntrlLock) {
          running = false;
          fstCntrlLock.notifyAll();
        }
      } //End finally block
    } //End run method
  };

  public void start() throws InterruptedException {
    synchronized (fstCntrlLock) {
      if (!running) {
        doSending = true;
        senderThread = new Thread(fsRunnable);
        senderThread.start();

        while (!running) {
          fstCntrlLock.wait();
        }
      }
    }
  }

  public void stop() throws InterruptedException {
    synchronized (afrqLock) {
      doSending = false;
      afrqLock.notifyAll();
    }

    synchronized (fstCntrlLock) {
      if (running) {
        doSending = false;
        fstCntrlLock.notifyAll();

        while (running) {
          fstCntrlLock.wait();
        }
      }
    }
  }

  public void setPlugInManager(PlugInManager piManager) {
    this.piManager = piManager;
    readProperties();
  }

  private void readProperties() {
    String tmp;

    tmp = piManager.getProperty(PROP_MAX_SEGMENT_LEN);
    maxSegmentLen = Integer.parseInt(tmp);
    segBuf = new byte[maxSegmentLen]; //Reusable

    tmp = piManager.getProperty(PROP_SEND_CYCLE_SLEEP_INTERVAL);
    sendCycleSleepInterval = Integer.parseInt(tmp);
  }

  public void scheduleFileTransfer(FileTransferRequest req) {
    synchronized (afrqLock) {
      //Slow but it works. Since "getting" is easier with an array list then a HashSet
      if (!activeFileReqQueue.contains(req)) {
        activeFileReqQueue.add(req);
        afrqLock.notifyAll();
      }
    }
  }

  public void abortFileTransfer(FileTransferRequest req) throws RogueNetException {
    FileTransferRequest realReq;
    int index;
    String clientTransId = null;
    DownloadFile dload;

    try {
      synchronized (afrqLock) {
        index = activeFileReqQueue.indexOf(req);
        if (index >= 0 && index < activeFileReqQueue.size()) {
          realReq = activeFileReqQueue.remove(index);

          if (realReq != null) {
            synchronized (realReq) {
              clientTransId = realReq.getClientTransferId();
              realReq.setStreamsPermClosed(true);
              realReq.closeFile();
            }
          }

          afrqLock.notifyAll();
        }
      }

      if (clientTransId != null) {
        synchronized (dtmLock) {
          dload = downloadTrackingMap.remove(clientTransId);
        }

        if (dload != null) {
          dload.setAborted(true);
        }
      }
    }
    catch (Exception e) {
      throw new RogueNetException(e);
    }
  }

  /**
   * Get's the next FileTransferRequest from the active request queue
   * using the Round Robin approach to service the queue.
   * Will block until a request is available or if doSending is false,
   * will wake up and return null.
   * @return Next available FileTransferRequest
   */
  private FileTransferRequest getNextRequest() throws InterruptedException {
    FileTransferRequest nextReq = null;

    synchronized (afrqLock) {
      while (doSending && activeFileReqQueue.isEmpty()) {
        afrqLock.wait();
      }

      if (doSending) {
        if (queuePos < activeFileReqQueue.size()) {
          nextReq = activeFileReqQueue.get(queuePos);
          queuePos++;
        }
        else {
          Thread.sleep(sendCycleSleepInterval); //Just sleep a bit once a complete iteration of the queue is accomplished!
          queuePos = 0;
          nextReq = activeFileReqQueue.get(queuePos);
        }
      }
    }

    return nextReq;
  }

  protected void sendNextSegment(FileTransferRequest req) throws RogueNetException, IOException {
    P2PHubMessage response;
    Properties resProps;
    String reqFile, replySubject, requestor, clientTransferId, serverTransferId;
    byte[] dataSegment;
    InputStream ins;
    int len;
    DownloadFile dload;

    if (piManager != null && piManager.getNetAgent() != null) {
      reqFile = req.getRequestFile().getName();
      clientTransferId = req.getClientTransferId();
      requestor = req.getRequestor();
      replySubject = req.getReplySubject();
      serverTransferId = req.getServerTransferId();

      //Create Response Message
      response = new P2PHubMessage();
      response.setRecipients(new String[] { requestor });
      response.setSubject(replySubject);

      resProps = new Properties();
      response.setProperties(resProps);

      resProps.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, clientTransferId);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP, serverTransferId);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP, FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_DATA_SEGMENT);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, reqFile);

      //Read File Data
      ins = req.getInputStream();
      len = ins.read(segBuf);

      if (len > 0) {
        //Data is available; send it!
        dataSegment = new byte[len];
        System.arraycopy(segBuf, 0, dataSegment, 0, len);
        response.setData(dataSegment);
        piManager.getNetAgent().sendMessage(response);

        //Update Download Tracking Information if requested...
        synchronized (dtmLock) {
          dload = downloadTrackingMap.get(clientTransferId);
        }

        if (dload != null) {
          dload.incrementCurrentSize(len);
          if (StringUtils.IsNVL(dload.getServerTransferId())) {
            dload.setServerTransferId(serverTransferId);
            dload.setReplySubject(replySubject);
          }
        }
      }
      else if (len < 0) {
        //No more data available close file and remove file request from active queue.
        req.closeFile();
        abortFileTransfer(req);
        sendTransferCompleted(req);
      }
    }
  }

  private void sendErrorMesg(FileTransferRequest req) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;

    if (piManager != null && piManager.getNetAgent() != null) {
      response = new P2PHubMessage();
      response.setRecipients(new String[] { req.getRequestor() });
      response.setSubject(req.getReplySubject());

      resProps = new Properties();
      response.setProperties(resProps);

      resProps.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, req.getClientTransferId());
      resProps.setProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP, FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_STATUS_PROP, FILE_STREAM_TRANSMITTER_STATUS_ERROR);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_MESSAGE_PROP, MESSAGE_FILE_TRANSFER_ERROR);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, req.getRequestFile().getName());

      piManager.getNetAgent().sendMessage(response);
    }
  }

  private void sendTransferCompleted(FileTransferRequest req) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;

    if (piManager != null && piManager.getNetAgent() != null) {
      response = new P2PHubMessage();
      response.setRecipients(new String[] { req.getRequestor() });
      response.setSubject(req.getReplySubject());

      resProps = new Properties();
      response.setProperties(resProps);

      resProps.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, req.getClientTransferId());
      resProps.setProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP, FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_STATUS_PROP, FILE_STREAM_TRANSMITTER_STATUS_COMPLETED);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, req.getRequestFile().getName());

      piManager.getNetAgent().sendMessage(response);
    }
  }

  public void addDownloadTracking(DownloadFile dload) {
    synchronized (dtmLock) {
      downloadTrackingMap.put(dload.getClientTransferId(), dload);
    }
  }

}
