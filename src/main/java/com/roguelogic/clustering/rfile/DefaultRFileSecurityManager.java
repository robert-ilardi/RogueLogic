/**
 * Created Aug 5, 2008
 */
package com.roguelogic.clustering.rfile;

/**
 * @author Robert C. Ilardi
 *
 */
public class DefaultRFileSecurityManager implements RFileSecurityManager {

  private String username;
  private String password;

  public DefaultRFileSecurityManager() {}

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean login(String username, String password) throws RemoteFileException {
    return this.username.equalsIgnoreCase(username) && this.password.equals(password);
  }

}
