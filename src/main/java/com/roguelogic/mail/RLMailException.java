/**
 * Created Feb 5, 2008
 */
package com.roguelogic.mail;

/**
 * @author Robert C. Ilardi
 *
 */

public class RLMailException extends Exception {

  public RLMailException() {
    super();
  }

  /**
   * @param mesg
   */
  public RLMailException(String mesg) {
    super(mesg);
  }

  /**
   * @param mesg
   * @param t
   */
  public RLMailException(String mesg, Throwable t) {
    super(mesg, t);
  }

  /**
   * @param t
   */
  public RLMailException(Throwable t) {
    super(t);
  }

}
