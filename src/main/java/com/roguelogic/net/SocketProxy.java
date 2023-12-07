/*
 * SocketProxy.java
 *
 * Created on November 11, 2003, 12:39 PM
 */

/*
 Copyright 2007 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/**
 *
 * @author  rilardi
 */

package com.roguelogic.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SocketProxy {
  private ServerSocket ss;

  private int listeningPort;

  private boolean listening = false;

  private boolean printData = false;

  /** Creates a new instance of SocketProxy */
  public SocketProxy(int listeningPort, String destinationHost, int destinationPort) {
    this.listeningPort = listeningPort;
  }

  public boolean isListening() {
    return listening;
  }

  public void setPrintData(boolean printData) {
    this.printData = printData;
  }

  public void listen() throws IOException, UnknownHostException {
    Socket s;

    listening = true;

    ss = new ServerSocket(listeningPort);

    while (listening) {
      s = null;

      System.out.println("Listening for new connection on Port: " + listeningPort);
      s = ss.accept();
      System.out.println("Connection Accepted from IP: " + s.getInetAddress().getHostAddress());
    }
  }

  public class SocketRelay implements Runnable {
    private Socket sourceSocket;
    private Socket destinationSocket;

    private SocketPair sockPair;

    private DataOutputStream destinationOutputStream;
    private DataInputStream sourceInputStream;

    private boolean relaying = false;

    private byte[] readBuffer;

    public static final int BUFFER_SIZE = 2048;

    public SocketRelay(SocketPair pair, Socket source, Socket destination) throws IOException {
      sourceSocket = source;
      destinationSocket = destination;
      sockPair = pair;

      readBuffer = new byte[BUFFER_SIZE];

      sourceInputStream = new DataInputStream(sourceSocket.getInputStream());
      destinationOutputStream = new DataOutputStream(destinationSocket.getOutputStream());

      System.out.println("Initializing Relay from: " + sourceSocket.getInetAddress().getHostAddress() + " to: " + destinationSocket.getInetAddress().getHostAddress());
    }

    public boolean isRelaying() {
      return relaying;
    }

    //Read the Socket Stream
    private void readSocket() throws IOException {
      int cnt = 0;

      cnt = sourceInputStream.read(readBuffer, 0, readBuffer.length);
      if (cnt != -1) {
        if (printData) {
          System.out.println(new String(readBuffer, 0, cnt));
        }
        destinationOutputStream.write(readBuffer, 0, cnt);
      }
      else {
        relaying = false; //Force Thread to Stop and close socket
      }
    }

    public void run() {
      relaying = true;

      try {
        while (isRelaying()) {
          readSocket();

          try {
            Thread.sleep(10);
          }
          catch (Exception e) {}
        }
      }
      catch (SocketException e) {
        if (sourceSocket != null && !sourceSocket.isClosed()) //Ignore Socket Close Exceptions
        {
          e.printStackTrace();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }

      sockPair.close();
    }

    public void close() {
      relaying = false;

      if (sourceSocket != null) {
        System.out.println("Closing Relay from: " + sourceSocket.getInetAddress().getHostAddress() + " to: " + destinationSocket.getInetAddress().getHostAddress());
        try {
          sourceSocket.close();
        }
        catch (Exception e) {
          //e.printStackTrace();
        }
      }

      sourceSocket = null;
    }

  } //End SocketReader Inner Class

  public class SocketPair {
    private SocketRelay incoming;
    private SocketRelay outgoing;

    private String destinationHost;
    private int destinationPort;

    private boolean tunnel = false;

    public SocketPair(Socket incomingSocket, String destinationHost, int destinationPort) throws UnknownHostException, IOException {
      this.destinationHost = destinationHost;
      this.destinationPort = destinationPort;

      Socket outgoingSocket = new Socket(this.destinationHost, this.destinationPort);

      incoming = new SocketRelay(this, incomingSocket, outgoingSocket);
      outgoing = new SocketRelay(this, outgoingSocket, incomingSocket);

      tunnel = true;

      Thread t1 = new Thread(incoming);
      Thread t2 = new Thread(outgoing);

      t1.start();
      t2.start();
    }

    public boolean isTunneling() {
      return tunnel;
    }

    public synchronized void close() {
      if (isTunneling()) {
        tunnel = false;

        try {
          incoming.close();
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        try {
          outgoing.close();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

  } //End SocketPair Inner Class

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    int retCode;
    String destinationHost;
    int listeningPort, destinationPort;
    SocketProxy sp = null;
    boolean printData = false;

    if (args.length < 3 || args.length > 4) {
      System.out.println("Usage: java com.roguelogic.net.SocketProxy [LISTENING_PORT] [DESTINATION_HOST] [DESTINATION_PORT] <PRINT_DATA: TRUE|FALSE>");
      retCode = 1;
    }
    else {

      try {

        //Parse Command Line Options
        try {
          listeningPort = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
          System.err.println("Listening Port MUST be an Integer value!");
          throw e;
        }

        destinationHost = args[1].trim();
        if (destinationHost.length() == 0) {
          System.err.println("Destination Host MUST not be blank!");
          throw new Exception("Destination Host NOT provided!");
        }

        try {
          destinationPort = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
          System.err.println("Destination Port MUST be an Integer value!");
          throw e;
        }

        if (args.length >= 4) {
          printData = args[3].equalsIgnoreCase("TRUE");
        }

        //Create Socket Proxy
        sp = new SocketProxy(listeningPort, destinationHost, destinationPort);

        sp.setPrintData(printData);

        //Listen for new Connections
        sp.listen();

        retCode = 0;
      } //End Big Try
      catch (Exception e) {
        retCode = 1;
        e.printStackTrace();
      }

    }

    System.out.println("\nReturn Code: " + retCode);
    System.exit(retCode);
  }

}
