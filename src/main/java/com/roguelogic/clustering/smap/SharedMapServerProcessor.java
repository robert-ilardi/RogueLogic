/**
 * Created Aug 4, 2008
 */

package com.roguelogic.clustering.smap;

import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_CLEAR;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_CONTAINS_KEY;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_GET;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_IS_ENTRY_LOCKED;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_LOCK_ENTRY;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_LOGIN;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_PUT;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_REMOVE;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_UNLOCK_ENTRY;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_FAILED;
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

public class SharedMapServerProcessor extends RLTalkSocketProcessor {

  private SharedMapServerImpl smServer;

  public SharedMapServerProcessor() {
    super();
  }

  public void setSharedMapServer(SharedMapServerImpl smServer) {
    this.smServer = smServer;
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    try {
      switch (cmDatPair.getCommand()) {
        case CMD_LOGIN:
          processLogin(cmDatPair);
          break;
        case CMD_GET:
          processGet(cmDatPair);
          break;
        case CMD_PUT:
          processPut(cmDatPair);
          break;
        case CMD_CLEAR:
          processClear(cmDatPair);
          break;
        case CMD_REMOVE:
          processRemove(cmDatPair);
          break;
        case CMD_CONTAINS_KEY:
          processContainsKey(cmDatPair);
          break;
        case CMD_LOCK_ENTRY:
          processLockEntry(cmDatPair);
          break;
        case CMD_UNLOCK_ENTRY:
          processUnlockEntry(cmDatPair);
          break;
        case CMD_IS_ENTRY_LOCKED:
          processIsEntryLocked(cmDatPair);
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
      resEnv.setAsyncState(TRANSACTION_FAILED);

      resEnv.setObjects(new Serializable[] { StringUtils.GetStackTraceString(e) });

      response.setData(resEnv);

      _rlTalkSend(response);
    }
    catch (Exception e2) {
      e2.printStackTrace();
    }
  }

  private void processLogin(CommandDataPair cmDatPair) throws SharedMemoryException, IOException, RLNetException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop, resEnv;
    String username, password;
    CommandDataPair response;
    boolean loginOk;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();
    username = (String) envelop.getObjects()[0];
    password = (String) envelop.getObjects()[1];

    loginOk = smServer.login(username, password);

    response = new CommandDataPair();
    response.setCommand(CMD_LOGIN);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { new Boolean(loginOk) });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processClear(CommandDataPair cmDatPair) throws SharedMemoryException, RLNetException, IOException, ClassNotFoundException {
    CommandDataPair response;
    RLTalkObjectEnvelop envelop, resEnv;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    smServer.clear();

    response = new CommandDataPair();
    response.setCommand(CMD_CLEAR);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processGet(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException, SharedMemoryException, RLNetException {
    Serializable key, value;
    CommandDataPair response;
    RLTalkObjectEnvelop envelop, resEnv;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    key = envelop.getObjects()[0];

    value = smServer.get(key);

    response = new CommandDataPair();
    response.setCommand(CMD_GET);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { value });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processRemove(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException, RLNetException, SharedMemoryException {
    Serializable key, value;
    CommandDataPair response;
    RLTalkObjectEnvelop envelop, resEnv;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    key = envelop.getObjects()[0];

    value = smServer.remove(key);

    response = new CommandDataPair();
    response.setCommand(CMD_REMOVE);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { value });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processContainsKey(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException, RLNetException, SharedMemoryException {
    Serializable key;
    boolean contains;
    CommandDataPair response;
    RLTalkObjectEnvelop envelop, resEnv;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    key = envelop.getObjects()[0];

    contains = smServer.containsKey(key);

    response = new CommandDataPair();
    response.setCommand(CMD_CONTAINS_KEY);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { new Boolean(contains) });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processPut(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException, RLNetException, SharedMemoryException {
    Serializable key, value, oldVal;
    CommandDataPair response;
    RLTalkObjectEnvelop envelop, resEnv;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    key = envelop.getObjects()[0];
    value = envelop.getObjects()[1];

    oldVal = smServer.put(key, value);

    response = new CommandDataPair();
    response.setCommand(CMD_PUT);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    if (oldVal != null) {
      resEnv.setObjects(new Serializable[] { oldVal });
    }

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processLockEntry(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException, RLNetException, SharedMemoryException {
    Serializable key;
    Long toObj;
    long timeout;
    boolean locked;
    CommandDataPair response;
    RLTalkObjectEnvelop envelop, resEnv;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    key = envelop.getObjects()[0];
    toObj = (Long) envelop.getObjects()[1];
    timeout = toObj.longValue();

    locked = smServer.lockEntry(key, timeout);

    response = new CommandDataPair();
    response.setCommand(CMD_LOCK_ENTRY);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { new Boolean(locked) });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processUnlockEntry(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException, RLNetException, SharedMemoryException {
    Serializable key;
    CommandDataPair response;
    RLTalkObjectEnvelop envelop, resEnv;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    key = envelop.getObjects()[0];

    smServer.unlockEntry(key);

    response = new CommandDataPair();
    response.setCommand(CMD_UNLOCK_ENTRY);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    response.setData(resEnv);

    _rlTalkSend(response);
  }

  private void processIsEntryLocked(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException, RLNetException, SharedMemoryException {
    Serializable key;
    boolean locked;
    CommandDataPair response;
    RLTalkObjectEnvelop envelop, resEnv;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    key = envelop.getObjects()[0];

    locked = smServer.isEntryLocked(key);

    response = new CommandDataPair();
    response.setCommand(CMD_IS_ENTRY_LOCKED);

    resEnv = new RLTalkObjectEnvelop();
    resEnv.setAsyncTransId(envelop.getAsyncTransId());
    resEnv.setAsyncState(TRANSACTION_SUCCEEDED);

    resEnv.setObjects(new Serializable[] { new Boolean(locked) });

    response.setData(resEnv);

    _rlTalkSend(response);
  }

}
