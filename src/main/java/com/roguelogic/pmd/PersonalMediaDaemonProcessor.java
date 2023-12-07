/**
 * Created Nov 20, 2007
 */
package com.roguelogic.pmd;

import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_CLOSE_FILE;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_FILE_LENGTH;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_FILE_LIST;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_SHARES;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_LOGIN;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_OPEN_FILE;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_READ_NEXT_FILE_CHUNK;
import static com.roguelogic.pmd.PMDConstants.STATUS_CODE_FAILURE;
import static com.roguelogic.pmd.PMDConstants.STATUS_CODE_SUCESS;
import static com.roguelogic.pmd.PMDConstants.STATUS_CODE_ACCESS_DENIED;
import static com.roguelogic.pmd.PMDConstants.STATUS_CODE_END_OF_STREAM;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class PersonalMediaDaemonProcessor extends RLTalkSocketProcessor {

  public static final String USOBJ_USERNAME = "Username";
  public static final String USOBJ_OPEN_FILE_HANDLE = "FileHandle";

  public static final int MAX_BUFFER = 32768;

  private PersonalMediaDaemon daemon;

  public PersonalMediaDaemonProcessor() {
    super();
  }

  protected void setDaemon(PersonalMediaDaemon daemon) {
    this.daemon = daemon;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandshake()
   */
  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandle(com.roguelogic.net.rltalk.CommandDataPair)
   */
  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    switch (cmDatPair.getCommand()) {
      case PMD_RLTCMD_LOGIN:
        processLogin(cmDatPair);
        break;
      case PMD_RLTCMD_GET_SHARES:
        processGetShares(cmDatPair);
        break;
      case PMD_RLTCMD_GET_FILE_LIST:
        processGetFileList(cmDatPair);
        break;
      case PMD_RLTCMD_GET_FILE_LENGTH:
        processGetFileLen(cmDatPair);
        break;
      case PMD_RLTCMD_OPEN_FILE:
        processOpenFile(cmDatPair);
        break;
      case PMD_RLTCMD_CLOSE_FILE:
        processCloseFile(cmDatPair);
        break;
      case PMD_RLTCMD_READ_NEXT_FILE_CHUNK:
        processReadNextFileChunk(cmDatPair);
        break;
      default:
        //Invalid Command - Close Connection
        userSession.endSession();
    }
  }

  private void processLogin(CommandDataPair cmDatPair) throws RLNetException {
    String[] tokens;
    String tmp;
    CommandDataPair reply;

    userSession.removeUserItem(USOBJ_USERNAME);

    tmp = cmDatPair.getString();
    tokens = tmp.split("\\|", 2);

    if (tokens != null && tokens.length == 2) {
      tokens = StringUtils.Trim(tokens);

      if (daemon.getUsername().equals(tokens[0]) && daemon.getPassword().equals(tokens[1])) {
        //Login OK!
        userSession.putUserItem(USOBJ_USERNAME, tokens[0]);

        reply = new CommandDataPair();
        reply.setCommand(PMD_RLTCMD_LOGIN);
        reply.setStatusCode(STATUS_CODE_SUCESS);
        reply.setData("Login OK");

        _rlTalkSend(reply);
      }
      else {
        //Invalid Username or Password

        reply = new CommandDataPair();
        reply.setCommand(PMD_RLTCMD_LOGIN);
        reply.setStatusCode(STATUS_CODE_FAILURE);
        reply.setData("Invalid Username or Password");

        _rlTalkSend(reply);

        userSession.endSession(); //Invalid username or password - Close Connection
      }
    }
    else {
      userSession.endSession(); //Invalid login CMDATPAIR - Close Connection
    }
  }

  private void processGetShares(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;

    if (!loginOk(true, true)) {
      return;
    }

    reply = new CommandDataPair();
    reply.setCommand(PMD_RLTCMD_GET_SHARES);
    reply.setStatusCode(STATUS_CODE_SUCESS);
    reply.setData(getDelimitedShareList());

    _rlTalkSend(reply);
  }

  private void processGetFileList(CommandDataPair cmDatPair) throws RLNetException {
    String share;
    CommandDataPair reply;

    if (!loginOk(true, true)) {
      return;
    }

    share = cmDatPair.getString();

    if (!hasAccess(share)) {
      reply = new CommandDataPair();
      reply.setCommand(PMD_RLTCMD_GET_FILE_LIST);
      reply.setStatusCode(STATUS_CODE_ACCESS_DENIED);
      reply.setData("Access Denied!");

      _rlTalkSend(reply);

      userSession.endSession();
    }
    else {
      reply = new CommandDataPair();
      reply.setCommand(PMD_RLTCMD_GET_FILE_LIST);
      reply.setStatusCode(STATUS_CODE_SUCESS);
      reply.setData(getDelimitedFileList(share));

      _rlTalkSend(reply);
    }
  }

  private void processGetFileLen(CommandDataPair cmDatPair) throws RLNetException {
    String path;
    CommandDataPair reply;
    File f;

    if (!loginOk(true, true)) {
      return;
    }

    path = cmDatPair.getString();

    if (!hasAccess(path)) {
      reply = new CommandDataPair();
      reply.setCommand(PMD_RLTCMD_GET_FILE_LENGTH);
      reply.setStatusCode(STATUS_CODE_ACCESS_DENIED);
      reply.setData("Access Denied!");

      _rlTalkSend(reply);

      userSession.endSession();
    }
    else {
      reply = new CommandDataPair();
      reply.setCommand(PMD_RLTCMD_GET_FILE_LENGTH);
      reply.setStatusCode(STATUS_CODE_SUCESS);

      f = new File(path.trim());
      reply.setData(f.length());

      _rlTalkSend(reply);
    }
  }

  private void processOpenFile(CommandDataPair cmDatPair) throws RLNetException {
    String path;
    CommandDataPair reply;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    boolean failure = false;

    if (!loginOk(true, true)) {
      return;
    }

    path = cmDatPair.getString();

    if (!hasAccess(path)) {
      reply = new CommandDataPair();
      reply.setCommand(PMD_RLTCMD_OPEN_FILE);
      reply.setStatusCode(STATUS_CODE_ACCESS_DENIED);
      reply.setData("Access Denied!");

      _rlTalkSend(reply);

      userSession.endSession();
    }
    else {
      bis = (BufferedInputStream) userSession.removeUserItem(USOBJ_OPEN_FILE_HANDLE);
      if (bis != null) {
        try {
          bis.close();
        }
        catch (Exception e) {}
        bis = null;
      }

      try {
        fis = new FileInputStream(path.trim());
        bis = new BufferedInputStream(fis);
        userSession.putUserItem(USOBJ_OPEN_FILE_HANDLE, bis);
        failure = false;
      } //End try block
      catch (Exception e) {
        failure = true;
        e.printStackTrace();
      }
      finally {
        if (failure) {
          if (bis != null) {
            try {
              bis.close();
            }
            catch (Exception e) {}
          }

          if (fis != null) {
            try {
              fis.close();
            }
            catch (Exception e) {}
          }

          userSession.removeUserItem(USOBJ_OPEN_FILE_HANDLE);
        }
      }

      reply = new CommandDataPair();
      reply.setCommand(PMD_RLTCMD_OPEN_FILE);
      reply.setStatusCode(failure ? STATUS_CODE_FAILURE : STATUS_CODE_SUCESS);

      _rlTalkSend(reply);
    }
  }

  private void processCloseFile(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    BufferedInputStream bis = null;

    if (!loginOk(true, true)) {
      return;
    }

    bis = (BufferedInputStream) userSession.removeUserItem(USOBJ_OPEN_FILE_HANDLE);
    if (bis != null) {
      try {
        bis.close();
      }
      catch (Exception e) {}
      bis = null;
    }

    reply = new CommandDataPair();
    reply.setCommand(PMD_RLTCMD_CLOSE_FILE);
    reply.setStatusCode(STATUS_CODE_SUCESS);

    _rlTalkSend(reply);
  }

  private void processReadNextFileChunk(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair reply;
    BufferedInputStream bis = null;
    int len;
    byte[] buf, chunk;

    if (!loginOk(true, true)) {
      return;
    }

    reply = new CommandDataPair();
    reply.setCommand(PMD_RLTCMD_READ_NEXT_FILE_CHUNK);

    bis = (BufferedInputStream) userSession.getUserItem(USOBJ_OPEN_FILE_HANDLE);

    if (bis != null) {
      try {
        buf = new byte[MAX_BUFFER];
        len = bis.read(buf);

        if (len != -1) {
          chunk = new byte[len];
          System.arraycopy(buf, 0, chunk, 0, len);

          reply.setStatusCode(STATUS_CODE_SUCESS);
          reply.setData(chunk);
        }
        else {
          reply.setStatusCode(STATUS_CODE_END_OF_STREAM);
        }
      }
      catch (Exception e) {
        reply.setStatusCode(STATUS_CODE_FAILURE);
        reply.setData(e.getMessage());
      }
    }
    else {
      reply.setStatusCode(STATUS_CODE_FAILURE);
      reply.setData("File Not Opened!");
    }

    _rlTalkSend(reply);
  }

  private boolean loginOk(boolean sendLoginInvalidMesg, boolean disconnectOnInvalid) throws RLNetException {
    boolean lOk = true;
    CommandDataPair reply;

    if (userSession.getUserItem(USOBJ_USERNAME) == null) {
      lOk = false;

      reply = new CommandDataPair();
      reply.setCommand(PMD_RLTCMD_LOGIN);
      reply.setStatusCode(STATUS_CODE_FAILURE);
      reply.setData("User NOT logged in!");

      _rlTalkSend(reply);

      if (disconnectOnInvalid) {
        userSession.endSession();
      }
    }

    return lOk;
  }

  private String getDelimitedShareList() {
    StringBuffer list = new StringBuffer();
    String[] tmpArr;

    tmpArr = daemon.getShareDirs();

    for (int i = 0; i < tmpArr.length; i++) {
      if (i > 0) {
        list.append("|");
      }

      list.append(tmpArr[i]);
    }

    return list.toString();
  }

  private boolean hasAccess(String path) {
    boolean granted = false;
    String[] tmpArr;

    if (!StringUtils.IsNVL(path)) {
      path = path.trim();

      tmpArr = daemon.getShareDirs();

      for (int i = 0; i < tmpArr.length; i++) {
        if (path.startsWith(tmpArr[i])) {
          granted = true;
          break;
        }
      }
    }

    return granted;
  }

  private String getDelimitedFileList(String share) {
    StringBuffer list = new StringBuffer();
    File dir;
    File[] fLst;

    dir = new File(share);
    fLst = dir.listFiles();

    if (fLst != null) {
      for (int i = 0; i < fLst.length; i++) {
        if (fLst[i].isFile()) {
          if (list.length() > 0) {
            list.append("|");
          }

          list.append(fLst[i].getName());
        }
      }
    }

    return list.toString();
  }

}
