/**
 * Created Oct 22, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_ACTION_INIT_TRANSFER;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_ACTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_PAYLOAD_TYPE_ACCESS_STATUS;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_PAYLOAD_TYPE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_REPLY_SUBJECT_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_REVERSE_DOWNLOAD_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_STATUS_DENIED;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_STATUS_GRANTED;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_STATUS_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.SHARED_DIRECTORY_ITEM_TYPE;
import static com.roguelogic.roguenet.RNAConstants.SSO_SESSION_TOKEN_PROP;

import java.util.ArrayList;
import java.util.Properties;

import com.roguelogic.entitlements.Item;
import com.roguelogic.entitlements.User;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileStreamReceiver implements RNAPlugIn {

  private PlugInManager plugInManager;

  private static RNAPlugInInfo FileStreamReceiverPII;

  public static final String PPI_LOGICAL_NAME = "File Stream Receiver";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "File Stream Receiver integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    FileStreamReceiverPII = new RNAPlugInInfo();

    FileStreamReceiverPII.setLogicalName(PPI_LOGICAL_NAME);
    FileStreamReceiverPII.setVersion(PPI_VERSION);
    FileStreamReceiverPII.setDescription(PPI_DESCRIPTION);
    FileStreamReceiverPII.setDeveloper(PPI_DEVELOPER);
    FileStreamReceiverPII.setUrl(PPI_URL);
    FileStreamReceiverPII.setCopyright(PPI_COPYRIGHT);
  }

  public FileStreamReceiver() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, FILE_STREAM_RECEIVER_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, FILE_STREAM_RECEIVER_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    String action;

    action = mesg.getProperty(FILE_STREAM_RECEIVER_ACTION_PROP);

    if (FILE_STREAM_RECEIVER_ACTION_INIT_TRANSFER.equals(action)) {
      //Initiate File Transfer
      if (checkDirPermissions(mesg)) {
        //Directory Access OK
        sendAccessGranted(mesg);

        //Trigger Remote Download
        triggerReverseDownload(mesg); //This means "upload" since it really is just a reverse download...
      }
      else {
        //Directory Access Denied
        sendAccessDenied(mesg);
      }
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
    return FileStreamReceiverPII;
  }

  private boolean checkDirPermissions(P2PHubMessage mesg) throws RogueNetException {
    boolean hasAccess = false;
    String sessionToken, itemValue, reqFile;
    User user;
    ArrayList<Item> items;

    if (plugInManager != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
      user = plugInManager.getEntitlementsManager().getSSOController().getSessionUser(sessionToken);

      if (user != null) {
        reqFile = mesg.getProperty(FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP);

        if (!StringUtils.IsNVL(reqFile)) {
          items = user.getItems();

          if (items != null) {
            for (Item item : items) {
              if (SHARED_DIRECTORY_ITEM_TYPE.equals(item.getType())) {
                itemValue = item.getValue();

                if (!StringUtils.IsNVL(itemValue) && (itemValue.equals(reqFile) || itemValue.equals(StringUtils.ParseDir(reqFile))) && item.isWrite()) {
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

  private void sendAccessDenied(P2PHubMessage mesg) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String reqFile;

    if (plugInManager != null && plugInManager.getNetAgent() != null) {
      response = new P2PHubMessage();
      response.setRecipients(new String[] { mesg.getSender() });
      response.setSubject(mesg.getProperty(FILE_STREAM_RECEIVER_REPLY_SUBJECT_PROP));

      resProps = new Properties();
      response.setProperties(resProps);

      resProps.setProperty(FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP, mesg.getProperty(FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP));
      resProps.setProperty(FILE_STREAM_RECEIVER_PAYLOAD_TYPE_PROP, FILE_STREAM_RECEIVER_PAYLOAD_TYPE_ACCESS_STATUS);
      resProps.setProperty(FILE_STREAM_RECEIVER_STATUS_PROP, FILE_STREAM_RECEIVER_STATUS_DENIED);

      reqFile = mesg.getProperty(FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP);

      if (!StringUtils.IsNVL(reqFile)) {
        resProps.setProperty(FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP, reqFile);
      }

      plugInManager.getNetAgent().sendMessage(response);
    }
  }

  private void sendAccessGranted(P2PHubMessage mesg) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String reqFile;

    if (plugInManager != null && plugInManager.getNetAgent() != null) {
      response = new P2PHubMessage();
      response.setRecipients(new String[] { mesg.getSender() });
      response.setSubject(mesg.getProperty(FILE_STREAM_RECEIVER_REPLY_SUBJECT_PROP));

      resProps = new Properties();
      response.setProperties(resProps);

      resProps.setProperty(FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP, mesg.getProperty(FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP));
      resProps.setProperty(FILE_STREAM_RECEIVER_PAYLOAD_TYPE_PROP, FILE_STREAM_RECEIVER_PAYLOAD_TYPE_ACCESS_STATUS);
      resProps.setProperty(FILE_STREAM_RECEIVER_STATUS_PROP, FILE_STREAM_RECEIVER_STATUS_GRANTED);

      reqFile = mesg.getProperty(FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP);

      if (!StringUtils.IsNVL(reqFile)) {
        resProps.setProperty(FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP, reqFile);
      }

      plugInManager.getNetAgent().sendMessage(response);
    }
  }

  private void triggerReverseDownload(P2PHubMessage mesg) throws RogueNetException {
    FileTransferUtility ftUtil;
    String localFile, remoteFile, clientTransId;

    localFile = mesg.getProperty(FILE_STREAM_RECEIVER_UPLOAD_FILE_PROP);
    remoteFile = mesg.getProperty(FILE_STREAM_RECEIVER_REVERSE_DOWNLOAD_FILE_PROP);
    clientTransId = mesg.getProperty(FILE_STREAM_RECEIVER_CLIENT_TRANSFER_ID_PROP);

    ftUtil = (FileTransferUtility) plugInManager.getPlugIn(FileTransferUtility.class.getName());
    ftUtil.initReverseDownload(mesg.getSender(), clientTransId, remoteFile, localFile);
  }

}
