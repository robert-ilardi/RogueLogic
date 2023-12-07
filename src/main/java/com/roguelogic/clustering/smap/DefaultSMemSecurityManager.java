/**
 * Created Aug 5, 2008
 */
package com.roguelogic.clustering.smap;

/**
 * @author Robert C. Ilardi
 *
 */
public class DefaultSMemSecurityManager implements SMemSecurityManager {

  private String username;
  private String password;

  public DefaultSMemSecurityManager() {}

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SMemSecurityManager#login(java.lang.String, java.lang.String)
   */
  public boolean login(String username, String password) throws SharedMemoryException {
    return this.username.equalsIgnoreCase(username) && this.password.equals(password);
  }

}
