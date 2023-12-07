/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im;

/**
 * @author Administrator
 */

import java.io.IOException;

import com.roguelogic.im.sflap.*;
import com.roguelogic.im.toc.*;

public class AIMClient implements TOCObserver {

  private TOCConnection conn = null;
  private TOCObserver observer;

  public AIMClient() {
    this(null);
  }

  public AIMClient(TOCObserver observer) {
    this.observer = observer;
  }

  public void connect(String host, int port) throws SFLAPFrameException, IMException, IOException {
    conn = new TOCConnection(host, port);

    if (observer != null) {
      conn.addObserver(observer);
    }
    else {
      conn.addObserver(this);
    }

    conn.connect();
  }

  public void login(String authHost, int authPort, String username, String password) throws IMException, IOException {
    conn.login(authHost, authPort, username, password);
  }

  public void addBuddy(String buddyName) throws IOException {
    conn.addBuddy(buddyName);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCObserver#imReceived(com.roguelogic.im.InstantMessage)
   */
  public void imReceived(InstantMessage im) {
    System.out.println(im);

    try {
      conn
          .sendIM(
              im.getSender(),
              "Thank you for your interest in speaking with Robert, however, he is not here at the moment, instead he created his own AIM Client Program to talk to you! Have a nice day :)",
              false);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCObserver#imPunted()
   */
  public void imPunted() {
    System.out.println("We got PUNTED off AIM by the Server!");
    conn.disconnect();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCObserver#imStatusUpdate(com.roguelogic.im.BuddyStatus)
   */
  public void imStatusUpdate(BuddyStatus bs) {
    System.out.println(bs);
  }

  public TOCObserver getObserver() {
    return observer;
  }

  public boolean isConnected() {
    return (conn != null ? conn.isConnected() : false);
  }

  public void signOff() {
    if (conn != null) {
      conn.disconnect();
    }
  }

  public void sendIm(String recipient, String mesg) throws IMException {
    try {
      conn.sendIM(recipient, mesg, false);
    }
    catch (Exception e) {
      throw new IMException("An error occurred while attempting to send IM to: " + recipient, e);
    }
  }

  public static void main(String[] args) throws Exception {
    AIMClient client = new AIMClient();

    client.connect("toc.oscar.aol.com", 9898);
    client.login("login.oscar.aol.com", 5190, args[0], args[1]);
    client.addBuddy("psanglo");
    client.addBuddy("panglo01");
  }

}