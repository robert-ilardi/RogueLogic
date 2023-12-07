/**
 * Created Jan 15, 2008
 */
package com.roguelogic.hptunnel;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class HPTException extends RLException {

  public HPTException() {
    super();
  }

  /**
   * @param mesg
   */
  public HPTException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public HPTException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public HPTException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
