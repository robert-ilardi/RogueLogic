/**
 * Created Nov 13, 2006
 */
package com.roguelogic.roguenet;

import java.util.Properties;

import com.roguelogic.entitlements.User;

/**
 * @author Robert C. Ilardi
 *
 */

public interface SSOController {
  public void initSSOCntrlr(Properties props) throws RogueNetException;

  public String establishSession(User user) throws RogueNetException;

  public void invalidateSession(String sessionToken) throws RogueNetException;

  public User getSessionUser(String sessionToken) throws RogueNetException;

  public void clearAllSessions() throws RogueNetException;

  public int getSessionCount() throws RogueNetException;
}
