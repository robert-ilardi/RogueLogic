package com.roguelogic.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * 
 * @author rilardi
 *
 */
public class RLJavaMailAuthenticator extends Authenticator {
  private PasswordAuthentication auth;

  public RLJavaMailAuthenticator(String user, String password) {
    auth = new PasswordAuthentication(user, password);
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return auth;
  }
}
