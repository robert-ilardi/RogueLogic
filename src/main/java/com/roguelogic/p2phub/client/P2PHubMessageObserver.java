/**
 * Created Sep 17, 2006
 */
package com.roguelogic.p2phub.client;

import com.roguelogic.p2phub.P2PHubMessage;

/**
 * @author Robert C. Ilardi
 *
 */

public interface P2PHubMessageObserver {
  public void onMessage(P2PHubMessage mesg);
}
