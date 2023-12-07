package com.roguelogic.sockrelay;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketSession;

public class SockRelayTargetClientProcessor implements SocketProcessor {

  private SocketSession rootSession;

  public SockRelayTargetClientProcessor() {}

  public void setRootSession(SocketSession rootSession) {
    this.rootSession = rootSession;
  }

  public void clearSession() {}

  public void destroyProcessor() {}

  public void process(SocketSession userSession, byte[] data) throws RLNetException {
    synchronized (rootSession) {
      rootSession.send(data);
    }
  }

}
