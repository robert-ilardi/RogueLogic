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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.roguelogic.workers.Worker;
import com.roguelogic.workers.WorkerCustomizer;
import com.roguelogic.workers.WorkerException;
import com.roguelogic.workers.WorkerParameter;
import com.roguelogic.workers.WorkerPool;

public class SocketClient implements WorkerCustomizer {

  public static final int DEFAULT_INITIAL_WORKER_THREADS = 1;
  public static final int DEFAULT_MAX_WORKER_THREADS = 1;

  public static final int BUFFER_LENGTH = 32768;

  private InetSocketAddress address;

  private Selector selector;

  private SocketChannel sockCh;

  private Object clientLock;
  private boolean reading;
  private boolean doReading;

  private WorkerPool workerPool;

  private Exception clientException;

  private Thread rawReaderThread;

  private SocketSession userSession;

  private Class<?> socketProcessorClass;

  private SocketReadManager readManager;

  private SocketProcessorCustomizer spCustomizer;

  private SocketSessionSweeper spSweeper;

  private int lingerTimeOverride = -1;
  private boolean reuseAddress = false;

  public SocketClient() throws RLNetException {
    clientLock = new Object();
    reading = false;
    clientException = null;
    doReading = false;
  }

  private Runnable rawReader = new Runnable() {
    public void run() {
      SocketChannel sockCh;
      ByteBuffer bBuf;
      byte[] data;
      int len, cnt;
      Set<SelectionKey> keys;
      Iterator<SelectionKey> iter;
      SelectionKey selKey = null;

      try {
        bBuf = ByteBuffer.allocate(BUFFER_LENGTH);

        synchronized (clientLock) {
          reading = true;
          clientLock.notifyAll();
        }

        while (doReading) {
          cnt = selector.select();

          if (cnt == 0) {
            continue;
          }

          keys = selector.selectedKeys();
          iter = keys.iterator();

          while (iter.hasNext()) {
            selKey = (SelectionKey) iter.next();
            iter.remove();

            if (selKey.isReadable()) {
              sockCh = (SocketChannel) selKey.channel();
              bBuf.clear();
              len = sockCh.read(bBuf);

              if (len > 0) {
                // Get bytes
                data = new byte[len];
                bBuf.flip();
                bBuf.get(data, 0, len);

                readManager.scheduleUserSessionReadEvent(userSession, data);
              }
              else if (len < 0) {
                selfClose();
              }
            } // End Is Readable Block
          }
        } // End while doListening

        if (selKey != null) {
          selKey.cancel();
        }

        if (userSession != null) {
          userSession.drainRawData();
        }

        synchronized (clientLock) {
          reading = false;
          clientLock.notifyAll();
        }

        if (readManager != null) {
          readManager.stop();
        }

      } // End Try Block
      catch (Exception e) {
        if (userSession != null) {
          userSession.drainRawData();
        }

        if (reading) {
          synchronized (clientLock) {
            reading = false;
            clientLock.notifyAll();
          }
        }

        if (readManager != null) {
          readManager.stop();
        }

        e.printStackTrace();
        clientException = e;
      }
      finally {
        selfClose();
      } // End finally block
    } // End run method
  };

  public void connect(InetSocketAddress address) throws RLNetException {
    StringBuffer sb;

    synchronized (clientLock) {
      if (reading) {
        throw new RLNetException("Server already Connected to = " + address.getHostName() + ":" + address.getPort());
      }

      try {
        doReading = true;
        clientException = null;

        // Setup Socket Client...
        this.address = address;

        selector = Selector.open();

        sockCh = SocketChannel.open();
        sockCh.configureBlocking(false);
        sockCh.register(selector, SelectionKey.OP_READ);
        sockCh.socket().setReuseAddress(reuseAddress);

        if (lingerTimeOverride > 0) {
          sockCh.socket().setSoLinger(true, lingerTimeOverride);
        }

        sockCh.connect(address);

        while (!sockCh.finishConnect()) {
          Thread.sleep(50);
        }

        if (sockCh.socket().isInputShutdown()) {
          throw new RLNetException("Connection operation did NOT complete properly!");
        }

        userSession = new SocketSession(sockCh);
        userSession.setClient(this);
        userSession.setHandshookStatus(true); // Clients do not automatically initiate the handshake, although user code can!

        // Setup Worker Pool with Socket Workers
        sb = new StringBuffer();
        sb.append("SocketWorkers(");
        sb.append(address.getHostName());
        sb.append(":");
        sb.append(address.getPort());
        sb.append(")");

        workerPool = new WorkerPool(sb.toString());
        workerPool.registerWorkerClass(SocketWorker.class);
        workerPool.setInitialSize(DEFAULT_INITIAL_WORKER_THREADS);
        workerPool.setMaxSize(DEFAULT_MAX_WORKER_THREADS);
        workerPool.setShrinkable(true);
        workerPool.setUseThreads(true);
        workerPool.setCustomizer(this);
        workerPool.createPool();

        // Start Raw Reader
        rawReaderThread = new Thread(rawReader);
        rawReaderThread.start();

        while (!reading) {
          clientLock.wait();
        }

        // Start Read Manager
        readManager = new SocketReadManager();
        readManager.setWorkerPool(workerPool);
        readManager.start();

        userSession.setReadManager(readManager); // Since we only have one User Session - Set it here!

        if (clientException != null) {
          throw clientException;
        }
      }
      catch (Exception e) {
        doReading = false;

        throw new RLNetException(e);
      }
    }
  }

