/*
 * Created on Jan 30, 2006
 */
package com.roguelogic.workers;

import com.roguelogic.util.RLException;

public class InvalidWorkerClassException extends RLException {

  public InvalidWorkerClassException() {
    super();
  }

  public InvalidWorkerClassException(String mesg) {
    super(mesg);
  }

  public InvalidWorkerClassException(Throwable t) {
    super(t);
  }

  public InvalidWorkerClassException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
