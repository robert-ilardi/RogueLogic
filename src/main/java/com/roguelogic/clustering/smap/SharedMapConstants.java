/**
 * Created Aug 5, 2008
 */
package com.roguelogic.clustering.smap;

/**
 * @author Robert C. Ilardi
 *
 */

public class SharedMapConstants {

  public static final int CMD_LOGIN = 1000;
  public static final int CMD_GET = 1010;
  public static final int CMD_PUT = 1020;
  public static final int CMD_REMOVE = 1030;
  public static final int CMD_CLEAR = 1040;
  public static final int CMD_CONTAINS_KEY = 1050;
  public static final int CMD_LOCK_ENTRY = 1060;
  public static final int CMD_UNLOCK_ENTRY = 1070;
  public static final int CMD_IS_ENTRY_LOCKED = 1080;

  public static final long INFINITE_TIMEOUT = -1;

  public static final int LOGIN_TIMEOUT = 60;
  public static final int PUT_TIMEOUT = 300;
  public static final int GET_TIMEOUT = 300;
  public static final int REMOVE_TIMEOUT = 300;
  public static final int CLEAR_TIMEOUT = 60;
  public static final int CONTAINS_KEY_TIMEOUT = 300;

}
