/*
 * Created on Mar 3, 2006
 */
package com.roguelogic.net.rltalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;

public class RLTalkCmdLineClient {

  private SocketClient sockClient;

  private String address;
  private int port;

  public RLTalkCmdLineClient(String address, int port) {
    this.address = address;
    this.port = port;
  }

  public void start() throws RLNetException {
    sockClient = new SocketClient();
    sockClient.setSocketProcessorClass(RLTalkCmdLineClientProcessor.class);
    sockClient.connect(address, port);

    System.out.println("RL-Talk Command Line Interface Client Connected to RL-Talk based Server Running at: " + address + " on Port = " + port);
  }

  public void stop() throws RLNetException {
    if (sockClient != null) {
      sockClient.close();
    }
  }

  public void doCmdLine() throws IOException, RLTalkException, RLNetException {
    BufferedReader stdin;
    String payload;
    int cmd, statusCode, multiplexerIndex;
    CommandDataPair cmDatPair;

    stdin = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
      cmd = readInt("Command(int): ", stdin);

      statusCode = readInt("Status Code(int): ", stdin);

      multiplexerIndex = readInt("Multiplexer Index(int): ", stdin);

      System.out.print("Data Payload(String): ");
      payload = stdin.readLine();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(cmd);
      cmDatPair.setStatusCode(statusCode);
      cmDatPair.setMultiplexerIndex(multiplexerIndex);
      cmDatPair.setData(payload);

      send(cmDatPair);
    }
  }

  private int readInt(String mesg, BufferedReader stdin) throws IOException {
    String tmp;
    int num;

    while (true) {
      System.out.print(mesg);
      tmp = stdin.readLine();

      try {
        num = Integer.parseInt(tmp);
        break;
      }
      catch (NumberFormatException e) {
        System.err.println("Invalid INT Try Again!");
      }
    }

    return num;
  }

  private synchronized void send(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
    Packet[] packets;

    //Synchronized to make sure a complete message
    //from a single thread will be sent before
    //the next message can be sent!
    packets = PacketFactory.GetPackets(cmDatPair); //Convert CDP to Packets 

    //Send Packets across the network
    for (int i = 0; i < packets.length; i++) {
      sockClient.send(packets[i].toByteArray());
    }
  }

  public static void main(String[] args) {
    RLTalkCmdLineClient rltClient = null;
    String address;
    int port;

    if (args.length != 2) {
      System.err.println("Usage java " + RLTalkCmdLineClient.class.getName() + " [ADDRESS] [PORT]");
      System.exit(1);
    }
    else {
      try {
        address = args[0];
        port = Integer.parseInt(args[1]);

        rltClient = new RLTalkCmdLineClient(address, port);
        rltClient.start();

        rltClient.doCmdLine(); //Blocks until exit command

        rltClient.stop();

        System.exit(0);
      }
      catch (Exception e) {
        e.printStackTrace();

        if (rltClient != null) {
          try {
            rltClient.stop();
          }
          catch (Exception e2) {}
        }

        System.exit(1);
      }
    }
  }

}
