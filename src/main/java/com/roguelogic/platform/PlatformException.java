/*
 * Created on Sep 29, 2005
 */
package com.roguelogic.platform;

import com.roguelogic.util.RLException;

/**
 * @author rilardi
 */

public class PlatformException extends RLException {

  public PlatformException() {
    super();
  }

  /**
   * @param mesg
   */
  public PlatformException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public PlatformException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public PlatformException(String mesg, Throwable t) {
    super(mesg, t);
  }

}

