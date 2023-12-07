/**
 * Created Jan 16, 2008
 */
package com.roguelogic.util;

/**
 * @author Robert C. Ilardi
 *
 */

public class BinaryLineReader {

  private byte[] data;
  private int curIndex;

  public BinaryLineReader(byte[] data) {
    this.data = data;
    curIndex = 0;
  }

  public void rewind() {
    curIndex = 0;
  }

  public void seek(int index) {
    curIndex = index;
  }

  public int getPosition() {
    return curIndex;
  }

  public String readLine() {
    String line = null;
    int startIndex = curIndex, len = 0;

    for (; curIndex < data.length; curIndex++) {
      if (data[curIndex] == 13 || data[curIndex] == 10) {
        line = new String(data, startIndex, len);

        //Peek if this line ended with a 13 and the next character is a 10
        //If it is a 10, skip!
        if (data[curIndex] == 13 && curIndex + 1 < data.length && data[curIndex + 1] == 10) {
          curIndex++;
        }

        curIndex++;

        break;
      }

      len++;
    }

    //The entire thing is a line?
    if (line == null && startIndex != curIndex) {
      line = new String(data, startIndex, len);
    }

    return line;
  }

  public int readByte() {
    if (curIndex < data.length) {
      return (int) data[curIndex++] & 0xff;
    }
    else {
      return -1;
    }
  }

  public int length() {
    return data.length;
  }

  public void clear() {
    data = null;
    curIndex = 0;
  }

}
