package com.roguelogic.netcloak;

import com.roguelogic.util.RLException;

public class NetCloakException extends RLException {

  public NetCloakException() {
    super();
  }

  public NetCloakException(String mesg) {
    super(mesg);
  }

  public NetCloakException(Throwable t) {
    super(t);
  }

  public NetCloakException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
