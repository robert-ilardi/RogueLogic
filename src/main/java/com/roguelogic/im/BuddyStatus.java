/*
 * Created on Apr 3, 2005
 */
package com.roguelogic.im;

import java.util.Date;

/**
 * @author rilardi
 */

public class BuddyStatus {

  private String username;
  private boolean online;
  private int evilAmount;
  private long signonTime;
  private long idleMinutes;
  private int userClass;

  public static final int USER_AOL = 0;
  public static final int USER_OSCAR_NORMAL = 1;
  public static final int USER_OSCAR_UNCONFIRMED = 2;
  public static final int USER_OSCAR_ADMIN = 3;
  public static final int USER_UNKNOWN = 4;

  public BuddyStatus(String username, boolean online, int evilAmount, long signonTime, long idleMinutes, int userClass) {
    this.username = username;
    this.online = online;
    this.evilAmount = evilAmount;
    this.signonTime = signonTime;
    this.idleMinutes = idleMinutes;
    this.userClass = userClass;
  }

  public static int GetUserClass(String ucStr) {
    int userClass = USER_UNKNOWN;

    if (ucStr != null) {
      if (ucStr.equalsIgnoreCase(" A")) {
        userClass = USER_AOL;
      }
      else if (ucStr.equalsIgnoreCase(" O")) {
        userClass = USER_OSCAR_NORMAL;
      }
      else if (ucStr.equalsIgnoreCase(" U")) {
        userClass = USER_OSCAR_UNCONFIRMED;
      }
    }

    return userClass;
  }

  /**
   * @return Returns the evilAmount.
   */
  public int getEvilAmount() {
    return evilAmount;
  }

  /**
   * @return Returns the idleMinutes.
   */
  public long getIdleMinutes() {
    return idleMinutes;
  }

  /**
   * @return Returns the online.
   */
  public boolean isOnline() {
    return online;
  }

  /**
   * @return Returns the signonTime.
   */
  public long getSignonTime() {
    return signonTime;
  }

  /**
   * @return Returns the userClass.
   */
  public int getUserClass() {
    return userClass;
  }

  /**
   * @return Returns the username.
   */
  public String getUsername() {
    return username;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("----------Buddy Status Update----------\n");
    sb.append("Username: ");
    sb.append(username);
    sb.append("\n");
    sb.append("Is Online? ");
    sb.append(online ? "YES" : "NO");
    sb.append("\n");
    sb.append("Evil Level: ");
    sb.append(evilAmount);
    sb.append("\n");
    sb.append("Online Since: ");
    sb.append(new Date(signonTime));
    sb.append("\n");
    sb.append("Idle Minutes: ");
    sb.append(idleMinutes);
    sb.append("\n");
    sb.append("User Class: ");
    sb.append(userClass);
    sb.append("\n");
    sb.append("---------------------------------------");

    return sb.toString();
  }

}