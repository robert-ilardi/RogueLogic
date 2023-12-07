package com.roguelogic.rltalkecho;
import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketServer;

public class RLTalkEchoServer {

  public RLTalkEchoServer() {}

  public static void main(String[] args) throws RLNetException {
    SocketServer server;

    server = new SocketServer();
    server.setSocketProcessorClass(RLTalkEchoProcessor.class);
    server.setInitialWorkersCnt(1);
    server.listen(Integer.parseInt(args[0]));
    System.out.println("RLTalk ECHO Server Running on Port = " + server.getPort());
  }

}
