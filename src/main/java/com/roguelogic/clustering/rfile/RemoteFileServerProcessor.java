/**
 * Created Aug 15, 2008
 */
package com.roguelogic.clustering.rfile;

import static com.roguelogic.clustering.rfile.RemoteFileConstants.*;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_FAILED_WITH_EXCEPTION;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_SUCCEEDED;

import java.io.IOException;
import java.io.Serializable;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkObjectEnvelop;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RemoteFileServerProcessor extends RLTalkSocketProcessor {

  private RemoteFileServer rfServer;

  public RemoteFileServerProcessor() {
    super();
  }

  public void setRemoteFileServer(RemoteFileServer rfServer) {
    this.rfServer = rfServer;
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
    try {
      switch (cmDatPair.getCommand()) {
        case CMD_LOGIN:
          processLogin(cmDatPair);
          break;
        case CMD_EXISTS:
          processExists(cmDatPair);
          break;
        case CMD_IS_DIRECTORY:
          processIsDirectory(cmDatPair);
          break;
        case CMD_IS_FILE:
          processIsFile(cmDatPair);
          break;
        case CMD_CREATE_DIRECTORY:
          processCreateDirectory(cmDatPair);
          break;
        case CMD_RENAME:
          processRename(cmDatPair);
          break;
        case CMD_DELETE:
          processDelete(cmDatPair);
          break;
        case CMD_LIST:
          processList(cmDatPair);
          break;
        default:
          //Error or HACK Attempt so disconnect
          userSession.endSession();
      } //End switch block
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
      sendExceptionToClient(cmDatPair, e);
    }
  }

  private void sendExceptionToClient(CommandDataPair cmDatPair, Exception e) {
    RLTalkObjectEnvelop envelop, resEnv;
    CommandDataPair response;

    try {
      envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

      if (envelop == null) {
        return;
      }

      response = new CommandDataPair();
      response.setCommand(cmDatPair.getCommand());

      resEnv = new RLTalkObjectEnvelop();
      resEnv.setAsyncTransId(envelop.getAsyncTransId());
      resEnv.setAsyncState(TRANSACTION_FAILED_WITH_EXCEPTION);

      resEnv.setObjects(new Serializable[] { StringUtils.GetStackTraceString(e) });

      response.setData(resEnv);

      _rlTalkSend(response);
    }
    catch (Exception e2) {
      e2.printStackTrace();
    }
  }

  private void processLogin(CommandDataPair cmDatPair) throws RemoteFileException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String username, password;
    CommandDataPair response;
    boolean loginOk;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    username = (String) envelop.getObjects()[0];
    password = (String) envelop.getObjects()[1];

    loginOk = rfServer.login(username, password);

    response = new CommandDataPair();
    response.setCommand(CMD_LOGIN);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { new Boolean(loginOk) });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processExists(CommandDataPair cmDatPair) throws RemoteFileException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String filePath;
    CommandDataPair response;
    boolean found;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    filePath = (String) envelop.getObjects()[0];

    found = rfServer.exists(filePath);

    response = new CommandDataPair();
    response.setCommand(CMD_EXISTS);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { new Boolean(found) });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processIsDirectory(CommandDataPair cmDatPair) throws RemoteFileException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String filePath;
    CommandDataPair response;
    boolean yes;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    filePath = (String) envelop.getObjects()[0];

    yes = rfServer.isDirectory(filePath);

    response = new CommandDataPair();
    response.setCommand(CMD_IS_DIRECTORY);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { new Boolean(yes) });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processIsFile(CommandDataPair cmDatPair) throws RemoteFileException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String filePath;
    CommandDataPair response;
    boolean yes;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    filePath = (String) envelop.getObjects()[0];

    yes = rfServer.isFile(filePath);

    response = new CommandDataPair();
    response.setCommand(CMD_IS_FILE);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { new Boolean(yes) });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processCreateDirectory(CommandDataPair cmDatPair) throws RemoteFileException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String filePath;
    CommandDataPair response;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    filePath = (String) envelop.getObjects()[0];

    rfServer.createDirectory(filePath);

    response = new CommandDataPair();
    response.setCommand(CMD_CREATE_DIRECTORY);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processRename(CommandDataPair cmDatPair) throws RemoteFileException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String filePath, toName;
    CommandDataPair response;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    filePath = (String) envelop.getObjects()[0];
    toName = (String) envelop.getObjects()[1];

    rfServer.rename(filePath, toName);

    response = new CommandDataPair();
    response.setCommand(CMD_RENAME);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processDelete(CommandDataPair cmDatPair) throws RemoteFileException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String filePath;
    CommandDataPair response;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    filePath = (String) envelop.getObjects()[0];

    rfServer.delete(filePath);

    response = new CommandDataPair();
    response.setCommand(CMD_DELETE);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processList(CommandDataPair cmDatPair) throws RemoteFileException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String filePath;
    CommandDataPair response;
    RDirEntry[] ls;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    filePath = (String) envelop.getObjects()[0];

    ls = rfServer.list(filePath);

    response = new CommandDataPair();
    response.setCommand(CMD_LIST);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { ls });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

}
