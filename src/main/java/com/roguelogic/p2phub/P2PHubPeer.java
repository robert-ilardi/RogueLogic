/**
 * Created Sep 16, 2006
 */
package com.roguelogic.p2phub;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class P2PHubPeer implements Serializable {

  public static final int TOKEN_USERNAME_INDEX = 0;
  public static final int TOKEN_PASSWORD_INDEX = 1;

  private String username;
  private String password;

  private String sessionToken;

  public P2PHubPeer() {}

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getSessionToken() {
    return sessionToken;
  }

  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  public static P2PHubPeer Decode(String configLine) {
    P2PHubPeer peer = null;
    String[] tokens;

    if (configLine != null && configLine.trim().length() > 0) {
      configLine = configLine.trim();
      tokens = configLine.split(":", 2);

      if (tokens != null && tokens.length == 2) {
        peer = new P2PHubPeer();
        peer.setUsername(tokens[TOKEN_USERNAME_INDEX]);
        peer.setPassword(tokens[TOKEN_PASSWORD_INDEX]);
      }

    } //End configLine null and length check

    return peer;
  }

}
