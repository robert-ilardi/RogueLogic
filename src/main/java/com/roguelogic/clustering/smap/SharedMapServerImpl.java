/**
 * Created Aug 4, 2008
 */
package com.roguelogic.clustering.smap;

import static com.roguelogic.clustering.smap.SharedMapConstants.INFINITE_TIMEOUT;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class SharedMapServerImpl implements SharedMapImplementation, SocketProcessorCustomizer, SocketSessionSweeper {

  private SMemSecurityManager secMan;

  private String host;
  private int port;

  private Object serverLock;
  private Object mapLock;

  private int readCnt;

  private SocketServer sockServer;

  private HashMap<Serializable, Serializable> map;
  private HashSet<Serializable> lockedEntries;

  public SharedMapServerImpl(int port) throws SharedMemoryException {
    this(null, port);
  }

  public SharedMapServerImpl(String host, int port) throws SharedMemoryException {
    this.host = host;
    this.port = port;

    readCnt = 0;

    mapLock = new Object();
    serverLock = new Object();

    map = new HashMap<Serializable, Serializable>();
    lockedEntries = new HashSet<Serializable>();

    listen();
  }

  private void listen() throws SharedMemoryException {
    try {
      synchronized (serverLock) {
        sockServer = new SocketServer();

        sockServer.setSocketProcessorClass(SharedMapServerProcessor.class);
        sockServer.setSocketProcessorCustomizer(this);
        sockServer.setSocketSessionSweeper(this);

        if (StringUtils.IsNVL(host)) {
          sockServer.listen(port);
        }
        else {
          sockServer.listen(host, port);
        }

        serverLock.notifyAll();
      }
    } //End try block
    catch (Exception e) {
      throw new SharedMemoryException("Could NOT start Server Implementation of Shared Map! Message: " + e.getMessage(), e);
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    SharedMapServerProcessor smsProc = (SharedMapServerProcessor) processor;
    smsProc.setSharedMapServer(this);
  }

  public void cleanup(SocketSession userSession) {}

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#close(java.io.Serializable)
   */
  public void close() throws SharedMemoryException {
    synchronized (serverLock) {
      if (sockServer != null) {
        sockServer.close();
      }

      serverLock.notifyAll();
    }
  }

  public void setSMemSecurityManager(SMemSecurityManager secMan) {
    this.secMan = secMan;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#login(java.io.Serializable)
   */
  public boolean login(String username, String password) throws SharedMemoryException {
    return secMan.login(username, password);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#get(java.io.Serializable)
   */
  public Serializable get(Serializable key) throws SharedMemoryException {
    Serializable value = null;

    synchronized (mapLock) {
      readCnt++;
    }

    value = map.get(key);

    synchronized (mapLock) {
      readCnt--;
      mapLock.notifyAll();
    }

    return value;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#put(java.io.Serializable, java.io.Serializable)
   */
  public Serializable put(Serializable key, Serializable value) throws SharedMemoryException {
    Serializable oldVal = null;

    try {
      synchronized (mapLock) {
        while (readCnt > 0) {
          mapLock.wait();
        }

        oldVal = map.put(key, value);
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to put to map storage: " + e.getMessage(), e);
    }

    return oldVal;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#remove(java.io.Serializable)
   */
  public Serializable remove(Serializable key) throws SharedMemoryException {
    Serializable oldVal = null;

    try {
      synchronized (mapLock) {
        while (readCnt > 0) {
          mapLock.wait();
        }

        oldVal = map.remove(key);
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to remove from map storage: " + e.getMessage(), e);
    }

    return oldVal;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#containsKey(java.io.Serializable)
   */
  public boolean containsKey(Serializable key) throws SharedMemoryException {
    boolean contains = false;

    synchronized (mapLock) {
      readCnt++;
    }

    contains = map.containsKey(key);

    synchronized (mapLock) {
      readCnt--;
      mapLock.notifyAll();
    }

    return contains;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#clear()
   */
  public void clear() throws SharedMemoryException {
    try {
      synchronized (mapLock) {
        while (readCnt > 0) {
          mapLock.wait();
        }

        map.clear();
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to clear map storage: " + e.getMessage(), e);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#lockEntry(java.io.Serializable, long)
   */
  public boolean lockEntry(Serializable key, long timeout) throws SharedMemoryException {
    boolean locked = false, timedOut = false;
    long startTime, endTime;

    try {
      synchronized (mapLock) {
        startTime = System.currentTimeMillis();

        while (readCnt > 0 || lockedEntries.contains(key)) {
          if (timeout != INFINITE_TIMEOUT) {
            mapLock.wait(timeout);
          }
          else {
            mapLock.wait();
          }

          endTime = System.currentTimeMillis();

          if (timeout != INFINITE_TIMEOUT && (endTime - startTime) > timeout) {
            timedOut = true;
            break;
          }
        }

        if (!timedOut) {
          lockedEntries.add(key);
          locked = true;

          mapLock.notifyAll();
        }
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to lock entry: " + e.getMessage(), e);
    }

    return locked;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#unlockEntry(java.io.Serializable)
   */
  public void unlockEntry(Serializable key) throws SharedMemoryException {
    try {
      synchronized (mapLock) {
        while (readCnt > 0) {
          mapLock.wait();
        }

        lockedEntries.remove(key);

        mapLock.notifyAll();
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to unlock entry: " + e.getMessage(), e);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#isEntryLocked(java.io.Serializable)
   */
  public boolean isEntryLocked(Serializable key) throws SharedMemoryException {
    boolean locked = false;

    synchronized (mapLock) {
      locked = lockedEntries.contains(key);
    }

    return locked;
  }

}
