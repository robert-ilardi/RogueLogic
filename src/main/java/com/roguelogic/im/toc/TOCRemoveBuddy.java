/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.toc;

import java.util.ArrayList;

/**
 * @author Administrator
 */

public class TOCRemoveBuddy implements TOCMessage {

  public static final String TOC_COMMAND = "toc_remove_buddy";

  private ArrayList buddies;

  public TOCRemoveBuddy() {
    buddies = new ArrayList();
  }

  public synchronized void removeBuddy(String buddyName) {
    buddies.add(buddyName);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCMessage#getTOCMessage()
   */
  public String getTOCMessage() {
    StringBuffer mesg = new StringBuffer();
    String username;

    mesg.append(TOC_COMMAND);

    for (int i = 0; i < buddies.size(); i++) {
      mesg.append(" ");
      username = (String) buddies.get(i);
      mesg.append(username);
    }

    return mesg.toString();
  }

}