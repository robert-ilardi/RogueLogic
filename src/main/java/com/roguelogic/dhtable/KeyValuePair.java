package com.roguelogic.dhtable;

import java.io.Serializable;

public class KeyValuePair implements Serializable {

  private Serializable key;
  private Serializable value;

  public KeyValuePair() {}

  public KeyValuePair(Serializable key, Serializable value) {
    this.key = key;
    this.value = value;
  }

  public Serializable getKey() {
    return key;
  }

  public void setKey(Serializable key) {
    this.key = key;
  }

  public Serializable getValue() {
    return value;
  }

  public void setValue(Serializable value) {
    this.value = value;
  }

}
