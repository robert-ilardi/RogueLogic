/*
 * Created on Mar 15, 2005
 */

/*
 Copyright 2007 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.roguelogic.util;

/**
 * @author rilardi
 */

import java.io.*;
import java.util.*;

public class ByteBuffer {

  private ArrayList<byte[]> bufferQueue;

  public ByteBuffer() {
    bufferQueue = new ArrayList<byte[]>();
  }

  public synchronized byte[] getBytes() {
    ByteArrayOutputStream baos = null;
    byte[] bArr = null;

    try {
      baos = new ByteArrayOutputStream();
      for (int i = 0; i < bufferQueue.size(); i++) {
        bArr = bufferQueue.get(i);
        baos.write(bArr);
      }
      bArr = baos.toByteArray();
    }
    catch (Exception e) {
      bArr = null;
    }
    finally {
      try {
        baos.reset();
        baos.close();
      }
      catch (Exception e) {}
      baos = null;
    }

    return bArr;
  }

  public synchronized void add(byte[] buffer) {
    if (buffer != null && buffer.length > 0) {
      bufferQueue.add(buffer);
    }
  }

  public synchronized void add(byte[] buffer, int len) {
    byte[] resizedBuffer;
    if (buffer != null && buffer.length >= len) {
      resizedBuffer = new byte[len];
      System.arraycopy(buffer, 0, resizedBuffer, 0, len);
      bufferQueue.add(resizedBuffer);
    }
  }

  public synchronized int length() {
    byte[] bArr = null;
    int totalLen = 0;

    for (int i = 0; i < bufferQueue.size(); i++) {
      bArr = bufferQueue.get(i);
      totalLen += bArr.length;
    }

    return totalLen;
  }

  public synchronized void clear() {
    bufferQueue.clear();
  }

  public synchronized byte[] remove(int length) {
    byte[] segment = null;
    byte[] bArr, newBuffer;
    int totalLen, cnt, lastIndex;

    if (length > 0) {
      cnt = 0;
      lastIndex = 0;
      totalLen = length();
      segment = new byte[(length > totalLen ? totalLen : length)];

      while (!bufferQueue.isEmpty()) {
        bArr = bufferQueue.remove(0);
        for (int i = 0; i < bArr.length; i++) {
          segment[cnt++] = bArr[i];
          lastIndex = i;

          if (cnt >= length) {
            break;
          }
        } //End for i loop through bArr

        if (cnt >= length) {

          //current buffer from queue has unused bytes
          lastIndex++;
          if (lastIndex < bArr.length) {
            newBuffer = new byte[bArr.length - lastIndex];
            for (int i = 0; i < newBuffer.length; i++) {
              newBuffer[i] = bArr[lastIndex + i];
            }
            bufferQueue.add(0, newBuffer);
          }

          break; //Stop going through the queue
        }

      } //End while !bufferQueue.isEmpty()

    } //End length>0 check

    return segment;
  }

  public static void main(String[] args) {
    ByteBuffer bb = new ByteBuffer();

    bb.add("Robert".getBytes());
    bb.add("Cosmo".getBytes());
    bb.add("Ilardi".getBytes());

    System.out.println(new String(bb.getBytes()));
    System.out.println(new String(bb.remove(7)));
    System.out.println(new String(bb.getBytes()));

    bb.clear();
    System.out.println(new String(bb.getBytes()));

    bb.add("Robert".getBytes());
    System.out.println(new String(bb.getBytes()));
    System.out.println(new String(bb.remove(3)));
    System.out.println(new String(bb.getBytes()));
    System.out.println(new String(bb.remove(3)));
    System.out.println(new String(bb.getBytes()));

    bb.add("Robert".getBytes());
    System.out.println(new String(bb.remove(6)));
    System.out.println(new String(bb.getBytes()));

    bb.add("Robert C. Ilardi".getBytes());
    System.out.println("|" + new String(bb.remove(100)) + "|");
    System.out.println(new String(bb.getBytes()));
  }

}
