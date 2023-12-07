/*
 * Created on Aug 18, 2006
 */
package com.roguelogic.dhtable;

import com.roguelogic.util.RLException;

/**
 * @author rilardi
 */

public class DistributedHashTableException extends RLException {

  public DistributedHashTableException() {
    super();
  }

  /**
   * @param mesg
   */
  public DistributedHashTableException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public DistributedHashTableException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public DistributedHashTableException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
