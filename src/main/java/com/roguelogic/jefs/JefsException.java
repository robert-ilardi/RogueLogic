/**
 * Created Sep 9, 2012
 */
package com.roguelogic.jefs;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class JefsException extends RLException {

  public JefsException() {
    super();
  }

  /**
   * @param mesg
   */
  public JefsException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public JefsException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public JefsException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
