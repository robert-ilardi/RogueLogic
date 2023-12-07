/**
 * Created Sep 24, 2006
 */
package com.roguelogic.roguenet;

import java.util.Properties;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class ConnectionMonitor implements Runnable {

  private NetworkAgent netAgent;
  private Properties rnaProps;

  private boolean doMonitoring = false;
  private boolean monitoring = false;
  private int monSleepMSecs;

  private Thread monitorThread = null;
  private Object monitorLock = new Object();

  private static final String PROP_MONITOR_SLEEP_SECS = "ConnectionMonitorSleepSecs";

  public ConnectionMonitor(NetworkAgent netAgent, Properties rnaProps) {
    this.netAgent = netAgent;
    this.rnaProps = rnaProps;

    readProperties();
  }

  private void readProperties() {
    String tmp;

    tmp = rnaProps.getProperty(PROP_MONITOR_SLEEP_SECS);
    monSleepMSecs = Integer.parseInt(tmp) * 1000;
  }

  public boolean isMonitoring() {
    return monitoring;
  }

  public void start() throws InterruptedException {
    synchronized (monitorLock) {
      RNALogger.GetLogger().stdOutPrintln("Starting Connection Monitor (" + StringUtils.GetTimeStamp() + "):");

      doMonitoring = true;

      monitorThread = new Thread(this);
      monitorThread.start();

      while (!monitoring) {
        monitorLock.wait();
      }
    }
  }

  public void stop() throws InterruptedException {
    synchronized (monitorLock) {
      RNALogger.GetLogger().stdOutPrintln("Stoping Connection Monitor (" + StringUtils.GetTimeStamp() + "):\n");

      doMonitoring = false;

      while (monitoring) {
        monitorLock.wait();
      }
    }
  }

  public void run() {
    long startSleepTs;

    try {
      synchronized (monitorLock) {
        monitoring = true;
        doMonitoring = true;
        monitorLock.notifyAll();
      }

      while (doMonitoring) {
        try {
          netAgent.ensureConnection();
        } //End try block
        catch (Exception e) {
          RNALogger.GetLogger().log(e);
        }
        finally {
          startSleepTs = System.currentTimeMillis();

          while ((startSleepTs + monSleepMSecs) >= System.currentTimeMillis()) {
            if (doMonitoring) {
              try {
                Thread.sleep(1000);
              }
              catch (Exception e) {}
            }
            else {
              break;
            }
          } //End while loop for sleep time check
        } //End finally
      } //End while monitor
    } //End outer try block
    finally {
      synchronized (monitorLock) {
        monitoring = false;
        doMonitoring = false;
        monitorLock.notifyAll();
      }
    }
  }

}
