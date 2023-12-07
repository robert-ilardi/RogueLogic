/**
 * Created Dec 12, 2008
 */
package com.roguelogic.offsite;

/**
 * @author Robert C. Ilardi
 * 
 */

public class OffSiteProtocolConstants {

  public static final int OFFS_RLTCMD_LOGIN = 1000;
  public static final int OFFS_RLTCMD_GET_LOGIN_MASK = 1010;
  public static final int OFFS_RLTCMD_START_UPLOAD = 1100;
  public static final int OFFS_RLTCMD_UPLOAD_NEXT_FILE_CHUNK = 1110;
  public static final int OFFS_RLTCMD_UPLOAD_COMPLETE = 1120;
  public static final int OFFS_RLTCMD_GET_REMOTE_FILE_INFO = 1130;
  public static final int OFFS_RLTCMD_SYNC_TOUCH_TS = 1140;
  public static final int OFFS_RLTCMD_REMOVE_FROM_BACKUP = 1150;

  public static final int STATUS_CODE_SUCESS = 1;
  public static final int STATUS_CODE_FAILURE = 2;

}
