/**
 * August 7, 2006
 */
package com.roguelogic.dhtable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.roguelogic.net.RLNetException;

/**
 * @author Robert C. Ilardi
 *
 */

public class DistributedHashTable {

  protected RemoteHashTable hashTable;
  protected ArrayList<DHTPeer> peerInfo;
  protected HashMap<DHTPeer, RemoteHashTableClient> peers;

  protected LoadBalancingModes lbMode;

  private int selfPntr;
  private int curPntr;

  protected DistributedHashTable() {
    peerInfo = new ArrayList<DHTPeer>();
    peers = new HashMap<DHTPeer, RemoteHashTableClient>();

    setLoadBalancingMode(LoadBalancingModes.RoundRobin); //Default LB Mode to Round Robin
  }

  public DistributedHashTable(int port, int peerIndex) throws DistributedHashTableException, RLNetException {
    this();
    setup(port, peerIndex);
  }

  private void setup(int port, int peerIndex) throws DistributedHashTableException, RLNetException {
    DHTPeer self = new DHTPeer();

    self.setPort(port);
    self.setPeerIndex(peerIndex);
    self.setSelf(true);

    peerInfo.add(self);
    sortPeerInfo();

    createServer();
  }

  private void createServer() throws DistributedHashTableException, RLNetException {
    DHTPeer self;

    self = getSelf();
    if (self == null) {
      throw new DistributedHashTableException("Could NOT Obtain Self Peer Refernce!");
    }

    hashTable = new RemoteHashTable(self.getPort());
    hashTable.start();
  }

  public synchronized void destory() {
    hashTable.stop();

    for (RemoteHashTableClient client : peers.values()) {
      client.destroy();
    }

    peers.clear();
    peerInfo.clear();

    hashTable = null;
    peers = null;
    peerInfo = null;
  }

  public synchronized void setLoadBalancingMode(LoadBalancingModes lbMode) {
    this.lbMode = lbMode;
  }

  public LoadBalancingModes getLoadBalancingMode() {
    return lbMode;
  }

  public synchronized void addPeer(DHTPeer peer) {
    RemoteHashTableClient client;

    peerInfo.add(peer);
    sortPeerInfo();

    client = new RemoteHashTableClient(peer.getAddress(), peer.getPort());
    peers.put(peer, client);
  }

  private void sortPeerInfo() {
    Collections.sort(peerInfo);
    selfPntr = findSelf();
    curPntr = selfPntr;
  }

  private int findSelf() {
    int pntr = -1;
    DHTPeer peer;

    for (int i = 0; i < peerInfo.size(); i++) {
      peer = peerInfo.get(i);

      if (peer.isSelf()) {
        pntr = i;
        break;
      }
    }

    return pntr;
  }

  private synchronized void incrementCurPntr() {
    if ((curPntr + 1) >= peerInfo.size()) {
      curPntr = 0;
    }
    else {
      curPntr++;
    }
  }

  protected DHTPeer getSelf() {
    return getPeer(selfPntr);
  }

  protected DHTPeer getPeer(int index) {
    DHTPeer peer = null;

    if (index >= 0 && index < peerInfo.size()) {
      peer = peerInfo.get(index);
    }

    return peer;
  }

  //START Local-Peer Hash Table Decision Wrapper Methods------------------------------>

