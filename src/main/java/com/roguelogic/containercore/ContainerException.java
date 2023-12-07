package com.roguelogic.containercore;

import com.roguelogic.util.RLException;

public class ContainerException extends RLException {

  public ContainerException() {
    super();
  }

  public ContainerException(String mesg) {
    super(mesg);
  }

  public ContainerException(Throwable t) {
    super(t);
  }

  public ContainerException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
