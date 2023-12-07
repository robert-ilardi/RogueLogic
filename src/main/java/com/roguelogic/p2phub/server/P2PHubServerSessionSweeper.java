package com.roguelogic.p2phub.server;

import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.p2phub.P2PHubPeer;

public class P2PHubServerSessionSweeper implements SocketSessionSweeper {

  private P2PHubServer server;

  public P2PHubServerSessionSweeper() {}

  public void setServer(P2PHubServer server) {
    this.server = server;
  }

  public void cleanup(SocketSession userSession) {
    P2PHubPeer self;

    if (userSession == null) {
      return;
    }

    self = (P2PHubPeer) userSession.getUserItem(P2PHubServerProcessor.USOBJ_P2P_HUB_SERVER_SESSION_PEER_INFO);
    server.removeSessionPeerAssociation(self);
    server.removeHeartBeatEntry(self);
  }

}
