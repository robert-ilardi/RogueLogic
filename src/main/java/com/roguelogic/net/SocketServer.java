/*
 * Created on Jan 27, 2006
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

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.roguelogic.workers.Worker;
import com.roguelogic.workers.WorkerCustomizer;
import com.roguelogic.workers.WorkerException;
import com.roguelogic.workers.WorkerParameter;
import com.roguelogic.workers.WorkerPool;
import com.roguelogic.workers.WorkerPoolException;

/**
 * @author rilardi
 */

public class SocketServer implements WorkerCustomizer {

  public static final int DEFAULT_INITIAL_WORKER_THREADS = 10;
  public static final int DEFAULT_MAX_WORKER_THREADS = 50;

  public static final int MAX_BUFFER_SIZE = 32768;

  private int port;
  private String bindAddress;

  private Selector selector;

  private ServerSocket serverSock;
  private ServerSocketChannel serverSockCh;

  private boolean doListening;
  private Object serverLock;
  private boolean listening;

  private Exception serverException;

  private ArrayList<SocketChannel> sockets;

  private Thread listenerThread;

  private WorkerPool workerPool;

  private HashMap<String, Object> globalSession;
  private HashMap<SocketChannel, SocketSession> userSessions;

  private Class<?> socketProcessorClass;

  private SocketServer serverRef;

  private SocketReadManager readManager;

  private SocketProcessorCustomizer spCustomizer;

  private SocketSessionSweeper spSweeper;

  private int initialWorkersCnt;
  private int maxWorkersCnt;

  private int lingerTimeOverride = -1;
  private boolean reuseAddress = false;

  public SocketServer() {
    serverRef = this;

    serverLock = new Object();
    listening = false;
    serverException = null;
    doListening = false;

    sockets = new ArrayList<SocketChannel>();
    globalSession = new HashMap<String, Object>();
    userSessions = new HashMap<SocketChannel, SocketSession>();

    initialWorkersCnt = DEFAULT_INITIAL_WORKER_THREADS;
    maxWorkersCnt = DEFAULT_MAX_WORKER_THREADS;
  }

  private Runnable listener = new Runnable() {
    public void run() {
      Socket sock;
      SocketChannel sockCh;
      Set<SelectionKey> keys;
      int cnt, len;
      Iterator<SelectionKey> iter;
      SelectionKey selKey = null;
      SocketSession userSession;
      ByteBuffer bBuf;
      byte[] data;

      try {

        synchronized (serverLock) {
          listening = true;
          serverLock.notifyAll();
        }

        bBuf = ByteBuffer.allocate(MAX_BUFFER_SIZE);

        while (doListening) {
          try {
            cnt = selector.select();

            if (cnt == 0) {
              continue;
            }

            keys = selector.selectedKeys();
            iter = keys.iterator();

            while (iter.hasNext()) {
              selKey = (SelectionKey) iter.next();
              iter.remove();

              if (!selKey.isValid()) {
                continue;
              }

              if (selKey.isAcceptable()) {
                //Client Connect Request (ACCEPT)
                sock = serverSock.accept();

                if (lingerTimeOverride > 0) {
                  sock.setSoLinger(true, lingerTimeOverride);
                }

                synchronized (serverLock) {
                  //Initialize Socket Stuff
                  sockCh = sock.getChannel();
                  sockCh.configureBlocking(false);
                  sockCh.register(selector, SelectionKey.OP_READ);
                  sockets.add(sockCh);

                  //Create User Session
                  userSession = new SocketSession(sockCh);
                  userSession.setServer(serverRef);
                  userSession.setReadManager(readManager);
                  userSessions.put(sockCh, userSession);

                  //Schedule a Pseudo-Read Event
                  //There is no data, but because it is a new user session
                  //some server side code may what to execute a handshake transaction
                  readManager.scheduleUserSessionReadEvent(userSession, null);
                }

              } //End Is Acceptable Block
              else if (selKey.isReadable()) {
                //Incoming Data (READ)
                sockCh = (SocketChannel) selKey.channel();

                //Obtain User Session
                userSession = userSessions.get(sockCh);

                //Is User Session on it's way out?
                if (userSession == null || userSession.isZombie()) {
                  close(sockCh);
                  continue;
                }

                try {
                  //Read into the native buffer
                  bBuf.clear();
                  len = sockCh.read(bBuf);

                  if (len > 0) {
                    //Get bytes
                    data = new byte[len];
                    bBuf.flip();
                    bBuf.get(data, 0, len);

                    //Schedule a Read Event
                    readManager.scheduleUserSessionReadEvent(userSession, data);
                  }
                  else if (len < 0) {
                    try {
                      close(sockCh);
                    }
                    catch (Exception e) {
                      e.printStackTrace();
                    }
                  }

                } //End readable try block
                catch (Exception e) {
                  e.printStackTrace();
                  try {
                    setClientForcedClose(sockCh);
                  }
                  catch (Exception e2) {
                    e2.printStackTrace();
                  }
                  try {
                    close(sockCh);
                  }
                  catch (Exception e3) {
                    e3.printStackTrace();
                  }
                }
              } //End Is Readable Block
            } //End while iter.hasNext
          } //End inner try block
          catch (Exception e) {
            e.printStackTrace();
          }
        } //End while doListening
      } //End Try Block
      catch (Exception e) {
        e.printStackTrace();
        serverException = e;
      }
      finally {
        if (selKey != null) {
          selKey.cancel();
        }

        if (workerPool != null) {
          try {
            workerPool.destroyPool();
          }
          catch (WorkerPoolException e) {
            e.printStackTrace();
          }
        }

        synchronized (serverLock) {
          listening = false;
          serverLock.notifyAll();
        }

        readManager.stop();
      } //End finally block
    } //End run method
  };

