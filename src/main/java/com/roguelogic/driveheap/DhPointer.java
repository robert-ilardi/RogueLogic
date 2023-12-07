/**
 * Created Sep 24, 2011
 */
package com.roguelogic.driveheap;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class DhPointer implements Serializable {

  private long address;
  private int length;
  private int blocks;

  protected DhPointer(long address, int length, int blocks) {
    this.address = address;
    this.length = length;
    this.blocks = blocks;
  }

  public long getAddress() {
    return address;
  }

  public int getLength() {
    return length;
  }

  public int getBlocks() {
    return blocks;
  }

  public synchronized void pushData(byte[] data) throws DhException, IOException {
    if (data == null || data.length == 0) {
      return;
    }

    if (data.length > length) {
      throw new DhException("Segmentation Fault!");
    }

    DhMemoryManager memMan = DhMemoryManager.getInstance();
    memMan.pushData(address, data);
  }

  public synchronized byte[] dereference() throws DhException, IOException {
    DhMemoryManager memMan = DhMemoryManager.getInstance();
    return memMan.dereference(address);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[DhVariablePointer -");

    sb.append(" Address: ");
    sb.append(address);

    sb.append("; Length: ");
    sb.append(length);

    sb.append("; Blocks: ");
    sb.append(blocks);

    sb.append("]");

    return sb.toString();
  }

}
