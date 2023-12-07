/*
 * Created on Aug 11, 2006
 */

package com.roguelogic.dhtable;

import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_CLEAR_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_CONTAINS_KEY_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_GET_KEY_LIST_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_GET_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_PUT_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_REMOVE_RESPONSE;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author rilardi
 */

public class RHTClientProcessor extends RLTalkSocketProcessor {

  private RemoteHashTableClient rhtClient;

  public RHTClientProcessor() {}

  protected void setRHTClient(RemoteHashTableClient rhtClient) {
    this.rhtClient = rhtClient;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandshake()
   */
  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandle(com.roguelogic.net.rltalk.CommandDataPair)
   */
  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    switch (cmDatPair.getCommand()) {
      case RHTCC_HT_CONTAINS_KEY_RESPONSE:
        rhtClient.processContainsKeyResponse(cmDatPair);
        break;
      case RHTCC_HT_PUT_RESPONSE:
        rhtClient.processPutResponse(cmDatPair);
        break;
      case RHTCC_HT_GET_RESPONSE:
        rhtClient.processGetResponse(cmDatPair);
        break;
      case RHTCC_HT_REMOVE_RESPONSE:
        rhtClient.processRemoveResponse(cmDatPair);
        break;
      case RHTCC_HT_CLEAR_RESPONSE:
        rhtClient.processClearResponse(cmDatPair);
        break;
      case RHTCC_HT_GET_KEY_LIST_RESPONSE:
        rhtClient.processGetKeyListResponse(cmDatPair);
        break;
      default:
        //Error or HACK Attempt so disconnect
        userSession.endSession();
    }
  }

}
