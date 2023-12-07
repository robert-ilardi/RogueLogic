/**
 * Created Sep 16, 2006
 */
package com.roguelogic.roguenet;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class RogueNetException extends RLException {

  public RogueNetException() {
    super();
  }

  /**
   * @param mesg
   */
  public RogueNetException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public RogueNetException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public RogueNetException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
