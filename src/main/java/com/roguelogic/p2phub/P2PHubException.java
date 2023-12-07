/**
 * Created Sep 16, 2006
 */
package com.roguelogic.p2phub;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class P2PHubException extends RLException {

  public P2PHubException() {
    super();
  }

  /**
   * @param mesg
   */
  public P2PHubException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public P2PHubException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public P2PHubException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
