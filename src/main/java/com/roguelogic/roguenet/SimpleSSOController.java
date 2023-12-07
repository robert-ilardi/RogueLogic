/**
 * Created Nov 13, 2006
 */
package com.roguelogic.roguenet;

import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.entitlements.User;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class SimpleSSOController implements SSOController {

  private HashMap<String, User> sessionCache;
  private Object scLock;

  public SimpleSSOController() {
    sessionCache = new HashMap<String, User>();
    scLock = new Object();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.SSOController#initSSOCntrlr(java.util.Properties)
   */
  public void initSSOCntrlr(Properties props) throws RogueNetException {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.SSOController#establishSession(com.roguelogic.entitlements.User)
   */
  public String establishSession(User user) throws RogueNetException {
    String sessionToken = null;

    if (user != null) {
      sessionToken = StringUtils.GenerateTimeUniqueId();

      synchronized (scLock) {
        if (sessionCache.containsKey(sessionToken)) {
          throw new RogueNetException("Session Token Collision!");
        }
        else {
          sessionCache.put(sessionToken, user);
        }
      }
    }

    return sessionToken;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.SSOController#invalidateSession(java.lang.String)
   */
  public void invalidateSession(String sessionToken) throws RogueNetException {
    synchronized (scLock) {
      sessionCache.remove(sessionToken);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.SSOController#getSessionUser(java.lang.String)
   */
  public User getSessionUser(String sessionToken) throws RogueNetException {
    User user = null;

    synchronized (scLock) {
      user = sessionCache.get(sessionToken);
    }

    return user;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.SSOController#clearAllSessions()
   */
  public void clearAllSessions() throws RogueNetException {
    synchronized (scLock) {
      sessionCache.clear();
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.SSOController#getSessionCount()
   */
  public int getSessionCount() throws RogueNetException {
    int count;

    synchronized (scLock) {
      count = sessionCache.size();
    }

    return count;
  }

}
