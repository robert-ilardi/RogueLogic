/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.toc;

/**
 * @author rilardi
 */

public class TOCSignon implements TOCMessage {

  public static final String TOC_COMMAND = "toc_signon";

  private String host;
  private int port;
  private String username;
  private String password;

  public TOCSignon(String host, int port, String username, String password) {
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.toc.TOCMessage#getTOCMessage()
   */
  public String getTOCMessage() {
    StringBuffer mesg = new StringBuffer();

    mesg.append(TOC_COMMAND);
    mesg.append(" ");
    mesg.append(host);
    mesg.append(" ");
    mesg.append(port);
    mesg.append(" ");
    mesg.append(AOLUtils.FormatUsername(username));
    mesg.append(" ");
    mesg.append(AOLUtils.Roast(password));
    mesg.append(" ");
    mesg.append(TOCConstants.TOC_DEFAULT_LANGUAGE);
    mesg.append(" ");
    mesg.append(TOCConstants.TOC_CLIENT_NAME);

    return mesg.toString();
  }

}