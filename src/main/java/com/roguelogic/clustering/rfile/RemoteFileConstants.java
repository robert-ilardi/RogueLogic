/**
 * Created Aug 15, 2008
 */
package com.roguelogic.clustering.rfile;

/**
 * @author Robert C. Ilardi
 *
 */

public class RemoteFileConstants {

  public static final int CMD_LOGIN = 1300;
  public static final int CMD_EXISTS = 1310;
  public static final int CMD_IS_DIRECTORY = 1320;
  public static final int CMD_IS_FILE = 1325;
  public static final int CMD_CREATE_DIRECTORY = 1330;
  public static final int CMD_RENAME = 1340;
  public static final int CMD_DELETE = 1350;
  public static final int CMD_LIST = 1360;

  public static final long INFINITE_TIMEOUT = -1;

  public static final int LOGIN_TIMEOUT = 60;

}
