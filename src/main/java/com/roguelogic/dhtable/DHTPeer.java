/**
 * August 7, 2006 
 */
package com.roguelogic.dhtable;

/**
 * @author Robert C. Ilardi
 *
 */

public class DHTPeer implements Comparable<DHTPeer> {

  private String address;
  private int port;

  private int peerIndex;

  private boolean self;

  public DHTPeer() {}

  public DHTPeer(String address, int port, int peerIndex) {
    this(address, port, peerIndex, false);
  }

  public DHTPeer(String address, int port, int peerIndex, boolean self) {
    this.address = address;
    this.port = port;
    this.peerIndex = peerIndex;
    this.self = self;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getPeerIndex() {
    return peerIndex;
  }

  public void setPeerIndex(int peerIndex) {
    this.peerIndex = peerIndex;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int compareTo(DHTPeer other) {
    if (other.peerIndex < this.peerIndex) {
      return -1;
    }
    else if (other.peerIndex > this.peerIndex) {
      return 1;
    }
    else {
      // other.peerIndex == this.peerIndex
      return 0;
    }
  }

  public boolean isSelf() {
    return self;
  }

  public void setSelf(boolean self) {
    this.self = self;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[DHTPeer - Address= ");
    sb.append(address);
    sb.append(", Port=");
    sb.append(port);
    sb.append(", PeerIndex=");
    sb.append(peerIndex);
    sb.append(", Self=");
    sb.append(self);
    sb.append("]");

    return sb.toString();
  }

  public int hashCode() {
    return peerIndex;
  }

  public boolean equals(Object obj) {
    boolean same = false;
    DHTPeer other;

    if (obj instanceof DHTPeer) {
      other = (DHTPeer) obj;
      same = (this.address.equalsIgnoreCase(other.address) && this.port == other.port && this.peerIndex == other.peerIndex && this.self == other.self);
    }

    return same;
  }
}
