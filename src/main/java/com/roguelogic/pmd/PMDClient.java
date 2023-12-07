/**
 * Created Nov 21, 2007
 */
package com.roguelogic.pmd;

import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_CLOSE_FILE;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_FILE_LENGTH;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_FILE_LIST;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_SHARES;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_LOGIN;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_OPEN_FILE;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_READ_NEXT_FILE_CHUNK;
import static com.roguelogic.pmd.PMDConstants.STATUS_CODE_END_OF_STREAM;
import static com.roguelogic.pmd.PMDConstants.STATUS_CODE_SUCESS;

import java.io.IOException;
import java.io.InputStream;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkUtils;
import com.roguelogic.pmd.protocol.pmd.PMDRemoteInputStream;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class PMDClient implements SocketProcessorCustomizer {

  private SocketClient sockClient;

  private Object syncReqLock;

  private boolean loginResRec;
  private boolean loginOk;
  private boolean openFileResRec;
  private boolean openFileOk;
  private boolean readChunkResRec;
  private boolean readChunkOk;
  private boolean getFileLenResRec;
  private boolean getFileLenOk;
  private boolean getRemoteSharesResRec;
  private boolean getRemoteSharesOk;
  private boolean getRemoteFileListResRec;
  private boolean getRemoteFileListOk;

  private byte[] chunkBuf;
  private int fileLen;
  private String[] remoteShares;
  private String[] remoteFileList;

  public static final long LOGIN_REQUEST_TTL = 60000;
  public static final long OPEN_FILE_REQUEST_TTL = 60000;
  public static final long READ_NEXT_FILE_CHUNK_REQUEST_TTL = 60000;
  public static final long GET_FILE_LEN_REQUEST_TTL = 60000;
  public static final long GET_REMOTE_SHARES_REQUEST_TTL = 60000;
  public static final long GET_REMOTE_FILE_LIST_REQUEST_TTL = 60000;

  public PMDClient() {
    syncReqLock = new Object();
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    PMDClientProcessor pmdcPrc;

    if (processor instanceof PMDClientProcessor) {
      pmdcPrc = (PMDClientProcessor) processor;
      pmdcPrc.setClient(this);
    }
  }

  public synchronized void connect(String address, int port) throws PMDException, RLNetException {
    if (sockClient != null) {
      throw new PMDException("Client Already Connected!");
    }

    sockClient = new SocketClient();
    sockClient.setSocketProcessorClass(PMDClientProcessor.class);
    sockClient.setSocketProcessorCustomizer(this);
    sockClient.connect(address, port);
  }

  public synchronized void close() {
    if (sockClient != null) {
      sockClient.close();
      sockClient = null;
    }
  }

  public synchronized boolean isConnected() {
    return sockClient != null ? sockClient.isConnected() : false;
  }

  public void processLogin(CommandDataPair response) {
    synchronized (syncReqLock) {
      loginResRec = true;

      loginOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      syncReqLock.notifyAll();
    }
  }

  public synchronized void login(String username, String password) throws PMDException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    if (sockClient == null) {
      throw new PMDException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(PMD_RLTCMD_LOGIN);
    request.setData(new StringBuffer().append(username).append("|").append(password).toString());

    synchronized (syncReqLock) {
      loginResRec = false;
      loginOk = false;
      startTime = System.currentTimeMillis();

      RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);

      while (!loginResRec && System.currentTimeMillis() < startTime + LOGIN_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!loginResRec) {
        throw new PMDException("Login Request Timed Out!");
      }

      if (!loginOk) {
        throw new PMDException("Login Failed!");
      }
    }
  }

  public synchronized void openFile(String path) throws PMDException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    if (sockClient == null) {
      throw new PMDException("Client NOT Connected!");
    }

    if (StringUtils.IsNVL(path)) {
      return;
    }

    request = new CommandDataPair();
    request.setCommand(PMD_RLTCMD_OPEN_FILE);
    request.setData(path.trim());

    synchronized (syncReqLock) {
      openFileResRec = false;
      openFileOk = false;
      startTime = System.currentTimeMillis();

      RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);

      while (!openFileResRec && System.currentTimeMillis() < startTime + OPEN_FILE_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!openFileResRec) {
        throw new PMDException("Open File Request Timed Out!");
      }

      if (!openFileOk) {
        throw new PMDException("Open File Request Failed!");
      }
    }
  }

  public synchronized void closeFile() throws PMDException, RLNetException {
    CommandDataPair request;

    if (sockClient == null) {
      throw new PMDException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(PMD_RLTCMD_CLOSE_FILE);

    RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
  }

  public synchronized byte[] readNextFileChunk() throws PMDException, RLNetException, InterruptedException {
    CommandDataPair request;
    byte[] chunk = null;
    long startTime;

    if (sockClient == null) {
      throw new PMDException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(PMD_RLTCMD_READ_NEXT_FILE_CHUNK);

    synchronized (syncReqLock) {
      readChunkResRec = false;
      readChunkOk = false;
      chunkBuf = null;
      startTime = System.currentTimeMillis();

      RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);

      while (!readChunkResRec && System.currentTimeMillis() < startTime + READ_NEXT_FILE_CHUNK_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!readChunkResRec) {
        throw new PMDException("Read Next File Chunk Request Timed Out!");
      }

      if (!readChunkOk) {
        throw new PMDException("Read Next File Chunk Request Failed!");
      }

      chunk = chunkBuf;
    }

    return chunk;
  }

  public synchronized InputStream getRemoteInputStream() throws IOException {
    PMDRemoteInputStream rIns = null;

    if (sockClient == null) {
      throw new IOException("Client NOT Connected!");
    }

    rIns = new PMDRemoteInputStream(this);

    return rIns;
  }

  public void processOpenFile(CommandDataPair response) {
    synchronized (syncReqLock) {
      openFileResRec = true;

      openFileOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      syncReqLock.notifyAll();
    }
  }

  public void processReadNextFileChunk(CommandDataPair response) {
    synchronized (syncReqLock) {
      readChunkResRec = true;

      readChunkOk = response.getStatusCode() == STATUS_CODE_SUCESS || response.getStatusCode() == STATUS_CODE_END_OF_STREAM;
      chunkBuf = response.getData();

      syncReqLock.notifyAll();
    }
  }

  public void processGetFileLen(CommandDataPair response) {
    synchronized (syncReqLock) {
      getFileLenResRec = true;

      getFileLenOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      if (getFileLenOk) {
        fileLen = response.getInt();
      }
      else {
        fileLen = -1;
      }

      syncReqLock.notifyAll();
    }
  }

  public int getFileLength(String path) throws PMDException, RLNetException, InterruptedException {
    int fLen = 0;
    CommandDataPair request;
    long startTime;

    if (sockClient == null) {
      throw new PMDException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(PMD_RLTCMD_GET_FILE_LENGTH);
    request.setData(path);

    synchronized (syncReqLock) {
      getFileLenResRec = false;
      getFileLenOk = false;
      fileLen = 0;
      startTime = System.currentTimeMillis();

      RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);

      while (!getFileLenResRec && System.currentTimeMillis() < startTime + GET_FILE_LEN_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!getFileLenResRec) {
        throw new PMDException("Get File Len Request Timed Out!");
      }

      if (!getFileLenOk) {
        throw new PMDException("Get File Len Request Failed!");
      }

      fLen = fileLen;
    }

    return fLen;
  }

  public void processGetShares(CommandDataPair response) {
    synchronized (syncReqLock) {
      getRemoteSharesResRec = true;

      getRemoteSharesOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      if (getRemoteSharesOk) {
        try {
          if (response.dataLen() > 0) {
            remoteShares = response.getString().split("\\|");
          }
        }
        catch (Exception e) {
          getRemoteSharesOk = false;
          remoteShares = null;
          e.printStackTrace();
        }
      }
      else {
        remoteShares = null;
      }

      syncReqLock.notifyAll();
    }
  }

  public String[] getRemoteShares() throws PMDException, RLNetException, InterruptedException {
    String[] shares = null;
    CommandDataPair request;
    long startTime;

    if (sockClient == null) {
      throw new PMDException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(PMD_RLTCMD_GET_SHARES);

    synchronized (syncReqLock) {
      getRemoteSharesResRec = false;
      getRemoteSharesOk = false;
      remoteShares = null;
      startTime = System.currentTimeMillis();

      RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);

      while (!getRemoteSharesResRec && System.currentTimeMillis() < startTime + GET_REMOTE_SHARES_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!getRemoteSharesResRec) {
        throw new PMDException("Get Remote Shares Request Timed Out!");
      }

      if (!getRemoteSharesOk) {
        throw new PMDException("Get Remote Shares Request Failed!");
      }

      shares = remoteShares;
    }

    return shares;
  }

  public void processGetFileList(CommandDataPair response) {
    synchronized (syncReqLock) {
      getRemoteFileListResRec = true;

      getRemoteFileListOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      if (getRemoteFileListOk) {
        try {
          if (response.dataLen() > 0) {
            remoteFileList = response.getString().split("\\|");
          }
        }
        catch (Exception e) {
          getRemoteFileListOk = false;
          remoteFileList = null;
          e.printStackTrace();
        }
      }
      else {
        remoteFileList = null;
      }

      syncReqLock.notifyAll();
    }
  }

  public String[] getRemoteFileList(String share) throws PMDException, RLNetException, InterruptedException {
    String[] fList = null;
    CommandDataPair request;
    long startTime;

    if (sockClient == null) {
      throw new PMDException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(PMD_RLTCMD_GET_FILE_LIST);
    request.setData(share);

    synchronized (syncReqLock) {
      getRemoteFileListResRec = false;
      getRemoteFileListOk = false;
      remoteFileList = null;
      startTime = System.currentTimeMillis();

      RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);

      while (!getRemoteFileListResRec && System.currentTimeMillis() < startTime + GET_REMOTE_FILE_LIST_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!getRemoteFileListResRec) {
        throw new PMDException("Get Remote File List Request Timed Out!");
      }

      if (!getRemoteFileListOk) {
        throw new PMDException("Get Remote File List Request Failed!");
      }

      fList = remoteFileList;
    }

    return fList;
  }

}
