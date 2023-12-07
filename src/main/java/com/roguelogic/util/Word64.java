/**
 * Created Dec 21, 2007
 */
package com.roguelogic.util;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class Word64 implements Serializable {

  public static final int WORD_BIT_SIZE = 64;

  private long data;

  public Word64() {
    data = 0;
  }

  public synchronized long getData() {
    return data;
  }

  public synchronized void setData(long data) {
    this.data = data;
  }

  public synchronized void setBitAt(int index, Bit b) {
    int curBit = getBitValueAt(index);
    int tmp;

    if (index < 0 || index >= WORD_BIT_SIZE) {
      //Out of range
      return;
    }

    if (curBit == b.getValue()) {
      //If the target bit is the same as the bit b being passed, we don't have to do anything...
      return;
    }

    if (curBit == 1) {
      //Reset bit at index to ZERO
      //Also, if bit b is ZERO, then we are done...
      tmp = (int) Math.pow(2, index);
      data = data - tmp;
    }

    if (b == Bit.ONE) {
      //Set the bit at index to ONE
      tmp = (int) Math.pow(2, index);
      data = data + tmp;
    }
  }

  public synchronized int getBitValueAt(int index) {
    long shiftBuf, targetBit;

    if (index < 0 || index >= WORD_BIT_SIZE) {
      //Out of range
      return Bit.INVALID.getValue();
    }

    shiftBuf = data >> index; //move the bit we are interested to the "front"
    targetBit = 1;
    targetBit = shiftBuf & targetBit; //since targetBit is set to 1 and everything else is zero, if the bit at index is 1 targetBit at this line will be 1 else it will be zero.

    return (int) targetBit;
  }

  public synchronized Bit getBitAt(int index) {
    switch (getBitValueAt(index)) {
      case 0:
        return Bit.ZERO;
      case 1:
        return Bit.ONE;
      default:
        return Bit.INVALID;
    }
  }

  public static void main(String[] args) {
    Word64 w = new Word64();
    w.setData(Long.parseLong(args[0]));

    for (int i = Word64.WORD_BIT_SIZE - 1; i >= 0; i--) {
      System.out.print(w.getBitValueAt(i));
    }

    System.out.println(" | Dec Val = " + w.getData());

    w.setBitAt(Integer.parseInt(args[1]), ("1".equals(args[2]) ? Bit.ONE : Bit.ZERO));

    for (int i = Word64.WORD_BIT_SIZE - 1; i >= 0; i--) {
      System.out.print(w.getBitValueAt(i));
    }

    System.out.println(" | Dec Val = " + w.getData());

  }

}
