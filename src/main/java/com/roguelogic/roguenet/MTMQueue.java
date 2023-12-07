/**
 * Created Feb 4, 2007
 */
package com.roguelogic.roguenet;

import java.util.ArrayList;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.workers.Worker;
import com.roguelogic.workers.WorkerCustomizer;
import com.roguelogic.workers.WorkerException;
import com.roguelogic.workers.WorkerParameter;
import com.roguelogic.workers.WorkerPool;

/**
 * @author Robert C. Ilardi
 *
 */

public class MTMQueue implements WorkerCustomizer {

  private ArrayList<P2PHubMessage> queue;
  private Object qLock;

  private int maxQueueProcessorThreads;

  private Thread starterThread;
  private boolean process;
  private boolean processing;
  private WorkerPool processorPool;

  private MTMQProcessor processor;

  public static final int DEFAULT_MAX_QUEUE_PROCESSOR_THREADS = 10;
  public static final String PROCESSOR_POOL_NAME = "MTMesgQueueProcessorPool";
  public static final int DEQUEUE_EMPTY_WAIT_TIME = 60000;

  public MTMQueue() {
    queue = new ArrayList<P2PHubMessage>();
    qLock = new Object();

    maxQueueProcessorThreads = DEFAULT_MAX_QUEUE_PROCESSOR_THREADS;
    process = false;
    processing = false;

    try {
      processorPool = new WorkerPool(PROCESSOR_POOL_NAME);
      processorPool.setInitialSize(1);
      processorPool.setUseThreads(true);
      processorPool.setShrinkable(true);
    }
    catch (Exception e) {
      e.printStackTrace(); //Should NEVER happen, but just in case log it only for debugging...
    }
  }

  public void enqueue(P2PHubMessage mesg) {
    synchronized (qLock) {
      queue.add(mesg);
      qLock.notifyAll();
    }
  }

  public P2PHubMessage dequeue() {
    P2PHubMessage mesg = null;

    synchronized (qLock) {
      //Wait a bit if the queue is empty...
      //Only once, because we want to return NULL
      //if the queue is empty for too long...
      if (queue.isEmpty()) {
        try {
          qLock.wait(DEQUEUE_EMPTY_WAIT_TIME);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (!queue.isEmpty()) {
        mesg = queue.remove(0);
      }
    }

    return mesg;
  }

  public synchronized void start() throws RogueNetException {
    try {
      process = true;

      processorPool.setMaxSize(maxQueueProcessorThreads);
      processorPool.registerWorkerClass(MTMQWorker.class);
      processorPool.setCustomizer(this);
      processorPool.createPool();

      starterThread = new Thread(queueThreadStarter);
      starterThread.start();

      synchronized (qLock) {
        try {
          while (!processing) {
            qLock.wait();
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    catch (Exception e) {
      throw new RogueNetException("An error occurred while attempting to start the MT Message Queue!", e);
    }
  }

  public synchronized void stop() {
    synchronized (qLock) {
      qLock.notifyAll();
      process = false;

      try {
        while (processing) {
          qLock.wait();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private Runnable queueThreadStarter = new Runnable() {
    public void run() {
      P2PHubMessage mesg;
      MTMQWorkerParam param;

      try {
        synchronized (qLock) {
          processing = true;
          qLock.notifyAll();
        }

        while (process) {
          mesg = dequeue();

          if (mesg != null && process) {
            param = new MTMQWorkerParam();
            param.setMessage(mesg);

            processorPool.performWork(param);
          }
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        try {
          processorPool.destroyPool();
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        synchronized (qLock) {
          processing = false;
          qLock.notifyAll();
        }
      }
    }
  };

  public void initWorker(Worker worker) throws WorkerException {
    MTMQWorker mtmqWorker;

    if (worker instanceof MTMQWorker) {
      mtmqWorker = (MTMQWorker) worker;
      mtmqWorker.setProcessor(processor);
    }
  }

  public void configureWorker(Worker worker, WorkerParameter param) throws WorkerException {}

  public void setProcessor(MTMQProcessor processor) {
    this.processor = processor;
  }

}
