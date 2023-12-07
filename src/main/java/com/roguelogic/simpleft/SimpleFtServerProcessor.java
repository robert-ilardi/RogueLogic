/**
 * Created Dec 11, 2008
 */
package com.roguelogic.simpleft;

import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_CHANGE_DIRECTORY;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_DATETIME;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_DOWNLOAD_DATA;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_ECHO;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_END_DOWNLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_END_UPLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_GET_LOGIN_MASK;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_LIST_DIRECTORY;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_LIST_SHARES;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_LOGIN;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_PING;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_START_DOWNLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_START_SDOWNLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_START_UPLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_UPLOAD_DATA;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_UPLOAD_DATA_NO_REPLY;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.STATUS_CODE_FAILURE;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.STATUS_CODE_SUCCESS;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

public class SimpleFtServerProcessor extends RLTalkSocketProcessor {

  public static final String USOBJ_USERNAME = "Username";
  public static final String USOBJ_USER_TRANSPORT_XOR_CODEC = "UserTransportXorCodec";
  public static final String USOBJ_LOGIN_MASK = "LoginMask";
  public static final String USOBJ_CURRENT_DIR = "CurrentDir";
  public static final String USOBJ_UPLOAD_HANDLE = "UploadHandle";
  public static final String USOBJ_DOWNLOAD_HANDLE = "DownloadHandle";
  public static final String USOBJ_DOWNLOAD_MODE = "DownloadMode";

  public static final int MAX_BUFFER = 32768;
  public static final int LOGIN_MASK_LEN = 1024;

  public static final String DOWNLOAD_MODE_PUSH = "PUSH";
  public static final String DOWNLOAD_MODE_PULL = "PULL";

  private SimpleFtServer server;

  public SimpleFtServerProcessor() {}

