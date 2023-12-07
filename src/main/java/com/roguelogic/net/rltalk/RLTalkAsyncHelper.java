package com.roguelogic.net.rltalk;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketSession;

public class RLTalkAsyncHelper {

  protected Object syncCallLock;
  protected int[] transactionIdPool;

  public static final int TRANSACTION_ID_NOT_RESERVED = 0;
  public static final int TRANSACTION_ID_RESERVED = 1;
  public static final int TRANSACTION_SUCCEEDED = 2;
  public static final int TRANSACTION_FAILED = 3;

  public static final int MAX_TRANSACTIONS = 999;

  public RLTalkAsyncHelper() {
    syncCallLock = new Object();
    transactionIdPool = new int[MAX_TRANSACTIONS];
    resetAllTransactions();
  }

  public int obtainTransactionId() throws RLTalkException {
    int transId = -1;

    synchronized (transactionIdPool) {
      for (int i = 0; i < transactionIdPool.length; i++) {
        if (transactionIdPool[i] == TRANSACTION_ID_NOT_RESERVED) {
          transactionIdPool[i] = TRANSACTION_ID_RESERVED;
          transId = i;
          break;
        }
      }

      if (transId == -1) {
        throw new RLTalkException("Could NOT reserve Transaction Id! Max number of Simultaneous Transactions in Progress!");
      }
    }

    return transId;
  }

  public void releaseTransactionId(int transId) {
    if (transId >= 0 && transId < transactionIdPool.length)
      synchronized (transactionIdPool) {
        transactionIdPool[transId] = TRANSACTION_ID_NOT_RESERVED;
      }
  }

  public boolean transactionComplete(int transId) {
    return (transId >= 0 && transId < transactionIdPool.length) && (transactionIdPool[transId] == TRANSACTION_SUCCEEDED || transactionIdPool[transId] == TRANSACTION_FAILED);
  }

  public int getTransactionFlag(int transId) {
    int flag = -1;

    if (transId >= 0 && transId < transactionIdPool.length) {
      flag = transactionIdPool[transId];
    }

    return flag;
  }

  public void setTransactionState(int transId, int state) {
    synchronized (syncCallLock) {
      transactionIdPool[transId] = state;
      syncCallLock.notifyAll();
    }
  }

  public void sendAndWait(SocketSession session, CommandDataPair cmDatPair, int transId) throws RLNetException, InterruptedException {
    synchronized (syncCallLock) {
      RLTalkUtils.RLTalkSend(session, cmDatPair);

      while (!transactionComplete(transId)) {
        syncCallLock.wait();
      }
    } //End Open Request Sync Call
  }

  public void sendAndWait(SocketSession session, CommandDataPair cmDatPair, int transId, int timeoutSecs) throws RLNetException, InterruptedException {
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
    synchronized (syncCallLock) {
      for (int i = 0; i < transactionIdPool.length; i++) {
        transactionIdPool[i] = TRANSACTION_FAILED;
      }

      syncCallLock.notifyAll();
    }
  }

  public void resetAllTransactions() {
    synchronized (syncCallLock) {
      for (int i = 0; i < transactionIdPool.length; i++) {
        transactionIdPool[i] = TRANSACTION_ID_NOT_RESERVED;
      }
    }
  }

}
