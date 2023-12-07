/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.sflap;

import com.roguelogic.util.RLException;

/**
 * @author rilardi
 */

public class SFLAPFrameException extends RLException {

  public SFLAPFrameException() {
    super();
  }

  /**
   * @param msg
   */
  public SFLAPFrameException(String msg) {
    super(msg);
  }

  /**
   * @param msg
   * @param t
   */
  public SFLAPFrameException(String msg, Throwable t) {
    super(msg, t);
  }

  /**
   * @param t
   */
  public SFLAPFrameException(Throwable t) {
    super(t);
  }

}