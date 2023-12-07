/**
 * Created Aug 27, 2006
 */
package com.roguelogic.entitlements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class User implements Serializable {

  protected int id;

  protected String username;
  protected String password;

  protected String status;
  protected Date created;
  protected Date lastLogin;
  protected Date lastMod;

  protected HashMap<String, Group> groups;

  public User() {}

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(Date lastLogin) {
    this.lastLogin = lastLogin;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public HashMap<String, Group> getGroups() {
    return groups;
  }

  public synchronized void setGroups(HashMap<String, Group> groups) {
    this.groups = groups;
  }

  public synchronized void addGroup(Group group) {
    if (groups == null) {
      groups = new HashMap<String, Group>();
    }

    if (group != null) {
      groups.put(group.getCode(), group);
    }
  }

  public Group getGroup(String code) {
    Group group = null;

    if (groups != null) {
      group = groups.get(code);
    }

    return group;
  }

  public boolean hasAccessToGroup(String code) {
    boolean hasAccess = false;
    Group group;

    group = getGroup(code);
    hasAccess = (group != null);

    return hasAccess;
  }

  public Item getItem(String code) {
    Item item = null;

    for (Group group : groups.values()) {
      item = group.getItem(code);
      if (item != null) {
        break;
      }
    }

    return item;
  }

  public Item getItem(String groupCode, String itemCode) {
    Item item = null;
    Group group;

    group = getGroup(groupCode);
    if (group != null) {
      item = group.getItem(itemCode);
    }

    return item;
  }

  public boolean hasAccessToItem(String code) {
    boolean hasAccess = false;
    Item item;

    item = getItem(code);
    hasAccess = (item != null);

    return hasAccess;
  }

  public boolean hasAccessToItem(String groupCode, String itemCode) {
    boolean hasAccess = false;
    Item item;

    item = getItem(groupCode, itemCode);
    hasAccess = (item != null);

    return hasAccess;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Date getLastMod() {
    return lastMod;
  }

  public void setLastMod(Date lastMod) {
    this.lastMod = lastMod;
  }

  public ArrayList<Item> getItems() {
    ArrayList<Item> items = null;
    ArrayList<Group> groupList;

    if (groups != null) {
      items = new ArrayList<Item>();
      groupList = new ArrayList<Group>(groups.values());

      for (Group group : groupList) {
        items.addAll(group.getItems().values());
      }
    }

    return items;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[User - Username: ");
    sb.append(username);
    sb.append(", Id: ");
    sb.append(id);
    sb.append(", HasPassword: ");
    sb.append((!StringUtils.IsNVL(password) ? "YES" : "NO"));
    sb.append(", Status: ");
    sb.append(status);
    sb.append(", Created: ");
    sb.append(created);
    sb.append(", LastLogin: ");
    sb.append(lastLogin);
    sb.append(", LastMod: ");
    sb.append(lastMod);
    sb.append("]");

    return sb.toString();
  }

}
