package com.roguelogic.ipcp2p;

import com.roguelogic.net.HSWSockSessionEnvelop;
import com.roguelogic.net.HandshakeWatcher;
import com.roguelogic.net.SocketSession;

public class IPCHandshakeWatcher extends HandshakeWatcher {

  public IPCHandshakeWatcher() {
    super();
  }

  @Override
  protected boolean validateHandshake(HSWSockSessionEnvelop envelop) {
    SocketSession sockSession;
    Boolean connValid = null;

    sockSession = envelop.getSockSession();
    if (!sockSession.isZombie()) {
      connValid = (Boolean) sockSession.getUserItem(IPCServerProcessor.UDI_CONNECTION_VALID_FLAG);
    }

    return connValid != null && connValid.booleanValue();
  }

}
