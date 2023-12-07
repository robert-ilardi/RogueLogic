/**
 * Created Feb 4, 2007
 */
package com.roguelogic.roguenet;

import java.io.Serializable;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.workers.WorkerParameter;

/**
 * @author Robert C. Ilardi
 *
 */

public class MTMQWorkerParam implements WorkerParameter, Serializable {

  P2PHubMessage message;

  public MTMQWorkerParam() {}

  public P2PHubMessage getMessage() {
    return message;
  }

  public void setMessage(P2PHubMessage message) {
    this.message = message;
  }

}
