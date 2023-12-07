/*
 Copyright 2007 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.roguelogic.net;

import java.util.ArrayList;

import com.roguelogic.workers.WorkerPool;

public class SocketReadManager {

  private Thread workerManagerThread;

  private Object workerManagerLock;
  private Object workerManagerStatusLock;

  private boolean managingWorkers;
  private boolean doManagement;

  private ArrayList<SocketSession> readReadySockSessionQueue;

  private WorkerPool workerPool;

  public SocketReadManager() {
    readReadySockSessionQueue = new ArrayList<SocketSession>();

    managingWorkers = false;
    doManagement = false;

    workerManagerLock = new Object();
    workerManagerStatusLock = new Object();
  }

  protected void setWorkerPool(WorkerPool workerPool) {
    this.workerPool = workerPool;
  }

  private Runnable workerManager = new Runnable() {
    public void run() {
      SocketSession userSession;
      SocketWorkerParameter sockParam = null;

      synchronized (workerManagerStatusLock) {
        managingWorkers = true;
        workerManagerStatusLock.notifyAll();
      }

      try {
        while (doManagement) {
          synchronized (workerManagerLock) {
            userSession = getNextReadReadySockSession();
            while (doManagement && userSession == null) {
              //System.out.println("Worker Manager Sync'ed");
              workerManagerLock.wait();
              if (doManagement) {
                userSession = getNextReadReadySockSession();
              }
            }
          }

          //Invoke Threaded Worker
          if (doManagement && userSession != null) {
            try {
              //Create Socket Worker Parameter
              sockParam = new SocketWorkerParameter();
              sockParam.setUserSession(userSession);

              //Invoke Socket Worker
              workerPool.performWork(sockParam);
            }
            catch (Exception e) {
              e.printStackTrace();
            }
          }
        } //End while listening loop
      } //End try block
      catch (Exception e) {
        e.printStackTrace();
      }

      synchronized (workerManagerStatusLock) {
        managingWorkers = false;
        workerManagerStatusLock.notifyAll();
      }
    } //End run method
  };

  protected void start() throws InterruptedException {
    //Start Worker Manager
    synchronized (workerManagerStatusLock) {
      if (!doManagement) {
        doManagement = true;
        workerManagerThread = new Thread(workerManager);
        workerManagerThread.start();

        while (!managingWorkers) {
          workerManagerStatusLock.wait();
        }
      }
    }
  }

  private SocketSession getNextReadReadySockSession() {
    SocketSession userSession = null;

    synchronized (workerManagerLock) {
      for (int i = 0; i < readReadySockSessionQueue.size(); i++) {
        userSession = readReadySockSessionQueue.get(i);

        if (userSession.hasRawDataQueued()) {
          readReadySockSessionQueue.remove(i);
          break;
        }
        else {
          userSession = null;
        }
      }
    }

    return userSession;
  }

  protected void scheduleUserSessionReadEvent(SocketSession userSession, byte[] data) {
    //System.out.println("~~~~~~~~~~~~~~> Scheduling Read Event...");

    synchronized (workerManagerLock) {
      //System.out.println("Scheduling Read Locked on Worker Manager");
      userSession.enqueueRawData(data);
      //System.out.println("After Enqueue Raw Data");

      if (userSession.isVirgin()) {
        //Set Initial Read Ready State
        enqueueReadySockSession(userSession);
        userSession.setVirginStatus(false);
      }
      else {
        workerManagerLock.notifyAll();
      }
    }
  }

  protected void enqueueReadySockSession(SocketSession userSession) {
    synchronized (workerManagerLock) {
      //System.out.println("enqueueReadySockSession");
      readReadySockSessionQueue.add(userSession);
      workerManagerLock.notifyAll();
    }
  }

  protected void stop() {
    synchronized (workerManagerLock) {
      doManagement = false;
      workerManagerLock.notifyAll();
    }
  }

  protected void waitForStop() throws InterruptedException {
    synchronized (workerManagerStatusLock) {
      while (managingWorkers) {
        workerManagerStatusLock.wait();
      }
    }
  }

  protected void clearQueuingMechanism() {
    synchronized (workerManagerLock) {
      readReadySockSessionQueue.clear();
    }
  }

}
