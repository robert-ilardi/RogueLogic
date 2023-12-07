/**
 * Created Oct 28, 2006
 */
package com.roguelogic.roguenet;

import java.util.HashMap;
import java.util.Properties;

import static com.roguelogic.roguenet.RNAConstants.*;

import com.roguelogic.entitlements.EntitlementsController;
import com.roguelogic.entitlements.SimpleFileEntitlementsConfig;
import com.roguelogic.entitlements.SimpleFileEntitlementsController;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.plugins.SynchronousTransactionReceiver;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileRNAEntitlementsManager implements RNAEntitlementsManager, SynchronousTransactionRequestor {

  public static final String PROP_USER_FILE = "FileRNAEM.UserFile";
  public static final String PROP_GROUP_FILE = "FileRNAEM.GroupFile";
  public static final String PROP_ITEM_FILE = "FileRNAEM.ItemFile";
  public static final String PROP_USER_GROUP_FILE = "FileRNAEM.UserGroupFile";
  public static final String PROP_GROUP_ITEM_FILE = "FileRNAEM.GroupItemFile";
  public static final String PROP_USERNAME_LEN = "FileRNAEM.UsernameLen";
  public static final String PROP_PASSWORD_LEN = "FileRNAEM.PasswordLen";
  public static final String PROP_GROUP_CODE_LEN = "FileRNAEM.GroupCodeLen";
  public static final String PROP_GROUP_DESCRIPTION_LEN = "FileRNAEM.GroupDescriptionLen";
  public static final String PROP_ITEM_CODE_LEN = "FileRNAEM.ItemCodeLen";
  public static final String PROP_ITEM_TYPE_LEN = "FileRNAEM.ItemTypeLen";
  public static final String PROP_ITEM_DESCRIPTION_LEN = "FileRNAEM.ItemDescriptionLen";
  public static final String PROP_ITEM_VALUE_LEN = "FileRNAEM.ItemValueLen";
  public static final String PROP_ITEM_READ_FLAG_LEN = "FileRNAEM.ItemReadFlagLen";
  public static final String PROP_ITEM_WRITE_FLAG_LEN = "FileRNAEM.ItemWriteFlagLen";
  public static final String PROP_ITEM_EXECUTE_FLAG_LEN = "FileRNAEM.ItemExecuteFlagLen";

  public static final long LOGIN_WAIT_PERIOD = 60000;

  private SimpleFileEntitlementsController sfec;
  private SimpleSSOController ssoCntrlr;
  private SSOLedger ssoLedger;

  private PlugInManager plugInManager;

  private HashMap<String, P2PHubMessage> transactionMap;
  private Object tmLock;

  public FileRNAEntitlementsManager() {
    transactionMap = new HashMap<String, P2PHubMessage>();
    tmLock = new Object();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAEntitlementsController#initEntitlementsManager(java.util.Properties)
   */
  public void initEntitlementsManager(Properties props) throws RogueNetException {
    initEntitlementsController(props);
    initSSOController(props);
    initSSOLedger();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAEntitlementsController#getEntitlementsController()
   */
  public EntitlementsController getEntitlementsController() {
    return sfec;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAEntitlementsController#getSSOController()
   */
  public SSOController getSSOController() {
    return ssoCntrlr;
  }

  private void initEntitlementsController(Properties props) throws RogueNetException {
    SimpleFileEntitlementsConfig config;
    String tmp;
    int num;

    try {
      config = new SimpleFileEntitlementsConfig();

      config.setUserFile(props.getProperty(PROP_USER_FILE));
      config.setGroupFile(props.getProperty(PROP_GROUP_FILE));
      config.setItemFile(props.getProperty(PROP_ITEM_FILE));
      config.setUserGroupFile(props.getProperty(PROP_USER_GROUP_FILE));
      config.setGroupItemFile(props.getProperty(PROP_GROUP_ITEM_FILE));

      tmp = props.getProperty(PROP_USERNAME_LEN);
      num = Integer.parseInt(tmp);
      config.setUsernameLen(num);

      tmp = props.getProperty(PROP_PASSWORD_LEN);
      num = Integer.parseInt(tmp);
      config.setPasswordLen(num);

      tmp = props.getProperty(PROP_GROUP_CODE_LEN);
      num = Integer.parseInt(tmp);
      config.setGroupCodeLen(num);

      tmp = props.getProperty(PROP_GROUP_DESCRIPTION_LEN);
      num = Integer.parseInt(tmp);
      config.setGroupDescriptionLen(num);

      tmp = props.getProperty(PROP_ITEM_CODE_LEN);
      num = Integer.parseInt(tmp);
      config.setItemCodeLen(num);

      tmp = props.getProperty(PROP_ITEM_TYPE_LEN);
      num = Integer.parseInt(tmp);
      config.setItemTypeLen(num);

      tmp = props.getProperty(PROP_ITEM_DESCRIPTION_LEN);
      num = Integer.parseInt(tmp);
      config.setItemDescriptionLen(num);

      tmp = props.getProperty(PROP_ITEM_VALUE_LEN);
      num = Integer.parseInt(tmp);
      config.setItemValueLen(num);

      tmp = props.getProperty(PROP_ITEM_READ_FLAG_LEN);
      num = Integer.parseInt(tmp);
      config.setItemReadFlagLen(num);

      tmp = props.getProperty(PROP_ITEM_WRITE_FLAG_LEN);
      num = Integer.parseInt(tmp);
      config.setItemWriteFlagLen(num);

      tmp = props.getProperty(PROP_ITEM_EXECUTE_FLAG_LEN);
      num = Integer.parseInt(tmp);
      config.setItemExecuteFlagLen(num);

      sfec = new SimpleFileEntitlementsController(config);
    } //End try block
    catch (Exception e) {
      throw new RogueNetException("An error occurred while attempting to initialize Simple File Entitlements Controller!", e);
    }
  }

  private void initSSOController(Properties props) throws RogueNetException {
    ssoCntrlr = new SimpleSSOController();
    ssoCntrlr.initSSOCntrlr(props);
  }

  private void initSSOLedger() {
    ssoLedger = new SSOLedger();
  }

  public String ssoLogin(String username, String password, String peer) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    String sessionToken = null, transId, tmp;
    SynchronousTransactionReceiver stReceiver;
    long start;

    if (!StringUtils.IsNVL(username) && !StringUtils.IsNVL(password) && !StringUtils.IsNVL(peer) && plugInManager != null) {
      stReceiver = (SynchronousTransactionReceiver) plugInManager.getPlugIn(SynchronousTransactionReceiver.class.getName());

      mesg = new P2PHubMessage();
      mesg.setSubject(SSO_RECEIVER_SUBJECT);
      mesg.setRecipients(new String[] { peer });

      props = new Properties();
      props.setProperty(SSO_ACTION_PROP, SSO_ACTION_LOGIN);
      props.setProperty(SSO_USERNAME_PROP, username);
      props.setProperty(SSO_PASSWORD_PROP, password);
      mesg.setProperties(props);

      if (stReceiver != null) {
        transId = stReceiver.sendSynchronousTransaction(this, mesg);

        try {
          synchronized (tmLock) {
            start = System.currentTimeMillis();
            while (!transactionMap.containsKey(transId)) {
              if ((start + LOGIN_WAIT_PERIOD) < System.currentTimeMillis()) {
                throw new RogueNetException("Did NOT receive Login Response within allowed interval! Single Sign On to Peer Failed!");
              }

              tmLock.wait(1000);
            }
          }
        }
        catch (InterruptedException e) {
          throw new RogueNetException("Transaction Thread interrupted while waiting!");
        }

        mesg = (P2PHubMessage) transactionMap.remove(transId);
        tmp = mesg.getProperty(SSO_ACTION_STATUS_PROP);

        if (SSO_ACTION_STATUS_SUCCESS.equals(tmp)) {
          sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
        }
        else {
          throw new RogueNetException("SSO Login Failed! " + mesg.getProperty(SSO_MESSAGE_PROP));
        }
      }
    }

    return sessionToken;
  }

  public boolean ssoLogout(String sessionToken, String peer) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    boolean sent = false;

    if (!StringUtils.IsNVL(sessionToken) && !StringUtils.IsNVL(peer) && plugInManager != null && plugInManager.getNetAgent() != null) {
      mesg = new P2PHubMessage();
      mesg.setSubject(SSO_RECEIVER_SUBJECT);
      mesg.setRecipients(new String[] { peer });

      props = new Properties();
      props.setProperty(SSO_ACTION_PROP, SSO_ACTION_LOGOUT);
      props.setProperty(SSO_SESSION_TOKEN_PROP, sessionToken);
      mesg.setProperties(props);

      sent = plugInManager.getNetAgent().sendMessage(mesg);

      if (!sent) {
        throw new RogueNetException("Single Sign On LOGOUT was NOT sent to Target Peer!");
      }
    }

    return sent;
  }

  public void processSynchronousTransactionResponse(String transId, P2PHubMessage mesg) {
    synchronized (tmLock) {
      transactionMap.put(transId, mesg);
      tmLock.notifyAll();
    }
  }

  public void setPlugInManager(PlugInManager plugInManager) {
    this.plugInManager = plugInManager;
  }

  public SSOLedger getSSOLedger() {
    return ssoLedger;
  }

}
