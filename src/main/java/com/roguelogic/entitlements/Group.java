/**
 * Created Aug 27, 2006
 */
package com.roguelogic.entitlements;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Robert C. Ilardi
 *
 */

public class Group implements Serializable {

  protected int id;

  protected String code;
  protected String description;

  protected Date created;
  protected Date lastMod;
  protected String status;

  protected HashMap<String, Item> items;

  public Group() {}

  public String getCode() {
    return code;
  }

  public void setCode(String groupCode) {
    this.code = groupCode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String groupDescription) {
    this.description = groupDescription;
  }

  public HashMap<String, Item> getItems() {
    return items;
  }

  public synchronized void setItems(HashMap<String, Item> items) {
    this.items = items;
  }

  public synchronized void addItem(Item item) {
    if (items == null) {
      items = new HashMap<String, Item>();
    }

    if (item != null) {
      items.put(item.getCode(), item);
    }
  }

  public Item getItem(String code) {
    Item item = null;

    if (items != null) {
      item = items.get(code);
    }

    return item;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getLastMod() {
    return lastMod;
  }

  public void setLastMod(Date lastMod) {
    this.lastMod = lastMod;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[Group - Code: ");
    sb.append(code);
    sb.append(", Id: ");
    sb.append(id);
    sb.append(", Description: ");
    sb.append(description);
    sb.append(", Created: ");
    sb.append(created);
    sb.append(", Status: ");
    sb.append(status);
    sb.append(", LastMod: ");
    sb.append(lastMod);
    sb.append("]");

    return sb.toString();
  }

}
