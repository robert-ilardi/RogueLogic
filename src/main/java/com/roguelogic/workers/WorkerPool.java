/*
 * Created on Jan 30, 2006
 */
package com.roguelogic.workers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import com.roguelogic.util.InvalidValueException;
import com.roguelogic.util.SystemUtils;

public class WorkerPool {

  private String poolName;

  private Class workerClass;

  private int initialSize;
  private int maxSize;

  private boolean shrinkable;
  private boolean useThreads;

  private ArrayList<WorkerContainer> workers;

  private Semaphore workersLock;

  private boolean frozen;

  private WorkerCustomizer customizer;

  private static final boolean DEBUG_MODE = false;

  public WorkerPool(String poolName) {
    this.poolName = poolName;
    workers = new ArrayList<WorkerContainer>();
    workersLock = null;
    useThreads = false;
    shrinkable = false;
    customizer = null;
  }

  public String getPoolName() {
    return poolName;
  }

  public void setCustomizer(WorkerCustomizer customizer) {
    this.customizer = customizer;
  }

  public boolean isFrozen() {
    return frozen;
  }

  public void registerWorkerClass(Class workerClass) throws InvalidWorkerClassException {
    if (workerClass == null) {
      throw new InvalidWorkerClassException("Worker Pool '" + poolName + "' - Can NOT register NULL Worker Class!");
    }

    if (!SystemUtils.DoesClassImplement(workerClass, Worker.class)) {
      throw new InvalidWorkerClassException("Worker Pool '" + poolName + "' - Can NOT register class '" + workerClass.getName() + "' because it does NOT implement the Worker Interface!");
    }

    if (Modifier.isAbstract(workerClass.getModifiers())) {
      throw new InvalidWorkerClassException("Worker Pool '" + poolName + "' - Can NOT register class '" + workerClass.getName() + "' because it is Abstract!");
    }

    this.workerClass = workerClass;
  }

  /**
   * @return Returns the initialSize.
   */
  public int getInitialSize() {
    return initialSize;
  }

  /**
   * @param initialSize The initialSize to set.
   */
  public void setInitialSize(int initialSize) throws InvalidValueException {
    if (initialSize < 0) {
      throw new InvalidValueException("Worker Pool '" + poolName + "' - Initial Size MUST be greater than or equal to ZERO!");
    }

    this.initialSize = initialSize;
  }

  /**
   * @return Returns the maxSize.
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * @param maxSize The maxSize to set.
   */
  public void setMaxSize(int maxSize) throws InvalidValueException {
    if (maxSize < 1 && maxSize < initialSize) {
      throw new InvalidValueException("Worker Pool '" + poolName + "' - Max Size MUST be greater than or equal to 1 and MUST be greater than or equal to Initial Size!");
    }

    this.maxSize = maxSize;
  }

  public synchronized int getPoolSize() {
    return workers.size();
  }

  /**
   * @return Returns the shrinkable.
   */
  public boolean isShrinkable() {
    return shrinkable;
  }

  /**
   * @param shrinkable The shrinkable to set.
   */
  public void setShrinkable(boolean shrinkable) {
    this.shrinkable = shrinkable;
  }

  public synchronized void createPool() throws WorkerPoolException {
    WorkerContainer container;

    System.out.println("Creating Worker Pool: " + poolName);

    if (workers.size() == 0) {
      if (initialSize == 0 && maxSize == 0) {
        throw new WorkerPoolException("Can NOT start Worker Pool '" + poolName + "' because Initial Size = " + initialSize + " and/or Max Size = " + maxSize + " are invalid!");
      }

      //Do initial Population
      try {
        for (int i = 1; i <= initialSize; i++) {
          container = createWorker();
          workers.add(container);
        }
      }
      catch (Exception e) {
        workers.clear();
        throw new WorkerPoolException("Could NOT populate Worker Pool '" + poolName + "' with initial number of workers!", e);
      }

      workersLock = new Semaphore(maxSize, true);

      frozen = false;
    } //End workerCnt check
  }

  public void freezePool() {
    System.out.println("Freezing Worker Pool: " + poolName);
    frozen = true;
  }

  public void resumePool() {
    System.out.println("Resuming Worker Pool: " + poolName);
    frozen = false;
  }

  public void destroyPool() throws WorkerPoolException {
    WorkerContainer container = null;

    System.out.println("Destroying Worker Pool: " + poolName);

    frozen = true;
    try {
      System.out.println(poolName + "> Waiting to Acquire All Workers (Cnt = " + maxSize + ")...");
      workersLock.acquire(maxSize); //Wait until we lock all workers
      System.out.println(poolName + "> Acquired All Workers...");
    }
    catch (InterruptedException e) {
      throw new WorkerPoolException("Failed to Acquire ALL Worker from Worker Pool '" + poolName + "'!", e);
    }

    System.out.println(poolName + "> Stopping Workers...");
    while (!workers.isEmpty()) {
      container = workers.remove(0);
      container.stopThread();
      container.destroyWorker();
    }

    System.out.println("Pool '" + poolName + "' Destroyed!");
  }

