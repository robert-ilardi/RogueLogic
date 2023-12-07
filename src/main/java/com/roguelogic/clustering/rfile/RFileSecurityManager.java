/**
 * Created Aug 5, 2008
 */
package com.roguelogic.clustering.rfile;

/**
 * @author Robert C. Ilardi
 *
 */

public interface RFileSecurityManager {
  public boolean login(String username, String password) throws RemoteFileException;
}
