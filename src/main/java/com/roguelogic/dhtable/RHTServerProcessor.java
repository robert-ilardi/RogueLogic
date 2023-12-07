/**
 * August 8, 2006 
 */
package com.roguelogic.dhtable;

import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_CLEAR_REQUEST;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_CLEAR_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_CONTAINS_KEY_REQUEST;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_CONTAINS_KEY_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_GET_KEY_LIST_REQUEST;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_GET_KEY_LIST_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_GET_REQUEST;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_GET_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_PUT_REQUEST;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_PUT_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_REMOVE_REQUEST;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTCC_HT_REMOVE_RESPONSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTSC_FAILURE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTSC_FALSE;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTSC_SUCCESS;
import static com.roguelogic.dhtable.RHTableCommandCodes.RHTSC_TRUE;

import java.io.Serializable;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 *
 */

public class RHTServerProcessor extends RLTalkSocketProcessor {

  private RemoteHashTable rhTable;

  public RHTServerProcessor() {
    super();
  }

  protected void setRHTable(RemoteHashTable rhTable) {
    this.rhTable = rhTable;
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
      case RHTCC_HT_CONTAINS_KEY_REQUEST:
        processContainsKeyRequest(cmDatPair);
        break;
      case RHTCC_HT_PUT_REQUEST:
        processPutRequest(cmDatPair);
        break;
      case RHTCC_HT_GET_REQUEST:
        processGetRequest(cmDatPair);
        break;
      case RHTCC_HT_REMOVE_REQUEST:
        processRemoveRequest(cmDatPair);
        break;
      case RHTCC_HT_CLEAR_REQUEST:
        processClearRequest(cmDatPair);
        break;
      case RHTCC_HT_GET_KEY_LIST_REQUEST:
        processGetKeyListRequest(cmDatPair);
        break;
      default:
        //Error or HACK Attempt so disconnect
        userSession.endSession();
    }
  }

  private void processContainsKeyRequest(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair response;
    Serializable key = null;
    boolean contains = false;

    response = new CommandDataPair();
    response.setCommand(RHTCC_HT_CONTAINS_KEY_RESPONSE);
    response.setMultiplexerIndex(cmDatPair.getMultiplexerIndex());
    response.setStatusCode(RHTSC_FAILURE);

    try {
      key = (Serializable) cmDatPair.getObject();

      if (key != null) {
        contains = rhTable.ltContainsKey(key);
      }

      response.setStatusCode((contains ? RHTSC_TRUE : RHTSC_FALSE));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    _rlTalkSend(response);
  }

  private void processPutRequest(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair response;
    KeyValuePair kvPair;

    response = new CommandDataPair();
    response.setCommand(RHTCC_HT_PUT_RESPONSE);
    response.setMultiplexerIndex(cmDatPair.getMultiplexerIndex());
    response.setStatusCode(RHTSC_FAILURE); //Assume we failed

    try {
      kvPair = (KeyValuePair) cmDatPair.getObject();

      if (kvPair != null) {
        rhTable.ltPut(kvPair.getKey(), kvPair.getValue());
      }

      response.setStatusCode(RHTSC_SUCCESS); //If the put is successful set to TRUE
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    _rlTalkSend(response);
  }

  private void processGetRequest(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair response;
    Serializable key, value;

    response = new CommandDataPair();
    response.setCommand(RHTCC_HT_GET_RESPONSE);
    response.setMultiplexerIndex(cmDatPair.getMultiplexerIndex());
    response.setStatusCode(RHTSC_FAILURE); //Assume we failed

    try {
      key = (Serializable) cmDatPair.getObject();

      if (key != null) {
        value = rhTable.ltGet(key);

        if (value != null) {
          response.setData(value);
        }
      }

      response.setStatusCode(RHTSC_SUCCESS); //If the get is successful set to TRUE
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    _rlTalkSend(response);
  }

  private void processRemoveRequest(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair response;
    Serializable key;

    response = new CommandDataPair();
    response.setCommand(RHTCC_HT_REMOVE_RESPONSE);
    response.setMultiplexerIndex(cmDatPair.getMultiplexerIndex());
    response.setStatusCode(RHTSC_FAILURE); //Assume we failed

    try {
      key = (Serializable) cmDatPair.getObject();

      if (key != null) {
        rhTable.ltRemove(key);
      }

      response.setStatusCode(RHTSC_SUCCESS); //If the remove is successful set to TRUE
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    _rlTalkSend(response);
  }

  private void processClearRequest(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair response;

    response = new CommandDataPair();
    response.setCommand(RHTCC_HT_CLEAR_RESPONSE);
    response.setMultiplexerIndex(cmDatPair.getMultiplexerIndex());

    rhTable.ltClear();
    response.setStatusCode(RHTSC_SUCCESS); //If the clear is successful set to TRUE

    _rlTalkSend(response);
  }

  private void processGetKeyListRequest(CommandDataPair cmDatPair) throws RLNetException {
    CommandDataPair response;
    KeyList keyList;

    response = new CommandDataPair();
    response.setCommand(RHTCC_HT_GET_KEY_LIST_RESPONSE);
    response.setMultiplexerIndex(cmDatPair.getMultiplexerIndex());
    response.setStatusCode(RHTSC_FAILURE); //Assume we failed

    try {
      keyList = rhTable.ltGetKeyList();
      response.setData(keyList);
      response.setStatusCode(RHTSC_SUCCESS); //If the get is successful set to TRUE
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    _rlTalkSend(response);
  }

}
