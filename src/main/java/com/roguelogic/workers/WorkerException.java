package com.roguelogic.workers;

import com.roguelogic.util.RLException;

public class WorkerException extends RLException {

  public WorkerException() {
    super();
  }

  public WorkerException(String mesg) {
    super(mesg);
  }

  public WorkerException(Throwable t) {
    super(t);
  }

  public WorkerException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
