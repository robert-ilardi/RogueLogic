/**
 * Created Oct 23, 2006
 */
package com.roguelogic.entitlements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class SimpleFileEntitlementsController implements EntitlementsController {

  public static final char STRING_PAD_CHAR = ' ';
  public static final char INT_PAD_CHAR = '0';

  public static final int NEW_LINE_LEN = "\n".getBytes().length;

  public static final int ENTRY_ACTIVE_FLAG_LEN = 1;

  public static final char[] USERFILE_FIELD_PADDING = { STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR };
  public static final boolean[] USERFILE_FIELD_PADDING_LEFT_DIRECTIONS = { false, false, false };
  public static final int USERFILE_TOKEN_CNT = 3;
  public static final int USERFILE_ENTRY_ACTIVE_FLAG_INDEX = 0;
  public static final int USERFILE_USERNAME_INDEX = 1;
  public static final int USERFILE_PASSWORD_INDEX = 2;

  protected int[] userFileFieldLens;
  protected int userFieldRecordLen;

  public static final char[] GROUPFILE_FIELD_PADDING = { STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR };
  public static final boolean[] GROUPFILE_FIELD_PADDING_LEFT_DIRECTIONS = { false, false, false };
  public static final int GROUPFILE_TOKEN_CNT = 3;
  public static final int GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX = 0;
  public static final int GROUPFILE_CODE_INDEX = 1;
  public static final int GROUPFILE_DESCRIPTION_INDEX = 2;

  protected int[] groupFileFieldLens;
  protected int groupFieldRecordLen;

  public static final char[] ITEMFILE_FIELD_PADDING = { STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR };
  public static final boolean[] ITEMFILE_FIELD_PADDING_LEFT_DIRECTIONS = { false, false, false, false, false, false, false, false };
  public static final int ITEMFILE_TOKEN_CNT = 8;
  public static final int ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX = 0;
  public static final int ITEMFILE_CODE_INDEX = 1;
  public static final int ITEMFILE_TYPE_INDEX = 2;
  public static final int ITEMFILE_DESCRIPTION_INDEX = 3;
  public static final int ITEMFILE_VALUE_INDEX = 4;
  public static final int ITEMFILE_READ_FLAG_INDEX = 5;
  public static final int ITEMFILE_WRITE_FLAG_INDEX = 6;
  public static final int ITEMFILE_EXECUTE_FLAG_INDEX = 7;

  protected int[] itemFileFieldLens;
  protected int itemFieldRecordLen;

  public static final char[] USERLINKFILE_FIELD_PADDING = { STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR };
  public static final boolean[] USERLINKFILE_FIELD_PADDING_LEFT_DIRECTIONS = { false, false, false };
  public static final int USERLINKFILE_TOKEN_CNT = 3;
  public static final int USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX = 0;
  public static final int USERLINKFILE_USERNAME_INDEX = 1;
  public static final int USERLINKFILE_GROUPCODE_INDEX = 2;

  protected int[] userLinkFileFieldLens;
  protected int userLinkFieldRecordLen;

  public static final char[] ITEMLINKFILE_FIELD_PADDING = { STRING_PAD_CHAR, STRING_PAD_CHAR, STRING_PAD_CHAR };
  public static final boolean[] ITEMLINKFILE_FIELD_PADDING_LEFT_DIRECTIONS = { false, false, false };
  public static final int ITEMLINKFILE_TOKEN_CNT = 3;
  public static final int ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX = 0;
  public static final int ITEMLINKFILE_ITEMCODE_INDEX = 1;
  public static final int ITEMLINKFILE_GROUPCODE_INDEX = 2;

  protected int[] itemLinkFileFieldLens;
  protected int itemLinkFieldRecordLen;

  protected SimpleFileEntitlementsConfig config;

  public SimpleFileEntitlementsController() {}

  public SimpleFileEntitlementsController(SimpleFileEntitlementsConfig config) throws EntitlementsException {
    setup(config);
  }

  public void setup(SimpleFileEntitlementsConfig config) throws EntitlementsException {
    if (config == null) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with NULL Configuration!");
    }

    this.config = config;

    if (StringUtils.IsNVL(config.getUserFile())) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with NULL User File!");
    }

    if (StringUtils.IsNVL(config.getGroupFile())) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with NULL Group File!");
    }

    if (StringUtils.IsNVL(config.getItemFile())) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with NULL Item File!");
    }

    if (StringUtils.IsNVL(config.getUserGroupFile())) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with NULL User Group File!");
    }

    if (StringUtils.IsNVL(config.getGroupItemFile())) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with NULL Group Item File!");
    }

    if (config.getUsernameLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Username Length <= 0");
    }

    if (config.getPasswordLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Password Length <= 0");
    }

    prepareUserFileFieldLens();

    if (config.getGroupCodeLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Group Code Length <= 0");
    }

    if (config.getGroupDescriptionLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Group Description Length <= 0");
    }

    prepareGroupFileFieldLens();

    if (config.getItemCodeLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Item Code Length <= 0");
    }

    if (config.getItemTypeLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Item Type Length <= 0");
    }

    if (config.getItemDescriptionLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Item Description Length <= 0");
    }

    if (config.getItemValueLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Item Value Length <= 0");
    }

    if (config.getItemReadFlagLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Item Read Flag Length <= 0");
    }

    if (config.getItemWriteFlagLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Item Write Flag Length <= 0");
    }

    if (config.getItemExecuteFlagLen() <= 0) {
      throw new EntitlementsException("Can NOT construct Entitlements Controller with Item Execute Flag Length <= 0");
    }

    prepareItemFileFieldLens();

    prepareUserLinkFileFieldLens();

    prepareItemLinkFileFieldLens();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#userExists(com.roguelogic.entitlements.User)
   */
  public synchronized boolean userExists(User user) throws EntitlementsException {
    boolean exists = false;
    RandomAccessFile raf = null;
    File f;

    if (user != null) {
      try {
        f = new File(config.getUserFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          exists = userExists(raf, user);
        }
      } //End try block
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting verify user existence of '" + user.getUsername() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user check

    return exists;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#createUser(com.roguelogic.entitlements.User)
   */
  public synchronized void createUser(User user) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (user != null) {
      try {
        raf = new RandomAccessFile(config.getUserFile(), "rw");

        if (userExists(raf, user)) {
          throw new EntitlementsException("User '" + user.getUsername() + "' already exists!");
        }

        //Seek to first deleted user entry if any
        if ((pos = seekToFirstDeletedUser(raf)) < 0) {
          //If none then:
          raf.seek(raf.length()); //Seek to the EOF so we append
        }
        else {
          raf.seek(pos - (userFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of user record
        }

        tokens = new String[USERFILE_TOKEN_CNT];
        tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE;
        tokens[USERFILE_USERNAME_INDEX] = user.getUsername();
        tokens[USERFILE_PASSWORD_INDEX] = user.getPassword();

        line = StringUtils.PrepareFixedLengthRecord(tokens, USERFILE_FIELD_PADDING, USERFILE_FIELD_PADDING_LEFT_DIRECTIONS, userFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to create the user '" + user.getUsername() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#updateUser(com.roguelogic.entitlements.User)
   */
  public synchronized void updateUser(User user) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (user != null) {
      try {
        raf = new RandomAccessFile(config.getUserFile(), "rw");

        //Seek to and check if the user exists
        if ((pos = seekToUser(raf, user)) < 0) {
          throw new EntitlementsException("User '" + user.getUsername() + "' does NOT exist!");
        }

        raf.seek(pos - (userFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of user record

        tokens = new String[USERFILE_TOKEN_CNT];
        tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE;
        tokens[USERFILE_USERNAME_INDEX] = user.getUsername();
        tokens[USERFILE_PASSWORD_INDEX] = user.getPassword();

        line = StringUtils.PrepareFixedLengthRecord(tokens, USERFILE_FIELD_PADDING, USERFILE_FIELD_PADDING_LEFT_DIRECTIONS, userFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to update the user '" + user.getUsername() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#deleteUser(com.roguelogic.entitlements.User)
   */
  public synchronized void deleteUser(User user) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (user != null) {
      try {
        raf = new RandomAccessFile(config.getUserFile(), "rw");

        //Seek to and check if the user exists
        if ((pos = seekToUser(raf, user)) < 0) {
          throw new EntitlementsException("User '" + user.getUsername() + "' does NOT exist!");
        }

        raf.seek(pos - (userFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of user record

        tokens = new String[1];
        tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_DELETED;

        line = StringUtils.PrepareFixedLengthRecord(tokens, USERFILE_FIELD_PADDING, USERFILE_FIELD_PADDING_LEFT_DIRECTIONS, userFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to delete the user '" + user.getUsername() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#listUsers()
   */
  public User[] listUsers() throws EntitlementsException {
    User[] users = null;
    RandomAccessFile raf = null;
    File f;

    try {
      f = new File(config.getUserFile());
      if (f.exists()) {
        raf = new RandomAccessFile(f, "r");
        users = listUsers(raf);
      }
    } //End try block
    catch (Exception e) {
      throw new EntitlementsException("An error occurred while attempting obtain the list of users!", e);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return users;
  }

  public User getUser(User inputUser, boolean loadGroups) throws EntitlementsException {
    User realUser = null;
    RandomAccessFile raf = null;
    File f;

    try {
      f = new File(config.getUserFile());
      if (f.exists()) {
        raf = new RandomAccessFile(f, "r");
        realUser = loadUser(getUserRecord(raf, inputUser.getUsername()), loadGroups);
      }
    } //End try block
    catch (Exception e) {
      throw new EntitlementsException("An error occurred while attempting obtain the User!", e);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return realUser;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#preformUserDBMaintenance()
   */
  public void preformUserDBMaintenance(Properties props) throws EntitlementsException {
    try {
      compactUserFile();
    }
    catch (Exception e) {
      throw new EntitlementsException(e);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#groupExists(com.roguelogic.entitlements.Group)
   */
  public boolean groupExists(Group group) throws EntitlementsException {
    boolean exists = false;
    RandomAccessFile raf = null;
    File f;

    if (group != null) {
      try {
        f = new File(config.getGroupFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          exists = groupExists(raf, group);
        }
      } //End try block
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting verify group existence of '" + group.getCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null group check

    return exists;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#createGroup(com.roguelogic.entitlements.Group)
   */
  public synchronized void createGroup(Group group) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (group != null) {
      try {
        raf = new RandomAccessFile(config.getGroupFile(), "rw");

        if (groupExists(raf, group)) {
          throw new EntitlementsException("Group '" + group.getCode() + "' already exists!");
        }

        //Seek to first deleted group entry if any
        if ((pos = seekToFirstDeletedGroup(raf)) < 0) {
          //If none then:
          raf.seek(raf.length()); //Seek to the EOF so we append
        }
        else {
          raf.seek(pos - (groupFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of group record
        }

        tokens = new String[GROUPFILE_TOKEN_CNT];
        tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE;
        tokens[GROUPFILE_CODE_INDEX] = group.getCode();
        tokens[GROUPFILE_DESCRIPTION_INDEX] = group.getDescription();

        line = StringUtils.PrepareFixedLengthRecord(tokens, GROUPFILE_FIELD_PADDING, GROUPFILE_FIELD_PADDING_LEFT_DIRECTIONS, groupFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to create the group '" + group.getCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null group check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#updateGroup(com.roguelogic.entitlements.Group)
   */
  public synchronized void updateGroup(Group group) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (group != null) {
      try {
        raf = new RandomAccessFile(config.getGroupFile(), "rw");

        //Seek to and check if the group exists
        if ((pos = seekToGroup(raf, group)) < 0) {
          throw new EntitlementsException("Group '" + group.getCode() + "' does NOT exist!");
        }

        raf.seek(pos - (groupFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of group record

        tokens = new String[GROUPFILE_TOKEN_CNT];
        tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE;
        tokens[GROUPFILE_CODE_INDEX] = group.getCode();
        tokens[GROUPFILE_DESCRIPTION_INDEX] = group.getDescription();

        line = StringUtils.PrepareFixedLengthRecord(tokens, GROUPFILE_FIELD_PADDING, GROUPFILE_FIELD_PADDING_LEFT_DIRECTIONS, groupFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to update the group '" + group.getCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null group check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#deleteGroup(com.roguelogic.entitlements.Group)
   */
  public synchronized void deleteGroup(Group group) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (group != null) {
      try {
        raf = new RandomAccessFile(config.getGroupFile(), "rw");

        //Seek to and check if the group exists
        if ((pos = seekToGroup(raf, group)) < 0) {
          throw new EntitlementsException("Group '" + group.getCode() + "' does NOT exist!");
        }

        raf.seek(pos - (groupFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of group record

        tokens = new String[1];
        tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_DELETED;

        line = StringUtils.PrepareFixedLengthRecord(tokens, GROUPFILE_FIELD_PADDING, GROUPFILE_FIELD_PADDING_LEFT_DIRECTIONS, groupFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to delete the group '" + group.getCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null group check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#listGroups()
   */
  public Group[] listGroups() throws EntitlementsException {
    Group[] groups = null;
    RandomAccessFile raf = null;
    File f;

    try {
      f = new File(config.getGroupFile());
      if (f.exists()) {
        raf = new RandomAccessFile(f, "r");
        groups = listGroups(raf);
      }
    } //End try block
    catch (Exception e) {
      throw new EntitlementsException("An error occurred while attempting obtain the list of groups!", e);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return groups;
  }

  public Group getGroup(Group inputGroup, boolean loadItems) throws EntitlementsException {
    Group realGroup = null;
    RandomAccessFile raf = null;
    File f;

    try {
      f = new File(config.getGroupFile());
      if (f.exists()) {
        raf = new RandomAccessFile(f, "r");
        realGroup = loadGroup(getGroupRecord(raf, inputGroup.getCode()), loadItems);
      }
    } //End try block
    catch (Exception e) {
      throw new EntitlementsException("An error occurred while attempting obtain the Group!", e);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return realGroup;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#preformGroupDBMaintenance()
   */
  public void preformGroupDBMaintenance(Properties props) throws EntitlementsException {
    try {
      compactGroupFile();
    }
    catch (Exception e) {
      throw new EntitlementsException(e);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#itemExists(com.roguelogic.entitlements.Item)
   */
  public boolean itemExists(Item item) throws EntitlementsException {
    boolean exists = false;
    RandomAccessFile raf = null;
    File f;

    if (item != null) {
      try {
        f = new File(config.getItemFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          exists = itemExists(raf, item);
        }
      } //End try block
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting verify item existence of '" + item.getCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null item check

    return exists;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#createItem(com.roguelogic.entitlements.Item)
   */
  public synchronized void createItem(Item item) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (item != null) {
      try {
        raf = new RandomAccessFile(config.getItemFile(), "rw");

        if (itemExists(raf, item)) {
          throw new EntitlementsException("Item '" + item.getCode() + "' already exists!");
        }

        //Seek to first deleted item entry if any
        if ((pos = seekToFirstDeletedItem(raf)) < 0) {
          //If none then:
          raf.seek(raf.length()); //Seek to the EOF so we append
        }
        else {
          raf.seek(pos - (itemFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of item record
        }

        tokens = new String[ITEMFILE_TOKEN_CNT];
        tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE;
        tokens[ITEMFILE_CODE_INDEX] = item.getCode();
        tokens[ITEMFILE_TYPE_INDEX] = item.getType();
        tokens[ITEMFILE_DESCRIPTION_INDEX] = item.getDescription();
        tokens[ITEMFILE_VALUE_INDEX] = item.getValue();
        tokens[ITEMFILE_READ_FLAG_INDEX] = (item.isRead() ? "Y" : "N");
        tokens[ITEMFILE_WRITE_FLAG_INDEX] = (item.isWrite() ? "Y" : "N");
        tokens[ITEMFILE_EXECUTE_FLAG_INDEX] = (item.isExecute() ? "Y" : "N");

        line = StringUtils.PrepareFixedLengthRecord(tokens, ITEMFILE_FIELD_PADDING, ITEMFILE_FIELD_PADDING_LEFT_DIRECTIONS, itemFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to create the item '" + item.getCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null item check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#updateItem(com.roguelogic.entitlements.Item)
   */
  public synchronized void updateItem(Item item) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (item != null) {
      try {
        raf = new RandomAccessFile(config.getItemFile(), "rw");

        //Seek to and check if the item exists
        if ((pos = seekToItem(raf, item)) < 0) {
          throw new EntitlementsException("Item '" + item.getCode() + "' does NOT exist!");
        }

        raf.seek(pos - (itemFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of item record

        tokens = new String[ITEMFILE_TOKEN_CNT];
        tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE;
        tokens[ITEMFILE_CODE_INDEX] = item.getCode();
        tokens[ITEMFILE_TYPE_INDEX] = item.getType();
        tokens[ITEMFILE_DESCRIPTION_INDEX] = item.getDescription();
        tokens[ITEMFILE_VALUE_INDEX] = item.getValue();
        tokens[ITEMFILE_READ_FLAG_INDEX] = (item.isRead() ? "Y" : "N");
        tokens[ITEMFILE_WRITE_FLAG_INDEX] = (item.isWrite() ? "Y" : "N");
        tokens[ITEMFILE_EXECUTE_FLAG_INDEX] = (item.isExecute() ? "Y" : "N");

        line = StringUtils.PrepareFixedLengthRecord(tokens, ITEMFILE_FIELD_PADDING, ITEMFILE_FIELD_PADDING_LEFT_DIRECTIONS, itemFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to update the item '" + item.getCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null item check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#deleteItem(com.roguelogic.entitlements.Item)
   */
  public synchronized void deleteItem(Item item) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (item != null) {
      try {
        raf = new RandomAccessFile(config.getItemFile(), "rw");

        //Seek to and check if the item exists
        if ((pos = seekToItem(raf, item)) < 0) {
          throw new EntitlementsException("Item '" + item.getCode() + "' does NOT exist!");
        }

        raf.seek(pos - (itemFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of item record

        tokens = new String[1];
        tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_DELETED;

        line = StringUtils.PrepareFixedLengthRecord(tokens, ITEMFILE_FIELD_PADDING, ITEMFILE_FIELD_PADDING_LEFT_DIRECTIONS, itemFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting to delete the item '" + item.getCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null item check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#listItems()
   */
  public Item[] listItems() throws EntitlementsException {
    Item[] items = null;
    RandomAccessFile raf = null;
    File f;

    try {
      f = new File(config.getItemFile());
      if (f.exists()) {
        raf = new RandomAccessFile(f, "r");
        items = listItems(raf);
      }
    } //End try block
    catch (Exception e) {
      throw new EntitlementsException("An error occurred while attempting obtain the list of items!", e);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return items;
  }

  public Item getItem(Item inputItem) throws EntitlementsException {
    Item realItem = null;
    RandomAccessFile raf = null;
    File f;

    try {
      f = new File(config.getItemFile());
      if (f.exists()) {
        raf = new RandomAccessFile(f, "r");
        realItem = loadItem(getItemRecord(raf, inputItem.getCode()));
      }
    } //End try block
    catch (Exception e) {
      throw new EntitlementsException("An error occurred while attempting obtain the Item!", e);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return realItem;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#preformItemDBMaintenance()
   */
  public void preformItemDBMaintenance(Properties props) throws EntitlementsException {
    try {
      compactItemFile();
    }
    catch (Exception e) {
      throw new EntitlementsException(e);
    }
  }

  public boolean userLinkExists(UserGroupLink link) throws EntitlementsException {
    boolean exists = false;
    RandomAccessFile raf = null;
    File f;

    if (link != null) {
      try {
        f = new File(config.getUserGroupFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          exists = userLinkExists(raf, link);
        }
      } //End try block
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting verify User-Group Link existence of '" + link.getUsername() + "' to '" + link.getGroupCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user and group check

    return exists;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#linkUser(com.roguelogic.entitlements.User, com.roguelogic.entitlements.Group)
   */
  public synchronized void linkUser(UserGroupLink link) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (link != null) {
      try {
        raf = new RandomAccessFile(config.getUserGroupFile(), "rw");

        if (userLinkExists(raf, link)) {
          throw new EntitlementsException("User-Group Link '" + link.getUsername() + "' to '" + link.getGroupCode() + "' already exists!");
        }

        //Seek to first deleted user-group link entry if any
        if ((pos = seekToFirstDeletedUserLink(raf)) < 0) {
          //If none then:
          raf.seek(raf.length()); //Seek to the EOF so we append
        }
        else {
          raf.seek(pos - (userLinkFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of user-group link record
        }

        tokens = new String[USERLINKFILE_TOKEN_CNT];
        tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE;
        tokens[USERLINKFILE_USERNAME_INDEX] = link.getUsername();
        tokens[USERLINKFILE_GROUPCODE_INDEX] = link.getGroupCode();

        line = StringUtils.PrepareFixedLengthRecord(tokens, USERLINKFILE_FIELD_PADDING, USERLINKFILE_FIELD_PADDING_LEFT_DIRECTIONS, userLinkFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting create User-Group Link '" + link.getUsername() + "' to '" + link.getGroupCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user and group check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#unlinkUser(com.roguelogic.entitlements.User, com.roguelogic.entitlements.Group)
   */
  public synchronized void unlinkUser(UserGroupLink link) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (link != null) {
      try {
        raf = new RandomAccessFile(config.getUserGroupFile(), "rw");

        //Seek to and check if the user-group link exists
        if ((pos = seekToUserLink(raf, link)) < 0) {
          throw new EntitlementsException("User-Group Link '" + link.getUsername() + "' to '" + link.getGroupCode() + "' already exists!");
        }

        raf.seek(pos - (userLinkFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of user-group link record

        tokens = new String[1];
        tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_DELETED;

        line = StringUtils.PrepareFixedLengthRecord(tokens, USERLINKFILE_FIELD_PADDING, USERLINKFILE_FIELD_PADDING_LEFT_DIRECTIONS, userLinkFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting delete User-Group Link '" + link.getUsername() + "' to '" + link.getGroupCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user and group check
  }

  public UserGroupLink[] listUserGroupLinks() throws EntitlementsException {
    UserGroupLink[] links = null;
    RandomAccessFile raf = null;
    File f;

    try {
      f = new File(config.getUserGroupFile());
      if (f.exists()) {
        raf = new RandomAccessFile(f, "r");
        links = listUserGroupLinks(raf);
      }
    } //End try block
    catch (Exception e) {
      throw new EntitlementsException("An error occurred while attempting obtain the list of User-Group Links!", e);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return links;
  }

  public void preformUserGroupDBMaintenance(Properties props) throws EntitlementsException {
    try {
      compactUserGroupFile();
    }
    catch (Exception e) {
      throw new EntitlementsException(e);
    }
  }

  public boolean itemLinkExists(GroupItemLink link) throws EntitlementsException {
    boolean exists = false;
    RandomAccessFile raf = null;
    File f;

    if (link != null) {
      try {
        f = new File(config.getGroupItemFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          exists = itemLinkExists(raf, link);
        }
      } //End try block
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting verify Item-Group Link existence of '" + link.getItemCode() + "' to '" + link.getGroupCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null item and group check

    return exists;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#linkItem(com.roguelogic.entitlements.Item, com.roguelogic.entitlements.Group)
   */
  public synchronized void linkItem(GroupItemLink link) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (link != null) {
      try {
        raf = new RandomAccessFile(config.getGroupItemFile(), "rw");

        if (itemLinkExists(raf, link)) {
          throw new EntitlementsException("Item-Group Link '" + link.getItemCode() + "' to '" + link.getGroupCode() + "' already exists!");
        }

        //Seek to first deleted item-group link entry if any
        if ((pos = seekToFirstDeletedItemLink(raf)) < 0) {
          //If none then:
          raf.seek(raf.length()); //Seek to the EOF so we append
        }
        else {
          raf.seek(pos - (itemLinkFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of item-group link record
        }

        tokens = new String[ITEMLINKFILE_TOKEN_CNT];
        tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE;
        tokens[ITEMLINKFILE_ITEMCODE_INDEX] = link.getItemCode();
        tokens[ITEMLINKFILE_GROUPCODE_INDEX] = link.getGroupCode();

        line = StringUtils.PrepareFixedLengthRecord(tokens, ITEMLINKFILE_FIELD_PADDING, ITEMLINKFILE_FIELD_PADDING_LEFT_DIRECTIONS, itemLinkFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting create Item-Group Link '" + link.getItemCode() + "' to '" + link.getGroupCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null item and group check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#unlinkItem(com.roguelogic.entitlements.Item, com.roguelogic.entitlements.Group)
   */
  public synchronized void unlinkItem(GroupItemLink link) throws EntitlementsException {
    RandomAccessFile raf = null;
    String line;
    String[] tokens;
    long pos;

    if (link != null) {
      try {
        raf = new RandomAccessFile(config.getGroupItemFile(), "rw");

        //Seek to and check if the item-group link exists
        if ((pos = seekToItemLink(raf, link)) < 0) {
          throw new EntitlementsException("Item-Group Link '" + link.getItemCode() + "' to '" + link.getGroupCode() + "' already exists!");
        }

        raf.seek(pos - (itemLinkFieldRecordLen + NEW_LINE_LEN)); //Rewind to beginning of item-group link record

        tokens = new String[1];
        tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_DELETED;

        line = StringUtils.PrepareFixedLengthRecord(tokens, ITEMLINKFILE_FIELD_PADDING, ITEMLINKFILE_FIELD_PADDING_LEFT_DIRECTIONS, itemLinkFileFieldLens, true);

        if (line != null && line.length() > 0) {
          raf.writeBytes(line);
        }

      } //End try block
      catch (EntitlementsException e) {
        throw e;
      }
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting delete Item-Group Link '" + link.getItemCode() + "' to '" + link.getGroupCode() + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null item and group check
  }

  public GroupItemLink[] listGroupItemLinks() throws EntitlementsException {
    GroupItemLink[] links = null;
    RandomAccessFile raf = null;
    File f;

    try {
      f = new File(config.getGroupItemFile());
      if (f.exists()) {
        raf = new RandomAccessFile(f, "r");
        links = listGroupItemLinks(raf);
      }
    } //End try block
    catch (Exception e) {
      throw new EntitlementsException("An error occurred while attempting obtain the list of Group-Item Links!", e);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return links;
  }

  public void preformGroupItemDBMaintenance(Properties props) throws EntitlementsException {
    try {
      compactGroupItemFile();
    }
    catch (Exception e) {
      throw new EntitlementsException(e);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#login(java.lang.String)
   */
  public synchronized User login(String username) throws EntitlementsException {
    User user = null;
    RandomAccessFile raf = null;
    File f;
    String[] tokens;

    if (user != null) {
      try {
        f = new File(config.getUserFile());
        if (f.exists()) {
          raf = new RandomAccessFile(config.getUserFile(), "r");
          tokens = getUserRecord(raf, username);
          raf.close();
          raf = null;

          if (tokens == null && tokens.length >= USERFILE_TOKEN_CNT) {
            throw new EntitlementsException("User '" + username + "' NOT found!");
          }
          else {
            user = loadUser(tokens, true);
          }
        } //End f.exists check
      } //End try block
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting login user '" + username + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user check

    return user;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.entitlements.EntitlementsController#login(java.lang.String, java.lang.String)
   */
  public synchronized User login(String username, String password) throws EntitlementsException {
    User user = null;
    RandomAccessFile raf = null;
    File f;
    String[] tokens;

    if (!StringUtils.IsNVL(username) && !StringUtils.IsNVL(password)) {
      try {
        f = new File(config.getUserFile());
        if (f.exists()) {
          raf = new RandomAccessFile(config.getUserFile(), "r");
          tokens = getUserRecord(raf, username);
          raf.close();
          raf = null;

          if (tokens == null && tokens.length >= USERFILE_TOKEN_CNT) {
            //throw new EntitlementsException("User '" + username + "' NOT found!");
            throw new EntitlementsException("Invalid Username or Password!");
          }
          else if (!tokens[USERFILE_PASSWORD_INDEX].equals(password)) {
            throw new EntitlementsException("Invalid Username or Password!");
          }
          else {
            user = loadUser(tokens, true);
          }
        } //End f.exists check
      } //End try block
      catch (Exception e) {
        throw new EntitlementsException("An error occurred while attempting login user '" + username + "'", e);
      }
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End null user check

    return user;
  }

  public synchronized void compactUserFile() throws IOException {
    File srcFile = new File(config.getUserFile());
    if (srcFile.exists()) {
      File tmpFile = File.createTempFile(StringUtils.GenerateTimeUniqueId(), null);
      compactUserFile(tmpFile);
      SystemUtils.CopyFile(tmpFile.getAbsolutePath(), config.getUserFile());
      tmpFile.delete();
    }
  }

  public synchronized void compactUserFile(File destFile) throws IOException {
    RandomAccessFile raf = null;
    FileOutputStream fos = null;
    String line;
    StringBuffer sb;
    String[] tokens;
    File srcFile;

    srcFile = new File(config.getUserFile());
    if (srcFile.exists()) {
      try {
        raf = new RandomAccessFile(srcFile, "r");
        fos = new FileOutputStream(destFile);

        line = raf.readLine();

        while (line != null) {
          if (line.length() > 0) {
            tokens = StringUtils.FixedLengthSplit(line, userFileFieldLens);
            if (tokens != null && tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
              sb = new StringBuffer(line);
              sb.append("\n");
              fos.write(sb.toString().getBytes());
            }
            line = raf.readLine();
          } //End line length check
        } //End while loop
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }

        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e) {}
        }
      }
    } //End user file exists check
  }

  public synchronized void compactGroupFile() throws IOException {
    File srcFile = new File(config.getGroupFile());
    if (srcFile.exists()) {
      File tmpFile = File.createTempFile(StringUtils.GenerateTimeUniqueId(), null);
      compactGroupFile(tmpFile);
      SystemUtils.CopyFile(tmpFile.getAbsolutePath(), config.getGroupFile());
      tmpFile.delete();
    }
  }

  public synchronized void compactGroupFile(File destFile) throws IOException {
    RandomAccessFile raf = null;
    FileOutputStream fos = null;
    String line;
    StringBuffer sb;
    String[] tokens;
    File srcFile;

    srcFile = new File(config.getGroupFile());
    if (srcFile.exists()) {
      try {
        raf = new RandomAccessFile(srcFile, "r");
        fos = new FileOutputStream(destFile);

        line = raf.readLine();

        while (line != null) {
          if (line.length() > 0) {
            tokens = StringUtils.FixedLengthSplit(line, groupFileFieldLens);
            if (tokens != null && tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
              sb = new StringBuffer(line);
              sb.append("\n");
              fos.write(sb.toString().getBytes());
            }
            line = raf.readLine();
          } //End line length check
        } //End while loop
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }

        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e) {}
        }
      }
    } //End group file exists check
  }

  public synchronized void compactItemFile() throws IOException {
    File srcFile = new File(config.getItemFile());
    if (srcFile.exists()) {
      File tmpFile = File.createTempFile(StringUtils.GenerateTimeUniqueId(), null);
      compactItemFile(tmpFile);
      SystemUtils.CopyFile(tmpFile.getAbsolutePath(), config.getItemFile());
      tmpFile.delete();
    }
  }

  public synchronized void compactItemFile(File destFile) throws IOException {
    RandomAccessFile raf = null;
    FileOutputStream fos = null;
    String line;
    StringBuffer sb;
    String[] tokens;
    File srcFile;

    srcFile = new File(config.getItemFile());
    if (srcFile.exists()) {
      try {
        raf = new RandomAccessFile(srcFile, "r");
        fos = new FileOutputStream(destFile);

        line = raf.readLine();

        while (line != null) {
          if (line.length() > 0) {
            tokens = StringUtils.FixedLengthSplit(line, itemFileFieldLens);
            if (tokens != null && tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
              sb = new StringBuffer(line);
              sb.append("\n");
              fos.write(sb.toString().getBytes());
            }
            line = raf.readLine();
          } //End line length check
        } //End while loop
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }

        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e) {}
        }
      }
    } //End item file exists check
  }

  public synchronized void compactUserGroupFile() throws IOException {
    File srcFile = new File(config.getUserGroupFile());
    if (srcFile.exists()) {
      File tmpFile = File.createTempFile(StringUtils.GenerateTimeUniqueId(), null);
      compactUserGroupFile(tmpFile);
      SystemUtils.CopyFile(tmpFile.getAbsolutePath(), config.getUserGroupFile());
      tmpFile.delete();
    }
  }

  public synchronized void compactUserGroupFile(File destFile) throws IOException {
    RandomAccessFile raf = null;
    FileOutputStream fos = null;
    String line;
    StringBuffer sb;
    String[] tokens;
    File srcFile;

    srcFile = new File(config.getUserGroupFile());
    if (srcFile.exists()) {
      try {
        raf = new RandomAccessFile(srcFile, "r");
        fos = new FileOutputStream(destFile);

        line = raf.readLine();

        while (line != null) {
          if (line.length() > 0) {
            tokens = StringUtils.FixedLengthSplit(line, userLinkFileFieldLens);
            if (tokens != null && tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
              sb = new StringBuffer(line);
              sb.append("\n");
              fos.write(sb.toString().getBytes());
            }
            line = raf.readLine();
          } //End line length check
        } //End while loop
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }

        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e) {}
        }
      }
    } //End user group file exists check
  }

  public synchronized void compactGroupItemFile() throws IOException {
    File srcFile = new File(config.getGroupItemFile());
    if (srcFile.exists()) {
      File tmpFile = File.createTempFile(StringUtils.GenerateTimeUniqueId(), null);
      compactGroupItemFile(tmpFile);
      SystemUtils.CopyFile(tmpFile.getAbsolutePath(), config.getGroupItemFile());
      tmpFile.delete();
    }
  }

  public synchronized void compactGroupItemFile(File destFile) throws IOException {
    RandomAccessFile raf = null;
    FileOutputStream fos = null;
    String line;
    StringBuffer sb;
    String[] tokens;
    File srcFile;

    srcFile = new File(config.getGroupItemFile());
    if (srcFile.exists()) {
      try {
        raf = new RandomAccessFile(srcFile, "r");
        fos = new FileOutputStream(destFile);

        line = raf.readLine();

        while (line != null) {
          if (line.length() > 0) {
            tokens = StringUtils.FixedLengthSplit(line, itemLinkFileFieldLens);
            if (tokens != null && tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
              sb = new StringBuffer(line);
              sb.append("\n");
              fos.write(sb.toString().getBytes());
            }
            line = raf.readLine();
          } //End line length check
        } //End while loop
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }

        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e) {}
        }
      }
    } //End item group file exists check
  }

  protected boolean userExists(RandomAccessFile raf, User user) throws IOException {
    boolean exists = false;
    String line;
    String[] tokens;

    if (raf != null && user != null && !StringUtils.IsNVL(user.getUsername())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[USERFILE_USERNAME_INDEX] != null
              && ENTRY_ACTIVE.equals(tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX].trim()) && tokens[USERFILE_USERNAME_INDEX].trim().equalsIgnoreCase(user.getUsername())) {
            exists = true;
            break;
          }
        }

        line = raf.readLine();
      }
    } //End null input parameter checks

    return exists;
  }

  protected User[] listUsers(RandomAccessFile raf) throws IOException {
    User[] users = null;
    String line;
    String[] tokens;
    ArrayList<User> userList;
    User user;

    if (raf != null) {
      userList = new ArrayList<User>();
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            user = new User();

            if (!StringUtils.IsNVL(tokens[USERFILE_USERNAME_INDEX])) {
              user.setUsername(tokens[USERFILE_USERNAME_INDEX].trim());
            }

            userList.add(user);
          }
        }

        line = raf.readLine();
      }

      if (!userList.isEmpty()) {
        users = new User[userList.size()];
        users = userList.toArray(users);
        userList.clear();
      }
    } //End null input parameter checks

    return users;
  }

  protected void prepareUserFileFieldLens() {
    userFileFieldLens = new int[USERFILE_TOKEN_CNT];

    userFileFieldLens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE_FLAG_LEN;
    userFileFieldLens[USERFILE_USERNAME_INDEX] = config.getUsernameLen();
    userFileFieldLens[USERFILE_PASSWORD_INDEX] = config.getPasswordLen();

    userFieldRecordLen = SystemUtils.Sum(userFileFieldLens);
  }

  protected long seekToUser(RandomAccessFile raf, User user) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null && user != null && !StringUtils.IsNVL(user.getUsername())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userFileFieldLens);
          if (tokens != null && tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[USERFILE_USERNAME_INDEX] != null && ENTRY_ACTIVE.equals(tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[USERFILE_USERNAME_INDEX].trim().equalsIgnoreCase(user.getUsername())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected long seekToFirstDeletedUser(RandomAccessFile raf) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userFileFieldLens);
          if (tokens != null && tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_DELETED.equals(tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected String[] getUserRecord(RandomAccessFile raf, String username) throws IOException {
    String line;
    String[] tokens = null;

    if (raf != null && !StringUtils.IsNVL(username)) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userFileFieldLens);
          if (tokens != null && tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[USERFILE_USERNAME_INDEX] != null && ENTRY_ACTIVE.equals(tokens[USERFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[USERFILE_USERNAME_INDEX].trim().equalsIgnoreCase(username)) {
            tokens = StringUtils.Trim(tokens);
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return tokens;
  }

  protected User loadUser(String[] userTokens, boolean loadGroups) throws IOException {
    User user = null;
    HashMap<String, Group> groupMap;

    if (userTokens != null && userTokens.length == USERFILE_TOKEN_CNT) {
      user = new User();
      user.setUsername(userTokens[USERFILE_USERNAME_INDEX]);

      if (loadGroups) {
        groupMap = loadGroupMap(userTokens[USERFILE_USERNAME_INDEX]);
        user.setGroups(groupMap);
      }
    } //End userTokens size check

    return user;
  }

  protected Group loadGroup(String[] groupTokens, boolean loadItems) throws IOException {
    Group group = null;
    HashMap<String, Item> itemMap;

    if (groupTokens != null && groupTokens.length == GROUPFILE_TOKEN_CNT) {
      group = new Group();
      group.setCode(groupTokens[GROUPFILE_CODE_INDEX]);
      group.setDescription(groupTokens[GROUPFILE_DESCRIPTION_INDEX]);

      if (loadItems) {
        itemMap = loadItemMap(groupTokens[GROUPFILE_CODE_INDEX]);
        group.setItems(itemMap);
      }
    } //End userTokens size check

    return group;
  }

  protected Item loadItem(String[] itemTokens) throws IOException {
    Item item = null;

    if (itemTokens != null && itemTokens.length == ITEMFILE_TOKEN_CNT) {
      item = new Item();
      item.setCode(itemTokens[ITEMFILE_CODE_INDEX]);
      item.setType(itemTokens[ITEMFILE_TYPE_INDEX]);
      item.setDescription(itemTokens[ITEMFILE_DESCRIPTION_INDEX]);
      item.setValue(itemTokens[ITEMFILE_VALUE_INDEX]);
      item.setRead("Y".equals(itemTokens[ITEMFILE_READ_FLAG_INDEX]));
      item.setWrite("Y".equals(itemTokens[ITEMFILE_WRITE_FLAG_INDEX]));
      item.setExecute("Y".equals(itemTokens[ITEMFILE_EXECUTE_FLAG_INDEX]));
    } //End userTokens size check

    return item;
  }

  protected HashMap<String, Group> loadGroupMap(String username) throws IOException {
    RandomAccessFile raf = null;
    HashMap<String, Group> groupMap = null;
    Group group;
    File f;
    String[] tokens;
    ArrayList<String> userGroupList;

    if (!StringUtils.IsNVL(username)) {
      try {
        //Load User's Group List
        userGroupList = loadUserGroupList(username);

        //Load Details for each Group
        f = new File(config.getGroupFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          groupMap = new HashMap<String, Group>();

          for (String groupCode : userGroupList) {
            tokens = getGroupRecord(raf, groupCode);

            if (tokens != null && tokens.length == GROUPFILE_TOKEN_CNT) {
              group = loadGroup(tokens, true);
              groupMap.put(groupCode, group);
            } //End userTokens size check
          } //End for each groupCode loop
        } //End f Exists check
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End NVL username check

    return groupMap;
  }

  protected ArrayList<String> loadUserGroupList(String username) throws IOException {
    RandomAccessFile raf = null;
    File f;
    String[] tokens;
    String line;
    ArrayList<String> userGroupList = null;

    if (!StringUtils.IsNVL(username)) {
      try {
        f = new File(config.getUserGroupFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          userGroupList = new ArrayList<String>();

          line = raf.readLine();

          while (line != null) {
            if (line.length() > 0) {
              tokens = StringUtils.FixedLengthSplit(line, userLinkFileFieldLens);
              if (tokens != null && tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[USERLINKFILE_USERNAME_INDEX] != null
                  && ENTRY_ACTIVE.equals(tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim()) && tokens[USERLINKFILE_USERNAME_INDEX].trim().equalsIgnoreCase(username)) {

                tokens = StringUtils.Trim(tokens);
                userGroupList.add(tokens[USERLINKFILE_GROUPCODE_INDEX]);
              }
            } //End line length check

            line = raf.readLine();
          } //End while loop
        } //End f Exists check
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End NVL username check

    return userGroupList;
  }

  protected String[] getGroupRecord(RandomAccessFile raf, String groupCode) throws IOException {
    String line;
    String[] tokens = null;

    if (raf != null && !StringUtils.IsNVL(groupCode)) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, groupFileFieldLens);
          if (tokens != null && tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[GROUPFILE_CODE_INDEX] != null && ENTRY_ACTIVE.equals(tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[GROUPFILE_CODE_INDEX].trim().equalsIgnoreCase(groupCode)) {
            tokens = StringUtils.Trim(tokens);
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return tokens;
  }

  protected HashMap<String, Item> loadItemMap(String groupCode) throws IOException {
    RandomAccessFile raf = null;
    HashMap<String, Item> itemMap = null;
    Item item;
    File f;
    String[] tokens;
    ArrayList<String> groupItemList;

    if (!StringUtils.IsNVL(groupCode)) {
      try {
        //Load Group's Item List
        groupItemList = loadGroupItemList(groupCode);

        //Load Details for each Item
        f = new File(config.getItemFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          itemMap = new HashMap<String, Item>();

          for (String itemCode : groupItemList) {
            tokens = getItemRecord(raf, itemCode);

            if (tokens != null && tokens.length == ITEMFILE_TOKEN_CNT) {
              item = loadItem(tokens);
              itemMap.put(itemCode, item);
            } //End userTokens size check
          } //End for each groupCode loop
        } //End f Exists check
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End NVL username check

    return itemMap;
  }

  protected ArrayList<String> loadGroupItemList(String groupCode) throws IOException {
    RandomAccessFile raf = null;
    File f;
    String[] tokens;
    String line;
    ArrayList<String> groupItemList = null;

    if (!StringUtils.IsNVL(groupCode)) {
      try {
        f = new File(config.getGroupItemFile());
        if (f.exists()) {
          raf = new RandomAccessFile(f, "r");
          groupItemList = new ArrayList<String>();

          line = raf.readLine();

          while (line != null) {
            if (line.length() > 0) {
              tokens = StringUtils.FixedLengthSplit(line, itemLinkFileFieldLens);
              if (tokens != null && tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[ITEMLINKFILE_GROUPCODE_INDEX] != null
                  && ENTRY_ACTIVE.equals(tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim()) && tokens[ITEMLINKFILE_GROUPCODE_INDEX].trim().equalsIgnoreCase(groupCode)) {

                tokens = StringUtils.Trim(tokens);
                groupItemList.add(tokens[ITEMLINKFILE_ITEMCODE_INDEX]);
              }
            } //End line length check

            line = raf.readLine();
          } //End while loop
        } //End f Exists check
      } //End try block
      finally {
        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    } //End NVL username check

    return groupItemList;
  }

  protected String[] getItemRecord(RandomAccessFile raf, String itemCode) throws IOException {
    String line;
    String[] tokens = null;

    if (raf != null && !StringUtils.IsNVL(itemCode)) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemFileFieldLens);
          if (tokens != null && tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[ITEMFILE_CODE_INDEX] != null && ENTRY_ACTIVE.equals(tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[ITEMFILE_CODE_INDEX].trim().equalsIgnoreCase(itemCode)) {
            tokens = StringUtils.Trim(tokens);
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return tokens;
  }

  protected boolean groupExists(RandomAccessFile raf, Group group) throws IOException {
    boolean exists = false;
    String line;
    String[] tokens;

    if (raf != null && group != null && !StringUtils.IsNVL(group.getCode())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, groupFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[GROUPFILE_CODE_INDEX] != null
              && ENTRY_ACTIVE.equals(tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX].trim()) && tokens[GROUPFILE_CODE_INDEX].trim().equalsIgnoreCase(group.getCode())) {
            exists = true;
            break;
          }
        }

        line = raf.readLine();
      }
    } //End null input parameter checks

    return exists;
  }

  protected Group[] listGroups(RandomAccessFile raf) throws IOException {
    Group[] groups = null;
    String line;
    String[] tokens;
    ArrayList<Group> groupList;
    Group group;

    if (raf != null) {
      groupList = new ArrayList<Group>();
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, groupFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            group = new Group();

            if (!StringUtils.IsNVL(tokens[GROUPFILE_CODE_INDEX])) {
              group.setCode(tokens[GROUPFILE_CODE_INDEX].trim());
            }

            if (!StringUtils.IsNVL(tokens[GROUPFILE_DESCRIPTION_INDEX])) {
              group.setDescription(tokens[GROUPFILE_DESCRIPTION_INDEX].trim());
            }

            groupList.add(group);
          }
        }

        line = raf.readLine();
      }

      if (!groupList.isEmpty()) {
        groups = new Group[groupList.size()];
        groups = groupList.toArray(groups);
        groupList.clear();
      }
    } //End null input parameter checks

    return groups;
  }

  protected void prepareGroupFileFieldLens() {
    groupFileFieldLens = new int[GROUPFILE_TOKEN_CNT];

    groupFileFieldLens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE_FLAG_LEN;
    groupFileFieldLens[GROUPFILE_CODE_INDEX] = config.getGroupCodeLen();
    groupFileFieldLens[GROUPFILE_DESCRIPTION_INDEX] = config.getGroupDescriptionLen();

    groupFieldRecordLen = SystemUtils.Sum(groupFileFieldLens);
  }

  protected long seekToGroup(RandomAccessFile raf, Group group) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null && group != null && !StringUtils.IsNVL(group.getCode())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, groupFileFieldLens);
          if (tokens != null && tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[GROUPFILE_CODE_INDEX] != null && ENTRY_ACTIVE.equals(tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[GROUPFILE_CODE_INDEX].trim().equalsIgnoreCase(group.getCode())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected long seekToFirstDeletedGroup(RandomAccessFile raf) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, groupFileFieldLens);
          if (tokens != null && tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_DELETED.equals(tokens[GROUPFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected boolean itemExists(RandomAccessFile raf, Item item) throws IOException {
    boolean exists = false;
    String line;
    String[] tokens;

    if (raf != null && item != null && !StringUtils.IsNVL(item.getCode())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[ITEMFILE_CODE_INDEX] != null
              && ENTRY_ACTIVE.equals(tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX].trim()) && tokens[ITEMFILE_CODE_INDEX].trim().equalsIgnoreCase(item.getCode())) {
            exists = true;
            break;
          }
        }

        line = raf.readLine();
      }
    } //End null input parameter checks

    return exists;
  }

  protected Item[] listItems(RandomAccessFile raf) throws IOException {
    Item[] items = null;
    String line;
    String[] tokens;
    ArrayList<Item> itemList;
    Item item;

    if (raf != null) {
      itemList = new ArrayList<Item>();
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            item = new Item();

            if (!StringUtils.IsNVL(tokens[ITEMFILE_CODE_INDEX])) {
              item.setCode(tokens[ITEMFILE_CODE_INDEX].trim());
            }

            if (!StringUtils.IsNVL(tokens[ITEMFILE_TYPE_INDEX])) {
              item.setType(tokens[ITEMFILE_TYPE_INDEX].trim());
            }

            if (!StringUtils.IsNVL(tokens[ITEMFILE_DESCRIPTION_INDEX])) {
              item.setDescription(tokens[ITEMFILE_DESCRIPTION_INDEX].trim());
            }

            itemList.add(item);
          }
        }

        line = raf.readLine();
      }

      if (!itemList.isEmpty()) {
        items = new Item[itemList.size()];
        items = itemList.toArray(items);
        itemList.clear();
      }
    } //End null input parameter checks

    return items;
  }

  protected void prepareItemFileFieldLens() {
    itemFileFieldLens = new int[ITEMFILE_TOKEN_CNT];

    itemFileFieldLens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE_FLAG_LEN;
    itemFileFieldLens[ITEMFILE_CODE_INDEX] = config.getItemCodeLen();
    itemFileFieldLens[ITEMFILE_TYPE_INDEX] = config.getItemTypeLen();
    itemFileFieldLens[ITEMFILE_DESCRIPTION_INDEX] = config.getItemDescriptionLen();
    itemFileFieldLens[ITEMFILE_VALUE_INDEX] = config.getItemValueLen();
    itemFileFieldLens[ITEMFILE_READ_FLAG_INDEX] = config.getItemReadFlagLen();
    itemFileFieldLens[ITEMFILE_WRITE_FLAG_INDEX] = config.getItemWriteFlagLen();
    itemFileFieldLens[ITEMFILE_EXECUTE_FLAG_INDEX] = config.getItemExecuteFlagLen();

    itemFieldRecordLen = SystemUtils.Sum(itemFileFieldLens);
  }

  protected long seekToItem(RandomAccessFile raf, Item item) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null && item != null && !StringUtils.IsNVL(item.getCode())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemFileFieldLens);
          if (tokens != null && tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[ITEMFILE_CODE_INDEX] != null && ENTRY_ACTIVE.equals(tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[ITEMFILE_CODE_INDEX].trim().equalsIgnoreCase(item.getCode())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected long seekToFirstDeletedItem(RandomAccessFile raf) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemFileFieldLens);
          if (tokens != null && tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_DELETED.equals(tokens[ITEMFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected boolean userLinkExists(RandomAccessFile raf, UserGroupLink link) throws IOException {
    boolean exists = false;
    String line;
    String[] tokens;

    if (raf != null && link != null && !StringUtils.IsNVL(link.getUsername()) && !StringUtils.IsNVL(link.getGroupCode())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userLinkFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[USERLINKFILE_USERNAME_INDEX] != null
              && tokens[USERLINKFILE_GROUPCODE_INDEX] != null && ENTRY_ACTIVE.equals(tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[USERLINKFILE_USERNAME_INDEX].trim().equalsIgnoreCase(link.getUsername()) && tokens[USERLINKFILE_GROUPCODE_INDEX].trim().equalsIgnoreCase(link.getGroupCode())) {
            exists = true;
            break;
          }
        }

        line = raf.readLine();
      }
    } //End null input parameter checks

    return exists;
  }

  protected UserGroupLink[] listUserGroupLinks(RandomAccessFile raf) throws IOException {
    UserGroupLink[] links = null;
    String line;
    String[] tokens;
    ArrayList<UserGroupLink> linksList;
    UserGroupLink link;

    if (raf != null) {
      linksList = new ArrayList<UserGroupLink>();
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userLinkFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            link = new UserGroupLink();

            if (!StringUtils.IsNVL(tokens[USERLINKFILE_USERNAME_INDEX])) {
              link.setUsername(tokens[USERLINKFILE_USERNAME_INDEX].trim());
            }

            if (!StringUtils.IsNVL(tokens[USERLINKFILE_GROUPCODE_INDEX])) {
              link.setGroupCode(tokens[USERLINKFILE_GROUPCODE_INDEX].trim());
            }

            linksList.add(link);
          }
        }

        line = raf.readLine();
      }

      if (!linksList.isEmpty()) {
        links = new UserGroupLink[linksList.size()];
        links = linksList.toArray(links);
        linksList.clear();
      }
    } //End null input parameter checks

    return links;
  }

  protected void prepareUserLinkFileFieldLens() {
    userLinkFileFieldLens = new int[USERLINKFILE_TOKEN_CNT];

    userLinkFileFieldLens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE_FLAG_LEN;
    userLinkFileFieldLens[USERLINKFILE_USERNAME_INDEX] = config.getUsernameLen();
    userLinkFileFieldLens[USERLINKFILE_GROUPCODE_INDEX] = config.getGroupCodeLen();

    userLinkFieldRecordLen = SystemUtils.Sum(userLinkFileFieldLens);
  }

  protected long seekToUserLink(RandomAccessFile raf, UserGroupLink link) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null && link != null && !StringUtils.IsNVL(link.getUsername()) && !StringUtils.IsNVL(link.getGroupCode())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userLinkFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[USERLINKFILE_USERNAME_INDEX] != null
              && tokens[USERLINKFILE_GROUPCODE_INDEX] != null && ENTRY_ACTIVE.equals(tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[USERLINKFILE_USERNAME_INDEX].trim().equalsIgnoreCase(link.getUsername()) && tokens[USERLINKFILE_GROUPCODE_INDEX].trim().equalsIgnoreCase(link.getGroupCode())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected long seekToFirstDeletedUserLink(RandomAccessFile raf) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, userLinkFileFieldLens);
          if (tokens != null && tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_DELETED.equals(tokens[USERLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected boolean itemLinkExists(RandomAccessFile raf, GroupItemLink link) throws IOException {
    boolean exists = false;
    String line;
    String[] tokens;

    if (raf != null && link != null && !StringUtils.IsNVL(link.getItemCode()) && !StringUtils.IsNVL(link.getGroupCode())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemLinkFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[ITEMLINKFILE_ITEMCODE_INDEX] != null
              && tokens[ITEMLINKFILE_GROUPCODE_INDEX] != null && ENTRY_ACTIVE.equals(tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[ITEMLINKFILE_ITEMCODE_INDEX].trim().equalsIgnoreCase(link.getItemCode()) && tokens[ITEMLINKFILE_GROUPCODE_INDEX].trim().equalsIgnoreCase(link.getGroupCode())) {
            exists = true;
            break;
          }
        }

        line = raf.readLine();
      }
    } //End null input parameter checks

    return exists;
  }

  protected GroupItemLink[] listGroupItemLinks(RandomAccessFile raf) throws IOException {
    GroupItemLink[] links = null;
    String line;
    String[] tokens;
    ArrayList<GroupItemLink> linksList;
    GroupItemLink link;

    if (raf != null) {
      linksList = new ArrayList<GroupItemLink>();
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.trim().length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemLinkFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_ACTIVE.equals(tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            link = new GroupItemLink();

            if (!StringUtils.IsNVL(tokens[ITEMLINKFILE_GROUPCODE_INDEX])) {
              link.setGroupCode(tokens[ITEMLINKFILE_GROUPCODE_INDEX].trim());
            }

            if (!StringUtils.IsNVL(tokens[ITEMLINKFILE_ITEMCODE_INDEX])) {
              link.setItemCode(tokens[ITEMLINKFILE_ITEMCODE_INDEX].trim());
            }

            linksList.add(link);
          }
        }

        line = raf.readLine();
      }

      if (!linksList.isEmpty()) {
        links = new GroupItemLink[linksList.size()];
        links = linksList.toArray(links);
        linksList.clear();
      }
    } //End null input parameter checks

    return links;
  }

  protected void prepareItemLinkFileFieldLens() {
    itemLinkFileFieldLens = new int[ITEMLINKFILE_TOKEN_CNT];

    itemLinkFileFieldLens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] = ENTRY_ACTIVE_FLAG_LEN;
    itemLinkFileFieldLens[ITEMLINKFILE_ITEMCODE_INDEX] = config.getUsernameLen();
    itemLinkFileFieldLens[ITEMLINKFILE_GROUPCODE_INDEX] = config.getGroupCodeLen();

    itemLinkFieldRecordLen = SystemUtils.Sum(itemLinkFileFieldLens);
  }

  protected long seekToItemLink(RandomAccessFile raf, GroupItemLink link) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null && link != null && !StringUtils.IsNVL(link.getItemCode()) && !StringUtils.IsNVL(link.getGroupCode())) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemLinkFileFieldLens);
          if (tokens != null && tokens.length > 2 && tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && tokens[ITEMLINKFILE_ITEMCODE_INDEX] != null
              && tokens[ITEMLINKFILE_GROUPCODE_INDEX] != null && ENTRY_ACTIVE.equals(tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())
              && tokens[ITEMLINKFILE_ITEMCODE_INDEX].trim().equalsIgnoreCase(link.getItemCode()) && tokens[ITEMLINKFILE_GROUPCODE_INDEX].trim().equalsIgnoreCase(link.getGroupCode())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

  protected long seekToFirstDeletedItemLink(RandomAccessFile raf) throws IOException {
    long pos = -1;
    String line;
    String[] tokens;

    if (raf != null) {
      raf.seek(0); //Start from the beginning of the file

      line = raf.readLine();

      while (line != null) {
        if (line.length() > 0) {
          tokens = StringUtils.FixedLengthSplit(line, itemLinkFileFieldLens);
          if (tokens != null && tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX] != null && ENTRY_DELETED.equals(tokens[ITEMLINKFILE_ENTRY_ACTIVE_FLAG_INDEX].trim())) {
            pos = raf.getFilePointer();
            break;
          }
        } //End line length check

        line = raf.readLine();
      } //End while loop
    } //End null input parameter checks

    return pos;
  }

}
