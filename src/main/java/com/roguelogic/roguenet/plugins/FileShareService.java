/**
 * Created Sep 27, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_DELETE_FILE;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_GET_FILE_LIST;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_GET_USER_SHARED_DIRECTORIES;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_RENAME_FILE;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_FILE_LIST_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_NEW_FILENAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_REQUEST_DIR_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_REQUEST_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_STATUS_FAILURE;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_STATUS_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_STATUS_SUCCESS;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_USER_SHARED_DIRECTORY_LIST_PROP;
import static com.roguelogic.roguenet.RNAConstants.SHARED_DIRECTORY_ITEM_TYPE;
import static com.roguelogic.roguenet.RNAConstants.SSO_SESSION_TOKEN_PROP;
import static com.roguelogic.roguenet.RNAConstants.SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP;

import java.io.File;
import java.io.FileFilter;
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

public class FileShareService implements RNAPlugIn {

  private PlugInManager plugInManager;

  private static RNAPlugInInfo FileShareServicePII;

  public static final String PPI_LOGICAL_NAME = "File Share Service";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "File Share Service (Non-Transfer Operations) integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    FileShareServicePII = new RNAPlugInInfo();

    FileShareServicePII.setLogicalName(PPI_LOGICAL_NAME);
    FileShareServicePII.setVersion(PPI_VERSION);
    FileShareServicePII.setDescription(PPI_DESCRIPTION);
    FileShareServicePII.setDeveloper(PPI_DEVELOPER);
    FileShareServicePII.setUrl(PPI_URL);
    FileShareServicePII.setCopyright(PPI_COPYRIGHT);
  }

  private static FileFilter BasicFileFilter = new FileFilter() {
    public boolean accept(File f) {
      return (f != null && !f.isDirectory());
    }
  };

  public FileShareService() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, FILE_SHARE_SERVICE_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, FILE_SHARE_SERVICE_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    String action;

    action = mesg.getProperty(FILE_SHARE_SERVICE_ACTION_PROP);

    if (FILE_SHARE_SERVICE_ACTION_GET_USER_SHARED_DIRECTORIES.equals(action)) {
      sendUserSharedDirList(mesg);
    }
    else if (FILE_SHARE_SERVICE_ACTION_GET_FILE_LIST.equals(action)) {
      if (checkDirReadPermissions(mesg)) {
        //File Access Allowed
        sendFileList(mesg);
      }
      else {
        //File Access Denied
        sendEmptyFileList(mesg);
      }
    }
    else if (FILE_SHARE_SERVICE_ACTION_RENAME_FILE.equals(action)) {
      if (checkDirWritePermissions(mesg)) {
        //File Access Allowed
        renameFile(mesg);
      }
      else {
        //File Access Denied
        sendFailureStatus(mesg);
      }
    }
    else if (FILE_SHARE_SERVICE_ACTION_DELETE_FILE.equals(action)) {
      if (checkDirWritePermissions(mesg)) {
        //File Access Allowed
        deleteFile(mesg);
      }
      else {
        //File Access Denied
        sendFailureStatus(mesg);
      }
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return FileShareServicePII;
  }

  private void sendUserSharedDirList(P2PHubMessage mesg) throws RogueNetException {
    String[] userSharedDirs;
    P2PHubMessage response;
    Properties resProps;
    String requestor, sessionToken, syncTransId;
    User user;

    if (plugInManager != null && plugInManager.getNetAgent() != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
      user = plugInManager.getEntitlementsManager().getSSOController().getSessionUser(sessionToken);

      if (user != null) {
        userSharedDirs = getUserSharedDirectories(user);

        //Create Synchronous Response Message
        requestor = mesg.getSender();
        syncTransId = mesg.getProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP);

        response = new P2PHubMessage();
        response.setRecipients(new String[] { requestor });
        response.setSubject(SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT);

        resProps = new Properties();
        response.setProperties(resProps);

        resProps.setProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP, syncTransId);

        resProps.setProperty(FILE_SHARE_SERVICE_USER_SHARED_DIRECTORY_LIST_PROP, StringUtils.ArrayToDelimitedString(userSharedDirs, "|", false));

        plugInManager.getNetAgent().sendMessage(response);
      } //End null user check
    } //End resource null check
  }

  private String[] getUserSharedDirectories(User user) throws RogueNetException {
    String[] userSharedDirs = null;
    String itemValue, itemType;
    ArrayList<Item> items;
    ArrayList<String> usdList;

    if (user != null) {
      items = user.getItems();

      if (items != null) {
        usdList = new ArrayList<String>();

        for (Item item : items) {
          itemValue = item.getValue();
          itemType = item.getType();

          if (SHARED_DIRECTORY_ITEM_TYPE.equalsIgnoreCase(itemType) && (item.isRead() || item.isWrite())) {
            usdList.add(itemValue);
          }
        }

        userSharedDirs = new String[usdList.size()];
        userSharedDirs = usdList.toArray(userSharedDirs);
      } //End items null check
    } //End user null check

    return userSharedDirs;
  }

  private void sendFileList(P2PHubMessage mesg) throws RogueNetException {
    String[] fileList;
    P2PHubMessage response;
    Properties resProps;
    String requestor, sessionToken, syncTransId, reqDir;
    User user;

    if (plugInManager != null && plugInManager.getNetAgent() != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
      user = plugInManager.getEntitlementsManager().getSSOController().getSessionUser(sessionToken);

      if (user != null) {
        reqDir = mesg.getProperty(FILE_SHARE_SERVICE_REQUEST_DIR_PROP);
        fileList = getFileList(reqDir);

        //Create Synchronous Response Message
        requestor = mesg.getSender();
        syncTransId = mesg.getProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP);

        response = new P2PHubMessage();
        response.setRecipients(new String[] { requestor });
        response.setSubject(SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT);

        resProps = new Properties();
        response.setProperties(resProps);

        resProps.setProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP, syncTransId);

        resProps.setProperty(FILE_SHARE_SERVICE_FILE_LIST_PROP, StringUtils.ArrayToDelimitedString(fileList, "|", false));

        plugInManager.getNetAgent().sendMessage(response);
      } //End null user check
    } //End resource null check
  }

  private void sendEmptyFileList(P2PHubMessage mesg) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String requestor, sessionToken, syncTransId;
    User user;

    if (plugInManager != null && plugInManager.getNetAgent() != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
      user = plugInManager.getEntitlementsManager().getSSOController().getSessionUser(sessionToken);

      if (user != null) {
        //Create Synchronous Response Message
        requestor = mesg.getSender();
        syncTransId = mesg.getProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP);

        response = new P2PHubMessage();
        response.setRecipients(new String[] { requestor });
        response.setSubject(SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT);

        resProps = new Properties();
        response.setProperties(resProps);

        resProps.setProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP, syncTransId);

        resProps.setProperty(FILE_SHARE_SERVICE_FILE_LIST_PROP, "");

        plugInManager.getNetAgent().sendMessage(response);
      } //End null user check
    } //End resource null check
  }

  private boolean checkDirReadPermissions(P2PHubMessage mesg) throws RogueNetException {
    boolean hasAccess = false;
    String sessionToken, itemValue, reqDir;
    User user;
    ArrayList<Item> items;

    if (plugInManager != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
      user = plugInManager.getEntitlementsManager().getSSOController().getSessionUser(sessionToken);

      if (user != null) {
        reqDir = mesg.getProperty(FILE_SHARE_SERVICE_REQUEST_DIR_PROP);

        if (!StringUtils.IsNVL(reqDir)) {
          items = user.getItems();

          if (items != null) {
            for (Item item : items) {
              if (SHARED_DIRECTORY_ITEM_TYPE.equals(item.getType())) {
                itemValue = item.getValue();

                if (!StringUtils.IsNVL(itemValue) && itemValue.equals(reqDir) && item.isRead()) {
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

  private boolean checkDirWritePermissions(P2PHubMessage mesg) throws RogueNetException {
    boolean hasAccess = false;
    String sessionToken, itemValue, reqFile;
    User user;
    ArrayList<Item> items;

    if (plugInManager != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
      user = plugInManager.getEntitlementsManager().getSSOController().getSessionUser(sessionToken);

      if (user != null) {
        reqFile = mesg.getProperty(FILE_SHARE_SERVICE_REQUEST_DIR_PROP);

        if (!StringUtils.IsNVL(reqFile)) {
          items = user.getItems();

          if (items != null) {
            for (Item item : items) {
              if (SHARED_DIRECTORY_ITEM_TYPE.equals(item.getType())) {
                itemValue = item.getValue();

                if (!StringUtils.IsNVL(itemValue) && itemValue.equals(reqFile) && item.isWrite()) {
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

  private String[] getFileList(String reqDir) {
    String[] fileList = null;
    File dir;
    File[] files;

    dir = new File(reqDir);
    files = dir.listFiles(BasicFileFilter);

    if (files != null) {
      fileList = new String[files.length];

      for (int i = 0; i < files.length; i++) {
        fileList[i] = files[i].getName();
      }
    }

    return fileList;
  }

  private void sendSuccessStatus(P2PHubMessage mesg) throws RogueNetException {
    sendStatusMessage(mesg, FILE_SHARE_SERVICE_STATUS_SUCCESS);
  }

  private void sendFailureStatus(P2PHubMessage mesg) throws RogueNetException {
    sendStatusMessage(mesg, FILE_SHARE_SERVICE_STATUS_FAILURE);
  }

  private void sendStatusMessage(P2PHubMessage mesg, String status) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String requestor, sessionToken, syncTransId;
    User user;

    if (plugInManager != null && plugInManager.getNetAgent() != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
      user = plugInManager.getEntitlementsManager().getSSOController().getSessionUser(sessionToken);

      if (user != null) {
        //Create Synchronous Response Message
        requestor = mesg.getSender();
        syncTransId = mesg.getProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP);

        response = new P2PHubMessage();
        response.setRecipients(new String[] { requestor });
        response.setSubject(SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT);

        resProps = new Properties();
        response.setProperties(resProps);

        resProps.setProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP, syncTransId);

        resProps.setProperty(FILE_SHARE_SERVICE_STATUS_PROP, status);

        plugInManager.getNetAgent().sendMessage(response);
      } //End null user check
    } //End resource null check
  }

  private void renameFile(P2PHubMessage mesg) throws RogueNetException {
    String remoteDir, remoteFile, newFilename;
    StringBuffer fullPath, newFullPath;
    File origFile, newFile;

    remoteDir = mesg.getProperty(FILE_SHARE_SERVICE_REQUEST_DIR_PROP);
    remoteFile = mesg.getProperty(FILE_SHARE_SERVICE_REQUEST_FILE_PROP);
    newFilename = mesg.getProperty(FILE_SHARE_SERVICE_NEW_FILENAME_PROP);

    if (!StringUtils.IsNVL(remoteFile) && !StringUtils.IsNVL(newFilename) && newFilename.trim().indexOf("/") == -1 && newFilename.trim().indexOf("\\") == -1) {
      fullPath = new StringBuffer();
      fullPath.append(remoteDir.trim());
      fullPath.append("/");
      fullPath.append(remoteFile.trim());

      newFullPath = new StringBuffer();
      newFullPath.append(remoteDir.trim());
      newFullPath.append("/");
      newFullPath.append(newFilename.trim());

      origFile = new File(fullPath.toString());
      newFile = new File(newFullPath.toString());

      if (origFile.renameTo(newFile)) {
        sendSuccessStatus(mesg);
      }
      else {
        sendFailureStatus(mesg);
      }
    }
    else {
      sendFailureStatus(mesg);
    }
  }

  private void deleteFile(P2PHubMessage mesg) throws RogueNetException {
    String remoteDir, remoteFile;
    StringBuffer fullPath;
    File f;

    remoteDir = mesg.getProperty(FILE_SHARE_SERVICE_REQUEST_DIR_PROP);
    remoteFile = mesg.getProperty(FILE_SHARE_SERVICE_REQUEST_FILE_PROP);

    if (!StringUtils.IsNVL(remoteFile)) {
      fullPath = new StringBuffer();
      fullPath.append(remoteDir.trim());
      fullPath.append("/");
      fullPath.append(remoteFile.trim());

      f = new File(fullPath.toString());

      if (f.delete()) {
        sendSuccessStatus(mesg);
      }
      else {
        sendFailureStatus(mesg);
      }
    }
    else {
      sendFailureStatus(mesg);
    }
  }

}
