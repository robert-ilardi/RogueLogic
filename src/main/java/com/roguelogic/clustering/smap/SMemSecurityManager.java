/**
 * Created Aug 5, 2008
 */
package com.roguelogic.clustering.smap;

/**
 * @author Robert C. Ilardi
 *
 */

public interface SMemSecurityManager {
  public boolean login(String username, String password) throws SharedMemoryException;
}
