package com.roguelogic.ipcp2p;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkUtils;
import com.roguelogic.util.SystemUtils;

public class IPCClient implements SocketProcessorCustomizer, SocketSessionSweeper {

  public static final int DEFAULT_MAX_TRIES = 2;

  IPCP2PService service;

  private IPCPeer peer;

  private SocketClient client;

  private int maxSendTries;

  public IPCClient(IPCP2PService service, IPCPeer peer) {
    this.service = service;
    this.peer = peer;
    maxSendTries = DEFAULT_MAX_TRIES;
  }

  public IPCPeer getPeer() {
    return peer;
  }

  public void setMaxSendTries(int maxSendTries) {
    if (maxSendTries >= 1) {
      this.maxSendTries = maxSendTries;
    }
  }

  public int getMaxSendTries() {
    return maxSendTries;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {}

  public void cleanup(SocketSession userSession) {
    client = null;
  }

  public synchronized void connect() throws RLNetException, IPCException {
    Boolean cvStatus;

    if (client == null) {
      System.out.println("Connecting IPC Client to: " + peer);
      client = new SocketClient();

      client.setSocketProcessorClass(IPCClientProcessor.class);
      client.setSocketProcessorCustomizer(this);
      client.setSocketSessionSweeper(this);

      client.connect(peer.getAddress(), peer.getPort());

      //client.putUserItem(IPCClientProcessor.UDI_CONNECTION_VALID_FLAG, new Boolean(false));

      if (peer.getConnectionKey() != null && peer.getConnectionKey().trim().length() > 0) {
        sendConnectionKey();

        cvStatus = waitForConnectionValidationStatus();

        if (cvStatus == null) {
          System.err.println("Failed to Receive Connection Validation Confirmation!");
          disconnect();
          throw new IPCException("Failed to Receive Connection Validation Confirmation!");
        }
        else if (!cvStatus.booleanValue()) {
          System.err.println("Connection Validation Failed! - Connection Key Invalid!");
          disconnect();
          throw new IPCException("Connection Validation Failed! - Connection Key Invalid!");
        }
        else {
          System.err.println("Connection Key Valid!");
        }
      } //End null connection key check
    } //End client null check
  }

  private Boolean waitForConnectionValidationStatus() {
    Boolean connValid = null;
    Object obj;

    System.out.println("Waiting for Connection Key Validation Status...");

    for (int i = 1; client != null && client.isConnected() && i <= service.getHswGracePeriodSecs(); i++) {
      obj = client.getUserItem(IPCClientProcessor.UDI_CONNECTION_VALID_FLAG);
      if (obj != null) {
        connValid = (Boolean) obj;
        if (connValid) {
          break;
        }
      }

      SystemUtils.Sleep(1); //Wait a bit...
    }

    return connValid;
  }

  public synchronized void disconnect() {
    if (client != null) {
      client.close();
      client = null;
    }
  }

  public synchronized void send(IPCMessage mesg) throws IPCException {
    SocketSession userSession;
    CommandDataPair cmDatPair;
    int tryCnt = 1;

    while (true) {
      try {
        System.out.println("Sending IPC Message to: " + peer);
        connect(); //Establish the connection if needed...

        //Step 2 - Send IPC Message
        userSession = client.getUserSession();

        cmDatPair = new CommandDataPair();
        cmDatPair.setCommand(IPCServerProcessor.CMD_RECEIVE_IPC_MESSAGE);
        cmDatPair.setData(mesg);

        RLTalkUtils.RLTalkSend(userSession, cmDatPair);

        break;
      } //End try block
      catch (Exception e) {
        disconnect();

        if (tryCnt >= maxSendTries) {
          throw new IPCException("An error occurred while attempting to send IPC Message to Peer = " + peer, e);
        }
        else {
          tryCnt++;
        }
      } //End Catch Block
    } //End while true
  }

  private void sendConnectionKey() throws RLTalkException, RLNetException {
    SocketSession userSession;
    CommandDataPair cmDatPair;

    if (peer.getConnectionKey() != null && peer.getConnectionKey().trim().length() > 0) {
      System.out.println("Sending Connection Key '" + peer.getConnectionKey() + "' to: " + peer);

      userSession = client.getUserSession();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(IPCServerProcessor.CMD_RECEIVE_CONNECTION_KEY);
      cmDatPair.setData(peer.getConnectionKey());

      RLTalkUtils.RLTalkSend(userSession, cmDatPair);
    }
  }

}
