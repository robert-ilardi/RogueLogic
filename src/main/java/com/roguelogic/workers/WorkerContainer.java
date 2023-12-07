package com.roguelogic.workers;

public class WorkerContainer implements Runnable {

  private Worker worker;
  private boolean checkedOut;

  private boolean shrinkable;
  private int workerId;

  private boolean useThreading;
  private Thread workerThread;
  private boolean workLoopEnabled;
  private boolean doWork;
  private Object workThreadLock;
  private WorkerParameter workerParam;

  private WorkerPool pool;

  protected WorkerContainer() {
    checkedOut = false;
    useThreading = false;
    workLoopEnabled = false;
    doWork = false;
    workThreadLock = new Object();
  }

  protected boolean isCheckedOut() {
    return checkedOut;
  }

  protected void setCheckedOut(boolean checkedOut) {
    this.checkedOut = checkedOut;
  }

  protected Worker getWorker() {
    return worker;
  }

  protected void setWorker(Worker worker) {
    this.worker = worker;
  }

  public boolean isUsingThreading() {
    return useThreading;
  }

  protected void setUseThreading(boolean useThreading) {
    this.useThreading = useThreading;
  }

  public void run() {
    try {
      while (workLoopEnabled) {
        synchronized (workThreadLock) {
          while (!doWork && workLoopEnabled) {
            workThreadLock.wait();
          }
        }

        if (!workLoopEnabled) {
          return;
        }

        try {
          performWork(workerParam);
        }
        catch (WorkerException we) {
          we.printStackTrace();
        }

        synchronized (workThreadLock) {
          workerParam = null;
          doWork = false;
          pool.releaseWorker(this);
          workThreadLock.notifyAll();
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      workLoopEnabled = false;

      if (doWork) {
        synchronized (workThreadLock) {
          workerParam = null;
          doWork = false;
          pool.releaseWorker(this);
          workThreadLock.notifyAll();
        }
      }
    }
  }

  protected void startThread() throws WorkerPoolException {
    if (!useThreading) {
      throw new WorkerPoolException("Threading NOT enabled for this Worker Container!");
    }
    else if (workLoopEnabled) {
      throw new WorkerPoolException("Worker Thread Already Running!");
    }
    else {
      workLoopEnabled = true;
      workerThread = new Thread(this);
      workerThread.start();
    }
  }

  protected void stopThread() throws WorkerPoolException {
    try {
      synchronized (workThreadLock) {
        while (doWork) {
          workThreadLock.wait();
        }

        workLoopEnabled = false;
        workThreadLock.notifyAll();
      }
    }
    catch (InterruptedException e) {
      throw new WorkerPoolException("Error while attempting to Stop Worker Thread!", e);
    }
  }

  protected void performThreadedWork(WorkerParameter workerParam) throws WorkerPoolException {
    synchronized (workThreadLock) {
      if (!doWork) {
        doWork = true;
        this.workerParam = workerParam;
        workThreadLock.notifyAll();
      }
      else {
        throw new WorkerPoolException("Worker Thread Already Performing Work!");
      }
    }
  }

  protected void performWork(WorkerParameter param) throws WorkerException {
    worker.performWork(param);
  }

  protected void setPool(WorkerPool pool) {
    this.pool = pool;
  }

  protected void setWorkerId(int workerId) {
    this.workerId = workerId;
  }

  protected int getWorkerId() {
    return workerId;
  }

  protected boolean isShrinkable() {
    return shrinkable;
  }

  protected void setShrinkable(boolean shrinkable) {
    this.shrinkable = shrinkable;
  }

  protected void destroyWorker() {
    this.worker.destroyWorker();
  }

}
