/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.hapair;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 * 
 */

public class HaPairException extends RLException {

  public HaPairException() {
  }

  /**
   * @param mesg
   */
  public HaPairException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public HaPairException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public HaPairException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
