/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.toc;

/**
 * @author Administrator
 */

public class TOCSendIM implements TOCMessage {

  public static final String TOC_COMMAND = "toc_send_im";

  private String recipient;
  private String imMesg;
  private boolean auto;

  public TOCSendIM(String recipient, String imMesg) {
    this(recipient, imMesg, false);
  }

  public TOCSendIM(String recipient, String imMesg, boolean auto) {
    this.recipient = recipient;
    this.imMesg = imMesg;
    this.auto = auto;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCMessage#getTOCMessage()
   */
  public String getTOCMessage() {
    StringBuffer mesg = new StringBuffer();

    mesg.append(TOC_COMMAND);
    mesg.append(" ");
    mesg.append(recipient);
    mesg.append(" ");
    mesg.append(AOLUtils.EncodeText(imMesg));
    if (auto) {
      mesg.append(" auto");
    }

    return mesg.toString();
  }

}