/*
 * Created on Mar 18, 2005
 */
package com.roguelogic.im.toc;

import com.roguelogic.im.BuddyStatus;
import com.roguelogic.im.InstantMessage;

/**
 * @author rilardi
 */

public interface TOCObserver {
  public void imReceived(InstantMessage im);
  public void imPunted();
  public void imStatusUpdate(BuddyStatus bs);
}