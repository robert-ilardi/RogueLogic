/*
 * Created on Aug 11, 2006
 */

package com.roguelogic.dhtable;

import com.roguelogic.util.RLException;

/**
 * @author rilardi
 */

public class RemoteHashTableException extends RLException {

  public RemoteHashTableException() {
    super();
  }

  /**
   * @param mesg
   */
  public RemoteHashTableException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public RemoteHashTableException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public RemoteHashTableException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
