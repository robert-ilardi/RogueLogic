/**
 * Created Feb 27, 2008
 */
package com.roguelogic.mail;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.internet.InternetAddress;

/**
 * @author Robert C. Ilardi
 * 
 */

public class EmailTest {

  public EmailTest() {}

  public static void main(String[] args) throws Exception {
    RLJavaMailService mailService;
    Properties props;
    RLTextMail email;
    ArrayList<InternetAddress> toList;

    props = new Properties();
    props.setProperty(RLJavaMailService.MAIL_SMTP_HOST, "smtp.gmail.com");
    props.setProperty(RLJavaMailService.MAIL_SMTP_PORT, "587");
    props.setProperty(RLJavaMailService.MAIL_SMTP_USER, "rilardi@gmail.com");
    props.setProperty(RLJavaMailService.MAIL_SMTP_PASSWORD, "sassi1822");

    // props.setProperty(RLJavaMailService.MAIL_SMTP_HOST, "smtp.bizmail.yahoo.com");
    // props.setProperty(FFJavaMailService.MAIL_SMTP_PORT, "465");

    // props.setProperty(RLJavaMailService.MAIL_SMTP_USER, "admin@roguelogic.com");
    // props.setProperty(RLJavaMailService.MAIL_PASSWORD, "");

    props.setProperty(RLJavaMailService.MAIL_SMTP_AUTH, "TRUE");
    props.setProperty(RLJavaMailService.MAIL_SMTP_STARTTLS, "TRUE");

    props.setProperty(RLJavaMailService.MAIL_DEBUG, "TRUE");

    props.setProperty(RLJavaMailService.RL_MAIL_IMAP_FOLDER_URL, "rlmail.folder.url");

    props.setProperty(RLJavaMailService.MAIL_POP3_HOST, "pop.gmail.com");
    props.setProperty(RLJavaMailService.MAIL_POP3_PORT, "995");
    props.setProperty(RLJavaMailService.MAIL_POP3_USER, "rilardi@gmail.com");
    props.setProperty(RLJavaMailService.MAIL_POP3_PASSWORD, "sassi1822");

    props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

    mailService = new RLJavaMailService(props);

    mailService.readPop3EmailHeaders();

    System.exit(0);

    email = new RLTextMail();
    email.setFrom(new InternetAddress("rilardi@gmail.com"));

    toList = new ArrayList<InternetAddress>();
    toList.add(new InternetAddress("rilardi@gmail.com"));
    email.setToList(toList);

    email.setSubject("Test SMTP Email using JavaMail...");
    email.setMessage("Hello World!\nThis is just a test to verify connectivity to RogueLogic SMTP from a JavaMail client!\nLet me know if you received this!\n\nThanks!\n-R");

    mailService.send(email);

    //mailService.readImapEmailHeaders();
  }

}
