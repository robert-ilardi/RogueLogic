package com.roguelogic.containercore;

import com.roguelogic.util.RLException;

public class ContainerKernelException extends RLException {

  public ContainerKernelException() {
    super();
  }

  public ContainerKernelException(String mesg) {
    super(mesg);
  }

  public ContainerKernelException(Throwable t) {
    super(t);
  }

  public ContainerKernelException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
