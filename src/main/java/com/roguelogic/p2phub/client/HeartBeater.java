/**
 * Created Feb 15, 2007
 */
package com.roguelogic.p2phub.client;

/**
 * @author Robert C. Ilardi
 *
 */

public class HeartBeater implements Runnable {

  private P2PHubClient client;
  private int hbIntervalSecs;

  private Thread hbThread;
  private boolean sendHBs;
  private boolean hbLooping;
  private Object hbtLock;

  public HeartBeater(P2PHubClient client) {
    this.client = client;
    sendHBs = false;
    hbLooping = false;
    hbtLock = new Object();
  }

  public void setHeartBeatInterval(int hbIntervalSecs) {
    this.hbIntervalSecs = hbIntervalSecs;
  }

  public void start() {
    synchronized (hbtLock) {
      if (!sendHBs) {
        sendHBs = true;

        hbThread = new Thread(this);
        hbThread.start();

        while (!hbLooping) {
          try {
            hbtLock.wait();
          }
          catch (Exception e) {}
        }
      }
    }
  }

  public void stop() {
    synchronized (hbtLock) {
      if (sendHBs) {
        sendHBs = false;

        while (hbLooping) {
          try {
            hbtLock.wait();
          }
          catch (Exception e) {}
        }
      }
    }
  }

  public void run() {
    try {
      synchronized (hbtLock) {
        hbLooping = true;
        hbtLock.notifyAll();
      }

      while (sendHBs) {
        sendHeartBeat();
        sleep();
      }
    } //End try block
    finally {
      synchronized (hbtLock) {
        hbLooping = false;
        hbtLock.notifyAll();
      }
    }
  }

  private void sendHeartBeat() {
    if (client != null) {
      try {
        client.sendHeartBeat();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void sleep() {
    try {
      for (int i = 1; i <= hbIntervalSecs && sendHBs; i++) {
        Thread.sleep(1000);
      }
    }
    catch (Exception e) {}
  }

}
