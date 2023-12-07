/**
 * Created Dec 12, 2008
 */
package com.roguelogic.simpleft;

/**
 * @author Robert C. Ilardi
 * 
 */

public class SimpleFtProtocolConstants {

  public static final int SFT_RLTCMD_LOGIN = 1000;
  public static final int SFT_RLTCMD_GET_LOGIN_MASK = 1010;

  public static final int SFT_RLTCMD_PING = 1100;
  public static final int SFT_RLTCMD_ECHO = 1101;
  public static final int SFT_RLTCMD_DATETIME = 1122;

  public static final int SFT_RLTCMD_LIST_SHARES = 1200;
  public static final int SFT_RLTCMD_LIST_DIRECTORY = 1220;
  public static final int SFT_RLTCMD_CHANGE_DIRECTORY = 1230;

  public static final int SFT_RLTCMD_START_DOWNLOAD = 1300;
  public static final int SFT_RLTCMD_START_SDOWNLOAD = 1305;
  public static final int SFT_RLTCMD_DOWNLOAD_DATA = 1310;
  public static final int SFT_RLTCMD_DOWNLOAD_DATA_ASYNC = 1315;
  public static final int SFT_RLTCMD_END_DOWNLOAD = 1320;

  public static final int SFT_RLTCMD_START_UPLOAD = 1400;
  public static final int SFT_RLTCMD_UPLOAD_DATA = 1410;
  public static final int SFT_RLTCMD_UPLOAD_DATA_NO_REPLY = 1415;
  public static final int SFT_RLTCMD_END_UPLOAD = 1420;

  public static final int STATUS_CODE_SUCCESS = 1;
  public static final int STATUS_CODE_FAILURE = 2;

}
