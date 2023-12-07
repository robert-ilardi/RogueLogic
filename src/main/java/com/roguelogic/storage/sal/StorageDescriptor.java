/*
 * Created on Aug 30, 2005
 */
package com.roguelogic.storage.sal;

import java.io.IOException;

/**
 * @author rilardi
 */

public class StorageDescriptor {

  private String name;
  private String path;
  private int mbSize;
  private boolean readable;
  private boolean writeable;

  private StorageDevice device;

  public StorageDescriptor() {}

  /**
   * @return Returns the mbSize.
   */
  public int getMbSize() {
    return mbSize;
  }

  /**
   * @param mbSize The mbSize to set.
   */
  public void setMbSize(int mbSize) {
    this.mbSize = mbSize;
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
   * @return Returns the readable.
   */
  public boolean isReadable() {
    return readable;
  }

  /**
   * @param readable The readable to set.
   */
  public void setReadable(boolean readable) {
    this.readable = readable;
  }

  /**
   * @return Returns the writeable.
   */
  public boolean isWriteable() {
    return writeable;
  }

  /**
   * @param writeable The writeable to set.
   */
  public void setWriteable(boolean writeable) {
    this.writeable = writeable;
  }

  /**
   * @return Returns the path.
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path The path to set.
   */
  public void setPath(String path) throws IOException {
    StringBuffer sb;

    if (path != null) {
      path = path.trim();
      if (path.indexOf('\\') != -1 || path.indexOf('*') != -1 || path.indexOf(';') != -1) {
        throw new IOException("Storage Descriptor Path can NOT contain: '\\', '*', ';'!");
      }

      if (!path.endsWith("/")) {
        sb = new StringBuffer(path);
        sb.append("/");
        path = sb.toString();
        sb = null;
      }

      this.path = path;
    }
  }

  public StorageDevice getDevice() {
    return device;
  }

  public void setDevice(StorageDevice device) {
    this.device = device;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[Storage Descriptor - Name: ");
    sb.append(name);
    sb.append(", Path: ");
    sb.append(path);
    sb.append(", Size(MB): ");
    sb.append((mbSize == -1 ? "UNLIMITED" : String.valueOf(mbSize)));
    sb.append(", Readable: ");
    sb.append((readable ? "YES" : "NO"));
    sb.append(", Writeable: ");
    sb.append((writeable ? "YES" : "NO"));
    sb.append("]");

    return sb.toString();
  }

}
