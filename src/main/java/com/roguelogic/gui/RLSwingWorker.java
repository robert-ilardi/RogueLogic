package com.roguelogic.gui;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public abstract class RLSwingWorker {
  private Object value; // see getValue(), setValue()

  private ProgressBarDialog bar;

  private Window window;

  private String title = "RogueLogic Platform";

  private String text = "Please wait......";

  private volatile boolean isDisposed = true;

  /**
   * Class to maintain reference to current worker thread under separate
   * synchronization control.
   */
  public RLSwingWorker(Window window, String title, String text) {
    this.window = window;
    this.title = title;
    this.text = text;
    init();
  }

  public RLSwingWorker(String text) {
    if (text != null)
      this.text = text;
    init();
  }

  /**
   * Start a thread that will call the <code>construct</code> method and
   * then exit.
   */
  public RLSwingWorker() {
    init();
  }

  public void init() {
    final Runnable doFinished = new Runnable() {
      public void run() {
        stopProgress();
        finished();
      }
    };

    Runnable doConstruct = new Runnable() {
      public void run() {
        try {
          setValue(eamConstruct());
        }
        finally {
          threadVar.clear();
        }

        SwingUtilities.invokeLater(doFinished);
      }
    };

    Thread t = new Thread(doConstruct);
    threadVar = new ThreadVar(t);
  }

  private static class ThreadVar {
    private Thread thread;

    ThreadVar(Thread t) {
      thread = t;
    }

    synchronized Thread get() {
      return thread;
    }

    synchronized void clear() {
      thread = null;
    }
  }

  private ThreadVar threadVar;

  /**
   * Get the value produced by the worker thread, or null if it hasn't been
   * constructed yet.
   */
  protected synchronized Object getValue() {
    return value;
  }

  /**
   * Set the value produced by worker thread
   */
  private synchronized void setValue(Object x) {
    value = x;
  }

  /**
   * Compute the value to be returned by the <code>get</code> method.
   */
  public abstract Object construct();

  /**
   * Called on the event dispatching thread (not on the worker thread) after
   * the <code>construct</code> method has returned.
   */
  public void finished() {}

  /**
   * A new method that interrupts the worker thread. Call this method to force
   * the worker to stop what it's doing.
   */
  public void interrupt() {
    Thread t = threadVar.get();
    if (t != null) {
      t.interrupt();
    }
    threadVar.clear();
  }

  /**
   * Return the value created by the <code>construct</code> method. Returns
   * null if either the constructing thread or the current thread was
   * interrupted before a value was produced.
   * 
   * @return the value created by the <code>construct</code> method
   */
  public Object get() {
    while (true) {
      Thread t = threadVar.get();
      if (t == null) {
        return getValue();
      }
      try {
        t.join();
      }
      catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt(); // propagate
        return null;
      }
    }
  }

  /**
   * Start the worker thread.
   */
  public void start() {
    Thread t = threadVar.get();
    if (t != null) {
      t.start();
    }
  }

  public void startProgress() {
    isDisposed = false;
    if (window == null || window instanceof JFrame) {
      bar = new ProgressBarDialog((JFrame) window, title, text);
    }
    else if (window instanceof JDialog) {
      bar = new ProgressBarDialog((JDialog) window, title, text);
    }
    else {
      isDisposed = true;
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // isDisposed = false;
        if (bar != null) {
          bar.setVisible(true);
        }

      }
    });
    /*
     * new Thread( new Runnable() { public void run() {
     * 
     * if (window == null || window instanceof JFrame ) { bar = new
     * ProgressBarDialog((JFrame)window, title, text); } else if (window
     * instanceof JDialog) { bar = new ProgressBarDialog((JDialog)window,
     * title, text); isDisposed = false; } SwingUtilities.invokeLater(new
     * Runnable() { public void run() { // isDisposed = false; // if(bar !=
     * null && bar.is) bar.setVisible(true);
     * 
     *  } }); } }).start();
     */
  }

  public void stopProgress() {
    if (!isDisposed) {
      System.out.println("calling disposed");
      if (bar != null) {
        bar.setVisible(false);
        bar.close();
        //bar.dispose();
      }
      isDisposed = true;
    }
  }

  private Object eamConstruct() {
    Object obj = null;
    startProgress();
    try {
      obj = construct();
    }
    catch (Throwable t) {
      t.printStackTrace();
      obj = t;
    }
    finally {
      //stopProgress();
    }
    return obj;
  }

}