  public void listen(int port) throws RLNetException {
    listen(null, port);
  }

  public void listen(String bindAddress, int port) throws RLNetException {
    StringBuffer sb;

    synchronized (serverLock) {
      if (listening) {
        throw new RLNetException("Server already listening on port = " + port);
      }

      try {
        doListening = true;
        serverException = null;

        //Setup Socket Server...
        this.port = port;
        this.bindAddress = bindAddress;

        selector = Selector.open();

        serverSockCh = ServerSocketChannel.open();
        serverSockCh.configureBlocking(false);

        serverSockCh.socket().setReuseAddress(reuseAddress);

        if (bindAddress == null) {
          serverSockCh.socket().bind(new InetSocketAddress(this.port));
        }
        else {
          serverSockCh.socket().bind(new InetSocketAddress(this.bindAddress, this.port));
        }

        serverSockCh.register(selector, SelectionKey.OP_ACCEPT);
        serverSock = serverSockCh.socket();

        //Setup Worker Pool with Socket Workers
        sb = new StringBuffer();
        sb.append("SocketWorkers(");
        sb.append(port);
        sb.append(")");

        workerPool = new WorkerPool(sb.toString());
        workerPool.registerWorkerClass(SocketWorker.class);
        workerPool.setInitialSize(initialWorkersCnt);
        workerPool.setMaxSize(maxWorkersCnt);
        workerPool.setShrinkable(true);
        workerPool.setUseThreads(true);
        workerPool.setCustomizer(this);
        workerPool.createPool();

        //Start Listener
        listenerThread = new Thread(listener);
        listenerThread.start();

        while (!listening) {
          serverLock.wait();
        }

        //Start Read Manager
        readManager = new SocketReadManager();
        readManager.setWorkerPool(workerPool);
        readManager.start();

        if (serverException != null) {
          throw serverException;
        }
      }
      catch (Exception e) {
        throw new RLNetException(e);
      }
    }
  }

  private void setClientForcedClose(SocketChannel sockCh) {
    SocketSession userSession = userSessions.get(sockCh);

    if (userSession != null) {
      userSession.setPeerForcedClose(true);
    }
  }

  public int getLingerTimeOverride() {
    return lingerTimeOverride;
  }

  public void setLingerTimeOverride(int lingerTimeOverride) {
    this.lingerTimeOverride = lingerTimeOverride;
  }

