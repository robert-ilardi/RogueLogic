/*
 * Created on Jan 30, 2006
 */

/*
 Copyright 2007 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.roguelogic.net;

import com.roguelogic.workers.Worker;
import com.roguelogic.workers.WorkerException;
import com.roguelogic.workers.WorkerParameter;

public class SocketWorker implements Worker {

  private SocketProcessor processor;

  public SocketWorker() {}

  public void setProcessor(SocketProcessor processor) {
    this.processor = processor;
  }

  public void performWork(WorkerParameter param) throws WorkerException {
    SocketWorkerParameter sockParam;
    SocketSession userSession = null;
    byte[] data;

    try {
      if (param instanceof SocketWorkerParameter) {
        sockParam = (SocketWorkerParameter) param;

        userSession = sockParam.getUserSession();
        data = userSession.dequeueRawData();

        if (processor != null) {
          processor.process(userSession, data);
        }
        else {
          throw new RLNetException("Socket Processor is NULL!");
        }
      } //End SocketWorkerParam instanceof check
    } //End Try Block
    catch (RLNetException e) {
      throw new WorkerException("An error occurred while attempting to process socket data!", e);
    }
    finally {
      if (processor != null) {
        processor.clearSession();
      }

      if (userSession != null) {
        userSession.signalReadReady();
      }
    }
  }

  /*
   *  (non-Javadoc)
   * @see com.roguelogic.workers.Worker#destroyWorker()
   * Called Once when the worker is destroyed!
   */
  public void destroyWorker() {
    if (processor != null) {
      processor.destroyProcessor();
      processor = null;
    }
  }

}
