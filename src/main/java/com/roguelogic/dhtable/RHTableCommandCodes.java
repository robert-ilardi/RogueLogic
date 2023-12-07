/**
 * August 8, 2006 
 */

package com.roguelogic.dhtable;

/**
 * @author Robert C. Ilardi
 *
 */

public final class RHTableCommandCodes {

  public static final int RHTCC_HT_CONTAINS_KEY_REQUEST = 2000;
  public static final int RHTCC_HT_PUT_REQUEST = 2010;
  public static final int RHTCC_HT_GET_REQUEST = 2020;
  public static final int RHTCC_HT_REMOVE_REQUEST = 2030;
  public static final int RHTCC_HT_CLEAR_REQUEST = 2040;
  public static final int RHTCC_HT_GET_KEY_LIST_REQUEST = 2050;

  public static final int RHTCC_HT_CONTAINS_KEY_RESPONSE = 2005;
  public static final int RHTCC_HT_PUT_RESPONSE = 2015;
  public static final int RHTCC_HT_GET_RESPONSE = 2025;
  public static final int RHTCC_HT_REMOVE_RESPONSE = 2035;
  public static final int RHTCC_HT_CLEAR_RESPONSE = 2045;
  public static final int RHTCC_HT_GET_KEY_LIST_RESPONSE = 2055;

  public static final int RHTSC_SUCCESS = 2;
  public static final int RHTSC_FAILURE = 3;

  public static final int RHTSC_TRUE = 4;
  public static final int RHTSC_FALSE = 5;

}