  public void close() {
    SocketChannel sockCh;

    synchronized (serverLock) {
      if (listening) {
        doListening = false;

        if (serverSock != null) {
          try {
            selector.close();
          }
          catch (Exception e) {
            e.printStackTrace();
          }

          try {
            serverSock.close();
          }
          catch (Exception e) {
            e.printStackTrace();
          }

          try {
            serverSockCh.close();
          }
          catch (Exception e) {
            e.printStackTrace();
          }

          try {
            while (listening) {
              serverLock.wait();
            }
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }

        try {
          readManager.waitForStop();
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        while (!sockets.isEmpty()) {
          sockCh = sockets.remove(0);
          close(sockCh);
        } //End while Sockets is empty loop

        globalSession.clear();

        readManager.clearQueuingMechanism();
        readManager = null;
      }
    }
  }

  public void close(SocketChannel sockCh) {
    Socket sock = null;
    SelectionKey key;
    SocketSession userSession = null;

    synchronized (serverLock) {
      //System.out.println("Closing: " + sockCh);
      key = sockCh.keyFor(selector);
      if (key != null) {
        key.cancel();
      }

      userSession = userSessions.remove(sockCh);
      if (userSession != null) {
        userSession.drainRawData();
        userSession.setZombie(true);
      }

      sockets.remove(sockCh);
    }

    //System.out.println("Closed: " + sockCh);

    if (spSweeper != null && userSession != null) {
      try {
        spSweeper.cleanup(userSession);
      }
      catch (Exception e2) {
        System.err.println("Socket Process Sweeper Error!");
        e2.printStackTrace();
      }
    }

    if (userSession != null) {
      userSession.destroy();
    }
    userSession = null;

    if (sockCh != null) {
      sock = sockCh.socket();
      if (sock != null) {
        try {
          sock.close();
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
  }

  public void initWorker(Worker worker) throws WorkerException {
    SocketProcessor processor;
    SocketWorker sockWorker;

    sockWorker = (SocketWorker) worker;

    try {
      processor = (SocketProcessor) socketProcessorClass.newInstance();
      sockWorker.setProcessor(processor);

      //User Level Socket Processor Implementation Customization
      if (spCustomizer != null) {
        spCustomizer.initSocketProcessor(processor);
      }
    }
    catch (Exception e) {
      throw new WorkerException("Could NOT Set Socket Processor to Worker during Init!", e);
    }
  }

  public void configureWorker(Worker worker, WorkerParameter param) throws WorkerException {}

  public void setSocketProcessorClass(Class<?> socketProcessorClass) {
    this.socketProcessorClass = socketProcessorClass;
  }

  public int getPort() {
    return port;
  }

  public String getBindAddress() {
    return bindAddress;
  }

  protected HashMap<String, Object> getGlobalSession() {
    return globalSession;
  }

  public void setSocketProcessorCustomizer(SocketProcessorCustomizer customizer) {
    this.spCustomizer = customizer;
  }

  public Object putGlobalItem(String key, Object value) {
    return globalSession.put(key, value);
  }

  public Object getGlobalItem(String key) {
    return globalSession.get(key);
  }

  public Object removeGlobalItem(String key) {
    return globalSession.remove(key);
  }

  public void setSocketSessionSweeper(SocketSessionSweeper sweeper) {
    this.spSweeper = sweeper;
  }

  public int getInitialWorkersCnt() {
    return initialWorkersCnt;
  }

  public void setInitialWorkersCnt(int initialWorkersCnt) {
    this.initialWorkersCnt = initialWorkersCnt;
  }

  public int getMaxWorkersCnt() {
    return maxWorkersCnt;
  }

  public void setMaxWorkersCnt(int maxWorkersCnt) {
    this.maxWorkersCnt = maxWorkersCnt;
  }

  public boolean isReuseAddress() {
    return reuseAddress;
  }

  public void setReuseAddress(boolean reuseAddress) {
    this.reuseAddress = reuseAddress;
  }

  public boolean isListening() {
    synchronized (serverLock) {
      return listening;
    }
  }

}
