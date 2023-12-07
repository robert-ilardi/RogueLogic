/**
 * Created Oct 22, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.entitlements.Item;
import com.roguelogic.entitlements.User;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.DownloadFile;
import com.roguelogic.roguenet.DownloadFileSender;
import com.roguelogic.roguenet.FileTransferRequest;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNALogger;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileStreamTransmitter implements RNAPlugIn {

  public static final String MESSAGE_FILE_DOES_NOT_EXIST = "File does NOT exist!";

  private PlugInManager plugInManager;

  private HashMap<String, DownloadFile> reverseDownloadMap;
  private Object rdmLock;

  private static RNAPlugInInfo FileStreamTransmitterPII;

  public static final String PPI_LOGICAL_NAME = "File Stream Transmitter";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "File Stream Transmitter integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    FileStreamTransmitterPII = new RNAPlugInInfo();

    FileStreamTransmitterPII.setLogicalName(PPI_LOGICAL_NAME);
    FileStreamTransmitterPII.setVersion(PPI_VERSION);
    FileStreamTransmitterPII.setDescription(PPI_DESCRIPTION);
    FileStreamTransmitterPII.setDeveloper(PPI_DEVELOPER);
    FileStreamTransmitterPII.setUrl(PPI_URL);
    FileStreamTransmitterPII.setCopyright(PPI_COPYRIGHT);
  }

  public FileStreamTransmitter() {
    reverseDownloadMap = new HashMap<String, DownloadFile>();
    rdmLock = new Object();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    DownloadFileSender sender;

    this.plugInManager = plugInManager;
    plugInManager.register(this, FILE_STREAM_TRANSMITTER_SUBJECT, null);

    try {
      sender = DownloadFileSender.GetInstance();
      sender.setPlugInManager(this.plugInManager);
      sender.start();
    }
    catch (Exception e) {
      throw new RogueNetException("An error occurred while attempting to start the Download File Sender!", e);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    try {
      plugInManager.unregister(this, FILE_STREAM_TRANSMITTER_SUBJECT, null);
    }
    finally {
      synchronized (rdmLock) {
        reverseDownloadMap.clear();
        reverseDownloadMap = null;
      }

      try {
        DownloadFileSender.GetInstance().stop(); //Force stop the File Sender
      }
      catch (Exception e) {
        RNALogger.GetLogger().log(e);
      }
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    String action;
    FileTransferRequest ftReq;

    action = mesg.getProperty(FILE_STREAM_TRANSMITTER_ACTION_PROP);

    if (FILE_STREAM_TRANSMITTER_ACTION_INIT_TRANSFER.equals(action)) {
      //Initiate File Transfer
      if (checkFilePermissions(mesg)) {
        //File Access Allowed
        if (checkFileExistence(mesg)) {
          //File Exists
          ftReq = sendFileInfo(mesg);
          scheduleFileTransfer(ftReq);
        }
        else {
          //File does NOT Exist
          sendFileDoesNotExist(mesg);
        }
      }
      else {
        //File Access Denied
        sendAccessDenied(mesg);
      }
    }
    else if (FILE_STREAM_TRANSMITTER_ACTION_INIT_CLIENT_REQUESTED_TRANSFER.equals(action)) {
      //Initiate File Transfer
      if (checkFilePermissionsForUpload(mesg)) {
        //File Access Allowed
        if (checkFileExistence(mesg)) {
          //File Exists
          ftReq = sendFileInfo(mesg);
          enableDownloadTracking(ftReq);
          scheduleFileTransfer(ftReq);
        }
        else {
          //File does NOT Exist
          sendFileDoesNotExist(mesg);
        }
      }
      else {
        //File Access Denied
        sendAccessDenied(mesg);
      }
    }
    else if (FILE_STREAM_TRANSMITTER_ACTION_ABORT_TRANSFER.equals(action)) {
      //Abort File Transfer
      abortFileTransfer(mesg);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return FileStreamTransmitterPII;
  }

  private boolean checkFilePermissions(P2PHubMessage mesg) throws RogueNetException {
    boolean hasAccess = false;
    String sessionToken, itemValue, reqFile;
    User user;
    ArrayList<Item> items;

    if (plugInManager != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
      user = plugInManager.getEntitlementsManager().getSSOController().getSessionUser(sessionToken);

      if (user != null) {
        reqFile = mesg.getProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP);

        if (!StringUtils.IsNVL(reqFile)) {
          items = user.getItems();

          if (items != null) {
            for (Item item : items) {
              if (SHARED_DIRECTORY_ITEM_TYPE.equals(item.getType())) {
                itemValue = item.getValue();

                if (!StringUtils.IsNVL(itemValue) && (itemValue.equals(reqFile) || itemValue.equals(StringUtils.ParseDir(reqFile))) && item.isRead()) {
                  hasAccess = true;
                  break;
                }
              }
            }
          }
        }
      }
    }

    return hasAccess;
  }

  private boolean checkFileExistence(P2PHubMessage mesg) {
    boolean exists = false;
    String reqFile;
    File f;

    reqFile = mesg.getProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP);

    if (!StringUtils.IsNVL(reqFile)) {
      reqFile = reqFile.trim();
      f = new File(reqFile);
      exists = f.exists();
    }

    return exists;
  }

  private void sendAccessDenied(P2PHubMessage mesg) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String reqFile;

    if (plugInManager != null && plugInManager.getNetAgent() != null) {
      response = new P2PHubMessage();
      response.setRecipients(new String[] { mesg.getSender() });
      response.setSubject(mesg.getProperty(FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP));

      resProps = new Properties();
      response.setProperties(resProps);

      resProps.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, mesg.getProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP));
      resProps.setProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP, FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_STATUS_PROP, FILE_STREAM_TRANSMITTER_STATUS_DENIED);

      reqFile = mesg.getProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP);

      if (!StringUtils.IsNVL(reqFile)) {
        resProps.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, reqFile);
      }

      plugInManager.getNetAgent().sendMessage(response);
    }
  }

  private FileTransferRequest sendFileInfo(P2PHubMessage mesg) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String reqFile, replySubject, requestor, clientTransferId, serverTransferId;
    File f;
    FileTransferRequest ftReq = null;

    if (plugInManager != null && plugInManager.getNetAgent() != null) {
      reqFile = mesg.getProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP);

      if (!StringUtils.IsNVL(reqFile)) {
        reqFile = reqFile.trim();
        clientTransferId = mesg.getProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP);
        requestor = mesg.getSender();
        replySubject = mesg.getProperty(FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP);
        serverTransferId = StringUtils.GenerateTimeUniqueId() + StringUtils.GetRandomChars(10);

        f = new File(reqFile);

        //Create Response Message
        response = new P2PHubMessage();
        response.setRecipients(new String[] { requestor });
        response.setSubject(replySubject);

        resProps = new Properties();
        response.setProperties(resProps);

        resProps.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, clientTransferId);
        resProps.setProperty(FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP, serverTransferId);
        resProps.setProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP, FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_FILE_INFO);
        resProps.setProperty(FILE_STREAM_TRANSMITTER_STATUS_PROP, FILE_STREAM_TRANSMITTER_STATUS_GRANTED);
        resProps.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, reqFile);
        resProps.setProperty(FILE_STREAM_TRANSMITTER_FILE_SIZE_PROP, String.valueOf(f.length()));

        //Create File Transfer Request
        ftReq = new FileTransferRequest();
        ftReq.setRequestor(requestor);
        ftReq.setReplySubject(replySubject);
        ftReq.setRequestFile(f);
        ftReq.setClientTransferId(clientTransferId);
        ftReq.setServerTransferId(serverTransferId);

        plugInManager.getNetAgent().sendMessage(response);
      }
    }

    return ftReq;
  }

  private void sendFileDoesNotExist(P2PHubMessage mesg) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String reqFile;

    if (plugInManager != null && plugInManager.getNetAgent() != null) {
      response = new P2PHubMessage();
      response.setRecipients(new String[] { mesg.getSender() });
      response.setSubject(mesg.getProperty(FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP));

      resProps = new Properties();
      response.setProperties(resProps);

      resProps.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, mesg.getProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP));
      resProps.setProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP, FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_STATUS_PROP, FILE_STREAM_TRANSMITTER_STATUS_ERROR);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_MESSAGE_PROP, MESSAGE_FILE_DOES_NOT_EXIST);

      reqFile = mesg.getProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP);

      if (!StringUtils.IsNVL(reqFile)) {
        resProps.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, reqFile);
      }

      plugInManager.getNetAgent().sendMessage(response);
    }
  }

  private void scheduleFileTransfer(FileTransferRequest req) throws RogueNetException {
    DownloadFileSender sender;

    try {
      sender = DownloadFileSender.GetInstance();
      sender.start(); //Make sure the File Sender is started!
      sender.scheduleFileTransfer(req);
    }
    catch (Exception e) {
      throw new RogueNetException("An error occurred while attempting to Schedule a File Transfer!", e);
    }
  }

  public void abortFileTransfer(P2PHubMessage mesg) throws RogueNetException {
    DownloadFileSender sender;
    FileTransferRequest req;
    String serverTransferId;

    serverTransferId = mesg.getProperty(FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP);

    if (!StringUtils.IsNVL(serverTransferId)) {
      req = new FileTransferRequest();
      req.setServerTransferId(serverTransferId);

      sender = DownloadFileSender.GetInstance();
      sender.abortFileTransfer(req);

      sendTransferAborted(mesg);
    }
  }

  private void sendTransferAborted(P2PHubMessage mesg) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String reqFile;

    if (plugInManager != null && plugInManager.getNetAgent() != null) {
      response = new P2PHubMessage();
      response.setRecipients(new String[] { mesg.getSender() });
      response.setSubject(mesg.getProperty(FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP));

      resProps = new Properties();
      response.setProperties(resProps);

      resProps.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, mesg.getProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP));
      resProps.setProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP, FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS);
      resProps.setProperty(FILE_STREAM_TRANSMITTER_STATUS_PROP, FILE_STREAM_TRANSMITTER_STATUS_ABORTED);

      reqFile = mesg.getProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP);

      if (!StringUtils.IsNVL(reqFile)) {
        resProps.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, reqFile);
      }

      plugInManager.getNetAgent().sendMessage(response);
    }
  }

  private boolean checkFilePermissionsForUpload(P2PHubMessage mesg) throws RogueNetException {
    String clientTransId;
    DownloadFile dload;

    clientTransId = mesg.getProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP);
    synchronized (rdmLock) {
      dload = reverseDownloadMap.get(clientTransId);
    }

    return (dload != null);
  }

  public void scheduleReverseDownload(DownloadFile dload) {
    synchronized (rdmLock) {
      reverseDownloadMap.put(dload.getClientTransferId(), dload);
    }
  }

  private void enableDownloadTracking(FileTransferRequest ftReq) {
    DownloadFileSender sender;
    String clientTransId;
    DownloadFile dload;

    clientTransId = ftReq.getClientTransferId();

    synchronized (rdmLock) {
      dload = reverseDownloadMap.remove(clientTransId);
    }

    sender = DownloadFileSender.GetInstance();
    sender.addDownloadTracking(dload);
  }

}
