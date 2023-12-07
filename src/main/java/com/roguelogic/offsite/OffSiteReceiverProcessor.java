/**
 * Created Dec 11, 2008
 */
package com.roguelogic.offsite;

import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_GET_LOGIN_MASK;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_GET_REMOTE_FILE_INFO;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_LOGIN;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_REMOVE_FROM_BACKUP;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_START_UPLOAD;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_SYNC_TOUCH_TS;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_UPLOAD_COMPLETE;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_UPLOAD_NEXT_FILE_CHUNK;
import static com.roguelogic.offsite.OffSiteProtocolConstants.STATUS_CODE_FAILURE;
import static com.roguelogic.offsite.OffSiteProtocolConstants.STATUS_CODE_SUCESS;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;
import com.roguelogic.net.rltalk.RLTalkXorCodec;
import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.SimpleXORCodec;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class OffSiteReceiverProcessor extends RLTalkSocketProcessor {

  public static final String USOBJ_USERNAME = "Username";
  public static final String USOBJ_UPLOAD_FILE_HANDLE = "UploadFileHandle";
  public static final String USOBJ_USER_TRANSPORT_XOR_CODEC = "UserTransportXorCodec";
  public static final String USOBJ_LOGIN_MASK = "LoginMask";

  public static final int MAX_BUFFER = 32768;
  public static final int LOGIN_MASK_LEN = 1024;

  private OffSiteReceiver receiver;

  public OffSiteReceiverProcessor() {}

  public void setReceiver(OffSiteReceiver receiver) {
    this.receiver = receiver;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandshake()
   */
  @Override
  protected void _rlTalkHandshake() throws RLNetException {
    byte[] keyData;
    RLTalkXorCodec transportXorCodec;

    keyData = receiver.getTransportXorKey();
    transportXorCodec = new RLTalkXorCodec();
    transportXorCodec.setKeyData(keyData);
    userSession.putUserItem(USOBJ_USER_TRANSPORT_XOR_CODEC, transportXorCodec);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandle(com.roguelogic.net.rltalk.CommandDataPair)
   */
  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    RLTalkXorCodec transportXorCodec;

    transportXorCodec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_USER_TRANSPORT_XOR_CODEC);
    cmDatPair = transportXorCodec.decrypt(cmDatPair);

    switch (cmDatPair.getCommand()) {
      case OFFS_RLTCMD_LOGIN:
        processLogin(cmDatPair);
        break;
      case OFFS_RLTCMD_GET_LOGIN_MASK:
        processGetLoginMask(cmDatPair);
        break;
      case OFFS_RLTCMD_START_UPLOAD:
        processStartUpload(cmDatPair);
        break;
      case OFFS_RLTCMD_UPLOAD_NEXT_FILE_CHUNK:
        processUploadNextFileChunk(cmDatPair);
        break;
      case OFFS_RLTCMD_UPLOAD_COMPLETE:
        processUploadComplete(cmDatPair);
        break;
      case OFFS_RLTCMD_GET_REMOTE_FILE_INFO:
        processGetRemoteFileInfo(cmDatPair);
        break;
      case OFFS_RLTCMD_SYNC_TOUCH_TS:
        processSyncTouchTs(cmDatPair);
        break;
      case OFFS_RLTCMD_REMOVE_FROM_BACKUP:
        processRemoveFromBackup(cmDatPair);
        break;
      default:
        // Invalid Command - Close Connection
        userSession.endSession();
    }
  }

  private boolean loginOk(boolean sendLoginInvalidMesg, boolean disconnectOnInvalid) throws RLNetException {
    boolean lOk = true;
    CommandDataPair reply;

    if (userSession.getUserItem(USOBJ_USERNAME) == null) {
      lOk = false;

      reply = new CommandDataPair();
      reply.setCommand(OFFS_RLTCMD_LOGIN);
      reply.setStatusCode(STATUS_CODE_FAILURE);
      reply.setData("User NOT logged in!");

      sendEncrypted(reply);

      if (disconnectOnInvalid) {
        userSession.endSession();
      }
    }

    return lOk;
  }

  private void processLogin(CommandDataPair cmDatPair) throws RLNetException {
    String[] tokens;
    String tmp;
    CommandDataPair reply;

    userSession.removeUserItem(USOBJ_USERNAME);

    tmp = demaskLoginString(cmDatPair.getData());
    tokens = tmp.split("\\|", 2);

    if (tokens != null && tokens.length == 2) {
      tokens = StringUtils.Trim(tokens);

      if (receiver.getUsername().equals(tokens[0]) && receiver.getPassword().equals(tokens[1])) {
        // Login OK!
        userSession.putUserItem(USOBJ_USERNAME, tokens[0]);

        reply = new CommandDataPair();
        reply.setCommand(OFFS_RLTCMD_LOGIN);
        reply.setStatusCode(STATUS_CODE_SUCESS);
        reply.setData("Login OK");

        sendEncrypted(reply);
      }
      else {
        // Invalid Username or Password

        reply = new CommandDataPair();
        reply.setCommand(OFFS_RLTCMD_LOGIN);
        reply.setStatusCode(STATUS_CODE_FAILURE);
        reply.setData("Invalid Username or Password");

        sendEncrypted(reply);

        userSession.endSession(); // Invalid username or password - Close Connection
      }
    }
    else {
      userSession.endSession(); // Invalid login CMDATPAIR - Close Connection
    }
  }

  private String demaskLoginString(byte[] maskedLoginStr) {
    String loginStr = "";
    SimpleXORCodec codec;
    byte[] loginMask = (byte[]) userSession.getUserItem(USOBJ_LOGIN_MASK);

    if (loginMask != null) {
      codec = new SimpleXORCodec();
      codec.setKeyData(loginMask);
      loginStr = new String(codec.decrypt(maskedLoginStr));
    }

    return loginStr;
  }

  private void processGetLoginMask(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    byte[] loginMask;

    loginMask = generateLoginMask();
    userSession.putUserItem(USOBJ_LOGIN_MASK, loginMask);

    reply = new CommandDataPair();
    reply.setCommand(OFFS_RLTCMD_GET_LOGIN_MASK);
    reply.setStatusCode(STATUS_CODE_SUCESS);
    reply.setData(loginMask);

    sendEncrypted(reply);
  }

  private byte[] generateLoginMask() {
    return SystemUtils.GenerateRandomBytes(LOGIN_MASK_LEN);
  }

  private void processStartUpload(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    String backupFilePath;

    if (!loginOk(true, true)) {
      return;
    }

    closeUploadFile();

    reply = new CommandDataPair();
    reply.setCommand(OFFS_RLTCMD_START_UPLOAD);

    backupFilePath = cmDatPair.getString();

    if (!StringUtils.IsNVL(backupFilePath)) {
      try {
        backupFilePath = receiver.getAbsoluteBackupFilePath(backupFilePath);

        ensureBackupFilePathExists(backupFilePath);

        fos = new FileOutputStream(backupFilePath);
        bos = new BufferedOutputStream(fos);

        userSession.putUserItem(USOBJ_UPLOAD_FILE_HANDLE, bos);

        reply.setStatusCode(STATUS_CODE_SUCESS);

        sendEncrypted(reply);
      } // End try block
      catch (Exception e) {
        receiver.log(e);

        if (bos != null) {
          try {
            bos.close();
          }
          catch (Exception e2) {}
        }

        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e2) {}
        }

        reply.setStatusCode(STATUS_CODE_FAILURE);
        reply.setData("Failed to Open Backup File!");

        sendEncrypted(reply);
      }
    } // End if block
    else {
      reply.setStatusCode(STATUS_CODE_FAILURE);
      reply.setData("Backup File Path NOT Provided!");

      sendEncrypted(reply);

      userSession.endSession();
    }
  }

  private void ensureBackupFilePathExists(String backupFilePath) {
    File dir;
    String dirPath;

    dirPath = FilenameUtils.GetParentDirectory(backupFilePath);

    dir = new File(dirPath);
    dir.mkdirs();
  }

  private void closeUploadFile() {
    BufferedOutputStream bos = (BufferedOutputStream) userSession.removeUserItem(USOBJ_UPLOAD_FILE_HANDLE);

    if (bos != null) {
      try {
        bos.close();
      }
      catch (Exception e) {
        receiver.log(e);
      }
    }
  }

  private void processUploadComplete(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;

    if (!loginOk(true, true)) {
      return;
    }

    closeUploadFile();

    reply = new CommandDataPair();
    reply.setCommand(OFFS_RLTCMD_UPLOAD_COMPLETE);

    reply.setStatusCode(STATUS_CODE_SUCESS);
    reply.setData("Upload File Closed");

    sendEncrypted(reply);
  }

  private void processUploadNextFileChunk(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    BufferedOutputStream bos = null;
    byte[] buf;

    if (!loginOk(true, true)) {
      return;
    }

    bos = (BufferedOutputStream) userSession.getUserItem(USOBJ_UPLOAD_FILE_HANDLE);

    if (bos != null) {
      try {
        buf = cmDatPair.getData();
        bos.write(buf);
      } // End try block
      catch (Exception e) {
        receiver.log(e);

        reply = new CommandDataPair();
        reply.setCommand(OFFS_RLTCMD_UPLOAD_NEXT_FILE_CHUNK);
        reply.setStatusCode(STATUS_CODE_FAILURE);
        reply.setData("Error while writing to backup file!");

        sendEncrypted(reply);

        userSession.endSession();
      }
    }
    else {
      reply = new CommandDataPair();
      reply.setCommand(OFFS_RLTCMD_UPLOAD_NEXT_FILE_CHUNK);
      reply.setStatusCode(STATUS_CODE_FAILURE);
      reply.setData("File Not Opened!");

      sendEncrypted(reply);

      userSession.endSession(); // File NOT Opened!?!? - Close Connection
    }
  }

  private void processGetRemoteFileInfo(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    String backupFilePath;
    File f;

    if (!loginOk(true, true)) {
      return;
    }

    backupFilePath = cmDatPair.getString();
    backupFilePath = receiver.getAbsoluteBackupFilePath(backupFilePath);
    f = new File(backupFilePath);

    reply = new CommandDataPair();
    reply.setCommand(OFFS_RLTCMD_GET_REMOTE_FILE_INFO);
    reply.setStatusCode(STATUS_CODE_SUCESS);

    if (f.exists() && f.isFile()) {
      reply.setData((new StringBuffer()).append(f.length()).append("|").append(f.lastModified()).toString());
    }
    else {
      // File Does NOT Exist Response Data...
      reply.setData("-1|-1");
    }

    sendEncrypted(reply);
  }

  private void processSyncTouchTs(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    String backupFilePath;
    File f;
    String[] tokens;
    long touchTs;

    if (!loginOk(true, true)) {
      return;
    }

    tokens = cmDatPair.getString().split("\\|");

    backupFilePath = tokens[0].trim();
    backupFilePath = receiver.getAbsoluteBackupFilePath(backupFilePath);
    f = new File(backupFilePath);

    reply = new CommandDataPair();
    reply.setCommand(OFFS_RLTCMD_SYNC_TOUCH_TS);

    if (f.exists()) {
      touchTs = Long.parseLong(tokens[1]);
      f.setLastModified(touchTs);
      reply.setStatusCode(STATUS_CODE_SUCESS);
    }
    else {
      reply.setStatusCode(STATUS_CODE_FAILURE);
    }

    sendEncrypted(reply);
  }

  private void sendEncrypted(CommandDataPair cmDatPair) throws RLNetException {
    RLTalkXorCodec transportXorCodec;

    transportXorCodec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_USER_TRANSPORT_XOR_CODEC);
    cmDatPair = transportXorCodec.encrypt(cmDatPair);

    _rlTalkSend(cmDatPair);
  }

  private void processRemoveFromBackup(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    String backupFilePath;
    File f;

    if (!loginOk(true, true)) {
      return;
    }

    backupFilePath = cmDatPair.getString();
    backupFilePath = receiver.getAbsoluteBackupFilePath(backupFilePath);
    f = new File(backupFilePath);

    reply = new CommandDataPair();
    reply.setCommand(OFFS_RLTCMD_REMOVE_FROM_BACKUP);
    reply.setStatusCode(STATUS_CODE_SUCESS);

    if (f.exists()) {
      if (f.isFile()) {
        f.delete();
      }
      else {
        SystemUtils.RecuriveDelete(f);
      }
    }

    sendEncrypted(reply);
  }

}