  public void setServer(SimpleFtServer server) {
    this.server = server;
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

    keyData = server.getTransportXorKey();
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
      case SFT_RLTCMD_LOGIN:
        processLogin(cmDatPair);
        break;
      case SFT_RLTCMD_GET_LOGIN_MASK:
        processGetLoginMask(cmDatPair);
        break;
      case SFT_RLTCMD_LIST_SHARES:
        processListShares(cmDatPair);
        break;
      case SFT_RLTCMD_LIST_DIRECTORY:
        processListDir(cmDatPair);
        break;
      case SFT_RLTCMD_CHANGE_DIRECTORY:
        processChangeDir(cmDatPair);
        break;
      case SFT_RLTCMD_PING:
        processPing(cmDatPair);
        break;
      case SFT_RLTCMD_ECHO:
        processEcho(cmDatPair);
        break;
      case SFT_RLTCMD_DATETIME:
        processDateTime(cmDatPair);
        break;
      case SFT_RLTCMD_START_DOWNLOAD:
        processStartDownload(cmDatPair);
        break;
      case SFT_RLTCMD_START_SDOWNLOAD:
        processStartDownload(cmDatPair);
        break;
      case SFT_RLTCMD_DOWNLOAD_DATA:
        processDownloadData(cmDatPair);
        break;
      case SFT_RLTCMD_END_DOWNLOAD:
        processEndDownload(cmDatPair);
        break;
      case SFT_RLTCMD_START_UPLOAD:
        processStartUpload(cmDatPair);
        break;
      case SFT_RLTCMD_UPLOAD_DATA:
        processUploadData(cmDatPair);
        break;
      case SFT_RLTCMD_UPLOAD_DATA_NO_REPLY:
        processUploadData(cmDatPair);
        break;
      case SFT_RLTCMD_END_UPLOAD:
        processEndUpload(cmDatPair);
        break;
      default:
        // Invalid Command - Close Connection
        userSession.endSessionAsync();
    }
  }

  private boolean loginOk(boolean sendLoginInvalidMesg, boolean disconnectOnInvalid) throws RLNetException {
    boolean lOk = true;
    CommandDataPair reply;

    if (userSession.getUserItem(USOBJ_USERNAME) == null) {
      lOk = false;

      reply = new CommandDataPair();
      reply.setCommand(SFT_RLTCMD_LOGIN);
      reply.setStatusCode(STATUS_CODE_FAILURE);
      reply.setData("User NOT logged in!");

      sendEncrypted(reply);

      if (disconnectOnInvalid) {
        userSession.endSession();
      }
    }

    return lOk;
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

  private byte[] generateLoginMask() {
    return SystemUtils.GenerateRandomBytes(LOGIN_MASK_LEN);
  }

  private void sendEncrypted(CommandDataPair cmDatPair) throws RLNetException {
    RLTalkXorCodec transportXorCodec;

    transportXorCodec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_USER_TRANSPORT_XOR_CODEC);
    cmDatPair = transportXorCodec.encrypt(cmDatPair);

    _rlTalkSend(cmDatPair);
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

      if (server.getUsername().equals(tokens[0]) && server.getPassword().equals(tokens[1])) {
        // Login OK!
        userSession.putUserItem(USOBJ_USERNAME, tokens[0]);

        reply = new CommandDataPair();
        reply.setCommand(SFT_RLTCMD_LOGIN);
        reply.setStatusCode(STATUS_CODE_SUCCESS);
        reply.setData("Login OK");

        sendEncrypted(reply);
      }
      else {
        // Invalid Username or Password

        reply = new CommandDataPair();
        reply.setCommand(SFT_RLTCMD_LOGIN);
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

  private void processGetLoginMask(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    byte[] loginMask;

    loginMask = generateLoginMask();
    userSession.putUserItem(USOBJ_LOGIN_MASK, loginMask);

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_GET_LOGIN_MASK);
    reply.setStatusCode(STATUS_CODE_SUCCESS);
    reply.setData(loginMask);

    sendEncrypted(reply);
  }

  private void processListShares(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    AccessControlList acl;
    Share s;
    StringBuffer sb;

    if (!loginOk(true, true)) {
      return;
    }

    acl = server.getAccessControlList();

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_LIST_SHARES);
    reply.setStatusCode(STATUS_CODE_SUCCESS);

    sb = new StringBuffer();

    for (int i = 0; i < acl.count(); i++) {
      s = acl.getShare(i);

      if (i > 0) {
        sb.append("|");
      }

      sb.append(s.getName());
    }

    reply.setData(sb.toString());

    sendEncrypted(reply);
  }

  private void processListDir(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    StringBuffer sb;
    String curDir;
    File dir;
    File[] lst;

    if (!loginOk(true, true)) {
      return;
    }

    curDir = (String) userSession.getUserItem(USOBJ_CURRENT_DIR);

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_LIST_DIRECTORY);

    if (StringUtils.IsNVL(curDir)) {
      reply.setStatusCode(STATUS_CODE_FAILURE);
    }
    else {
      sb = new StringBuffer();

      dir = new File(curDir);
      lst = dir.listFiles();

      if (lst != null) {
        for (int i = 0; i < lst.length; i++) {
          if (i > 0) {
            sb.append("\n");
          }

          sb.append(lst[i].isDirectory() ? "D:" : "F:");
          sb.append(lst[i].getName());
        }

        reply.setData(sb.toString());
      }

      reply.setStatusCode(STATUS_CODE_SUCCESS);
    }

    sendEncrypted(reply);
  }

  private void processChangeDir(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    String curDir, tmp;
    StringBuffer sb;

    if (!loginOk(true, true)) {
      return;
    }

    curDir = cmDatPair.getString().trim();
    curDir = FilenameUtils.NormalizeToUnix(curDir);

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_CHANGE_DIRECTORY);

    if (curDir.equals("..")) {
      curDir = (String) userSession.getUserItem(USOBJ_CURRENT_DIR);
      curDir = FilenameUtils.GetParentDirectory(curDir);
    }
    else if (curDir.indexOf("/") < 0) {
      tmp = (String) userSession.getUserItem(USOBJ_CURRENT_DIR);

      if (!StringUtils.IsNVL(tmp)) {
        sb = new StringBuffer();
        sb.append(tmp);

        if (tmp.charAt(tmp.length() - 1) != '/') {
          sb.append("/");
        }

        sb.append(curDir);
        curDir = sb.toString();
      }
    }

    if (server.getAccessControlList().isAccessible(curDir)) {
      userSession.putUserItem(USOBJ_CURRENT_DIR, curDir);

      reply.setStatusCode(STATUS_CODE_SUCCESS);
    }
    else {
      reply.setStatusCode(STATUS_CODE_FAILURE);
    }

    sendEncrypted(reply);
  }

  private void processPing(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_PING);
    reply.setStatusCode(STATUS_CODE_SUCCESS);
    reply.setData(cmDatPair.getData());

    sendEncrypted(reply);
  }

  private void processEcho(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_ECHO);
    reply.setStatusCode(STATUS_CODE_SUCCESS);
    reply.setData(cmDatPair.getData());

    sendEncrypted(reply);
  }

  private void processDateTime(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_DATETIME);
    reply.setStatusCode(STATUS_CODE_SUCCESS);
    reply.setData(StringUtils.QuickDateFormat("yyyy-MM-dd HH:mm:ss zzz Z"));

    sendEncrypted(reply);
  }

  private void processStartUpload(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    String filename, path;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;

    if (!loginOk(true, true)) {
      return;
    }

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_START_UPLOAD);

    filename = FilenameUtils.NormalizeToUnix(cmDatPair.getString());

    if (filename.indexOf("/") > 0) {
      //Filename is a path, fail to secure from hacks!
      reply.setStatusCode(STATUS_CODE_FAILURE);
    }
    else {
      path = (String) userSession.getUserItem(USOBJ_CURRENT_DIR);
      path = new StringBuffer().append(path).append("/").append(filename).toString();

      if (server.getAccessControlList().isWriteable(path)) {
        //Acces Granted
        try {
          fos = new FileOutputStream(path);
          bos = new BufferedOutputStream(fos);

          userSession.putUserItem(USOBJ_UPLOAD_HANDLE, bos);

          reply.setStatusCode(STATUS_CODE_SUCCESS);
        }
        catch (Exception e) {
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

          reply.setData(e.getMessage());
          reply.setStatusCode(STATUS_CODE_FAILURE);
        }
      }
      else {
        //Access Denied
        reply.setData("Access Denied");
        reply.setStatusCode(STATUS_CODE_FAILURE);
      }
    }

    sendEncrypted(reply);
  }

  private void processEndUpload(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    BufferedOutputStream bos;

    if (!loginOk(true, true)) {
      return;
    }

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_END_UPLOAD);

    bos = (BufferedOutputStream) userSession.removeUserItem(USOBJ_UPLOAD_HANDLE);

    if (bos != null) {
      try {
        bos.close();
        reply.setStatusCode(STATUS_CODE_SUCCESS);
      }
      catch (Exception e) {
        reply.setData(e.getMessage());
        reply.setStatusCode(STATUS_CODE_FAILURE);
      }
    }
    else {
      reply.setData("File NOT Opened!");
      reply.setStatusCode(STATUS_CODE_FAILURE);
    }

    sendEncrypted(reply);
  }

  private void processUploadData(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    BufferedOutputStream bos;

    if (!loginOk(true, true)) {
      return;
    }

    bos = (BufferedOutputStream) userSession.getUserItem(USOBJ_UPLOAD_HANDLE);

    if (cmDatPair.getCommand() == SFT_RLTCMD_UPLOAD_DATA_NO_REPLY) {
      if (bos != null) {
        try {
          bos.write(cmDatPair.getData());
        }
        catch (Exception e) {
          userSession.endSessionAsync();
        }
      }
      else {
        userSession.endSessionAsync();
      }
    }
    else {
      reply = new CommandDataPair();
      reply.setCommand(SFT_RLTCMD_UPLOAD_DATA);

      if (bos != null) {
        try {
          bos.write(cmDatPair.getData());
          reply.setStatusCode(STATUS_CODE_SUCCESS);
        }
        catch (Exception e) {
          reply.setData(e.getMessage());
          reply.setStatusCode(STATUS_CODE_FAILURE);
        }
      }
      else {
        reply.setData("File NOT Opened!");
        reply.setStatusCode(STATUS_CODE_FAILURE);
      }

      sendEncrypted(reply);
    }
  }

  private void processStartDownload(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    String filename, path;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    File f;

    if (!loginOk(true, true)) {
      return;
    }

    reply = new CommandDataPair();
    reply.setCommand(cmDatPair.getCommand());

    filename = FilenameUtils.NormalizeToUnix(cmDatPair.getString());

    if (filename.indexOf("/") > 0) {
      //Filename is a path, fail to secure from hacks!
      reply.setStatusCode(STATUS_CODE_FAILURE);
    }
    else {
      path = (String) userSession.getUserItem(USOBJ_CURRENT_DIR);
      path = new StringBuffer().append(path).append("/").append(filename).toString();

      if (server.getAccessControlList().isReadable(path)) {
        //Access Granted
        try {
          f = new File(path);
          fis = new FileInputStream(f);
          bis = new BufferedInputStream(fis);

          userSession.putUserItem(USOBJ_DOWNLOAD_HANDLE, bis);
          userSession.putUserItem(USOBJ_DOWNLOAD_MODE, cmDatPair.getCommand() == SFT_RLTCMD_START_DOWNLOAD ? DOWNLOAD_MODE_PUSH : DOWNLOAD_MODE_PULL);

          reply.setData(f.length());
          reply.setStatusCode(STATUS_CODE_SUCCESS);
        }
        catch (Exception e) {
          if (bis != null) {
            try {
              bis.close();
            }
            catch (Exception e2) {}
          }

          if (fis != null) {
            try {
              fis.close();
            }
            catch (Exception e2) {}
          }

          reply.setData(e.getMessage());
          reply.setStatusCode(STATUS_CODE_FAILURE);
        }
      }
      else {
        //Access Denied
        reply.setData("Access Denied");
        reply.setStatusCode(STATUS_CODE_FAILURE);
      }
    }

    sendEncrypted(reply);
  }

  private void processEndDownload(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    BufferedInputStream bis;

    if (!loginOk(true, true)) {
      return;
    }

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_END_DOWNLOAD);

    bis = (BufferedInputStream) userSession.removeUserItem(USOBJ_DOWNLOAD_HANDLE);

    if (bis != null) {
      try {
        bis.close();
        reply.setStatusCode(STATUS_CODE_SUCCESS);
      }
      catch (Exception e) {
        reply.setData(e.getMessage());
        reply.setStatusCode(STATUS_CODE_FAILURE);
      }
    }
    else {
      reply.setData("File NOT Opened!");
      reply.setStatusCode(STATUS_CODE_FAILURE);
    }

    sendEncrypted(reply);
  }

  private void processDownloadData(CommandDataPair cmDatPair) throws RLNetException {
    if (!loginOk(true, true)) {
      return;
    }

    String mode = (String) userSession.getUserItem(USOBJ_DOWNLOAD_MODE);

    if (DOWNLOAD_MODE_PULL.equals(mode)) {
      pullDownloadData();
    }
    else {
      initDownloadPush();
    }
  }

  private void pullDownloadData() throws RLNetException {
    CommandDataPair reply;
    BufferedInputStream bis;
    byte[] buf, buf2;
    int len;

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_DOWNLOAD_DATA);

    bis = (BufferedInputStream) userSession.getUserItem(USOBJ_DOWNLOAD_HANDLE);

    if (bis != null) {
      try {
        buf = new byte[10240];
        len = bis.read(buf);

        if (len == buf.length) {
          reply.setData(buf);
        }
        else if (len > 0) {
          buf2 = new byte[len];
          System.arraycopy(buf, 0, buf2, 0, len);
          reply.setData(buf2);
        }

        reply.setStatusCode(STATUS_CODE_SUCCESS);
      }
      catch (Exception e) {
        reply.setData(e.getMessage());
        reply.setStatusCode(STATUS_CODE_FAILURE);
      }
    }
    else {
      reply.setData("File NOT Opened!");
      reply.setStatusCode(STATUS_CODE_FAILURE);
    }

    sendEncrypted(reply);
  }

  private void initDownloadPush() throws RLNetException {
    CommandDataPair reply;

    reply = new CommandDataPair();
    reply.setCommand(SFT_RLTCMD_DOWNLOAD_DATA);

    reply.setData("Not Yet Implemented! Use SDOWNLOAD instead.");
    reply.setStatusCode(STATUS_CODE_FAILURE);

    sendEncrypted(reply);
  }

}
