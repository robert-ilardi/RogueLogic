package com.roguelogic.workers;

public interface WorkerCustomizer {
  public void initWorker(Worker worker) throws WorkerException;

  public void configureWorker(Worker worker, WorkerParameter param) throws WorkerException;
}
