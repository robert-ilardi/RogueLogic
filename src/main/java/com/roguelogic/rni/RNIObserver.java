/**
 * Created Jan 30, 2007
 */
package com.roguelogic.rni;

import java.util.Properties;

/**
 * @author Robert C. Ilardi
 *
 */

public interface RNIObserver {
  public void receive(Properties props);
}
