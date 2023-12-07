package com.roguelogic.containercore;

import com.roguelogic.util.RLException;

public class TransportLayerException extends RLException {

  public TransportLayerException() {
    super();
  }

  public TransportLayerException(String mesg) {
    super(mesg);
  }

  public TransportLayerException(Throwable t) {
    super(t);
  }

  public TransportLayerException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
