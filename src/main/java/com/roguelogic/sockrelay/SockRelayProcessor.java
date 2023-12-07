package com.roguelogic.sockrelay;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketSession;

public class SockRelayProcessor implements SocketProcessor {

  private int targetPort;
  private String targetAddress;

  public static final String USOBJ_TARGET_SOCK_CLIENT = "SockRelayServerTargetSockClient";

  public SockRelayProcessor() {}

  public void setTargetAddress(String targetAddress) {
    this.targetAddress = targetAddress;
  }

  public void setTargetPort(int targetPort) {
    this.targetPort = targetPort;
  }

  public void clearSession() {}

  public void destroyProcessor() {}

  public void process(SocketSession userSession, byte[] data) throws RLNetException {
    SocketClient sockClient;
    SockRelayTargetClientCustomizer customizer;

    synchronized (userSession) {
      if (!userSession.wasHandshook()) {
        userSession.setHandshookStatus(true);

        customizer = new SockRelayTargetClientCustomizer();
        customizer.setRootSession(userSession);

        sockClient = new SocketClient();
        sockClient.setSocketProcessorClass(SockRelayTargetClientProcessor.class);
        sockClient.setSocketProcessorCustomizer(customizer);
        sockClient.connect(targetAddress, targetPort);

        userSession.putUserItem(USOBJ_TARGET_SOCK_CLIENT, sockClient);
      }
      else {
        sockClient = (SocketClient) userSession.getUserItem(USOBJ_TARGET_SOCK_CLIENT);
        synchronized (sockClient) {
          if (sockClient != null) {
            sockClient.send(data);
          }
        }
      }
    }
  }

}
