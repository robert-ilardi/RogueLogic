/**
 * Created Jan 8, 2009
 */
package com.roguelogic.util;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 * 
 */

public interface StorageBackedQueue {

  public void initQueue() throws StorageBackedQueueException;

  /**
   * Close and permanently destroy.
   * 
   * @throws StorageBackedQueueException
   */
  public void destroyQueue() throws StorageBackedQueueException;

  public void enqueue(Serializable element) throws StorageBackedQueueException;

  public Serializable dequeue() throws StorageBackedQueueException;

  public Serializable peek() throws StorageBackedQueueException;

  public void clear() throws StorageBackedQueueException;

  public boolean isEmpty() throws StorageBackedQueueException;

  /**
   * Close but do not destroy.
   * 
   * @throws StorageBackedQueueException
   */
  public void closeQueue() throws StorageBackedQueueException;

}
