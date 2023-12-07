/**
 * Created Nov 20, 2007
 */
package com.roguelogic.pmd;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class PMDException extends RLException {

  public PMDException() {
    super();
  }

  /**
   * @param mesg
   */
  public PMDException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public PMDException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public PMDException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
