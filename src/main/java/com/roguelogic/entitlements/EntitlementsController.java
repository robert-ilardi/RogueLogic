/**
 * Created Oct 23, 2006
 */
package com.roguelogic.entitlements;

import java.util.Properties;

/**
 * @author Robert C. Ilardi
 *
 */

public interface EntitlementsController {

  public static final String ENTRY_ACTIVE = "A";
  public static final String ENTRY_DELETED = "D";

  public boolean userExists(User user) throws EntitlementsException;

  public void createUser(User user) throws EntitlementsException;

  public void updateUser(User user) throws EntitlementsException;

  public void deleteUser(User user) throws EntitlementsException;

  public User[] listUsers() throws EntitlementsException;

  public User getUser(User user, boolean loadGroups) throws EntitlementsException;

  public void preformUserDBMaintenance(Properties props) throws EntitlementsException;

  public boolean groupExists(Group group) throws EntitlementsException;

  public void createGroup(Group group) throws EntitlementsException;

  public void updateGroup(Group group) throws EntitlementsException;

  public void deleteGroup(Group group) throws EntitlementsException;

  public Group[] listGroups() throws EntitlementsException;

  public Group getGroup(Group group, boolean loadItems) throws EntitlementsException;

  public void preformGroupDBMaintenance(Properties props) throws EntitlementsException;

  public boolean itemExists(Item item) throws EntitlementsException;

  public void createItem(Item item) throws EntitlementsException;

  public void updateItem(Item item) throws EntitlementsException;

  public void deleteItem(Item item) throws EntitlementsException;

  public Item[] listItems() throws EntitlementsException;

  public Item getItem(Item item) throws EntitlementsException;

  public void preformItemDBMaintenance(Properties props) throws EntitlementsException;

  public boolean userLinkExists(UserGroupLink link) throws EntitlementsException;

  public void linkUser(UserGroupLink link) throws EntitlementsException;

  public void unlinkUser(UserGroupLink link) throws EntitlementsException;

  public UserGroupLink[] listUserGroupLinks() throws EntitlementsException;

  public void preformUserGroupDBMaintenance(Properties props) throws EntitlementsException;

  public boolean itemLinkExists(GroupItemLink link) throws EntitlementsException;

  public void linkItem(GroupItemLink link) throws EntitlementsException;

  public void unlinkItem(GroupItemLink link) throws EntitlementsException;

  public GroupItemLink[] listGroupItemLinks() throws EntitlementsException;

  public void preformGroupItemDBMaintenance(Properties props) throws EntitlementsException;

  public User login(String username) throws EntitlementsException;

  public User login(String username, String password) throws EntitlementsException;

}
