package com.roguelogic.netcloak;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketSession;

public class NetCloakProxyClientProcessor implements SocketProcessor {

  public static final String USOBJ_CONNECTION_ID = "NetCloak_TunnelConnectionId";

  public static final String GSOBJ_REMOTE_TARGET_PORT = "NetCloak_ProxyRemoteTargetPort";

  private NetCloakClient client;

  public NetCloakProxyClientProcessor() {}

  public void setClient(NetCloakClient client) {
    this.client = client;
  }

  public void clearSession() {}

  public void destroyProcessor() {
    client = null;
  }

  public void process(SocketSession userSession, byte[] data) throws RLNetException {
    Integer cIdObj;
    int connectionId, remoteTargetPort;

    if (!userSession.wasHandshook()) {
      //Perform Server Initiated Handshake
      userSession.setHandshookStatus(true);

      //Open Tunnel Session
      try {
        remoteTargetPort = (Integer) userSession.getGlobalItem(GSOBJ_REMOTE_TARGET_PORT);
        connectionId = client.open(remoteTargetPort);
        if (connectionId != -1) {
          userSession.putUserItem(USOBJ_CONNECTION_ID, connectionId);
          client.associateSessionWithTunnel(userSession, connectionId);
        }
        else {
          userSession.endSession();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        userSession.endSession();
      }
    }
    else {
      //Normal Read Processing
      if (!userSession.isZombie()) {
        cIdObj = (Integer) userSession.getUserItem(USOBJ_CONNECTION_ID);
        if (cIdObj != null) {
          connectionId = cIdObj.intValue();
          client.send(connectionId, data);
        }
      }
    }
  }

}