  public void performWork(WorkerParameter param) throws WorkerPoolException, WorkerException {
    WorkerContainer container = null;
    boolean acquiredLock = false;

    if (frozen) {
      throw new WorkerPoolException("Worker Pool '" + poolName + " is Frozen!");
    }

    if (maxSize == 0) {
      throw new WorkerPoolException("Can NOT start Worker Pool '" + poolName + "' because Max Size is ZERO!");
    }

    try {
      workersLock.acquire(); //Block until a Worker is available
      acquiredLock = true;

      //Obtain Worker from Pool
      container = reserveWorker();

      if (customizer != null && container != null) {
        customizer.configureWorker(container.getWorker(), param);
      }

      //Perform Work!
      if (useThreads) {
        //Threaded Version
        container.performThreadedWork(param);
        acquiredLock = false;
      }
      else {
        //Non-Threaded Version
        container.performWork(param);
        acquiredLock = false;
        releaseWorker(container);
      }
    }
    catch (WorkerException e) {
      throw e;
    }
    catch (Exception e) {
      throw new WorkerPoolException("Failed to Acquire Worker from Worker Pool '" + poolName + "'!", e);
    }
    finally {
      if (acquiredLock) {
        releaseWorker(container);
      }
    }
  }

  private synchronized WorkerContainer reserveWorker() throws WorkerPoolException {
    WorkerContainer container = null;

    try {
      for (int i = 0; i < workers.size(); i++) {
        container = workers.get(i);
        if (!container.isCheckedOut()) {
          container.setCheckedOut(true);
          container.setWorkerId(i);

          if (DEBUG_MODE) {
            System.out.println("Worker Pool '" + poolName + "' ; Reserving Worker (Reuse) with Index = " + i);
          }

          break;
        }
        else {
          container = null;
        }
      }

      //Increase Available Worker Pool if within limits
      if (container == null && (maxSize == 0 || workers.size() < maxSize)) {
        container = createWorker();
        container.setCheckedOut(true);
        container.setWorkerId(workers.size());
        container.setShrinkable(shrinkable);

        workers.add(container);

        if (DEBUG_MODE) {
          System.out.println("Worker Pool '" + poolName + "' ; Unable to reuse worker, creating new Worker. Worker Cnt = " + workers.size() + " ; Max Size = " + maxSize);
        }
      }

      if (container == null && DEBUG_MODE) {
        System.out.println("Worker Pool '" + poolName + "' ; Unable to reuse and/or create additional Worker. Worker Cnt = " + workers.size() + " ; Max Size = " + maxSize);
      }
    }
    catch (Exception e) {
      throw new WorkerPoolException("Error while attempting to Reserve Worker from Pool '" + poolName + "'!", e);
    }

    return container;
  }

  protected synchronized void releaseWorker(WorkerContainer container) {
    if (DEBUG_MODE) {
      System.out.println("Worker Pool '" + poolName + "' ; Releasing Worker with Id = " + container.getWorkerId());
    }

    //Undo Checkout of Worker from Pool
    if (container != null) {
      container.setCheckedOut(false);
    }

    //If Shrinking is enabled, remove
    //unused workers from the pool until initial size
    if (container.isShrinkable()) {
      if (DEBUG_MODE) {
        System.out.println("Worker Pool '" + poolName + "' ; Shrinking - Worker Cnt = " + workers.size());
      }

      if (container.isUsingThreading()) {
        try {
          container.stopThread();
        }
        catch (WorkerPoolException e) {
          System.err.println("Unable to verify Worker Thread is Stopped!");
          e.printStackTrace();
        }
      }

      container.destroyWorker();
      workers.remove(container);

      if (DEBUG_MODE) {
        System.out.println("Worker Pool '" + poolName + "' ; Shrunk - Worker Cnt = " + workers.size());
      }
    }

    workersLock.release();
  }

  private WorkerContainer createWorker() throws InstantiationException, IllegalAccessException, WorkerPoolException, WorkerException {
    Worker worker;
    WorkerContainer container;

    worker = (Worker) workerClass.newInstance();

    if (customizer != null) {
      customizer.initWorker(worker);
    }

    container = new WorkerContainer();
    container.setCheckedOut(false);
    container.setUseThreading(useThreads);
    container.setWorker(worker);
    container.setPool(this);

    if (useThreads) {
      container.startThread();
    }

    return container;
  }

  public boolean isUseThreads() {
    return useThreads;
  }

  public void setUseThreads(boolean useThreads) {
    this.useThreads = useThreads;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("--- WorkerPool ---\n");

    sb.append("Pool Name: ");
    sb.append(poolName);
    sb.append("\n");

    sb.append("Worker Class: ");
    sb.append((workerClass != null ? workerClass.getName() : "[NULL]"));
    sb.append("\n");

    sb.append("Initial Pool Size: ");
    sb.append(initialSize);
    sb.append("\n");

    sb.append("Max Pool Size: ");
    sb.append(maxSize);
    sb.append("\n");

    sb.append("Is Shrinkable: ");
    sb.append((shrinkable ? "YES" : "NO"));
    sb.append("\n");

    sb.append("Using Threads: ");
    sb.append((useThreads ? "YES" : "NO"));
    sb.append("\n");

    sb.append("Worker Count: ");
    sb.append(workers.size());
    sb.append("\n");

    return sb.toString();
  }

}
