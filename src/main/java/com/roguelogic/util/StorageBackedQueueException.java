/**
 * Created Jan 8, 2009
 */
package com.roguelogic.util;

/**
 * @author Robert C. Ilardi
 *
 */
public class StorageBackedQueueException extends RLException {

  public StorageBackedQueueException() {}

  /**
   * @param mesg
   */
  public StorageBackedQueueException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public StorageBackedQueueException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public StorageBackedQueueException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
