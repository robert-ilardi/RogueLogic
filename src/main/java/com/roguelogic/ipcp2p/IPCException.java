package com.roguelogic.ipcp2p;

import com.roguelogic.util.RLException;

public class IPCException extends RLException {

  public IPCException() {
    super();
  }

  public IPCException(String mesg) {
    super(mesg);
  }

  public IPCException(Throwable t) {
    super(t);
  }

  public IPCException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
