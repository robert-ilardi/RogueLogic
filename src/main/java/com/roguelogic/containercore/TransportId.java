package com.roguelogic.containercore;

import java.io.Serializable;

public class TransportId implements Serializable {

  private int idNum;

  public TransportId() {}

  public TransportId(int idNum) {
    this.idNum = idNum;
  }

  public int getIdNum() {
    return idNum;
  }

  public void setIdNum(int idNum) {
    this.idNum = idNum;
  }

  public int hashCode() {
    return idNum;
  }

  public boolean equals(Object obj) {
    boolean same = false;
    TransportId other;

    if (obj instanceof TransportId) {
      other = (TransportId) obj;
      same = (idNum == other.idNum);
    }

    return same;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[TransportId - IdNum = ");
    sb.append(idNum);
    sb.append("]");

    return sb.toString();
  }

}
