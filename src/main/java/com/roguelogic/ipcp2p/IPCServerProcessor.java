package com.roguelogic.ipcp2p;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

public class IPCServerProcessor extends RLTalkSocketProcessor {

  public static final int CMD_RECEIVE_IPC_MESSAGE = 124;
  public static final int CMD_RECEIVE_CONNECTION_KEY = 390;

  public static final String UDI_CONNECTION_VALID_FLAG = "IPCConnectionValidFlag";

  private IPCServer ipcServer;

  public IPCServerProcessor() {
    super();
  }

  protected void setIPCServer(IPCServer server) {
    this.ipcServer = server;
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {
    ipcServer.addHandshakeWatch(userSession);
  }

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    IPCMessage ipcMesg;
    Object obj;
    String connectionKey;
    boolean connectionValid;

    //If connection is not validated disconnect!
    if (cmDatPair.getCommand() != CMD_RECEIVE_CONNECTION_KEY) {
      obj = userSession.getUserItem(UDI_CONNECTION_VALID_FLAG);
      if (obj != null) {
        connectionValid = (Boolean) obj;
        if (!connectionValid) {
          userSession.endSession();
          return;
        }
      }
      else {
        userSession.endSession();
        return;
      }
    }

    switch (cmDatPair.getCommand()) {
      case CMD_RECEIVE_IPC_MESSAGE:
        try {
          obj = cmDatPair.getObject();
          if (obj instanceof IPCMessage) {
            ipcMesg = (IPCMessage) obj;
            ipcServer.recieve(ipcMesg);
          }
        }
        catch (Exception e) {
          System.err.println("An error occurred while attempting to deserialize IPC Message! See Stack Trace for more information. Disconnecting Client from IPC P2P Server! CmDatPair Dump: "
              + cmDatPair);
          e.printStackTrace();
          this.userSession.endSession();
        }
        break;
      case CMD_RECEIVE_CONNECTION_KEY:
        connectionKey = cmDatPair.getString();
        ipcServer.validateConnectionKey(userSession, connectionKey);
        break;
      default:
        System.err.println("Invalid Command Data Pair Received! Disconnecting Client from IPC P2P Server! CmDatPair Dump: " + cmDatPair);
        this.userSession.endSession();
    }
  }

}
