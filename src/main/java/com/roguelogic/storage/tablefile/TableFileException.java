/**
 * Created Sep 14, 2007
 */
package com.roguelogic.storage.tablefile;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class TableFileException extends RLException {

  public TableFileException() {
    super();
  }

  /**
   * @param mesg
   */
  public TableFileException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public TableFileException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public TableFileException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
