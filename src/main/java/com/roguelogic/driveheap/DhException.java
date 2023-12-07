/**
 * Created Sep 24, 2011
 */
package com.roguelogic.driveheap;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class DhException extends RLException {

  public DhException() {
    super();
  }

  /**
   * @param mesg
   */
  public DhException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public DhException(Throwable t) {
    super(t);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param mesg
   * @param t
   */
  public DhException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
