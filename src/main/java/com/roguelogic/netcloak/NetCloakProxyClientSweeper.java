package com.roguelogic.netcloak;

import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;

public class NetCloakProxyClientSweeper implements SocketSessionSweeper {

  public NetCloakProxyClientSweeper() {}

  public synchronized void cleanup(SocketSession userSession) {
    NetCloakClient ncc;
    Integer cIdObj;
    int connectionId;

    try {
      cIdObj = (Integer) userSession.getUserItem(NetCloakProxyClientProcessor.USOBJ_CONNECTION_ID);
      if (cIdObj != null) {
        connectionId = cIdObj.intValue();
        ncc = (NetCloakClient) userSession.getGlobalItem(NetCloakClient.GSOBJ_NET_CLOAK_CLIENT);

        ncc.close(connectionId);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
