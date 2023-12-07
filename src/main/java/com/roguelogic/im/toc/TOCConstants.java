/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.toc;

/**
 * @author rilardi
 */

public class TOCConstants {
  public static final String TOC_VERSION = "0.9";
  public static final String TOC_SITE_URL = "http://www.roguelogic.com";
  public static final String TOC_CLIENT_NAME = "rltoc0.9";
  public static final String TOC_DEFAULT_LANGUAGE = "english";
  public static final String TOC_DEFAULT_INFO = "Powered By: RL TOC Client v" + TOC_VERSION + "! - " + TOC_SITE_URL;

  public static final String COMMAND_IM_IN = "IM_IN";
  public static final String COMMAND_ERROR = "ERROR";
  public static final String COMMAND_SIGNON = "SIGN_ON";
  public static final String COMMAND_UPDATE_BUDDY_STATUS="UPDATE_BUDDY";

  public static final int IM_SENDER_INDEX = 1;
  public static final int IM_AUTO_INDEX = 2;
  public static final int IM_MESSAGE_INDEX = 3;
  
  public static final int BUDDY_STATUS_USERNAME_INDEX=1;
  public static final int BUDDY_STATUS_IS_ONLINE_INDEX=2;
  public static final int BUDDY_STATUS_EVIL_AMOUNT_INDEX=3;
  public static final int BUDDY_STATUS_SIGNON_TIME_INDEX=4;
  public static final int BUDDY_STATUS_IDLE_MINUTES_INDEX=5;
  public static final int BUDDY_STATUS_USER_CLASS_INDEX=6;
  
  public static final String[] USER_CLASS_STRINGS={"AOL Member", "Oscar Normal", "Oscar Unconfirmed", "Oscar Admin", "Mysterious"};
  
}