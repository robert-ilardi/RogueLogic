/*
 * Created on Mar 18, 2005
 */
package com.roguelogic.im;

import java.util.Date;

/**
 * @author rilardi
 */

public class InstantMessage {

  private String sender;
  private String recipient;
  private String message;
  private boolean auto;
  private Date received;

  public InstantMessage(String sender, String recipient, String message, boolean auto, Date received) {
    this.sender = sender;
    this.recipient = recipient;
    this.message = message;
    this.auto = auto;
    this.received = received;
  }

  /**
   * @return Returns the auto.
   */
  public boolean isAuto() {
    return auto;
  }

  /**
   * @return Returns the message.
   */
  public String getMessage() {
    return message;
  }

  /**
   * @return Returns the recipient.
   */
  public String getRecipient() {
    return recipient;
  }

  /**
   * @return Returns the sender.
   */
  public String getSender() {
    return sender;
  }

  /**
   * @return Returns the recieved.
   */
  public Date getReceived() {
    return received;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("----------Instant Message----------\n");
    sb.append("From: ");
    sb.append(sender);
    sb.append("\n");
    sb.append("To: ");
    sb.append(recipient);
    sb.append("\n");
    sb.append("Message: ");
    sb.append(message);
    sb.append("\n");
    sb.append("Auto Response: ");
    sb.append((auto ? "YES" : "NO"));
    sb.append("\n");
    sb.append("Received At: ");
    sb.append(received);
    sb.append("\n");
    sb.append("-----------------------------------");

    return sb.toString();
  }

}