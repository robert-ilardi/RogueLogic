/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.hapair;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 * 
 */

public class HaHeartBeat implements Serializable {

  public static final int HB_TYPE_REQUEST = 0;
  public static final int HB_TYPE_REPLY = 1;

  public static final int NODE_MODE_HOT_STANDBY = 0;
  public static final int NODE_MODE_ACTIVE = 1;

  private int sendingNodeIndex;
  private String sendingNodeName;
  private int sendingNodeMode;

  private long systemTime;
  private String echoMesg;

  private int type;

  public HaHeartBeat() {
  }

  public String getEchoMesg() {
    return echoMesg;
  }

  public void setEchoMesg(String echoMesg) {
    this.echoMesg = echoMesg;
  }

  public long getSystemTime() {
    return systemTime;
  }

  public void setSystemTime(long systemTime) {
    this.systemTime = systemTime;
  }

  public int getSendingNodeIndex() {
    return sendingNodeIndex;
  }

  public void setSendingNodeIndex(int sendingNodeIndex) {
    this.sendingNodeIndex = sendingNodeIndex;
  }

  public String getSendingNodeName() {
    return sendingNodeName;
  }

  public void setSendingNodeName(String sendingNodeName) {
    this.sendingNodeName = sendingNodeName;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getSendingNodeMode() {
    return sendingNodeMode;
  }

  public void setSendingNodeMode(int sendingNodeMode) {
    this.sendingNodeMode = sendingNodeMode;
  }

}
