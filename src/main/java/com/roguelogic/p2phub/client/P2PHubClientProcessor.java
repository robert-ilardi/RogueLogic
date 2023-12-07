package com.roguelogic.p2phub.client;

import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_LOGIN_RESPONSE;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_MESSAGE_RECEIVE_REQUEST;
import static com.roguelogic.p2phub.P2PHubCommandCodes.*;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

public class P2PHubClientProcessor extends RLTalkSocketProcessor {

  private P2PHubClient client;

  public P2PHubClientProcessor() {
    super();
  }

  public void setClient(P2PHubClient client) {
    this.client = client;
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    switch (cmDatPair.getCommand()) {
      case P2PHUB_LOGIN_RESPONSE:
        processLogin(cmDatPair);
        break;
      case P2PHUB_SEND_MESSAGE_RESPONSE:
        processSendMessage(cmDatPair);
        break;
      case P2PHUB_MESSAGE_RECEIVE_REQUEST:
        processMessageReceived(cmDatPair);
        break;
      case P2PHUB_GET_PEER_LIST_RESPONSE:
        processGetPeerList(cmDatPair);
        break;
      default:
        throw new RLTalkException("Unhandled Command: " + cmDatPair);
    }
  }

  private void processLogin(CommandDataPair cmDatPair) throws RLNetException {
    client.processLoginResponse(cmDatPair);
  }

  private void processSendMessage(CommandDataPair cmDatPair) throws RLNetException {
    client.processSendMessageResponse(cmDatPair);
  }

  private void processMessageReceived(CommandDataPair cmDatPair) throws RLNetException {
    client.processMessageReceiveRequest(cmDatPair);
  }

  private void processGetPeerList(CommandDataPair cmDatPair) throws RLNetException {
    client.processGetPeerList(cmDatPair);
  }

}
