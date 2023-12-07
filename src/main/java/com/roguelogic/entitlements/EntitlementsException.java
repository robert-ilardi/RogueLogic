/**
 * Created Oct 23, 2006
 */
package com.roguelogic.entitlements;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class EntitlementsException extends RLException {

  public EntitlementsException() {
    super();
  }

  /**
   * @param mesg
   */
  public EntitlementsException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public EntitlementsException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public EntitlementsException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
