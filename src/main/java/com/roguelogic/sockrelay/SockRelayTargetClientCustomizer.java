package com.roguelogic.sockrelay;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;

public class SockRelayTargetClientCustomizer implements SocketProcessorCustomizer {

  private SocketSession rootSession;

  public SockRelayTargetClientCustomizer() {}

  public void setRootSession(SocketSession rootSession) {
    this.rootSession = rootSession;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    SockRelayTargetClientProcessor srtcp = (SockRelayTargetClientProcessor) processor;
    srtcp.setRootSession(rootSession);
  }

}
