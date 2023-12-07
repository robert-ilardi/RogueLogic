/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 * 
 */
public class IpcEvent implements Serializable {

  private String originatingProcessName;
  private String targetProcessName;

  private Serializable data;

  private boolean requiresIpcAck;

  private String eventId;

  public IpcEvent() {
  }

  public Serializable getData() {
    return data;
  }

  public void setData(Serializable data) {
    this.data = data;
  }

  public String getOriginatingProcessName() {
    return originatingProcessName;
  }

  public void setOriginatingProcessName(String originatingProcessName) {
    this.originatingProcessName = originatingProcessName;
  }

  public String getTargetProcessName() {
    return targetProcessName;
  }

  public void setTargetProcessName(String targetProcessName) {
    this.targetProcessName = targetProcessName;
  }

  public boolean isRequiresIpcAck() {
    return requiresIpcAck;
  }

  public void setRequiresIpcAck(boolean requiresIpcAck) {
    this.requiresIpcAck = requiresIpcAck;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[AmgIpcEvent - ");

    sb.append("OriginatingProcessName: ");
    sb.append(originatingProcessName);

    sb.append("; TargetProcessName: ");
    sb.append(targetProcessName);

    sb.append("; RequiresIpcAck: ");
    sb.append(requiresIpcAck ? "YES" : "NO");

    sb.append("; EventId: ");
    sb.append(eventId);

    sb.append("]");

    return sb.toString();
  }

}
