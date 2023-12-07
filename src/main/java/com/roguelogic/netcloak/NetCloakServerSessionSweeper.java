package com.roguelogic.netcloak;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;

public class NetCloakServerSessionSweeper implements SocketSessionSweeper {

  public static final String USOBJ_NET_CLOAK_USER_SOCKET_CLIENT_MAP = "NetCloakUserSocketClientMap";

  public NetCloakServerSessionSweeper() {}

  public void cleanup(SocketSession userSession) {
    HashMap<Integer, SocketClient> sockClientMap;
    Collection<SocketClient> sockClients;
    Iterator<SocketClient> iter;
    SocketClient sockClient;

    if (userSession == null) {
      return;
    }

    sockClientMap = (HashMap<Integer, SocketClient>) userSession.getUserItem(USOBJ_NET_CLOAK_USER_SOCKET_CLIENT_MAP);
    sockClients = sockClientMap.values();
    iter = sockClients.iterator();

    while (iter.hasNext()) {
      sockClient = iter.next();
      sockClient.close();
    }

    sockClientMap.clear();
  }

}
