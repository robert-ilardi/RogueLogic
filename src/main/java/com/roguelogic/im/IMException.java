/*
 * Created on Mar 18, 2005
 */
package com.roguelogic.im;

import com.roguelogic.util.RLException;

/**
 * @author rilardi
 */
public class IMException extends RLException {

  /**
   * 
   */
  public IMException() {
    super();
  }

  /**
   * @param msg
   */
  public IMException(String msg) {
    super(msg);
  }

  /**
   * @param msg
   * @param t
   */
  public IMException(String msg, Throwable t) {
    super(msg, t);
  }

  /**
   * @param t
   */
  public IMException(Throwable t) {
    super(t);
  }

}
