package com.roguelogic.ipcp2p;

public class IPCPeer {

  private String address;
  private int port;

  private String connectionKey;

  public IPCPeer() {}

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

  public String getConnectionKey() {
    return connectionKey;
  }

  public void setConnectionKey(String connectionKey) {
    this.connectionKey = connectionKey;
  }

  public int hashCode() {
    return address.hashCode();
  }

  public boolean equals(Object obj) {
    boolean same = false;
    IPCPeer other;

    if (obj instanceof IPCPeer) {
      other = (IPCPeer) obj;
      same = (address.equalsIgnoreCase(other.address) && port == other.port);
    }

    return same;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[IPCPeer - Address: ");
    sb.append(address);
    sb.append(", Port: ");
    sb.append(port);
    sb.append(", ConnectionKey: ");
    sb.append(connectionKey);
    sb.append("]");

    return sb.toString();
  }

}
