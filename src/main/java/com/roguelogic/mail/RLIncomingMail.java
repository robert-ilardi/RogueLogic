/**
 * Created Nov 24, 2008
 */
package com.roguelogic.mail;

import java.io.Serializable;
import java.util.Date;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class RLIncomingMail implements Serializable {

  private String id;
  private String mesg;
  private String subject;
  private String from;
  private Date sentTs;
  private Date receivedTs;
  private String[] recipients;

  public RLIncomingMail() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMesg() {
    return mesg;
  }

  public void setMesg(String mesg) {
    this.mesg = mesg;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public Date getSentTs() {
    return sentTs;
  }

  public void setSentTs(Date sentTs) {
    this.sentTs = sentTs;
  }

  public String[] getRecipients() {
    return recipients;
  }

  public void setRecipients(String[] recipients) {
    this.recipients = recipients;
  }

  public Date getReceivedTs() {
    return receivedTs;
  }

  public void setReceivedTs(Date receivedTs) {
    this.receivedTs = receivedTs;
  }

  public String toString(boolean full) {
    StringBuffer sb = new StringBuffer();

    sb.append("Subject: ");
    sb.append(subject);
    sb.append("\n");

    sb.append("From: ");
    sb.append(from);
    sb.append("\n");

    sb.append("Recipients: ");
    if (recipients != null) {
      sb.append(StringUtils.CombineWithDelimiter(recipients, ", ", recipients.length, false, false));
    }
    sb.append("\n");

    sb.append("MessageId: ");
    sb.append(id);
    sb.append("\n");

    sb.append("Sent: ");
    sb.append(sentTs);
    sb.append("\n");

    sb.append("Received: ");
    sb.append(receivedTs);
    sb.append("\n");

    if (full) {
      sb.append("Message: ");
      sb.append(mesg);
      sb.append("\n");
    }

    return sb.toString();
  }

  public String toString() {
    return toString(true);
  }

  public String toShortString() {
    return toString(false);
  }

}
