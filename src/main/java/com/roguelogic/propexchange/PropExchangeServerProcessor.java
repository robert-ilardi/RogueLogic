/**
 * Created Jan 15, 2007
 */
package com.roguelogic.propexchange;

import static com.roguelogic.propexchange.PropExchangeCommandCodes.PECC_ASYNC_SEND_PROPERTIES;
import static com.roguelogic.propexchange.PropExchangeCommandCodes.PECC_SYNC_SEND_PROPERTIES;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 *
 */

public class PropExchangeServerProcessor extends RLTalkSocketProcessor {

  private PropExchangeServer server;

  public PropExchangeServerProcessor() {
    super();
  }

  protected void setServer(PropExchangeServer server) {
    this.server = server;
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {
    server.registerSession(userSession);
  }

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    if (cmDatPair.getCommand() == PECC_ASYNC_SEND_PROPERTIES || cmDatPair.getCommand() == PECC_SYNC_SEND_PROPERTIES) {
      server.receiveProperties(userSession, cmDatPair);
    }
    else {
      userSession.endSession(); //Disconnect
    }
  }

}
