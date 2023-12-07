/**
 * Created Nov 12, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.*;

import java.util.Properties;

import com.roguelogic.entitlements.EntitlementsException;
import com.roguelogic.entitlements.User;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNALogger;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;

/**
 * @author Robert C. Ilardi
 *
 */

public class SSOReceiver implements RNAPlugIn {

  private PlugInManager plugInManager;

  private static RNAPlugInInfo SSOReceiverPII;

  public static final String PPI_LOGICAL_NAME = "Single Sign On (SSO) Receiver";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Single Sign On (SSO) Receiver integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    SSOReceiverPII = new RNAPlugInInfo();

    SSOReceiverPII.setLogicalName(PPI_LOGICAL_NAME);
    SSOReceiverPII.setVersion(PPI_VERSION);
    SSOReceiverPII.setDescription(PPI_DESCRIPTION);
    SSOReceiverPII.setDeveloper(PPI_DEVELOPER);
    SSOReceiverPII.setUrl(PPI_URL);
    SSOReceiverPII.setCopyright(PPI_COPYRIGHT);
  }

  public SSOReceiver() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, SSO_RECEIVER_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, SSO_RECEIVER_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    String action;

    action = mesg.getProperty(SSO_ACTION_PROP);

    if (SSO_ACTION_LOGIN.equalsIgnoreCase(action)) {
      //Perform Login
      performLogin(mesg);
    }
    else if (SSO_ACTION_LOGOUT.equalsIgnoreCase(action)) {
      //Perform Logout
      performLogout(mesg);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return SSOReceiverPII;
  }

  private void performLogin(P2PHubMessage mesg) {
    P2PHubMessage response;
    Properties resProps;
    String username, password, sessionToken, errMesg = null;
    User user;

    try {
      if (plugInManager != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getEntitlementsController() != null
          && plugInManager.getEntitlementsManager().getSSOController() != null && plugInManager.getNetAgent() != null) {
        username = mesg.getProperty(SSO_USERNAME_PROP);
        password = mesg.getProperty(SSO_PASSWORD_PROP);

        try {
          user = plugInManager.getEntitlementsManager().getEntitlementsController().login(username, password);
        }
        catch (EntitlementsException e) {
          user = null;
          errMesg = e.getMessage();
          RNALogger.GetLogger().log(e);
        }

        response = new P2PHubMessage();
        response.setRecipients(new String[] { mesg.getSender() });
        response.setSubject(SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT);

        resProps = new Properties();
        response.setProperties(resProps);

        resProps.setProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP, mesg.getProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP));

        if (user != null) {
          sessionToken = plugInManager.getEntitlementsManager().getSSOController().establishSession(user);

          resProps.setProperty(SSO_SESSION_TOKEN_PROP, sessionToken);
          resProps.setProperty(SSO_ACTION_STATUS_PROP, SSO_ACTION_STATUS_SUCCESS);
          resProps.setProperty(SSO_MESSAGE_PROP, "Single Single On - OK!");
        }
        else {
          resProps.setProperty(SSO_ACTION_STATUS_PROP, SSO_ACTION_STATUS_FAILURE);
          resProps.setProperty(SSO_MESSAGE_PROP, (errMesg != null ? errMesg : "Invalid username or password!"));
        }

        plugInManager.getNetAgent().sendMessage(response);
      }
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  private void performLogout(P2PHubMessage mesg) {
    String sessionToken;

    try {
      if (plugInManager != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
        sessionToken = mesg.getProperty(SSO_SESSION_TOKEN_PROP);
        plugInManager.getEntitlementsManager().getSSOController().invalidateSession(sessionToken);
      }
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

}
