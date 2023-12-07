/*
 * Created on Oct 20, 2005
 */
package com.roguelogic.platform;

import java.io.Serializable;

/**
 * @author rilardi
 */

public class PlatformModuleSignature implements Serializable {

  public static final int BUSINESS_MODULE = 0;
  public static final int INTERFACE_MODULE = 1;

  private String name;
  private int type;

  public PlatformModuleSignature() {}

  public PlatformModuleSignature(String name, int type) {
    this.name = name;
    this.type = type;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the type.
   */
  public int getType() {
    return type;
  }

  /**
   * @param type The type to set.
   */
  public void setType(int type) {
    this.type = type;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[PlatformModuleSignature - Name: ");
    sb.append(name);
    sb.append(", Type: ");
    sb.append(type);
    sb.append("]");

    return sb.toString();
  }

}

