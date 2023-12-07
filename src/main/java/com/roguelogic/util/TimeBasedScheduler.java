/**
 * Created Dec 10, 2008
 */
package com.roguelogic.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Robert C. Ilardi
 * 
 */

public class TimeBasedScheduler implements Runnable {

  public static final int SLEEP_INTERVAL = 10; // Seconds

  private int[] schedule;
  private boolean triggerTaskOnStart;

  private Thread bsThread;
  private boolean running;
  private Object runningLock;
  private boolean scheduling;

  private GregorianCalendar lastExecution;

  private Runnable task;

  public TimeBasedScheduler() {
    runningLock = new Object();
    running = false;
    scheduling = false;
  }

  public void start() {
    synchronized (runningLock) {
      if (scheduling) {
        return;
      }

      scheduling = true;
      lastExecution = new GregorianCalendar();

      bsThread = new Thread(this);
      bsThread.start();

      while (!running) {
        try {
          runningLock.wait();
        }
        catch (Exception e) {}
      }
    }
  }

  public void shutdown() {
    synchronized (runningLock) {
      scheduling = false;

      while (running) {
        try {
          runningLock.wait();
        }
        catch (Exception e) {}
      }
    }
  }

  public void waitWhileRunning() {
    synchronized (runningLock) {
      while (running) {
        try {
          runningLock.wait();
        }
        catch (Exception e) {}
      }
    }
  }

  public void run() {
    try {
      synchronized (runningLock) {
        running = true;
        runningLock.notifyAll();
      }

      // Run Task On Boot
      if (triggerTaskOnStart) {
        lastExecution = new GregorianCalendar();

        if (task != null) {
          task.run();
        }
        else {
          System.err.println("!!!!!!! TimeBasedSchedule> WARNING!!!! No Task Provided to Execute! Skipping...");
        }
      }

      // Normal Scheduled Task Triggers...
      while (scheduling) {
        if (checkTime()) {
          // Execute scheduled task...
          lastExecution = new GregorianCalendar();

          if (task != null) {
            task.run();
          }
          else {
            System.err.println("!!!!!!! TimeBasedSchedule> WARNING!!!! No Task Provided to Execute! Skipping...");
          }
        }

        SystemUtils.Sleep(SLEEP_INTERVAL);
      } // End while scheduling loop
    } // End try block
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      synchronized (runningLock) {
        running = false;
        runningLock.notifyAll();
      }
    }
  }

  private boolean checkTime() {
    GregorianCalendar cur;
    boolean run = false;
    int curH, curM, lastH, lastM, schedH, schedM;

    cur = new GregorianCalendar();

    // Perform Reset if clock rolls to next day...
    if (lastExecution.get(Calendar.HOUR_OF_DAY) > cur.get(Calendar.HOUR_OF_DAY)) {
      lastExecution = cur;
    }

    curH = cur.get(Calendar.HOUR_OF_DAY);
    curM = cur.get(Calendar.MINUTE);
    lastH = lastExecution.get(Calendar.HOUR_OF_DAY);
    lastM = lastExecution.get(Calendar.MINUTE);

    for (int i = 0; i < schedule.length; i += 2) {
      schedH = schedule[i];
      schedM = schedule[i + 1];

      if (curH >= schedH && curM >= schedM) {
        // Schedule is greater than or equal to current time...
        if (schedH > lastH || (schedH == lastH && schedM > lastM)) {
          run = true;
          break;
        }
      }
    }

    return run;
  }

  /**
   * Sets the schedule using a supplied int array where each group of two elements of the array represent HH and mm (24 hour clock). Example: [0] = 20 [1] = 15. This would mean 22:15
   * 
   * @param schedule
   */
  public void setSchedule(int[] schedule) {
    this.schedule = schedule;
  }

  /**
   * Sets the task to be executed on the supplied schedule
   * 
   * @param task
   */
  public void setTask(Runnable task) {
    this.task = task;
  }

  /**
   * Sets the schedule using a supplied String array where each element of the array is a time in the format of HH:mm (24 hour clock).
   * 
   * @param times
   */
  public void setSchedule(String[] times) {
    int cnt = 0;
    schedule = new int[times.length * 2];

    for (String tm : times) {
      String[] tmpArr = tm.split(":");
      tmpArr = StringUtils.Trim(tmpArr);

      schedule[cnt++] = Integer.parseInt(tmpArr[0]);
      schedule[cnt++] = Integer.parseInt(tmpArr[1]);
    }
  }

  public void setTriggerTaskOnStart(boolean triggerTaskOnStart) {
    this.triggerTaskOnStart = triggerTaskOnStart;
  }

}
