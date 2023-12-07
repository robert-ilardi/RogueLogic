/**
 * Created Sep 27, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_ACTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_REPLY_SUBJECT_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_REVERSE_DOWNLOAD_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_ACTION_INIT_CLIENT_REQUESTED_TRANSFER;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_ACTION_INIT_TRANSFER;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_ACTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_FILE_SIZE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_DATA_SEGMENT;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_INFO;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_STATUS_ABORTED;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_STATUS_ERROR;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_STATUS_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.FILE_TRANSFER_UTILITY_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.SSO_SESSION_TOKEN_PROP;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.DownloadFile;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNALogger;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.gui.FileTransferProgressDialog;
import com.roguelogic.roguenet.gui.FileTransferUtilityDialog;
import com.roguelogic.util.Base64Codec;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileTransferUtility implements RNAPlugIn {

  public static final String PLUG_IN_NAME = "File Transfer Utility";

  private PlugInManager plugInManager;

  private FileTransferUtilityDialog ftuDialog;

  private HashMap<String, DownloadFile> downloadTransferMap;
  private Object dlTmLock;

  private static RNAPlugInInfo FileTransferUtilityPII;

  public static final String PPI_LOGICAL_NAME = "File Transfer Utility";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "File Transfer Utility integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    FileTransferUtilityPII = new RNAPlugInInfo();

    FileTransferUtilityPII.setLogicalName(PPI_LOGICAL_NAME);
    FileTransferUtilityPII.setVersion(PPI_VERSION);
    FileTransferUtilityPII.setDescription(PPI_DESCRIPTION);
    FileTransferUtilityPII.setDeveloper(PPI_DEVELOPER);
    FileTransferUtilityPII.setUrl(PPI_URL);
    FileTransferUtilityPII.setCopyright(PPI_COPYRIGHT);
  }

  public FileTransferUtility() {
    downloadTransferMap = new HashMap<String, DownloadFile>();
    dlTmLock = new Object();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, FILE_TRANSFER_UTILITY_SUBJECT, PLUG_IN_NAME);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, FILE_TRANSFER_UTILITY_SUBJECT, PLUG_IN_NAME);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {
    if (ftuDialog != null && ftuDialog.isVisible()) {
      ftuDialog.toFront();
    }
    else {
      ftuDialog = new FileTransferUtilityDialog(plugInManager);
      ftuDialog.setVisible(true);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    String payloadType;

    payloadType = mesg.getProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP);

    if (FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_INFO.equals(payloadType)) {
      //Download File Info
      handleDownloadFileInfo(mesg);
    }
    else if (FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS.equals(payloadType)) {
      //Download Access Status
      handleDownloadAccessStatus(mesg);
    }
    else if (FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_DATA_SEGMENT.equals(payloadType)) {
      //Download Data Segment
      handleDownloadDataSegment(mesg);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */

  public RNAPlugInInfo getPlugInInfo() {
    return FileTransferUtilityPII;
  }

  public void initDownload(P2PHubPeer agent, String remoteDir, String remoteFile, String localFilePath, boolean showStatusGUI) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    String sessionToken, transId;
    DownloadFile dload;
    String remoteFilePath;

    if (agent != null && plugInManager != null) {
      sessionToken = plugInManager.getEntitlementsManager().getSSOLedger().getSessionToken(agent.getUsername());
      transId = StringUtils.GenerateTimeUniqueId() + StringUtils.GetRandomChars(10);

      remoteFilePath = remoteDir + "/" + remoteFile;

      mesg = new P2PHubMessage();
      mesg.setSubject(FILE_STREAM_TRANSMITTER_SUBJECT);
      mesg.setRecipients(new String[] { agent.getSessionToken() });

      props = new Properties();
      props.setProperty(SSO_SESSION_TOKEN_PROP, sessionToken);
      props.setProperty(FILE_STREAM_TRANSMITTER_ACTION_PROP, FILE_STREAM_TRANSMITTER_ACTION_INIT_TRANSFER);
      props.setProperty(FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP, FILE_TRANSFER_UTILITY_SUBJECT);
      props.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, remoteFilePath);
      props.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, transId);
      mesg.setProperties(props);

      try {
        dload = new DownloadFile();
        dload.setSsoSessionToken(sessionToken);
        dload.setP2phSessionToken(agent.getSessionToken());
        dload.setClientTransferId(transId);
        dload.setLocalFile(new File(localFilePath));
        dload.setRemoteDir(remoteDir);
        dload.setRemoteFile(remoteFile);

        addDownloadTransaction(dload);

        if (showStatusGUI) {
          new FileTransferProgressDialog(plugInManager, dload); //Display GUI Monitor for File Transfer
        }

        plugInManager.getNetAgent().sendMessage(mesg);
      }
      catch (RogueNetException e) {
        removeDownloadTransaction(transId);
        throw e;
      }
    } //End resource null check
  }

  private void addDownloadTransaction(DownloadFile dload) {
    synchronized (dlTmLock) {
      downloadTransferMap.put(dload.getClientTransferId(), dload);
    }
  }

  private void removeDownloadTransaction(String clientTransferId) {
    synchronized (dlTmLock) {
      downloadTransferMap.remove(clientTransferId);
    }
  }

  private DownloadFile getDownloadTransaction(String clientTransferId) {
    DownloadFile dload;

    synchronized (dlTmLock) {
      dload = downloadTransferMap.get(clientTransferId);
    }

    return dload;
  }

  private void handleDownloadFileInfo(P2PHubMessage mesg) {
    DownloadFile dload;
    String clientTransferId, serverTransferId, tmp;
    long fileSize;

    clientTransferId = mesg.getProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP);
    dload = getDownloadTransaction(clientTransferId);

    if (dload != null) {
      serverTransferId = mesg.getProperty(FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP);
      dload.setServerTransferId(serverTransferId);

      tmp = mesg.getProperty(FILE_STREAM_TRANSMITTER_FILE_SIZE_PROP);
      fileSize = Long.parseLong(tmp);
      dload.setTotalSize(fileSize);
    } //End dload null check
  }

  private void handleDownloadAccessStatus(P2PHubMessage mesg) {
    String status, clientTransferId;
    DownloadFile dload;

    status = mesg.getProperty(FILE_STREAM_TRANSMITTER_STATUS_PROP);
    clientTransferId = mesg.getProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP);

    if (FILE_STREAM_TRANSMITTER_STATUS_ABORTED.equals(status) || FILE_STREAM_TRANSMITTER_STATUS_ERROR.equals(status)) {
      //Transfer Aborted
      dload = getDownloadTransaction(clientTransferId);

      try {
        dload.closeFile();
      }
      catch (Exception e) {
        RNALogger.GetLogger().log(e);
      }

      removeDownloadTransaction(clientTransferId);

      dload.setAborted(true);
    }
  }

  private void handleDownloadDataSegment(P2PHubMessage mesg) throws RogueNetException {
    DownloadFile dload;
    String clientTransferId;
    byte[] dataSeg;

    try {
      clientTransferId = mesg.getProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP);
      dload = getDownloadTransaction(clientTransferId);

      if (dload != null) {
        dataSeg = Base64Codec.Decode(mesg.getBase64Data(), false);

        if (dataSeg != null && dataSeg.length > 0) {
          dload.getOutputStream().write(dataSeg);

          if (dload.incrementCurrentSize(dataSeg.length) >= dload.getTotalSize()) {
            //Download Completed
            dload.closeFile();
            removeDownloadTransaction(clientTransferId);
          }
        }
      } //End dload null check
    } //End try block
    catch (Exception e) {
      throw new RogueNetException("An error occurred while processing download data segment!", e);
    }
  }

  public void initUpload(P2PHubPeer agent, String localFilePath, String remoteDir, String remoteFile, boolean showStatusGUI) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    String sessionToken, transId;
    DownloadFile dload;
    String remoteFilePath;
    File dloadFile;

    if (agent != null && plugInManager != null) {
      sessionToken = plugInManager.getEntitlementsManager().getSSOLedger().getSessionToken(agent.getUsername());
      transId = StringUtils.GenerateTimeUniqueId() + StringUtils.GetRandomChars(10);

      remoteFilePath = remoteDir + "/" + remoteFile;

      mesg = new P2PHubMessage();
      mesg.setSubject(FILE_STREAM_RECEIVER_SUBJECT);
      mesg.setRecipients(new String[] { agent.getSessionToken() });

      props = new Properties();
      props.setProperty(SSO_SESSION_TOKEN_PROP, sessionToken);
      props.setProperty(FILE_STREAM_RECEIVER_ACTION_PROP, FILE_STREAM_TRANSMITTER_ACTION_INIT_TRANSFER);
      props.setProperty(FILE_STREAM_RECEIVER_REPLY_SUBJECT_PROP, FILE_TRANSFER_UTILITY_SUBJECT);
      props.setProperty(FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP, remoteFilePath);
      props.setProperty(FILE_STREAM_RECEIVER_REVERSE_DOWNLOAD_FILE_PROP, localFilePath);
      props.setProperty(FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP, transId);
      mesg.setProperties(props);

      try {
        dloadFile = new File(localFilePath);

        dload = new DownloadFile();
        dload.setSsoSessionToken(sessionToken);
        dload.setP2phSessionToken(agent.getSessionToken());
        dload.setClientTransferId(transId);
        dload.setLocalFile(dloadFile);
        dload.setTotalSize(dloadFile.length());
        dload.setRemoteDir(remoteDir);
        dload.setRemoteFile(remoteFile);
        dload.setReverse(true);

        scheduleReverseDownload(dload);

        if (showStatusGUI) {
          new FileTransferProgressDialog(plugInManager, dload); //Display GUI Monitor for File Transfer
        }

        plugInManager.getNetAgent().sendMessage(mesg);
      }
      catch (RogueNetException e) {
        //abortUploadTransfer(transId);
        throw e;
      }
    } //End resource null check
  }

  private void scheduleReverseDownload(DownloadFile dload) {
    FileStreamTransmitter fsTrans;

    fsTrans = (FileStreamTransmitter) plugInManager.getPlugIn(FileStreamTransmitter.class.getName());
    fsTrans.scheduleReverseDownload(dload);
  }

  public void initReverseDownload(String peerSessionToken, String clientTransId, String remoteFile, String localFile) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    DownloadFile dload;

    if (plugInManager != null) {
      mesg = new P2PHubMessage();
      mesg.setSubject(FILE_STREAM_TRANSMITTER_SUBJECT);
      mesg.setRecipients(new String[] { peerSessionToken });

      props = new Properties();
      props.setProperty(FILE_STREAM_TRANSMITTER_ACTION_PROP, FILE_STREAM_TRANSMITTER_ACTION_INIT_CLIENT_REQUESTED_TRANSFER);
      props.setProperty(FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP, FILE_TRANSFER_UTILITY_SUBJECT);
      props.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, remoteFile);
      props.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, clientTransId);
      mesg.setProperties(props);

      try {
        dload = new DownloadFile();
        dload.setP2phSessionToken(peerSessionToken);
        dload.setClientTransferId(clientTransId);
        dload.setLocalFile(new File(localFile));
        dload.setRemoteFile(remoteFile);

        addDownloadTransaction(dload);

        plugInManager.getNetAgent().sendMessage(mesg);
      }
      catch (RogueNetException e) {
        removeDownloadTransaction(clientTransId);
        throw e;
      }
    } //End resource null check
  }

}
