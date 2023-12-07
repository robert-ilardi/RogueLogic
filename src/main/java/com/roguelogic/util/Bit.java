/**
 * Created Dec 21, 2007
 */
package com.roguelogic.util;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public enum Bit implements Serializable {
  ZERO(0), ONE(1), INVALID(-1);

  private int value;

  Bit(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public String toString() {
    return String.valueOf(value);
  }

}
