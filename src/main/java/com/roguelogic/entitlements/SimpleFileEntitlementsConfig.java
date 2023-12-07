/**
 * Created Oct 24, 2006
 */
package com.roguelogic.entitlements;

/**
 * @author Robert C. Ilardi
 *
 */

public class SimpleFileEntitlementsConfig {

  private String userFile;
  private String groupFile;
  private String itemFile;
  private String userGroupFile;
  private String groupItemFile;

  private int usernameLen;
  private int passwordLen;

  private int groupCodeLen;
  private int groupDescriptionLen;

  private int itemCodeLen;
  private int itemTypeLen;
  private int itemDescriptionLen;
  private int itemValueLen;
  private int itemReadFlagLen;
  private int itemWriteFlagLen;
  private int itemExecuteFlagLen;

  public SimpleFileEntitlementsConfig() {}

  public String getGroupFile() {
    return groupFile;
  }

  public void setGroupFile(String groupFile) {
    this.groupFile = groupFile;
  }

  public String getGroupItemFile() {
    return groupItemFile;
  }

  public void setGroupItemFile(String groupItemFile) {
    this.groupItemFile = groupItemFile;
  }

  public String getItemFile() {
    return itemFile;
  }

  public void setItemFile(String itemFile) {
    this.itemFile = itemFile;
  }

  public String getUserFile() {
    return userFile;
  }

  public void setUserFile(String userFile) {
    this.userFile = userFile;
  }

  public String getUserGroupFile() {
    return userGroupFile;
  }

  public void setUserGroupFile(String userGroupFile) {
    this.userGroupFile = userGroupFile;
  }

  public int getPasswordLen() {
    return passwordLen;
  }

  public void setPasswordLen(int passwordLen) {
    this.passwordLen = passwordLen;
  }

  public int getUsernameLen() {
    return usernameLen;
  }

  public void setUsernameLen(int usernameLen) {
    this.usernameLen = usernameLen;
  }

  public int getGroupCodeLen() {
    return groupCodeLen;
  }

  public void setGroupCodeLen(int groupCodeLen) {
    this.groupCodeLen = groupCodeLen;
  }

  public int getGroupDescriptionLen() {
    return groupDescriptionLen;
  }

  public void setGroupDescriptionLen(int groupDescriptionLen) {
    this.groupDescriptionLen = groupDescriptionLen;
  }

  public int getItemCodeLen() {
    return itemCodeLen;
  }

  public void setItemCodeLen(int itemCodeLen) {
    this.itemCodeLen = itemCodeLen;
  }

  public int getItemDescriptionLen() {
    return itemDescriptionLen;
  }

  public void setItemDescriptionLen(int itemDescriptionLen) {
    this.itemDescriptionLen = itemDescriptionLen;
  }

  public int getItemValueLen() {
    return itemValueLen;
  }

  public void setItemValueLen(int itemValueLen) {
    this.itemValueLen = itemValueLen;
  }

  public int getItemExecuteFlagLen() {
    return itemExecuteFlagLen;
  }

  public void setItemExecuteFlagLen(int itemExecuteFlagLen) {
    this.itemExecuteFlagLen = itemExecuteFlagLen;
  }

  public int getItemReadFlagLen() {
    return itemReadFlagLen;
  }

  public void setItemReadFlagLen(int itemReadFlagLen) {
    this.itemReadFlagLen = itemReadFlagLen;
  }

  public int getItemWriteFlagLen() {
    return itemWriteFlagLen;
  }

  public void setItemWriteFlagLen(int itemWriteFlagLen) {
    this.itemWriteFlagLen = itemWriteFlagLen;
  }

  public int getItemTypeLen() {
    return itemTypeLen;
  }

  public void setItemTypeLen(int itemTypeLen) {
    this.itemTypeLen = itemTypeLen;
  }

}
