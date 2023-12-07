/**
 * Created Jun 29, 2009
 */
package com.roguelogic.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Robert C. Ilardi
 *
 */

public class SimpleInMemoryLog implements Serializable {

  public static final int DEFAULT_MAX_MESG_LIMIT = 100;

  private ArrayList<String> mesgs;

  private int maxInMemMesgLimit;

  public SimpleInMemoryLog() {
    mesgs = new ArrayList<String>();
    maxInMemMesgLimit = DEFAULT_MAX_MESG_LIMIT;
  }

  public synchronized void addMessage(String mesg) {
    if (mesgs.size() >= maxInMemMesgLimit) {
      mesgs.remove(0);
    }

    mesgs.add(mesg);
  }

  public ArrayList<String> getMesgs() {
    return mesgs;
  }

  public void clear() {
    mesgs.clear();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < mesgs.size(); i++) {
      if (i > 0) {
        sb.append("\n");
      }

      sb.append(mesgs.get(i));
    }

    return sb.toString();
  }

  public int getMaxInMemMesgLimit() {
    return maxInMemMesgLimit;
  }

  public synchronized void setMaxInMemMesgLimit(int maxInMemMesgLimit) {
    this.maxInMemMesgLimit = maxInMemMesgLimit;
  }

  public synchronized void addMessageAndPrint(String mesg) {
    addMessage(mesg);
    System.out.println(mesg);
  }

  public synchronized void addExceptionAndPrint(Exception e) {
    addException(e);
    e.printStackTrace();
  }

  public synchronized void addException(Exception e) {
    String sts = StringUtils.GetStackTraceString(e);
    addMessage(sts);
  }

}
