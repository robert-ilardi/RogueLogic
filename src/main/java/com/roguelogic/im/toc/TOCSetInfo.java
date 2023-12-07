/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.toc;

/**
 * @author Administrator
 */

public class TOCSetInfo implements TOCMessage {

  public static final String TOC_COMMAND = "toc_set_info";

  private String userInfo;

  public TOCSetInfo(String userInfo) {
    this.userInfo = userInfo;
  }

  public TOCSetInfo() {
    userInfo = "";
  }

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCMessage#getTOCMessage()
   */
  public String getTOCMessage() {
    StringBuffer mesg = new StringBuffer();

    mesg.append(TOC_COMMAND);
    mesg.append(" ");
    if (userInfo != null && userInfo.trim().length() > 0) {
      mesg.append(AOLUtils.EncodeText(userInfo.trim() + "\n\n" + TOCConstants.TOC_DEFAULT_INFO));
    }
    else {
      mesg.append(AOLUtils.EncodeText(TOCConstants.TOC_DEFAULT_INFO));
    }

    return mesg.toString();
  }

}