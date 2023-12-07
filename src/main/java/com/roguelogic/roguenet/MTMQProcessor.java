/**
 * Created Feb 4, 2007
 */
package com.roguelogic.roguenet;

import com.roguelogic.p2phub.P2PHubMessage;

/**
 * @author Robert C. Ilardi
 *
 */

public interface MTMQProcessor {
  public void processMessageMT(P2PHubMessage mesg);
}
