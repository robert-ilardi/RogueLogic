/**
 * Created Aug 4, 2008
 */
package com.roguelogic.clustering.smap;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class SharedMap implements SharedMapImplementation {

  private SharedMapImplementation smImpl;

  private String host;
  private int port;
  private boolean serverMode;
  private SMemSecurityManager secMan;

  public SharedMap(int port, boolean serverMode, SMemSecurityManager secMan) throws SharedMemoryException {
    this("localhost", port, serverMode, secMan);
  }

  /**
   * Use this constructor to create client shared map
   * @param host
   * @param port
   * @throws SharedMemoryException
   */
  public SharedMap(String host, int port) throws SharedMemoryException {
    this(host, port, false, null);
  }

  public SharedMap(String host, int port, boolean serverMode, SMemSecurityManager secMan) throws SharedMemoryException {
    this.host = host;
    this.port = port;
    this.serverMode = serverMode;
    this.secMan = secMan;

    init();
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public boolean isServerMode() {
    return serverMode;
  }

  private void init() throws SharedMemoryException {
    if (serverMode) {
      //Server Mode
      smImpl = createServerImpl();
    }
    else {
      //Client Mode
      smImpl = createClientImpl();
    }
  }

  private SharedMapImplementation createServerImpl() throws SharedMemoryException {
    SharedMapImplementation smImpl;

    smImpl = new SharedMapServerImpl(host, port);
    smImpl.setSMemSecurityManager(secMan);

    return smImpl;
  }

  private SharedMapImplementation createClientImpl() throws SharedMemoryException {
    SharedMapImplementation smImpl = null;

    smImpl = new SharedMapClientImpl(host, port);

    return smImpl;
  }

  //Start SharedMapImplementation Methods
  //---------------------------------------------------------------------->

  public void close() throws SharedMemoryException {
    smImpl.close();
  }

  public void setSMemSecurityManager(SMemSecurityManager secMan) {
    this.secMan = secMan;
    smImpl.setSMemSecurityManager(secMan);
  }

  public boolean login(String username, String password) throws SharedMemoryException {
    return smImpl.login(username, password);
  }

  public Serializable get(Serializable key) throws SharedMemoryException {
    return smImpl.get(key);
  }

  public Serializable put(Serializable key, Serializable value) throws SharedMemoryException {
    return smImpl.put(key, value);
  }

  public Serializable remove(Serializable key) throws SharedMemoryException {
    return smImpl.remove(key);
  }

  public boolean containsKey(Serializable key) throws SharedMemoryException {
    return smImpl.containsKey(key);
  }

  public void clear() throws SharedMemoryException {
    smImpl.clear();
  }

  public boolean lockEntry(Serializable key, long timeout) throws SharedMemoryException {
    return smImpl.lockEntry(key, timeout);
  }

  public void unlockEntry(Serializable key) throws SharedMemoryException {
    smImpl.unlockEntry(key);
  }

  public boolean isEntryLocked(Serializable key) throws SharedMemoryException {
    return smImpl.isEntryLocked(key);
  }

  //---------------------------------------------------------------------->
  //End SharedMapImplementation Methods

}
