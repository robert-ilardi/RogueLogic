package com.roguelogic.net.rltalk;

import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_FAILED;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_FAILED_WITH_EXCEPTION;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_SUCCEEDED;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_UNKNOWN_STATE;

import java.util.ArrayList;
import java.util.HashMap;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketSession;
import com.roguelogic.util.StringUtils;

public class RLTalkObjectEnvelopAsyncHelper {

  private HashMap<String, Integer> transIdLedger;

  protected Object syncCallLock;

  public RLTalkObjectEnvelopAsyncHelper() {
    syncCallLock = new Object();
    transIdLedger = new HashMap<String, Integer>();
  }

  public String obtainTransactionId() throws RLTalkException {
    String transId;

    transId = StringUtils.GenerateTimeUniqueId();

    return transId;
  }

  public boolean transactionComplete(String transId) {
    boolean completed = false;
    Integer val;

    synchronized (syncCallLock) {
      val = transIdLedger.get(transId);
      completed = TRANSACTION_SUCCEEDED.equals(val) || TRANSACTION_FAILED.equals(val) || TRANSACTION_FAILED_WITH_EXCEPTION.equals(val);
    }

    return completed;
  }

  public void sendAndWait(SocketSession session, CommandDataPair cmDatPair, String transId) throws RLNetException, InterruptedException {
    synchronized (syncCallLock) {
      RLTalkUtils.RLTalkSend(session, cmDatPair);

      while (!transactionComplete(transId)) {
        syncCallLock.wait();
      }
    } //End Open Request Sync Call
  }

  public void sendAndWait(SocketSession session, CommandDataPair cmDatPair, String transId, int timeoutSecs) throws RLNetException, InterruptedException {
    synchronized (syncCallLock) {
      if (timeoutSecs < 0) {
        timeoutSecs *= -1;
      }

      RLTalkUtils.RLTalkSend(session, cmDatPair);

      for (int i = 1; i <= timeoutSecs; i++) {
        if (!transactionComplete(transId)) {
          syncCallLock.wait(1000);
        }
        else {
          break;
        }
      }

      if (!transactionComplete(transId)) {
        throw new RLTalkException("SendAndWait Timeout Expired! Transaction has NOT been completed within time limit!");
      }
    } //End Open Request Sync Call
  }

  public void killAllTransactions() {
    ArrayList<String> transIds;

    synchronized (syncCallLock) {
      transIds = new ArrayList<String>(transIdLedger.keySet());

      for (String key : transIds) {
        transIdLedger.put(key, TRANSACTION_FAILED);
      }

      syncCallLock.notifyAll();
    }
  }

  public Integer getTransactionFlag(String transId) {
    Integer state;

    synchronized (syncCallLock) {
      state = transIdLedger.get(transId);

      if (state == null) {
        state = TRANSACTION_UNKNOWN_STATE;
      }
    }

    return state;
  }

  public void setTransactionState(String transId, Integer state) {
    synchronized (syncCallLock) {
      transIdLedger.put(transId, state);
      syncCallLock.notifyAll();
    }
  }

  public void removeTransactionId(String transId) {
    synchronized (syncCallLock) {
      transIdLedger.remove(transId);
      syncCallLock.notifyAll();
    }
  }

  public void clearAllTransactionIds() {
    synchronized (syncCallLock) {
      transIdLedger.clear();
      syncCallLock.notifyAll();
    }
  }

}
