/**
 * Created Aug 27, 2006
 */
package com.roguelogic.entitlements;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Robert C. Ilardi
 *
 */

public class Item implements Serializable {

  protected int id;

  protected String code;
  protected String type;
  protected String description;

  protected String value;

  protected boolean read;
  protected boolean write;
  protected boolean execute;

  protected Date created;
  protected Date lastMod;
  protected String status;

  public Item() {}

  public String getCode() {
    return code;
  }

  public void setCode(String itemCode) {
    this.code = itemCode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String itemDescription) {
    this.description = itemDescription;
  }

  public boolean isExecute() {
    return execute;
  }

  public void setExecute(boolean execute) {
    this.execute = execute;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public boolean isWrite() {
    return write;
  }

  public void setWrite(boolean write) {
    this.write = write;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[Item - Code: ");
    sb.append(code);
    sb.append(", Id: ");
    sb.append(id);
    sb.append(", Type: ");
    sb.append(type);
    sb.append(", Description: ");
    sb.append(description);
    sb.append(", Value: ");
    sb.append(value);
    sb.append(", RWX: ");
    sb.append(read ? "Y" : "N");
    sb.append(write ? "Y" : "N");
    sb.append(execute ? "Y" : "N");
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
