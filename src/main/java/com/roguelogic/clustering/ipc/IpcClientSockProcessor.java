/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 * 
 */

public class IpcClientSockProcessor extends RLTalkSocketProcessor {

  private IpcClient client;

  public IpcClientSockProcessor() {
  }

  public void setClient(IpcClient client) {
    this.client = client;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.citigroup.amg.net.amgtalk.AmgTalkSocketProcessor#_amgTalkHandshake()
   */
  @Override
  protected void _rlTalkHandshake() throws RLNetException {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.citigroup.amg.net.amgtalk.AmgTalkSocketProcessor#_amgTalkHandle(com
   * .citigroup.amg.net.amgtalk.CommandDataPair)
   */
  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    switch (cmDatPair.getCommand()) {
      case IpcConstants.AMG_TLK_CMD_IPC_ACK:
        processIpcAck(cmDatPair);
        break;
    }
  }

  private void processIpcAck(CommandDataPair cmDatPair) {
    client.processIpcAck(cmDatPair);
  }

}
