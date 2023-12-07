/**
 * Created Nov 3, 2007
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

package com.roguelogic.sambuca;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import com.roguelogic.util.SystemUtils;

/**
 * Raw Socket Data "Packet" Queue.
 *  
 * @author Robert C. Ilardi
 *
 */

public class PacketQueue {

  public static final byte[] EMPTY_LINE = new byte[] { 13, 10, 13, 10 };

  private ArrayList<byte[]> queue;

  public PacketQueue() {
    queue = new ArrayList<byte[]>();
  }

  /**
   * "Pushes" or inserts a packet into the front of the queue.
   * 
   * @param packet The packet of data to be pushed.
   */
  public synchronized void push(byte[] packet) {
    queue.add(0, packet); //Add to front of queue...
  }

  /**
   * Appends a packet onto the end of the queue.
   * 
   * @param packet The packet of data to be enqueued.
   */
  public synchronized void enqueue(byte[] packet) {
    queue.add(packet);
  }

  /**
   * 
   * @return Returns TRUE if reading from the front of the queue will return a Complete HTTP Header. FALSE if otherwise. 
   */
  public synchronized boolean hasCompleteHttpHeader() {
    boolean completeHttpHeader = false;
    byte[] packet, buf;
    int bufLen;

    bufLen = 0;
    buf = new byte[4];
    SystemUtils.FillArray(buf, (byte) 0);

    for (int i = 0; i < queue.size(); i++) {
      packet = queue.get(i);

      for (int j = 0; j < packet.length; j++) {
        if (packet[j] == 13) {
          if (bufLen == 0 || buf[bufLen - 1] == 10) {
            buf[bufLen] = packet[j];
            bufLen++;
          }
          else {
            bufLen = 0;
          }
        }
        else if (packet[j] == 10) {
          if (bufLen > 0 && buf[bufLen - 1] == 13) {
            buf[bufLen] = packet[j];
            bufLen++;

            if (bufLen == 4) {
              completeHttpHeader = true;
              break;
            }
          }
          else {
            bufLen = 0;
          }
        }
        else {
          bufLen = 0;
        }
      } //End for j

      if (completeHttpHeader) {
        break;
      }
    } //End for i

    return completeHttpHeader;
  }

  /**
   * This method should be used to obtain a complete HTTP Header from the queue. It MUST only be called
   * if the hasCompleteHttpHeader() method returns TRUE.
   * 
   * @return Returns a complete HTTP Header and dequeues the exact number of bytes of the header from the queue.
   * if a package contains header data plus additional bytes, only the header bytes will be returned and the remaining
   * data is left as it's own package on the front of the queue.
   */
  public synchronized byte[] dequeueHttpHeader() {
    byte[] httpHeader = null;
    boolean completeHttpHeader = false;
    byte[] packet, buf;
    int bufLen, endIndex, i;
    ByteArrayOutputStream baos;

    baos = new ByteArrayOutputStream();
    bufLen = 0;
    buf = new byte[4];
    SystemUtils.FillArray(buf, (byte) 0);

    for (i = 0; i < queue.size(); i++) {
      packet = queue.get(i);
      endIndex = packet.length;

      for (int j = 0; j < packet.length; j++) {
        if (packet[j] == 13) {
          if (bufLen == 0 || buf[bufLen - 1] == 10) {
            buf[bufLen] = packet[j];
            bufLen++;
          }
          else {
            bufLen = 0;
          }
        }
        else if (packet[j] == 10) {
          if (bufLen > 0 && buf[bufLen - 1] == 13) {
            buf[bufLen] = packet[j];
            bufLen++;

            if (bufLen == 4) {
              endIndex = j + 1;
              completeHttpHeader = true;
              break;
            }
          }
          else {
            bufLen = 0;
          }
        }
        else {
          bufLen = 0;
        }
      } //End for j

      baos.write(packet, 0, endIndex);

      if (completeHttpHeader) {
        for (int j = 0; j <= i; j++) {
          queue.remove(0);
        }

        if (endIndex != packet.length) {
          //Push back partial packet
          buf = new byte[packet.length - endIndex];
          System.arraycopy(packet, endIndex, buf, 0, buf.length);
          queue.add(0, buf);
        }

        httpHeader = baos.toByteArray();

        break;
      }

    } //End for i

    return httpHeader;
  }

  /**
   * 
   * @param len number of bytes of data to be dequeued.
   * @return Returns a byte array of raw http data from the queue.
   * @throws SambucaException
   */
  public synchronized byte[] dequeueHttpData(int len) throws SambucaException {
    byte[] httpData = null;
    byte[] packet, buf;
    int remainingLen;
    ByteArrayOutputStream baos;

    remainingLen = len;
    baos = new ByteArrayOutputStream();

    while (!queue.isEmpty()) {
      packet = queue.remove(0);

      if (packet.length > remainingLen) {
        //Only take what we need...
        baos.write(packet, 0, remainingLen);

        //Push back remainder
        buf = new byte[packet.length - remainingLen];
        System.arraycopy(packet, remainingLen - 1, buf, 0, buf.length);
        queue.add(0, buf);

        remainingLen = 0;
      }
      else {
        baos.write(packet, 0, packet.length);
        remainingLen -= packet.length;
      }

      if (remainingLen == 0) {
        break;
      }
    } //End while (!queue.isEmpty())

    if (remainingLen == 0) {
      httpData = baos.toByteArray();
    }
    else {
      throw new SambucaException("Incomplete or Corrupt Packets Detected!");
    }

    return httpData;
  }

  /**
   * 
   * @param len number of bytes to check for
   * @return Returns TRUE if the queue contains enough bytes of data of length len. FALSE if otherwise.
   * @throws SambucaException
   */
  public synchronized boolean hasEnoughHttpData(int len) throws SambucaException {
    boolean enough = false;
    byte[] packet;
    int foundLen;

    foundLen = 0;

    for (int i = 0; i < queue.size(); i++) {
      packet = queue.get(i);

      foundLen += packet.length;

      if (foundLen >= len) {
        enough = true;
        break;
      }
    }

    return enough;
  }

  /**
   * 
   * @return Returns TRUE if the Queue is EMPTY (contains exactly ZERO packets). FALSE if otherwise. 
   */
  public synchronized boolean isEmpty() {
    return queue.isEmpty();
  }

  public static void main(String[] args) {
    PacketQueue queue = new PacketQueue();

    //queue.enqueue("get /pages/rilardi.htm http/1.1\r\n\r\n".getBytes());
    queue.enqueue("get /pages/rilardi.htm http/1.1\r\n".getBytes());
    queue.enqueue("HackMark=RCI\r\n".getBytes());
    queue.enqueue("\r\n".getBytes());
    queue.enqueue("Hello World!\r\n".getBytes());

    if (queue.hasCompleteHttpHeader()) {
      System.out.println(new String(queue.dequeueHttpHeader()));
    }
    else {
      System.out.println("Queue does NOT contain complete http header!");
    }

  }

}
