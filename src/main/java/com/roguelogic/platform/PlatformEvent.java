/*
 * Created on Oct 20, 2005
 */
package com.roguelogic.platform;

import java.io.Serializable;

/**
 * @author rilardi
 */

public class PlatformEvent implements Serializable {

  public static final int BROADCAST_MODE_NONE = 0;
  public static final int BROADCAST_MODE_ALL = 1;
  public static final int BROADCAST_MODE_BUSINESS_MODULES_ONLY = 2;
  public static final int BROADCAST_MODE_INTERFACE_MODULES_ONLY = 3;

  private Serializable data;

  private PlatformModuleSignature source;
  private PlatformModuleSignature[] destinations;

  private int broadcastMode;

  public PlatformEvent() {}

  /**
   * @return Returns the data.
   */
  public Serializable getData() {
    return data;
  }

  /**
   * @param data The data to set.
   */
  public void setData(Serializable data) {
    this.data = data;
  }

  /**
   * @return Returns the destinations.
   */
  public PlatformModuleSignature[] getDestinations() {
    return destinations;
  }

  /**
   * @param destinations The destinations to set.
   */
  public void setDestinations(PlatformModuleSignature[] destinations) {
    this.destinations = destinations;
  }

  /**
   * @return Returns the source.
   */
  public PlatformModuleSignature getSource() {
    return source;
  }

  /**
   * @param source The source to set.
   */
  public void setSource(PlatformModuleSignature source) {
    this.source = source;
  }

  /**
   * @return Returns the broadcastMode.
   */
  public int getBroadcastMode() {
    return broadcastMode;
  }

  /**
   * @param broadcastMode The broadcastMode to set.
   */
  public void setBroadcastMode(int broadcastMode) {
    this.broadcastMode = broadcastMode;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    return sb.toString();
  }

}

