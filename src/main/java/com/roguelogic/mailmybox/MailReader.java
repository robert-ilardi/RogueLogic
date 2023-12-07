/**
 * Created: Oct 23, 2008 
 */
package com.roguelogic.mailmybox;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import com.roguelogic.mail.RLIncomingMail;
import com.roguelogic.mail.RLJavaMailService;
import com.roguelogic.mail.RLMailException;

/**
 * @author Robert C. Ilardi
 * 
 */

public class MailReader {

  public static final String PROP_READER_METHOD = "EmailServiceReaderMethod";

  public static final String PROP_POP3_HOST = "EmailServicePop3Host";
  public static final String PROP_POP3_PORT = "EmailServicePop3Port";
  public static final String PROP_POP3_USER = "EmailServicePop3User";
  public static final String PROP_POP3_PASSWD = "EmailServicePop3Passwd";

  public static final String PROP_IMAP_HOST = "EmailServiceImapHost";
  public static final String PROP_IMAP_PORT = "EmailServiceImapPort";
  public static final String PROP_IMAP_USER = "EmailServiceImapUser";
  public static final String PROP_IMAP_PASSWD = "EmailServiceImapPasswd";
  public static final String PROP_IMAP_START_TLS = "EmailServiceImapStartTLS";

  public static final String PROP_READER_USE_SECURE_PROTOCOL = "EmailServiceReaderUseSecureProtocol";

  public static final int READER_MODE_POP3 = 0;
  public static final int READER_MODE_IMAP = 1;

  public static final String READER_METHOD_POP3_STR = "POP3";
  public static final String READER_METHOD_IMAP_STR = "IMAP";

  private int readerMethod;

  private String host;
  private int port;
  private String user;
  private String passwd;
  private boolean startTls;
  private boolean useSecureProtocol;

  private MailMyBoxDaemon daemon;

  private Properties jMailProps;

  public MailReader(MailMyBoxDaemon daemon) throws MMBException {
    this.daemon = daemon;

    readConfiguration();
  }

  private void readConfiguration() throws MMBException {
    String tmp;

    tmp = daemon.getProperty(PROP_READER_METHOD);

    if (READER_METHOD_POP3_STR.equalsIgnoreCase(tmp)) {
      readerMethod = READER_MODE_POP3;

      readPop3Config();
    }
    else if (READER_METHOD_IMAP_STR.equalsIgnoreCase(tmp)) {
      readerMethod = READER_MODE_IMAP;

      readImapConfig();
    }
    else {
      throw new MMBException("Invalid Mail Reader Method! Check Configuration for Property = 'EmailServiceReaderMethod'. Can be either POP3 or IMAP.");
    }

    tmp = daemon.getProperty(PROP_READER_USE_SECURE_PROTOCOL);
    useSecureProtocol = "TRUE".equalsIgnoreCase(tmp);

    createJMailProperties();
  }

  private void readPop3Config() {
    host = daemon.getProperty(PROP_POP3_HOST);
    port = Integer.parseInt(daemon.getProperty(PROP_POP3_PORT));
    user = daemon.getProperty(PROP_POP3_USER);
    passwd = daemon.getProperty(PROP_POP3_PASSWD);
  }

  private void readImapConfig() {
    host = daemon.getProperty(PROP_IMAP_HOST);
    port = Integer.parseInt(daemon.getProperty(PROP_IMAP_PORT));
    user = daemon.getProperty(PROP_IMAP_USER);
    passwd = daemon.getProperty(PROP_IMAP_PASSWD);
    startTls = "TRUE".equalsIgnoreCase(daemon.getProperty(PROP_IMAP_START_TLS));
  }

  public int getReaderMethod() {
    return readerMethod;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getUser() {
    return user;
  }

  private void createJMailProperties() {
    switch (readerMethod) {
      case READER_MODE_POP3:
        jMailProps = createPop3JMailProps();
        break;
      case READER_MODE_IMAP:
        jMailProps = createImapJMailProps();
        break;
    }

    jMailProps.setProperty(RLJavaMailService.RL_MAIL_USE_SECURE_PROTOCOL, useSecureProtocol ? "TRUE" : "FALSE");
  }

  private Properties createPop3JMailProps() {
    Properties props = new Properties();

    props.setProperty(RLJavaMailService.MAIL_POP3_HOST, host);
    props.setProperty(RLJavaMailService.MAIL_POP3_PORT, String.valueOf(port));
    props.setProperty(RLJavaMailService.MAIL_POP3_USER, user);
    props.setProperty(RLJavaMailService.MAIL_POP3_PASSWORD, passwd);

    return props;
  }

  private Properties createImapJMailProps() {
    Properties props = new Properties();

    props.setProperty(RLJavaMailService.MAIL_IMAP_HOST, host);
    props.setProperty(RLJavaMailService.MAIL_IMAP_PORT, String.valueOf(port));
    props.setProperty(RLJavaMailService.MAIL_IMAP_USER, user);
    props.setProperty(RLJavaMailService.MAIL_IMAP_PASSWORD, passwd);

    if (startTls) {
      props.setProperty(RLJavaMailService.MAIL_IMAP_STARTTLS, "TRUE");
    }

    return props;
  }

  public synchronized ArrayList<RLIncomingMail> retrieveMessageHeadersFromServer() throws RLMailException {
    ArrayList<RLIncomingMail> mesgs;

    switch (readerMethod) {
      case READER_MODE_POP3:
        mesgs = retrieveMessageHeadersViaPop3();
        break;
      case READER_MODE_IMAP:
        mesgs = retrieveMessageHeadersViaImap();
        break;
      default:
        mesgs = null;
    }

    return mesgs;
  }

  private ArrayList<RLIncomingMail> retrieveMessageHeadersViaPop3() throws RLMailException {
    ArrayList<RLIncomingMail> mesgs;
    RLJavaMailService mailService;

    mailService = new RLJavaMailService(jMailProps);

    mesgs = mailService.readPop3EmailHeaders();

    return mesgs;
  }

  private ArrayList<RLIncomingMail> retrieveMessageHeadersViaImap() throws RLMailException {
    ArrayList<RLIncomingMail> mesgs = null;
    RLJavaMailService mailService;

    mailService = new RLJavaMailService(jMailProps);
    mesgs = mailService.readImapEmailHeaders();

    return mesgs;
  }

  public synchronized void close() {}

  public ArrayList<RLIncomingMail> retrieveNewMessages(Date lastMesgDt) throws RLMailException {
    ArrayList<RLIncomingMail> mesgs;

    switch (readerMethod) {
      case READER_MODE_POP3:
        mesgs = retrieveNewMessagesViaPop3(lastMesgDt);
        break;
      case READER_MODE_IMAP:
        mesgs = retrieveNewMessagesViaImap(lastMesgDt);
        break;
      default:
        mesgs = null;
    }

    return mesgs;
  }

  private ArrayList<RLIncomingMail> retrieveNewMessagesViaPop3(Date lastMesgDt) throws RLMailException {
    ArrayList<RLIncomingMail> mesgs;
    RLJavaMailService mailService;

    mailService = new RLJavaMailService(jMailProps);

    mesgs = mailService.readNewPop3Emails(lastMesgDt);

    return mesgs;
  }

  private ArrayList<RLIncomingMail> retrieveNewMessagesViaImap(Date lastMesgDt) throws RLMailException {
    ArrayList<RLIncomingMail> mesgs;
    RLJavaMailService mailService;

    mailService = new RLJavaMailService(jMailProps);
    mesgs = mailService.readNewImapEmails(lastMesgDt);

    return mesgs;
  }

}
