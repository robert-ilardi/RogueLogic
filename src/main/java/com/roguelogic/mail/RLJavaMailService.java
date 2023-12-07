/**
 * Created Feb 27, 2008
 */
package com.roguelogic.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class RLJavaMailService {

  public static final String MAIL_TRANSPORT_PROTO = "mail.transport.protocol";
  public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
  public static final String MAIL_SMTP_HOST = "mail.smtp.host";
  public static final String MAIL_SMTP_PORT = "mail.smtp.port";
  public static final String MAIL_SMTP_USER = "mail.smtp.user";
  public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
  public static final String MAIL_FROM = "mail.from";
  public static final String MAIL_TO = "mail.to";
  public static final String MAIL_DEBUG = "mail.debug";
  public static final String MAIL_SMTP_STARTTLS = "mail.smtp.starttls.enable";

  public static final String MAIL_IMAP_HOST = "mail.imap.host";
  public static final String MAIL_IMAP_PORT = "mail.imap.port";
  public static final String MAIL_IMAP_USER = "mail.imap.user";
  public static final String MAIL_IMAP_PASSWORD = "mail.imap.password";
  public static final String MAIL_IMAP_STARTTLS = "mail.imap.starttls.enable";

  public static final String RL_MAIL_IMAP_FOLDER_URL = "rlmail.imap.folder.url";
  public static final String RL_MAIL_USE_SECURE_PROTOCOL = "rlmail.use.secure.protocol";

  public static final String MAIL_POP3_HOST = "mail.pop3.host";
  public static final String MAIL_POP3_PORT = "mail.pop3.port";
  public static final String MAIL_POP3_USER = "mail.pop3.user";
  public static final String MAIL_POP3_PASSWORD = "mail.pop3.password";

  public static final String MAIL_IMAP_SOCK_FACTORY = "mail.imap.socketFactory.class";
  public static final String MAIL_POP3_SOCK_FACTORY = "mail.pop3.socketFactory.class";

  public static final String SSL_SOCKET_FACTORY_CLASS = "javax.net.ssl.SSLSocketFactory";

  private Properties mailProps;

  public RLJavaMailService(Properties mailProps) {
    this.mailProps = mailProps;
  }

  public void send(RLMail email) throws RLMailException {
    try {
      email.load();
      MimeMessage mesg = email.compose(getSendMailSession());
      Transport.send(mesg);
    }
    catch (MessagingException me) {
      throw new RLMailException("Transport Failure!", me);
    }
  }

  private Session getSendMailSession() throws RLMailException {
    String hostName = mailProps.getProperty(MAIL_SMTP_HOST);
    if (hostName == null || hostName.length() == 0) {
      throw new RLMailException("Mail Hostname Not Configured!");
    }

    String needAuth = mailProps.getProperty(MAIL_SMTP_AUTH);
    Authenticator authenticator = null;
    if (needAuth != null && needAuth.equalsIgnoreCase("true")) {
      String user = mailProps.getProperty(MAIL_SMTP_USER);
      if (user == null || user.length() == 0) {
        throw new RLMailException("Mail User Must Be Specified!");
      }

      authenticator = new RLJavaMailAuthenticator(user, mailProps.getProperty(MAIL_SMTP_PASSWORD));
    }

    return Session.getInstance(mailProps, authenticator);
  }

  private Session getImapMailSession() throws RLMailException {
    Authenticator authenticator = null;
    String user = mailProps.getProperty(MAIL_IMAP_USER);
    authenticator = new RLJavaMailAuthenticator(user, mailProps.getProperty(MAIL_IMAP_PASSWORD));

    return Session.getInstance(mailProps, authenticator);
  }

  private Session getPop3MailSession() throws RLMailException {
    Authenticator authenticator = null;
    String user = mailProps.getProperty(MAIL_POP3_USER);
    authenticator = new RLJavaMailAuthenticator(user, mailProps.getProperty(MAIL_POP3_PASSWORD));

    return Session.getInstance(mailProps, authenticator);
  }

  private ArrayList<RLIncomingMail> createIncomingMails(Message[] mesgs) throws MessagingException, IOException {
    ArrayList<RLIncomingMail> mails = null;
    RLIncomingMail mail;
    String[] tmpArr;
    Address[] addrArr;

    mails = new ArrayList<RLIncomingMail>();

    for (int i = mesgs.length - 1; i >= 0; i--) {
      // System.out.println(i + ": " + mesgs[i].getFrom()[0] + "\t" + mesgs[i].getSubject());
      mail = new RLIncomingMail();
      mails.add(mail);

      tmpArr = mesgs[i].getHeader("Message-ID");
      if (tmpArr != null && tmpArr.length > 0) {
        mail.setId(StringUtils.CombineWithDelimiter(tmpArr, "|", tmpArr.length, false, false));
      }

      mail.setSubject(mesgs[i].getSubject());

      if (mesgs[i].getFrom() != null && mesgs[i].getFrom().length > 0) {
        if (mesgs[i].getFrom()[0] instanceof InternetAddress) {
          mail.setFrom(((InternetAddress) mesgs[i].getFrom()[0]).getAddress());
        }
        else {
          mail.setFrom(mesgs[i].getFrom()[0].toString());
        }
      }

      mail.setSentTs(mesgs[i].getSentDate());
      mail.setReceivedTs(mesgs[i].getReceivedDate());

      addrArr = mesgs[i].getAllRecipients();

      if (addrArr != null) {
        tmpArr = new String[addrArr.length];

        for (int j = 0; j < addrArr.length; j++) {
          if (addrArr[j] instanceof InternetAddress) {
            tmpArr[j] = ((InternetAddress) addrArr[j]).getAddress();
          }
          else {
            tmpArr[j] = addrArr[j].toString();
          }
        }

        mail.setRecipients(tmpArr);
      }

      mail.setMesg(getText(mesgs[i].getContent()));

      //System.out.println("Message " + i + " of " + mesgs.length + " - ");
      //System.out.println(mail);
      //System.out.println("-------------------------------------------");
    } // End for i loop through messages

    return mails;
  }

  private String getText(Object content) throws MessagingException, IOException {
    String tmp, body = null;
    Part p;
    Multipart mp;

    if (content instanceof String) {
      body = (String) content;
    }
    else if (content instanceof Part) {
      p = (Part) content;

      if (p.isMimeType("text/*")) {
        body = (String) p.getContent();
      }
      else if (p.isMimeType("multipart/*")) {
        mp = (Multipart) p.getContent();
        body = getText(mp);
      }
    }
    else if (content instanceof Multipart) {
      mp = (Multipart) content;

      for (int i = 0; i < mp.getCount(); i++) {
        tmp = getText(mp.getBodyPart(i));

        if (tmp != null) {
          body = tmp;
          break;
        }
      }
    }

    return body;
  }

  private Message[] getMessagesAfterDate(Message[] allMesgs, Date lastMesgDt) throws MessagingException {
    Message[] newMesgs;
    ArrayList<Message> al;
    Date mDt;

    if (lastMesgDt == null) {
      newMesgs = allMesgs;
    }
    else {
      al = new ArrayList<Message>();

      for (int i = allMesgs.length - 1; i >= 0; i--) {
        mDt = allMesgs[i].getReceivedDate();

        if (mDt == null || mDt.compareTo(lastMesgDt) >= 0) {
          al.add(allMesgs[i]);
        }
        else {
          break;
        }
      }

      newMesgs = new Message[al.size()];
      newMesgs = al.toArray(newMesgs);
    }

    return newMesgs;
  }

  public ArrayList<RLIncomingMail> readImapEmailHeaders() throws RLMailException {
    ArrayList<RLIncomingMail> mails = null;
    Session session;
    Store store = null;
    Folder folder = null;
    Message[] mesgs;

    try {
      session = getImapMailSession();

      if ("TRUE".equalsIgnoreCase(mailProps.getProperty(RL_MAIL_USE_SECURE_PROTOCOL))) {
        store = session.getStore("imaps");
        store.connect(mailProps.getProperty(MAIL_IMAP_HOST), mailProps.getProperty(MAIL_IMAP_USER), mailProps.getProperty(MAIL_IMAP_PASSWORD));
      }
      else {
        store = session.getStore("imap");
        store.connect();
      }

      folder = store.getFolder("INBOX");

      folder.open(Folder.READ_ONLY);

      // System.out.println("MessageCnt: " + folder.getMessageCount());

      mesgs = folder.getMessages();

      mails = createIncomingMailHeaders(mesgs);
    } // End try block
    catch (Exception e) {
      throw new RLMailException("An error occurred while attempting to retrieve email: " + e.getMessage(), e);
    }
    finally {
      if (folder != null) {
        try {
          folder.close(false);
        }
        catch (Exception e) {}
      }

      if (store != null) {
        try {
          store.close();
        }
        catch (Exception e) {}
      }
    }

    return mails;
  }

  public ArrayList<RLIncomingMail> readPop3EmailHeaders() throws RLMailException {
    ArrayList<RLIncomingMail> mails = null;
    Session session;
    Store store = null;
    Folder folder = null;
    Message[] mesgs;

    try {
      session = getPop3MailSession();

      if ("TRUE".equalsIgnoreCase(mailProps.getProperty(RL_MAIL_USE_SECURE_PROTOCOL))) {
        store = session.getStore("pop3s");
        store.connect(mailProps.getProperty(MAIL_POP3_HOST), mailProps.getProperty(MAIL_POP3_USER), mailProps.getProperty(MAIL_POP3_PASSWORD));
      }
      else {
        store = session.getStore("pop3");
        store.connect();
      }

      folder = store.getFolder("INBOX");

      folder.open(Folder.READ_ONLY);

      // System.out.println("MessageCnt: " + folder.getMessageCount());

      mesgs = folder.getMessages();

      mails = createIncomingMailHeaders(mesgs);
    } // End try block
    catch (Exception e) {
      throw new RLMailException("An error occurred while attempting to retrieve email: " + e.getMessage(), e);
    }
    finally {
      if (folder != null) {
        try {
          folder.close(false);
        }
        catch (Exception e) {}
      }

      if (store != null) {
        try {
          store.close();
        }
        catch (Exception e) {}
      }
    }

    return mails;
  }

  private ArrayList<RLIncomingMail> createIncomingMailHeaders(Message[] mesgs) throws MessagingException {
    ArrayList<RLIncomingMail> mails = null;
    RLIncomingMail mail;
    String[] tmpArr;
    Address[] addrArr;

    mails = new ArrayList<RLIncomingMail>();

    for (int i = mesgs.length - 1; i >= 0; i--) {
      // System.out.println(i + ": " + mesgs[i].getFrom()[0] + "\t" + mesgs[i].getSubject());
      mail = new RLIncomingMail();
      mails.add(mail);

      tmpArr = mesgs[i].getHeader("Message-ID");
      if (tmpArr != null && tmpArr.length > 0) {
        mail.setId(StringUtils.CombineWithDelimiter(tmpArr, "|", tmpArr.length, false, false));
      }

      mail.setSubject(mesgs[i].getSubject());

      if (mesgs[i].getFrom() != null && mesgs[i].getFrom().length > 0) {
        if (mesgs[i].getFrom()[0] instanceof InternetAddress) {
          mail.setFrom(((InternetAddress) mesgs[i].getFrom()[0]).getAddress());
        }
        else {
          mail.setFrom(mesgs[i].getFrom()[0].toString());
        }
      }

      mail.setSentTs(mesgs[i].getSentDate());
      mail.setReceivedTs(mesgs[i].getReceivedDate());

      addrArr = mesgs[i].getAllRecipients();

      if (addrArr != null) {
        tmpArr = new String[addrArr.length];

        for (int j = 0; j < addrArr.length; j++) {
          if (addrArr[j] instanceof InternetAddress) {
            tmpArr[j] = ((InternetAddress) addrArr[j]).getAddress();
          }
          else {
            tmpArr[j] = addrArr[j].toString();
          }
        }

        mail.setRecipients(tmpArr);
      }

      //System.out.println("Message " + i + " of " + mesgs.length + " - ");
      //System.out.println(mail);
      //System.out.println("-------------------------------------------");
    } // End for i loop through messages

    return mails;
  }

  public ArrayList<RLIncomingMail> readNewImapEmails(Date lastMesgDt) throws RLMailException {
    ArrayList<RLIncomingMail> mails = null;
    Session session;
    Store store = null;
    Folder folder = null;
    Message[] mesgs;

    try {
      session = getImapMailSession();

      if ("TRUE".equalsIgnoreCase(mailProps.getProperty(RL_MAIL_USE_SECURE_PROTOCOL))) {
        store = session.getStore("imaps");
        store.connect(mailProps.getProperty(MAIL_IMAP_HOST), mailProps.getProperty(MAIL_IMAP_USER), mailProps.getProperty(MAIL_IMAP_PASSWORD));
      }
      else {
        store = session.getStore("imap");
        store.connect();
      }

      folder = store.getFolder("INBOX");

      folder.open(Folder.READ_ONLY);

      // System.out.println("MessageCnt: " + folder.getMessageCount());

      mesgs = folder.getMessages();

      mesgs = getMessagesAfterDate(mesgs, lastMesgDt);

      mails = createIncomingMails(mesgs);
    } // End try block
    catch (Exception e) {
      throw new RLMailException("An error occurred while attempting to retrieve email: " + e.getMessage(), e);
    }
    finally {
      if (folder != null) {
        try {
          folder.close(false);
        }
        catch (Exception e) {}
      }

      if (store != null) {
        try {
          store.close();
        }
        catch (Exception e) {}
      }
    }

    return mails;
  }

  public ArrayList<RLIncomingMail> readNewPop3Emails(Date lastMesgDt) throws RLMailException {
    ArrayList<RLIncomingMail> mails = null;
    Session session;
    Store store = null;
    Folder folder = null;
    Message[] mesgs;

    try {
      session = getPop3MailSession();

      if ("TRUE".equalsIgnoreCase(mailProps.getProperty(RL_MAIL_USE_SECURE_PROTOCOL))) {
        store = session.getStore("pop3s");
        store.connect(mailProps.getProperty(MAIL_POP3_HOST), mailProps.getProperty(MAIL_POP3_USER), mailProps.getProperty(MAIL_POP3_PASSWORD));
      }
      else {
        store = session.getStore("pop3");
        store.connect();
      }

      folder = store.getFolder("INBOX");

      folder.open(Folder.READ_ONLY);

      mesgs = folder.getMessages();

      mesgs = getMessagesAfterDate(mesgs, lastMesgDt);

      mails = createIncomingMails(mesgs);
    } // End try block
    catch (Exception e) {
      throw new RLMailException("An error occurred while attempting to retrieve email: " + e.getMessage(), e);
    }
    finally {
      if (folder != null) {
        try {
          folder.close(false);
        }
        catch (Exception e) {}
      }

      if (store != null) {
        try {
          store.close();
        }
        catch (Exception e) {}
      }
    }

    return mails;
  }

}
