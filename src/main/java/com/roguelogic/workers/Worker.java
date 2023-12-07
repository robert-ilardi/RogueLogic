/*
 * Created on Jan 30, 2006
 */
package com.roguelogic.workers;

public interface Worker {
  public void performWork(WorkerParameter param) throws WorkerException;

  public void destroyWorker();
}
