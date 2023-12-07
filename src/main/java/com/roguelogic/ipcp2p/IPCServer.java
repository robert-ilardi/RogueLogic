package com.roguelogic.ipcp2p;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkUtils;

public class IPCServer implements SocketProcessorCustomizer, SocketSessionSweeper {

  private SocketServer server;

  private IPCP2PService ipcP2pService;

  private String connectionKey;

  private IPCHandshakeWatcher handshakeWatcher;

  public IPCServer(IPCP2PService ipcP2pService) throws IPCException {
    if (ipcP2pService == null) {
      throw new IPCException("IPC P2P Service can NOT be NULL when creating new IPC P2P Server!");
    }

    this.ipcP2pService = ipcP2pService;
  }

  public String getConnectionKey() {
    return connectionKey;
  }

  public void setConnectionKey(String connectionKey) {
    this.connectionKey = connectionKey;
  }

  public void addHandshakeWatch(SocketSession sockSession) {
    if (connectionKey != null && connectionKey.trim().length() > 0) {
      //Verify Handshake
      System.out.println("Adding Socket Session to Handshake Watch List...");
      handshakeWatcher.addWatch(sockSession);
    }
    else {
      //If connectionKey is null or empty this IPC Server is public!
      sockSession.putUserItem(IPCServerProcessor.UDI_CONNECTION_VALID_FLAG, true);
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    IPCServerProcessor ipcsp;

    if (processor instanceof IPCServerProcessor) {
      ipcsp = (IPCServerProcessor) processor;
      ipcsp.setIPCServer(this);
    }
  }

  public void cleanup(SocketSession userSession) {
    handshakeWatcher.removeWatch(userSession);
  }

  public synchronized void start() throws RLNetException {
    if (server == null) {
      handshakeWatcher = new IPCHandshakeWatcher();
      handshakeWatcher.setGracePeriodSecs(ipcP2pService.getHswGracePeriodSecs());
      handshakeWatcher.start();

      server = new SocketServer();
      server.setSocketProcessorClass(IPCServerProcessor.class);
      server.setSocketProcessorCustomizer(this);
      server.setSocketSessionSweeper(this);
      server.setInitialWorkersCnt(1);
      server.listen(ipcP2pService.getPort());
    }
    else {
      System.err.println("IPC P2P Server already listening on port = " + ipcP2pService.getPort());
    }
  }

  public synchronized void stop() {
    if (handshakeWatcher != null) {
      handshakeWatcher.stop();
      handshakeWatcher = null;
    }

    if (server != null) {
      server.close();
      server = null;
    }
  }

  protected void recieve(IPCMessage mesg) {
    ipcP2pService.receive(mesg);
  }

  public void validateConnectionKey(SocketSession sockSession, String connectionKey) throws RLTalkException, RLNetException {
    CommandDataPair cmDatPair;

    if (this.connectionKey != null && this.connectionKey.equals(connectionKey)) {
      System.out.println("Connection Key is Valid!");
      sockSession.putUserItem(IPCServerProcessor.UDI_CONNECTION_VALID_FLAG, true);
      handshakeWatcher.removeWatch(sockSession);

      //Send Result
      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(IPCClientProcessor.CMD_RECEIVE_CONNECTION_KEY_RESULT);
      cmDatPair.setStatusCode(IPCClientProcessor.CONNECTION_KEY_RESULT_VALID);
      RLTalkUtils.RLTalkSend(sockSession, cmDatPair);
    }
    else {
      //Disconnection Client
      System.out.println("Connection Key is Invalid! Disconnecting Client...");
      sockSession.endSession();
    }
  }

}
