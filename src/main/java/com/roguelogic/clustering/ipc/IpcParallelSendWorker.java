/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

import com.roguelogic.workers.Worker;
import com.roguelogic.workers.WorkerException;
import com.roguelogic.workers.WorkerParameter;

/**
 * @author Robert C. Ilardi
 * 
 */

public class IpcParallelSendWorker implements Worker {

  public IpcParallelSendWorker() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.citigroup.amg.workers.Worker#destroyWorker()
   */
  public void destroyWorker() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.citigroup.amg.workers.Worker#performWork(com.citigroup.amg.workers.
   * WorkerParameter)
   */
  public void performWork(WorkerParameter param) throws WorkerException {
    IpcPeer peer;
    IpcEvent event;
    IpcParallelSendWorkerParam pswp;

    if (param instanceof IpcParallelSendWorkerParam) {
      try {
        pswp = (IpcParallelSendWorkerParam) param;
        peer = pswp.getPeer();
        event = pswp.getEvent();

        peer.sendTo(event);
      }// End try block
      catch (Exception e) {
        throw new WorkerException(e);
      }
    }

  }

}
