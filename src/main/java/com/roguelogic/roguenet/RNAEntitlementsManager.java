/**
 * Created Oct 28, 2006
 */
package com.roguelogic.roguenet;

import java.util.Properties;

import com.roguelogic.entitlements.EntitlementsController;

/**
 * @author Robert C. Ilardi
 *
 */

public interface RNAEntitlementsManager {
  public void initEntitlementsManager(Properties props) throws RogueNetException;

  public void setPlugInManager(PlugInManager plugInManager);

  public EntitlementsController getEntitlementsController();

  public SSOController getSSOController();

  public String ssoLogin(String username, String password, String peer) throws RogueNetException;

  public boolean ssoLogout(String sessionToken, String peer) throws RogueNetException;

  public SSOLedger getSSOLedger();
}
