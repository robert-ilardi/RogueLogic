package com.roguelogic.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.SystemUtils;

public abstract class RLMail implements Serializable {
  //public static final String TEXT_PLAIN = "text/plain";
  //public static final String TEXT_HTML = "text/html";

  public static final String ISO_8859_1 = "iso-8859-1";
  //public static final String US_ASCII = "us-ascii";
  //public static final String VALUE_SEPARATOR = ",";
  //public static final String NAME_VALUE_SEPARATOR = ":";
  //public static final String MSG_BOUNDARY = "\n*END*\n";

  //public static final String DEFAULT_CHARSET = ISO_8859_1;

  /**
   * X-Priority = 1|2|3|4|5; 1 (Highest), 2 (High), 3 (Normal), 4 (Low), 5 (Lowest). 
   * The default is 3.
   */
  public static final String X_PRIORITY_HEADER = "X-Priority";
  public static final int PRIORITY_HIGHEST = 1;
  public static final int PRIORITY_HIGH = 2;
  public static final int PRIORITY_NORMAL = 3;
  public static final int PRIORITY_LOW = 4;
  public static final int PRIORITY_LOWEST = 5;

  public static final int NUM_OF_RAND_CHAR = 4;

  protected String id;
  protected Map headers = new HashMap();
  protected String subject;
  protected String charset = ISO_8859_1;
  protected InternetAddress from;
  protected List toList = new ArrayList();
  protected List ccList = new ArrayList();
  protected List bccList = new ArrayList();
  protected String message;

  protected Map attachments = new HashMap();
  protected Map attachmentsBins;

  protected List pageTargets = new ArrayList();
  protected String pageMessage;

  protected Properties props;

  public RLMail() {
    id = GenerateMailID();
  }

  /**
   * Generates Mail id by month, day, and a unique int between 0 and 9999.
   * 
   * @return
   */
  public static String GenerateMailID() {
    StringBuffer sb = new StringBuffer();

    long current = System.currentTimeMillis();
    Random random = new Random(current);

    int first = 'a';
    int last = 'z' + 1;
    int domain = last - first;
    for (int i = NUM_OF_RAND_CHAR; i > 0; i--) {
      sb.append((char) (random.nextInt(domain) + first));
    }

    sb.append(Long.toString(current, 36));

    return sb.toString();
  }

  /**
   * Compose email message for direct JavaMail service.
   * 
   * @param session
   * @return
   * @throws EAMMailServiceException
   */
  public abstract MimeMessage compose(Session session) throws RLMailException;

  /**
   * load attachments, inline images, and stack trace of throwables into memory before sending
   * 
   * @throws RLMailException
   */
  public abstract void load() throws RLMailException;

  /**
   * Setter for mail id.
   * 
   * @param id A String
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Getter for mail id.
   * 
   * @return A String value of id
   */
  public String getId() {
    return id;
  }

  /**
   * Specifies the mail headers. Such as, X-Mailer, X-Priority.
   * 
   * @param headers A Map of mail headers in name-value pairs.
   */
  public void setHeaders(Map headers) {
    this.headers = headers;
  }

  /**
   * Getter for the mail headers.
   * 
   * @return A Map with mail headers.
   */
  public Map getHeaders() {
    return headers;
  }

