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

public class PropExchangeClientProcessor extends RLTalkSocketProcessor {

  private PropExchangeClient client;

  public PropExchangeClientProcessor() {
    super();
  }

  protected void setClient(PropExchangeClient client) {
    this.client = client;
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    if (cmDatPair.getCommand() == PECC_ASYNC_SEND_PROPERTIES || cmDatPair.getCommand() == PECC_SYNC_SEND_PROPERTIES) {
      client.receiveProperties(cmDatPair);
    }
    else {
      userSession.endSession(); //Disconnect
    }
  }

}
