/**
 * Created Aug 6, 2008
 */
package com.roguelogic.joe.framework;

/**
 * @author Robert C. Ilardi
 *
 */

public interface SecurityManager {
  public boolean login(String username, String password);
}
