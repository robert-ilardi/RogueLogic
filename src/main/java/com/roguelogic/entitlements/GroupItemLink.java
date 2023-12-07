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

public class GroupItemLink implements Serializable {

  protected String groupCode;
  protected String itemCode;

  protected Date created;
  protected Date lastMod;
  protected String status;

  public GroupItemLink() {}

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

  public String getItemCode() {
    return itemCode;
  }

  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  public Date getLastMod() {
    return lastMod;
  }

  public void setLastMod(Date lastMod) {
    this.lastMod = lastMod;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[GroupItem - GroupCode: ");
    sb.append(groupCode);
    sb.append(", ItemCode: ");
    sb.append(itemCode);
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
