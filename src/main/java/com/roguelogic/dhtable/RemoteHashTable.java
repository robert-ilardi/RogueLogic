/**
 * Created Aug 10, 2006
 */
package com.roguelogic.dhtable;

import java.io.Serializable;
import java.util.Hashtable;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;

/**
 * @author Robert C. Ilardi
 *
 */
public class RemoteHashTable implements SocketProcessorCustomizer {

  protected Hashtable<Serializable, Serializable> table;
  protected SocketServer server;
  protected int port;

  protected RemoteHashTable() {
    table = new Hashtable<Serializable, Serializable>();
  }

  public RemoteHashTable(int port) {
    this();
    setPort(port);
  }

  protected void setPort(int port) {
    this.port = port;
  }

  public synchronized void start() throws RLNetException {
    if (server == null) {
      server = new SocketServer();
      server.setSocketProcessorClass(RHTServerProcessor.class);
      server.setSocketProcessorCustomizer(this);
      server.setInitialWorkersCnt(1);
      server.listen(port);
    }
    else {
      System.err.println("Remote Hash Table Server already listening on port = " + port);
    }
  }

  public synchronized void stop() {
    if (server != null) {
      server.close();
      server = null;
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    RHTServerProcessor rhtsProcessor;

    if (processor instanceof RHTServerProcessor) {
      rhtsProcessor = (RHTServerProcessor) processor;
      rhtsProcessor.setRHTable(this);
    }
  }

  protected synchronized boolean ltContainsKey(Serializable key) {
    return table.containsKey(key);
  }

  protected synchronized void ltPut(Serializable key, Serializable value) {
    table.put(key, value);
  }

  protected synchronized Serializable ltGet(Serializable key) {
    return table.get(key);
  }

  protected synchronized void ltRemove(Serializable key) {
    table.remove(key);
  }

  protected synchronized void ltClear() {
    table.clear();
  }

  protected synchronized KeyList ltGetKeyList() {
    KeyList keyList = new KeyList();

    for (Serializable key : table.keySet()) {
      keyList.addKey(key);
    }

    return keyList;
  }

}
