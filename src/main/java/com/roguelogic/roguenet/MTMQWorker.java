/**
 * Created Feb 4, 2007
 */
package com.roguelogic.roguenet;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.workers.Worker;
import com.roguelogic.workers.WorkerException;
import com.roguelogic.workers.WorkerParameter;

/**
 * @author Robert C. Ilardi
 *
 */

public class MTMQWorker implements Worker {

  private MTMQProcessor processor;

  public MTMQWorker() {}

  public void setProcessor(MTMQProcessor processor) {
    this.processor = processor;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.workers.Worker#performWork(com.roguelogic.workers.WorkerParameter)
   */
  public void performWork(WorkerParameter param) throws WorkerException {
    MTMQWorkerParam mtmqwParam;
    P2PHubMessage mesg;

    if (param instanceof MTMQWorkerParam) {
      mtmqwParam = (MTMQWorkerParam) param;
      if (mtmqwParam.getMessage() != null) {
        mesg = mtmqwParam.getMessage();
        processor.processMessageMT(mesg);
      }
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.workers.Worker#destroyWorker()
   */
  public void destroyWorker() {}

}
