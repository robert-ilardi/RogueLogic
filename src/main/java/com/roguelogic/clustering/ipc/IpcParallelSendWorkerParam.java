/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

import com.roguelogic.workers.WorkerParameter;

/**
 * @author Robert C. Ilardi
 * 
 */

public class IpcParallelSendWorkerParam implements WorkerParameter {

  private IpcPeer peer;
  private IpcEvent event;

  public IpcParallelSendWorkerParam() {
  }

  public IpcEvent getEvent() {
    return event;
  }

  public void setEvent(IpcEvent event) {
    this.event = event;
  }

  public IpcPeer getPeer() {
    return peer;
  }

  public void setPeer(IpcPeer peer) {
    this.peer = peer;
  }

}