  /**
   * Spcifies one mail header with <code>name</code> as the key and <value> as the value.
   * 
   * @param name A String with the mail header name.
   * @param value A String with the value of the mail header.
   */
  public void addHeader(String name, String value) {
    headers.put(name, value);
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public String getCharset() {
    return charset;
  }

  /**
   * Setter for email subject.
   * 
   * @param subject A String with email subject.
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Getter for email subject.
   * 
   * @return A String with email subject.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Overloads Setter for sender address.
   * 
   * @param from A String format of sender's address.
   */
  /* public void setFrom(String from) {    
   try{
   this.from = EAMMailHelper.createInternetAddress(from);
   }catch(FeedFlingException se){
   this.from = null;
   }
   }*/

  /**
   * Setter for sender address.
   * 
   * @param from A InternetAddress with sender's address.
   */
  public void setFrom(InternetAddress from) {
    this.from = from;
  }

  /**
   * Getter for sender address.
   * 
   * @return A InternetAddress with sender's address.
   */
  public InternetAddress getFrom() {
    return from;
  }

  /**
   * Setter for the list of mail receivers.
   * 
   * @param toList A List with the list of mail receivers.
   */
  public void setToList(List toList) {
    this.toList = toList;
  }

  /**
   * Getter for the list of mail receivers.
   * 
   * @return A List with the list of mail receivers.
   */
  public List getToList() {
    return toList;
  }

  /**
   * Adds one receiver by his/her email address.
   * 
   * @param to A InternetAddress with receiver's address.
   */
  public void addTo(InternetAddress to) {
    toList.add(to);
  }

  /**
   * Setter for the list of cc receivers.
   * 
   * @param ccList A List with the list of cc receivers.
   */
  public void setCcList(List ccList) {
    this.ccList = ccList;
  }

  /**
   * Getter for the list of cc receivers.
   * 
   * @return A List with the list of cc receivers.
   */
  public List getCcList() {
    return ccList;
  }

  /**
   * Adds one cc receiver by his/her email address.
   * 
   * @param cc InternetAddress with cc receiver's address.
   */
  public void addCc(InternetAddress cc) {
    ccList.add(cc);
  }

  /**
   * Setter for the list of bcc receivers.
   * 
   * @param bccList A List with the list of bcc receivers.
   */
  public void setBccList(List bccList) {
    this.bccList = bccList;
  }

  /**
   * Getter for the list of bcc receivers.
   * 
   * @return A List with the list of bcc receivers.
   */
  public List getBccList() {
    return bccList;
  }

  /**
   * Adds one bcc receiver by his/her email address.
   * 
   * @param bcc A InternetAddress with bcc receiver's email address.
   */
  public void addBcc(InternetAddress bcc) {
    bccList.add(bcc);
  }

  /**
   * Setter for the message text.
   * 
   * @param message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Getter for the message text.
   * 
   * @return A String with the text to be sent.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Setter for page messgae
   * 
   * @param pageMessage A String with page message.
   */
  public void setPageMessage(String pageMessage) {
    this.pageMessage = pageMessage;
  }

  /**
   * Getter for page message
   * 
   * @return A String with page message
   */
  public String getPageMessage() {
    return pageMessage;
  }

  /**
   * Setter for the map of attachments.
   * 
   * @param attachmentList A List with the list of file names of attachments
   */
  /*public void setAttachments(Map attachments) {
   this.attachments = attachments;
   }*/

  /**
   * Getter for the map of attachments.
   * 
   * @return A Map with the list of attachment ids and file names of attachments
   */
  public Map getAttachments() {
    return attachments;
  }

  /**
   * Adds one attachment by its attach id and file name.
   * 
   * @param aid A unique string with attachment id.
   * @param filename A String with the file name of an attachment.
   */
  public void addAttachment(String aid, String filename) {
    String ext = FilenameUtils.GetFileExt(filename);

    if (ext != null) {
      attachments.put(aid + "." + ext, filename);
    }
    else {
      attachments.put(aid, filename);
    }
  }

  public Map getAttachmentsBins() {
    return attachmentsBins;
  }

  protected void loadAttachments() throws RLMailException {
    try {
      if (attachments.size() > 0) {
        attachmentsBins = new HashMap();

        Iterator itr = attachments.keySet().iterator();
        String aid = null;
        String temp = null;

        while (itr.hasNext()) {
          aid = (String) itr.next();
          temp = (String) attachments.get(aid);

          attachmentsBins.put(aid, SystemUtils.LoadDataFromFile(temp));
        }
      }
    }
    catch (Exception e) {
      throw new RLMailException(e);
    }
  }

  public List getPageTargets() {
    return pageTargets;
  }

  public void setPageTargets(List pageTargets) {
    this.pageTargets = pageTargets;
  }

  public void addPageTarget(String target) {
    pageTargets.add(target);
  }

  public void setProperties(Properties props) {
    this.props = props;
  }

}
