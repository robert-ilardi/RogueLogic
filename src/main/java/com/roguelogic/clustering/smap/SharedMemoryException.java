/**
 * Created Aug 4, 2008
 */
package com.roguelogic.clustering.smap;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class SharedMemoryException extends RLException {

  public SharedMemoryException() {
    super();
  }

  /**
   * @param mesg
   */
  public SharedMemoryException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public SharedMemoryException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public SharedMemoryException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
