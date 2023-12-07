/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.toc;

/**
 * @author Administrator
 */
public class TOCInitDone implements TOCMessage {

  public static final String TOC_COMMAND = "toc_init_done";

  public TOCInitDone() {}

  /* (non-Javadoc)
   * @see com.roguelogic.im.toc.TOCMessage#getTOCMessage()
   */
  public String getTOCMessage() {
    return TOC_COMMAND;
  }

}