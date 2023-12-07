/**
 * Created Nov 17, 2006
 */
package com.roguelogic.roguenet;

import java.util.HashMap;

/**
 * @author Robert C. Ilardi
 *
 */

public class SSOLedger {

  private HashMap<String, String> sessionTokenCache;
  private Object stcLock;

  public SSOLedger() {
    sessionTokenCache = new HashMap<String, String>();
    stcLock = new Object();
  }

  public void addLink(String p2pHubUsername, String sessionToken) {
    synchronized (stcLock) {
      sessionTokenCache.put(p2pHubUsername, sessionToken);
    }
  }

  public void removeLink(String p2pHubUsername) {
    synchronized (stcLock) {
      sessionTokenCache.remove(p2pHubUsername);
    }
  }

  public void removeAll() {
    synchronized (stcLock) {
      sessionTokenCache.clear();
    }
  }

  public String getSessionToken(String p2pHubUsername) {
    String sessionToken;

    synchronized (stcLock) {
      sessionToken = sessionTokenCache.get(p2pHubUsername);
    }

    return sessionToken;
  }

}
