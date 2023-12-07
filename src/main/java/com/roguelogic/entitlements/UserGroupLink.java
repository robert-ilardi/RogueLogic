/**
 * Created Nov 10, 2006
 */
package com.roguelogic.entitlements;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Robert C. Ilardi
 *
 */

public class UserGroupLink implements Serializable {

  protected String username;
  protected String groupCode;

  protected Date created;
  protected Date lastMod;
  protected String status;

  public UserGroupLink() {}

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getGroupCode() {
    return groupCode;
  }

  public void setGroupCode(String groupCode) {
    this.groupCode = groupCode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Date getLastMod() {
    return lastMod;
  }

  public void setLastMod(Date lastMod) {
    this.lastMod = lastMod;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[UserGroup - Username: ");
    sb.append(username);
    sb.append(", GroupCode: ");
    sb.append(groupCode);
    sb.append(", Created: ");
    sb.append(created);
    sb.append(", Status: ");
    sb.append(status);
    sb.append(", lastMod: ");
    sb.append(lastMod);
    sb.append("]");

    return sb.toString();
  }

}
