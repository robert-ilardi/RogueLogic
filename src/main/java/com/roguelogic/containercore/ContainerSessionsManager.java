package com.roguelogic.containercore;

import java.io.Serializable;
import java.util.HashMap;

import com.roguelogic.ipcp2p.IPCDataObserver;
import com.roguelogic.ipcp2p.IPCMessage;
import com.roguelogic.util.OutOfUniqueIdsException;
import com.roguelogic.util.RLException;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.UniqueIdPool;

public class ContainerSessionsManager implements IPCDataObserver {

  public static final int SESSION_ID_LEN = 64;
  public static final int UNIQUE_SESSION_NUMBER_LEN = 6;
  public static final int MAX_UNIQUE_SESSION_NUMBER = 999999;

  private HashMap<String, ContainerSession> sessions;
  private UniqueIdPool uniqueIdPool;

  private ContainerKernel cntnrKernel;

  public ContainerSessionsManager(ContainerKernel cntnrKernel) {
    this.cntnrKernel = cntnrKernel;
    sessions = new HashMap<String, ContainerSession>();

    try {
      uniqueIdPool = new UniqueIdPool("LCTP-Session-Manager-ID-Pool", MAX_UNIQUE_SESSION_NUMBER);
    }
    catch (RLException e) {
      e.printStackTrace();
    }
  }

  public synchronized ContainerSession createSession() throws OutOfUniqueIdsException {
    ContainerSession session;
    String sessionId;

    sessionId = generateSessionId();
    session = new ContainerSession(sessionId, this);
    sessions.put(sessionId, session);

    return session;
  }

  private synchronized String generateSessionId() throws OutOfUniqueIdsException {
    StringBuffer sId = new StringBuffer();
    String tuId;
    int maxRandomChars, uniqueNum;

    tuId = StringUtils.GenerateTimeUniqueId();
    maxRandomChars = (SESSION_ID_LEN - (UNIQUE_SESSION_NUMBER_LEN + tuId.length()));

    for (int i = 1; i <= maxRandomChars; i++) {
      if (((int) (Math.random() * 100)) > 50) {
        sId.append((char) (65 + ((int) (26 * Math.random()))));
      }
      else {
        sId.append((char) (48 + ((int) (10 * Math.random()))));
      }
    }

    uniqueNum = uniqueIdPool.obtainId();
    sId.append(StringUtils.LPad(String.valueOf(uniqueNum), '0', UNIQUE_SESSION_NUMBER_LEN));

    sId.append(tuId);

    return sId.toString();
  }

  public ContainerSession getSession(String sessionId) {
    ContainerSession session;

    session = sessions.get(sessionId);

    return session;
  }

  public void remove(String sessionId) {
    ContainerSession session = sessions.remove(sessionId);
  }

  public boolean reestablishSession(String sessionId) {
    boolean reestablished = false;
    ContainerSession session;

    session = getSession(sessionId);

    reestablished = (session != null);

    return reestablished;
  }

  public void removeAll() {

  }

  public void signalDataChange(ContainerSession session, Serializable key, Serializable value) {
    synchronized (session.getDataMapLock()) {

    }
  }

  public void signalDataDeletion(ContainerSession session, Serializable key) {
    synchronized (session.getDataMapLock()) {

    }
  }

  public void signalDataCleared(ContainerSession session) {
    synchronized (session.getDataMapLock()) {

    }
  }

  public void receiveData(IPCMessage mesg) {}

}
