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

public class IpcProcessHostSockProcessor extends RLTalkSocketProcessor {

  private IpcProcessHost processHost;

  public IpcProcessHostSockProcessor() {
  }

  public void setProcessHost(IpcProcessHost processHost) {
    this.processHost = processHost;
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
      case IpcConstants.AMG_TLK_CMD_LOGIN:
        processLogin(cmDatPair);
        break;
      case IpcConstants.AMG_TLK_CMD_SEND_EVENT:
        processEvent(cmDatPair);
        break;
    }
  }

  private void processLogin(CommandDataPair cmDatPair) {
  }

  private void processEvent(CommandDataPair cmDatPair) {
    IpcEvent event;
    boolean eventSuccessful;

    try {
      event = (IpcEvent) cmDatPair.getObject();
      eventSuccessful = processHost.handleIncomingEvent(event);

      if (event.isRequiresIpcAck()) {
        sendIpcAck(event, eventSuccessful);
      }
    } // End try block
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendIpcAck(IpcEvent event, boolean eventSuccessful) throws RLNetException {
    CommandDataPair reply;

    reply = new CommandDataPair();
    reply.setCommand(IpcConstants.AMG_TLK_CMD_IPC_ACK);
    reply.setStatusCode(eventSuccessful ? IpcConstants.IPC_ACK : IpcConstants.IPC_NACK);
    reply.setData(event.getEventId());

    _rlTalkSend(reply);
  }

}
