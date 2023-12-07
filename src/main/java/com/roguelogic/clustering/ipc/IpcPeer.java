/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

import java.io.IOException;
import java.io.Serializable;

import com.roguelogic.net.RLNetException;

/**
 * @author Robert C. Ilardi
 * 
 */

public class IpcPeer implements Serializable {

  private String processName;
  private String address;
  private int port;

  private transient IpcProcessHost processHost;
  private transient IpcClient client;

  public IpcPeer() {
  }

  public IpcPeer(String processName, String address, int port, IpcProcessHost processHost) {
    setProcessName(processName);
    this.address = address;
    this.port = port;
    this.processHost = processHost;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(String processName) {
    this.processName = processName.trim().toUpperCase();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("AmgIpcPeer - ");

    sb.append("ProcessName: ");
    sb.append(processName);

    sb.append("; Address: ");
    sb.append(address);

    sb.append("; Port: ");
    sb.append(port);

    sb.append("]");

    return sb.toString();
  }

  public synchronized void sendTo(IpcEvent event) throws RLNetException, IOException {
    if (client == null) {
      client = new IpcClient(processHost, address, port);
    }

    client.send(event);
  }

  public synchronized void close() throws RLNetException {
    if (client != null) {
      client.disconnect();
      client = null;
    }
  }

  public synchronized boolean isAvailable() {
    if (client == null) {
      client = new IpcClient(processHost, address, port);
    }

    return client.isAvailable();
  }

}
