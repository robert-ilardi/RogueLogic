package com.roguelogic.ipcp2p;

import java.io.Serializable;
import java.util.ArrayList;

import com.roguelogic.net.HandshakeWatcher;
import com.roguelogic.net.RLNetException;

public class IPCP2PService {

  private int port;
  private ArrayList<IPCPeer> peers;

  private ArrayList<IPCDataObserver> observers;

  private IPCServer ipcServer;
  private ArrayList<IPCClient> ipcClients;

  private int maxSendTries;

  private String connectionKey;
  private int hswGracePeriodSecs;

  public IPCP2PService() {
    peers = new ArrayList<IPCPeer>();
    observers = new ArrayList<IPCDataObserver>();
    ipcClients = new ArrayList<IPCClient>();

    maxSendTries = IPCClient.DEFAULT_MAX_TRIES;
    hswGracePeriodSecs = HandshakeWatcher.DEFAULT_GRACE_PERIOD_SECS;
    connectionKey = null;
  }

  public IPCPeer[] getPeers() {
    return (IPCPeer[]) peers.toArray(new IPCPeer[0]);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getMaxSendTries() {
    return maxSendTries;
  }

  public void setMaxSendTries(int maxSendTries) {
    if (maxSendTries >= 1) {
      this.maxSendTries = maxSendTries;
    }
  }

  public String getConnectionKey() {
    return connectionKey;
  }

  public void setConnectionKey(String connectionKey) {
    this.connectionKey = connectionKey;
  }

  public synchronized void addPeer(String address, int port, String connectionKey) {
    IPCPeer peer;

    if (address != null && address.trim().length() > 0 && port > 0) {
      //Created Peer Object
      peer = new IPCPeer();
      peer.setAddress(address.trim());
      peer.setPort(port);
      peer.setConnectionKey(connectionKey);

      addPeer(peer);
    }
  }

  public synchronized void addPeer(IPCPeer peer) {
    IPCClient client;

    if (peer != null) {
      peers.add(peer);

      //Created IPC Client for Peer
      client = new IPCClient(this, peer);
      client.setMaxSendTries(maxSendTries);

      ipcClients.add(client);
    }
  }

  public synchronized void removePeer(String address, int port) {
    IPCPeer peer;

    if (address != null && address.trim().length() > 0 && port > 0) {
      //Created Peer Object
      peer = new IPCPeer();
      peer.setAddress(address.trim());
      peer.setPort(port);

      removePeer(peer);
    }
  }

  public synchronized void removePeer(IPCPeer peer) {
    if (peer != null) {
      peers.remove(peer);

      for (IPCClient client : ipcClients) {
        if (peer.equals(client.getPeer())) {
          ipcClients.remove(client);
          client.disconnect();
          break;
        }
      }
    }
  }

  public synchronized void addDataObserver(IPCDataObserver observer) {
    if (observer != null) {
      observers.add(observer);
    }
  }

  public synchronized void removeDataObserver(IPCDataObserver observer) {
    if (observer != null) {
      observers.remove(observer);
    }
  }

  public synchronized void broadcast(String subject, String name, Serializable data) throws IPCException {
    IPCMessage mesg;

    mesg = new IPCMessage();

    mesg.setSubject(subject);
    mesg.setName(name);
    mesg.setData(data);

    broadcast(mesg);
  }

  public synchronized void broadcast(IPCMessage mesg) throws IPCException {
    int exCnt = 0;

    for (IPCClient client : ipcClients) {
      try {
        client.send(mesg);
      }
      catch (IPCException e) {
        exCnt++;
        System.err.print("IPC Broadcast Error while sending IPC Message to: " + client.getPeer());
        e.printStackTrace();
      }
    }

    if (exCnt == ipcClients.size()) {
      throw new IPCException("Check IPC P2P Network... All IPC Message Send Calls have FAILED!");
    }
  }

  public synchronized void receive(IPCMessage mesg) {
    IPCDataObserver observer;

    if (mesg != null) {
      for (int i = 0; i < observers.size(); i++) {
        observer = observers.get(i);
        observer.receiveData(mesg);
      }
    }
  }

  public synchronized void start() throws IPCException {
    if (ipcServer == null) {
      try {
        ipcServer = new IPCServer(this);
        ipcServer.setConnectionKey(connectionKey);
        ipcServer.start();
      }
      catch (Exception e) {
        throw new IPCException("An error occurred while attempting to start the IPC P2P Server...", e);
      }
    }
  }

  public synchronized void stop() {
    if (ipcServer != null) {
      ipcServer.stop();
      ipcServer = null;
    }

    for (IPCClient client : ipcClients) {
      client.disconnect();
    }
  }

  public synchronized void doClientConnects() throws RLNetException, IPCException {
    for (IPCClient client : ipcClients) {
      client.connect();
    }
  }

  public int getHswGracePeriodSecs() {
    return hswGracePeriodSecs;
  }

  public void setHswGracePeriodSecs(int hswGracePeriodSecs) {
    this.hswGracePeriodSecs = hswGracePeriodSecs;
  }

}
