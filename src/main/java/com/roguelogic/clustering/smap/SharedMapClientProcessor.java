/**
 * Created Aug 5, 2008
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
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_SUCCEEDED;

import java.io.IOException;
import java.io.Serializable;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkObjectEnvelop;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 *
 */
public class SharedMapClientProcessor extends RLTalkSocketProcessor {

  private SharedMapClientImpl smClient;

  public SharedMapClientProcessor() {
    super();
  }

  public void setSmClient(SharedMapClientImpl smClient) {
    this.smClient = smClient;
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
    }
  }

  private void processLogin(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Boolean loginOk = null;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      loginOk = (Boolean) envelop.getObjects()[0];
    }

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), loginOk);
  }

  private void processGet(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Serializable value = null;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState()) && envelop.getObjects() != null) {
      value = (Serializable) envelop.getObjects()[0];
    }

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), value);
  }

  private void processPut(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Serializable value = null;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState()) && envelop.getObjects() != null) {
      value = (Serializable) envelop.getObjects()[0];
    }

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), value);
  }

  private void processContainsKey(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Boolean contains = null;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState()) && envelop.getObjects() != null) {
      contains = (Boolean) envelop.getObjects()[0];
    }

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), contains);
  }

  private void processClear(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
  }

  private void processRemove(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Serializable value = null;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState()) && envelop.getObjects() != null) {
      value = (Serializable) envelop.getObjects()[0];
    }

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), value);
  }

  private void processLockEntry(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Boolean locked = null;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState()) && envelop.getObjects() != null) {
      locked = (Boolean) envelop.getObjects()[0];
    }

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), locked);
  }

  private void processUnlockEntry(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
  }

  private void processIsEntryLocked(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Boolean locked = null;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState()) && envelop.getObjects() != null) {
      locked = (Boolean) envelop.getObjects()[0];
    }

    smClient.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), locked);
  }

}