  public int getLingerTimeOverride() {
    return lingerTimeOverride;
  }

  public void setLingerTimeOverride(int lingerTimeOverride) {
    this.lingerTimeOverride = lingerTimeOverride;
  }

  private void selfClose() {
    SelectionKey key;

    synchronized (clientLock) {
      doReading = false;

      if (userSession != null) {
        userSession.drainRawData();
        userSession.setZombie(true);
      }

      if (sockCh != null) {
        if (selector != null) {
          key = sockCh.keyFor(selector);
          if (key != null) {
            key.cancel();
          }

          try {
            selector.close();
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }

        try {
          sockCh.close();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (readManager != null) {
        try {
          readManager.stop();
          readManager.waitForStop();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (userSession != null) {
        if (spSweeper != null) {
          spSweeper.cleanup(userSession);
        }

        userSession.destroy();
        userSession = null;
      }

      if (readManager != null) {
        readManager.clearQueuingMechanism();
        readManager = null;
      }

      if (workerPool != null) {
        try {
          workerPool.destroyPool();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        workerPool = null;
      }
    } // End synchronzied block
  }

  public void close() {
    SelectionKey key;

    synchronized (clientLock) {
      doReading = false;

      if (sockCh != null) {
        key = sockCh.keyFor(selector);
        if (key != null) {
          key.cancel();
        }

        try {
          selector.close();
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        try {
          sockCh.close();
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        try {
          while (reading) {
            clientLock.wait();
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public InetSocketAddress getSocketAddress() {
    return address;
  }

  public synchronized void send(byte[] data) throws RLNetException {
    userSession.send(data);
  }

  public SocketSession getUserSession() {
    return userSession;
  }

  public void setSocketProcessorClass(Class<?> socketProcessorClass) {
    this.socketProcessorClass = socketProcessorClass;
  }

  public void connect(String address, int port) throws RLNetException {
    connect(new InetSocketAddress(address, port));
  }

  public void initWorker(Worker worker) throws WorkerException {
    SocketProcessor processor;
    SocketWorker sockWorker;

    sockWorker = (SocketWorker) worker;

    try {
      processor = (SocketProcessor) socketProcessorClass.newInstance();
      sockWorker.setProcessor(processor);

      // User Level Socket Processor Implementation Customization
      if (spCustomizer != null) {
        spCustomizer.initSocketProcessor(processor);
      }
    }
    catch (Exception e) {
      throw new WorkerException("Could NOT Set Socket Processor to Worker during Init!", e);
    }
  }

  public void configureWorker(Worker worker, WorkerParameter param) throws WorkerException {}

  public Object getUserItem(String key) {
    return userSession.getUserItem(key);
  }

  public Object putUserItem(String key, Object value) {
    return userSession.putUserItem(key, value);
  }

  public Object removeUserItem(String key) {
    return userSession.removeUserItem(key);
  }

  public void clearUserData() {
    userSession.clearUserData();
  }

  public void setSocketProcessorCustomizer(SocketProcessorCustomizer customizer) {
    this.spCustomizer = customizer;
  }

  public void setSocketSessionSweeper(SocketSessionSweeper sweeper) {
    this.spSweeper = sweeper;
  }

  public boolean isConnected() {
    return reading;
  }

  public boolean isReuseAddress() {
    return reuseAddress;
  }

  public void setReuseAddress(boolean reuseAddress) {
    this.reuseAddress = reuseAddress;
  }

}
