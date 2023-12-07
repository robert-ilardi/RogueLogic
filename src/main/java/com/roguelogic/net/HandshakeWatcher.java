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

public abstract class HandshakeWatcher {

  public static final long DEFAULT_WATCHER_SLEEP_MS = 1000;
  public static final int DEFAULT_GRACE_PERIOD_SECS = 20;

  protected long watcherSleepMs;
  protected int gracePeriodSecs;

  private boolean watch;

  private Thread watcherThread;
  private ArrayList<HSWSockSessionEnvelop> envelops;

  private Object hwLock;

  public HandshakeWatcher() {
    watcherSleepMs = DEFAULT_WATCHER_SLEEP_MS;
    gracePeriodSecs = DEFAULT_GRACE_PERIOD_SECS;
    watch = false;
    hwLock = new Object();

    envelops = new ArrayList<HSWSockSessionEnvelop>();
  }

  private Runnable watcher = new Runnable() {
    public void run() {
      HSWSockSessionEnvelop envelop = null;
      SocketSession sockSession;
      int i = 0;

      while (watch) {
        try {
          synchronized (hwLock) {
            try {
              while (watch && envelops.isEmpty()) {
                hwLock.wait();
              }
            }
            catch (InterruptedException e) {}
          }

          if (watch) {
            synchronized (hwLock) {
              for (i = 0; i < envelops.size(); i++) {
                envelop = envelops.get(i);

                if (validateHandshake(envelop)) {
                  System.out.println("Socket Session Handshake is valid!");
                  envelops.remove(i);
                  break;
                }
                else if (expired(envelop)) {
                  System.out.println("Socket Session Handshake Grace Period has Expired! Terminating Connection...");
                  envelops.remove(i);
                  sockSession = envelop.getSockSession();
                  sockSession.endSession();
                  break;
                }
              }
            }
          } //End if watch check
        }//End try block
        catch (Exception e) {
          //We don't want an exception to break this loop!

          //Terminate the offending Connection
          if (envelop != null) {
            envelops.remove(i); //i is kept
            sockSession = envelop.getSockSession();
            sockSession.endSession();
          }

          //Just log the error
          e.printStackTrace();
        }

        sockSession = null;
        envelop = null;

        if (watch) {
          try {
            Thread.sleep(watcherSleepMs);
          }
          catch (Exception e) {}
        }
      } //End while watch

      envelops.clear();
    } //End run method
  };

  public int getGracePeriodSecs() {
    return gracePeriodSecs;
  }

  public void setGracePeriodSecs(int gracePeriodSecs) {
    this.gracePeriodSecs = gracePeriodSecs;
  }

  public long getWatcherSleepMs() {
    return watcherSleepMs;
  }

  public void setWatcherSleepMs(long watcherSleepMs) {
    this.watcherSleepMs = watcherSleepMs;
  }

  public void start() {
    synchronized (hwLock) {
      if (!watch) {
        watch = true;
        watcherThread = new Thread(watcher);
        watcherThread.start();
      }
    }
  }

  public void stop() {
    synchronized (hwLock) {
      if (watch) {
        watch = false;
        hwLock.notifyAll();
      }
    }
  }

  public void addWatch(SocketSession sockSession) {
    HSWSockSessionEnvelop envelop;

    synchronized (hwLock) {
      envelop = new HSWSockSessionEnvelop(sockSession);
      envelops.add(envelop);
      hwLock.notifyAll();
    }
  }

  public void removeWatch(SocketSession sockSession) {
    HSWSockSessionEnvelop envelop;

    synchronized (hwLock) {
      for (int i = 0; i < envelops.size(); i++) {
        envelop = (HSWSockSessionEnvelop) envelops.get(i);
        if (envelop.getSockSession() == sockSession) {
          envelops.remove(i);
          break;
        }
      }

      hwLock.notifyAll();
    }
  }

  protected boolean expired(HSWSockSessionEnvelop envelop) {
    return System.currentTimeMillis() >= envelop.getInsertTs() + (1000 * gracePeriodSecs);
  }

  protected abstract boolean validateHandshake(HSWSockSessionEnvelop envelop);

}
