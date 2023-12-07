/**
 * Created Jan 20, 2007
 */
package com.roguelogic.propexchange;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class PropExchangeException extends RLException {

  public PropExchangeException() {
    super();
  }

  /**
   * @param mesg
   */
  public PropExchangeException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public PropExchangeException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public PropExchangeException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
