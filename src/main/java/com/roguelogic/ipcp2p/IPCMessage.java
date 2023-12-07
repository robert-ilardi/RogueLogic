package com.roguelogic.ipcp2p;

import java.io.Serializable;

public final class IPCMessage implements Serializable {

  private String subject;
  private String name;
  private Serializable data;

  public IPCMessage() {}

  public Serializable getData() {
    return data;
  }

  public void setData(Serializable data) {
    this.data = data;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[IPCMessage - Subject: ");
    sb.append(subject);
    sb.append(", Name: ");
    sb.append(name);
    sb.append(", DataClass: ");
    sb.append((data != null ? data.getClass().getName() : "NULL"));
    sb.append("]");

    return sb.toString();
  }

}
