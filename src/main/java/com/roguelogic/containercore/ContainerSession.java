package com.roguelogic.containercore;

import java.io.Serializable;
import java.util.HashMap;

public class ContainerSession {

  private ContainerSessionsManager sessionsManager;

  private String sessionId;
  private HashMap<Serializable, Serializable> dataMap;

  private Object dataMapLock;

  private long lastTouch;

  protected ContainerSession(String sessionId, ContainerSessionsManager sessionsManager) {
    this.sessionId = sessionId;
    this.sessionsManager = sessionsManager;
    dataMap = new HashMap<Serializable, Serializable>();
    dataMapLock = new Object();
    lastTouch = System.currentTimeMillis();
  }

  public String getSessionId() {
    return sessionId;
  }

  protected Object getDataMapLock() {
    return dataMapLock;
  }

  public Serializable putItem(Serializable key, Serializable value) {
    synchronized (dataMapLock) {
      Serializable oldItem = dataMap.put(key, value);
      lastTouch = System.currentTimeMillis();

      if (sessionsManager != null) {
        sessionsManager.signalDataChange(this, key, value);
      }

      return oldItem;
    }
  }

  public Serializable getItem(Serializable key) {
    synchronized (dataMapLock) {
      lastTouch = System.currentTimeMillis();
      return dataMap.get(key);
    }
  }

  public Serializable removeItem(Serializable key) {
    synchronized (dataMapLock) {
      Serializable oldItem = dataMap.remove(key);
      lastTouch = System.currentTimeMillis();

      if (sessionsManager != null) {
        sessionsManager.signalDataDeletion(this, key);
      }

      return oldItem;
    }
  }

  public void clear() {
    synchronized (dataMapLock) {
      dataMap.clear();
      lastTouch = System.currentTimeMillis();

      if (sessionsManager != null) {
        sessionsManager.signalDataCleared(this);
      }
    }
  }

  public long getLastTickle() {
    return lastTouch;
  }

  public void touch() {
    synchronized (dataMapLock) {
      lastTouch = System.currentTimeMillis();
    }
  }

  public void tickle(String key) {
    synchronized (dataMapLock) {
      Serializable value = dataMap.get(key);
      lastTouch = System.currentTimeMillis();
      sessionsManager.signalDataChange(this, key, value);
    }
  }

}