  protected synchronized boolean containsKey(DHTPeer peer, Serializable key) {
    RemoteHashTableClient client;
    boolean has = false;

    if (peer.isSelf()) {
      //Local
      has = hashTable.ltContainsKey(key);
    }
    else {
      //Remote
      client = peers.get(peer);
      try {
        has = client.containsKey(key);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    return has;
  }

  protected synchronized void remove(DHTPeer peer, Serializable key) {
    RemoteHashTableClient client;

    if (peer.isSelf()) {
      //Local
      hashTable.ltRemove(key);
    }
    else {
      //Remote
      client = peers.get(peer);
      try {
        client.remove(key);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected synchronized void clear(DHTPeer peer) {
    RemoteHashTableClient client;

    if (peer.isSelf()) {
      //Local
      hashTable.ltClear();
    }
    else {
      //Remote
      client = peers.get(peer);
      try {
        client.clear();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected synchronized KeyList getKeyList(DHTPeer peer) {
    RemoteHashTableClient client;
    KeyList keyList = null;

    if (peer.isSelf()) {
      //Local
      keyList = hashTable.ltGetKeyList();
    }
    else {
      //Remote
      client = peers.get(peer);
      try {
        keyList = client.getKeyList();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    return keyList;
  }

  //END Local-Peer Hash Table Decision Wrapper Methods------------------------------>

  //START Local Hash Table Methods--------------------------------------->

  protected synchronized boolean ltContains(Serializable key) {
    return hashTable.ltContainsKey(key);
  }

  protected synchronized void ltPut(Serializable key, Serializable value) {
    hashTable.ltPut(key, value);
  }

  protected synchronized Serializable ltGet(Serializable key) {
    return hashTable.ltGet(key);
  }

  protected synchronized void ltRemove(Serializable key) {
    hashTable.ltRemove(key);
  }

  //END Local Hash Table Methods--------------------------------------->

  //START Remote Hash Table Methods--------------------------------------->

  public synchronized Serializable get(Serializable key) {
    Serializable value = null;

    switch (lbMode) {
      case RoundRobin:
        value = rrGet(key);
        break;
    }

    return value;
  }

  public synchronized void put(Serializable key, Serializable value) {
    switch (lbMode) {
      case RoundRobin:
        rrPut(key, value);
        break;
    }
  }

  public synchronized void remove(Serializable key) {
    switch (lbMode) {
      case RoundRobin:
        rrRemove(key);
        break;
    }
  }

  public synchronized boolean containsKey(Serializable key) {
    boolean has = false;

    switch (lbMode) {
      case RoundRobin:
        has = rrContainsKey(key);
        break;
    }

    return has;
  }

  public synchronized void clear() {
    switch (lbMode) {
      case RoundRobin:
        rrClear();
        break;
    }
  }

  public synchronized KeyList getKeyList() {
    KeyList keyList = null;

    switch (lbMode) {
      case RoundRobin:
        keyList = rrGetKeyList();
        break;
    }

    return keyList;
  }

  //END Remote Hash Table Methods--------------------------------------->

  //START Round Robin Hash Table Method Implementation--------------------------------------->

  protected synchronized Serializable rrGet(Serializable key) {
    RemoteHashTableClient client;
    Serializable value = null;

    for (DHTPeer peer : peerInfo) {
      if (peer.isSelf()) {
        //Local
        value = hashTable.ltGet(key);
      }
      else {
        //Remote
        client = peers.get(peer);
        try {
          value = client.get(key);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (value != null) {
        break;
      }
    }

    return value;
  }

  protected synchronized void rrPut(Serializable key, Serializable value) {
    RemoteHashTableClient client;
    DHTPeer peer;

    if (!rrContainsKey(key)) {
      peer = getPeer(curPntr);

      if (peer.isSelf()) {
        //Local
        hashTable.ltPut(key, value);
      }
      else {
        //Remote
        client = peers.get(peer);
        try {
          client.put(key, value);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }

      incrementCurPntr(); //So we use the next Peer on the next put call
    } //End !rrContains(key) Check
  }

  protected synchronized void rrRemove(Serializable key) {
    for (DHTPeer peer : peerInfo) {
      if (containsKey(peer, key)) {
        remove(peer, key);
      }
    }
  }

  protected synchronized boolean rrContainsKey(Serializable key) {
    boolean has = false;

    for (DHTPeer peer : peerInfo) {
      has = containsKey(peer, key);
      if (has) {
        break;
      }
    }

    return has;
  }

  protected synchronized void rrClear() {
    for (DHTPeer peer : peerInfo) {
      clear(peer);
    }
  }

  protected synchronized KeyList rrGetKeyList() {
    KeyList keyList, totalKL = new KeyList();

    for (DHTPeer peer : peerInfo) {
      keyList = getKeyList(peer);
      if (keyList != null) {
        totalKL.addAll(keyList);
      }
    }

    return totalKL;
  }

  //END Round Robin Hash Table Method Implementation--------------------------------------->

}
