package com.roguelogic.ipcp2p;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

public class IPCClientProcessor extends RLTalkSocketProcessor {

  public static final int CMD_RECEIVE_CONNECTION_KEY_RESULT = 716;

  public static final String UDI_CONNECTION_VALID_FLAG = "IPCConnectionValidFlag";

  public static final int CONNECTION_KEY_RESULT_INVALID = 0;
  public static final int CONNECTION_KEY_RESULT_VALID = 1;

  public IPCClientProcessor() {
    super();
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    switch (cmDatPair.getCommand()) {
      case CMD_RECEIVE_CONNECTION_KEY_RESULT:
        userSession.putUserItem(IPCClientProcessor.UDI_CONNECTION_VALID_FLAG, new Boolean(cmDatPair.getStatusCode() == CONNECTION_KEY_RESULT_VALID));
        break;
      default:
        System.err.println("Invalid Command Data Pair Received! CmDatPair Dump: " + cmDatPair);
        this.userSession.endSession();
    }
  }
}
