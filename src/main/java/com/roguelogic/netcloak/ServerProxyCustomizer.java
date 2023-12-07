/*
 * Created on Mar 3, 2006
 */
package com.roguelogic.netcloak;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;

public class ServerProxyCustomizer implements SocketProcessorCustomizer {

  private int connectionId;
  private SocketSession userSession;

  public ServerProxyCustomizer() {}

  /**
   * @param connectionId The connectionId to set.
   */
  public void setConnectionId(int connectionId) {
    this.connectionId = connectionId;
  }

  /**
   * @param userSession The userSession to set.
   */
  public void setUserSession(SocketSession userSession) {
    this.userSession = userSession;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    NetCloakServerProxyProcessor proxyProcessor = (NetCloakServerProxyProcessor) processor;
    proxyProcessor.setRootSession(userSession);
    proxyProcessor.setConnectionId(connectionId);
  }

}
