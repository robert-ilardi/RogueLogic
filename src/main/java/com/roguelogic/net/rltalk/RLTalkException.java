/*
 * Created on Oct 26, 2005
 */
package com.roguelogic.net.rltalk;

import com.roguelogic.net.RLNetException;

/**
 * @author rilardi
 */

public class RLTalkException extends RLNetException {

  public RLTalkException() {
    super();
  }

  /**
   * @param mesg
   */
  public RLTalkException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public RLTalkException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public RLTalkException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
