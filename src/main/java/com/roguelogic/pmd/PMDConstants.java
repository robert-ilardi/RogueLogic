/**
 * Created Nov 20, 2007
 */
package com.roguelogic.pmd;

/**
 * @author Robert C. Ilardi
 *
 */

public class PMDConstants {

  public static final int PMD_RLTCMD_LOGIN = 1000;
  public static final int PMD_RLTCMD_GET_SHARES = 1100;
  public static final int PMD_RLTCMD_GET_FILE_LIST = 1150;
  public static final int PMD_RLTCMD_GET_FILE_LENGTH = 1175;
  public static final int PMD_RLTCMD_OPEN_FILE = 1200;
  public static final int PMD_RLTCMD_CLOSE_FILE = 1225;
  public static final int PMD_RLTCMD_READ_NEXT_FILE_CHUNK = 1250;

  public static final int STATUS_CODE_SUCESS = 1;
  public static final int STATUS_CODE_FAILURE = 2;
  public static final int STATUS_CODE_ACCESS_DENIED = 3;
  public static final int STATUS_CODE_END_OF_STREAM = 4;

}
