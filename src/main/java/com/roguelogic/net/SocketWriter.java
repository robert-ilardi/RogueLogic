/*
 * Created on Feb 8, 2006
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

package com.roguelogic.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SocketWriter {

  public static final int WRITE_TIMEOUT = 30000; //30 Seconds
  public static final int MAX_BUFFER_SIZE = 32768;

  private Selector selector;
  private ByteBuffer bBuf;

  private SocketChannel sockCh;

  public SocketWriter(SocketChannel sockCh) throws IOException {
    this.sockCh = sockCh;
    selector = Selector.open();
    bBuf = ByteBuffer.allocate(MAX_BUFFER_SIZE);
  }

  public synchronized void write(byte[] data) throws IOException {
    SelectionKey key = null;
    int attempts;
    int len;

    if (data != null && data.length > 0) {
      attempts = 0;

      for (int i = 0; i < data.length; i += MAX_BUFFER_SIZE) {
        bBuf.clear();
        bBuf.put(data, i, ((data.length - i) < MAX_BUFFER_SIZE ? (data.length - i) : MAX_BUFFER_SIZE));
        bBuf.flip();

        try {
          while (bBuf.hasRemaining()) {
            len = sockCh.write(bBuf);
            attempts++;

            if (len == 0) {
              key = sockCh.register(selector, SelectionKey.OP_WRITE);

              if (selector.select(WRITE_TIMEOUT) == 0) {
                if (attempts > 2)
                  throw new IOException("Write Timeout / Client Disconnected");
              }
            }
            else {
              attempts = 0;
            }
          } //End while buf has remaining
        } //End Try Block
        finally {
          if (key != null) {
            key.cancel();
            key = null;
          }

          selector.selectNow();
        } //End finally block
      } //End for i loop
    } //End bug null check
  }

  public synchronized void close() {
    try {
      selector.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    bBuf = null;
    sockCh = null;
  }

}
