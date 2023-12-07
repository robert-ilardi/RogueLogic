package com.roguelogic.mail;

import java.util.Iterator;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * 
 * @author rilardi
 *
 */
public class RLTextMail extends RLMail {

  /**
   * Composes <code>MimeMessage</code> for direct JavaMail service.
   * 
   */
  public MimeMessage compose(Session session) throws RLMailException {
    MimeMessage mime = new MimeMessage(session);

    try {
      if (charset == null || charset.length() == 0) {
        mime.setSubject(subject);
      }
      else {
        mime.setSubject(subject, charset);
      }

      if (attachments.size() > 0) {
        buildMultipart(mime);
      }
      else {
        mime.setText(message);
      }

      if (from == null) {
        throw new RLMailException("Sender Address Must Be Provided!");
      }
      mime.setFrom(from);

      int numOfRec = 0;

      if (toList != null && toList.size() > 0) {
        numOfRec += toList.size();
        mime.setRecipients(Message.RecipientType.TO, (InternetAddress[]) toList.toArray(new InternetAddress[toList.size()]));
      }

      if (ccList != null && ccList.size() > 0) {
        numOfRec += ccList.size();
        mime.setRecipients(Message.RecipientType.CC, (InternetAddress[]) ccList.toArray(new InternetAddress[ccList.size()]));
      }

      if (bccList != null && bccList.size() > 0) {
        numOfRec += bccList.size();
        mime.setRecipients(Message.RecipientType.BCC, (InternetAddress[]) bccList.toArray(new InternetAddress[bccList.size()]));
      }

      if (numOfRec == 0) {
        //throw new FeedFlingException("Must Specify At Least One Receiver!");
        mime.setRecipient(Message.RecipientType.TO, new InternetAddress(props.getProperty(RLJavaMailService.MAIL_TO)));
      }

      if (headers != null && headers.size() > 0) {
        Iterator keys = headers.keySet().iterator();
        while (keys.hasNext()) {
          String key = (String) keys.next();
          String value = (String) headers.get(key);
          mime.addHeader(key, value);
        }
      }
    }
    catch (MessagingException me) {
      throw new RLMailException(me);
    }

    return mime;
  }

  public void load() throws RLMailException {
    loadAttachments();
  }

  private void buildMultipart(MimeMessage mime) throws RLMailException {
    MimeMultipart multipart = new MimeMultipart();

    try {
      BodyPart part = new MimeBodyPart();
      part.setText(message);
      multipart.addBodyPart(part);

      DataSource ds = null;
      String aid = null;
      String attach = null;
      byte[] bin = null;
      Iterator itr = attachments.keySet().iterator();
      while (itr.hasNext()) {
        aid = (String) itr.next();
        attach = (String) attachments.get(aid);

        if (attachmentsBins != null) {
          bin = (byte[]) attachmentsBins.get(aid);
        }

        if (bin != null && bin.length > 0) {
          ds = new ByteArrayDataSource(bin, null);
        }
        else {
          ds = new FileDataSource(attach);
        }

        part = new MimeBodyPart();
        part.setDataHandler(new DataHandler(ds));
        part.setFileName(attach);
        multipart.addBodyPart(part);
      }

      mime.setContent(multipart);
    }
    catch (Exception e) {
      throw new RLMailException(e);
    }
  }

}
