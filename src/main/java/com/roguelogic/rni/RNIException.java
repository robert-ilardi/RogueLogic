/**
 * Created Feb 1, 2007
 */
package com.roguelogic.rni;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNIException extends RLException {

  public RNIException() {
    super();
  }

  /**
   * @param mesg
   */
  public RNIException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public RNIException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public RNIException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
