/*
 * Created on Apr 7, 2005
 */
package com.roguelogic.im;

import java.util.Vector;

import com.roguelogic.im.toc.TOCObserver;

/**
 * @author rilardi
 */

public class IMQueue implements TOCObserver {

  private Vector queue;
  private boolean punted;
  private int readIndex;

  public IMQueue() {
    queue = new Vector();
    readIndex = 0;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCObserver#imReceived(com.roguelogic.im.InstantMessage)
   */
  public void imReceived(InstantMessage im) {
    if (im != null) {
      queue.add(im);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCObserver#imPunted()
   */
  public void imPunted() {
    punted = true;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCObserver#imStatusUpdate(com.roguelogic.im.BuddyStatus)
   */
  public void imStatusUpdate(BuddyStatus bs) {
    if (bs != null) {
      queue.add(bs);
    }
  }

  public boolean isPunted() {
    return punted;
  }

  public void clearRead() {
    for (int i = 0; i <= readIndex && i < queue.size(); i++) {
      queue.remove(0);
    }
    readIndex = 0;
  }

  public int size() {
    return queue.size();
  }

  public Object get(int index) {
    Object obj = null;

    if (index >= 0 && index < queue.size()) {
      obj = queue.get(index);
      readIndex = index;
    }

    return obj;
  }

  public void clearAll() {
    queue.clear();
    readIndex = 0;
  }

}