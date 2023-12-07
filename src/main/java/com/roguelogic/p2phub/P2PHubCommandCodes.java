/**
 * Created Sep 16, 2006
 */
package com.roguelogic.p2phub;

/**
 * @author Robert C. Ilardi
 *
 */

public class P2PHubCommandCodes {

  public static final int P2PHUB_LOGIN_REQUEST = 1100;
  public static final int P2PHUB_LOGIN_RESPONSE = 1150;

  public static final int P2PHUB_SEND_MESSAGE_REQUEST = 1200;
  public static final int P2PHUB_SEND_MESSAGE_RESPONSE = 1250;

  public static final int P2PHUB_MESSAGE_RECEIVE_REQUEST = 1300;
  public static final int P2PHUB_MESSAGE_RECEIVE_RESPONSE = 1350;

  public static final int P2PHUB_GET_PEER_LIST_REQUEST = 1400;
  public static final int P2PHUB_GET_PEER_LIST_RESPONSE = 1450;

  public static final int P2PHUB_HEART_BEAT_REQUEST = 1500;

  public static final int P2PHUB_SC_SUCCESS = 2;
  public static final int P2PHUB_SC_FAILURE = 3;

  public static final int P2PHUB_SC_TRUE = 4;
  public static final int P2PHUB_SC_FALSE = 5;

}
