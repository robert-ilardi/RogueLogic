package com.roguelogic.sockrelay;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;

public class SockRelay implements SocketProcessorCustomizer, SocketSessionSweeper {

  private int listeningPort;

  private int targetPort;
  private String targetAddress;

  private SocketServer server;

  public SockRelay(int listeningPort, String targetAddress, int targetPort) {
    this.listeningPort = listeningPort;
    this.targetAddress = targetAddress;
    this.targetPort = targetPort;
  }

  public void start() throws RLNetException {
    if (server == null) {
      server = new SocketServer();
      server.setSocketProcessorClass(SockRelayProcessor.class);
      server.setSocketProcessorCustomizer(this);
      server.setSocketSessionSweeper(this);
      server.setInitialWorkersCnt(2);
      server.listen(listeningPort);
    }
    else {
      System.err.println("SocketRelayServer already listening on port = " + listeningPort + " forwarding data to " + targetAddress + " on port = " + targetPort);
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    SockRelayProcessor srsp = (SockRelayProcessor) processor;
    srsp.setTargetAddress(targetAddress);
    srsp.setTargetPort(targetPort);
  }

  public void cleanup(SocketSession userSession) {
    SocketClient sockClient = (SocketClient) userSession.getUserItem(SockRelayProcessor.USOBJ_TARGET_SOCK_CLIENT);
    sockClient.close();
  }

  public static void main(String[] args) {
    int listeningPort, targetPort;
    String targetAddress;
    SockRelay relayServer;

    if (args.length < 3 || args.length > 4) {
      System.err.println("java " + SockRelay.class.getName() + " [LISTENING_PORT] [TARGET_ADDRESS] [TARGET_PORT] <PRINT_DATA:Y|N>");
      System.exit(1);
    }
    else {
      try {
        listeningPort = Integer.parseInt(args[0].trim());
        targetAddress = args[1].trim();
        targetPort = Integer.parseInt(args[2].trim());

        relayServer = new SockRelay(listeningPort, targetAddress, targetPort);
        relayServer.start();
      }
      catch (Throwable t) {
        t.printStackTrace();
        System.exit(1);
      }
    }
  }

}
