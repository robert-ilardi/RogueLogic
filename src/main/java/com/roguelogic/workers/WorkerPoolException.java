package com.roguelogic.workers;

import com.roguelogic.util.RLException;

public class WorkerPoolException extends RLException {

  public WorkerPoolException() {
    super();
  }

  public WorkerPoolException(String mesg) {
    super(mesg);
  }

  public WorkerPoolException(Throwable t) {
    super(t);
  }

  public WorkerPoolException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
