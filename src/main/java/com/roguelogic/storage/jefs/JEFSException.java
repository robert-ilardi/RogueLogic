/**
 * Created Jan 7, 2008
 */
package com.roguelogic.storage.jefs;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class JEFSException extends RLException {

  public JEFSException() {}

  /**
   * @param mesg
   */
  public JEFSException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public JEFSException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public JEFSException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
