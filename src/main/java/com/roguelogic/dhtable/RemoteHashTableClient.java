/*
 * Created on Aug 11, 2006
 */
package com.roguelogic.dhtable;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkAsyncHelper;

/**
 * @author rilardi
 */

public class RemoteHashTableClient implements SocketProcessorCustomizer {

  protected String address;
  protected int port;

  protected SocketClient client;

  private RLTalkAsyncHelper rltAsyncHelper;

  protected HashMap<Integer, Boolean> containsTransactionMap;
  protected HashMap<Integer, Serializable> getValueTransactionMap;
  protected HashMap<Integer, KeyList> keyListTransactionMap;

  public static final int CONTAINS_KEY_TIMEOUT = 60;
  public static final int PUT_TIMEOUT = 60;
  public static final int GET_TIMEOUT = 60;
  public static final int REMOVE_TIMEOUT = 60;
  public static final int CLEAR_TIMEOUT = 60;
  public static final int GET_KEY_LIST_TIMEOUT = 60;

  public RemoteHashTableClient(String address, int port) {
    this.address = address;
    this.port = port;

    rltAsyncHelper = new RLTalkAsyncHelper();
    containsTransactionMap = new HashMap<Integer, Boolean>();
    getValueTransactionMap = new HashMap<Integer, Serializable>();
    keyListTransactionMap = new HashMap<Integer, KeyList>();
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    RHTClientProcessor rhtcProcessor;

    if (processor instanceof RHTClientProcessor) {
      rhtcProcessor = (RHTClientProcessor) processor;
      rhtcProcessor.setRHTClient(this);
    }
  }

  protected synchronized void ensureConnection() throws RLNetException {
    if (client == null || !client.isConnected()) {
      client = new SocketClient();
      client.setSocketProcessorClass(RHTClientProcessor.class);
      client.setSocketProcessorCustomizer(this);
      client.connect(address, port);
    }
  }

  public synchronized void destroy() {
    if (client != null) {
      client.close();
    }
  }

  public void processContainsKeyResponse(CommandDataPair cmDatPair) {
    int transactionId;
    boolean contains;

    contains = (cmDatPair.getStatusCode() == RHTableCommandCodes.RHTSC_TRUE);
    transactionId = cmDatPair.getMultiplexerIndex();

    containsTransactionMap.put(transactionId, contains);

    if (cmDatPair.getStatusCode() == RHTableCommandCodes.RHTSC_TRUE
        || cmDatPair.getStatusCode() == RHTableCommandCodes.RHTSC_FALSE) {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
    }
    else {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
    }
  }

  public void processPutResponse(CommandDataPair cmDatPair) {
    int transactionId;

    transactionId = cmDatPair.getMultiplexerIndex();

    if (cmDatPair.getStatusCode() == RHTableCommandCodes.RHTSC_SUCCESS) {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
    }
    else {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
    }
  }

  public void processRemoveResponse(CommandDataPair cmDatPair) {
    int transactionId;

    transactionId = cmDatPair.getMultiplexerIndex();

    if (cmDatPair.getStatusCode() == RHTableCommandCodes.RHTSC_SUCCESS) {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
    }
    else {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
    }
  }

  public void processGetResponse(CommandDataPair cmDatPair) {
    int transactionId;
    Serializable value;

    transactionId = cmDatPair.getMultiplexerIndex();

    if (cmDatPair.getStatusCode() != RHTableCommandCodes.RHTSC_SUCCESS) {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
    }
    else {
      try {
        value = (Serializable) cmDatPair.getObject();
        getValueTransactionMap.put(transactionId, value);
        rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
      }
      catch (Exception e) {
        e.printStackTrace();
        rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
      }
    }
  }

  public void processClearResponse(CommandDataPair cmDatPair) {
    int transactionId;

    transactionId = cmDatPair.getMultiplexerIndex();

    if (cmDatPair.getStatusCode() == RHTableCommandCodes.RHTSC_SUCCESS) {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
    }
    else {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
    }
  }

  public void processGetKeyListResponse(CommandDataPair cmDatPair) {
    int transactionId;
    KeyList keyList;

    transactionId = cmDatPair.getMultiplexerIndex();

    if (cmDatPair.getStatusCode() != RHTableCommandCodes.RHTSC_SUCCESS) {
      rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
    }
    else {
      try {
        keyList = (KeyList) cmDatPair.getObject();
        keyListTransactionMap.put(transactionId, keyList);
        rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
      }
      catch (Exception e) {
        e.printStackTrace();
        rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
      }
    }
  }

  public synchronized boolean containsKey(Serializable key) throws RLNetException, IOException, InterruptedException,
      RemoteHashTableException {
    boolean contains = false;
    CommandDataPair cmDatPair;
    int transactionId = -1;

    try {
      if (key != null) {
        ensureConnection();

        transactionId = rltAsyncHelper.obtainTransactionId();

        cmDatPair = new CommandDataPair();
        cmDatPair.setCommand(RHTableCommandCodes.RHTCC_HT_CONTAINS_KEY_REQUEST);
        cmDatPair.setMultiplexerIndex(transactionId);
        cmDatPair.setData(key);

        //Send and Wait for Response
        rltAsyncHelper.sendAndWait(client.getUserSession(), cmDatPair, transactionId, CONTAINS_KEY_TIMEOUT);

        if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
          throw new RemoteHashTableException("Remote CONTAINS KEY operation FAILED!");
        }

        contains = containsTransactionMap.get(transactionId);
      } //End null key check
    }
    finally {
      rltAsyncHelper.releaseTransactionId(transactionId);
    }

    return contains;
  }

  public synchronized void put(Serializable key, Serializable value) throws RLNetException, IOException,
      InterruptedException, RemoteHashTableException {
    CommandDataPair cmDatPair;
    KeyValuePair kvPair;
    int transactionId = -1;

    try {
      if (key != null && value != null) {
        transactionId = rltAsyncHelper.obtainTransactionId();

        ensureConnection();

        cmDatPair = new CommandDataPair();
        cmDatPair.setCommand(RHTableCommandCodes.RHTCC_HT_PUT_REQUEST);

        kvPair = new KeyValuePair(key, value);
        cmDatPair.setData(kvPair);

        rltAsyncHelper.sendAndWait(client.getUserSession(), cmDatPair, transactionId, PUT_TIMEOUT);

        if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
          throw new RemoteHashTableException("Remote PUT operation FAILED!");
        }
      } //End null parameter check
    } //End try block
    finally {
      rltAsyncHelper.releaseTransactionId(transactionId);
    }
  }

  public synchronized Serializable get(Serializable key) throws RLNetException, IOException, InterruptedException,
      RemoteHashTableException {
    Serializable value = null;
    CommandDataPair cmDatPair;
    int transactionId = -1;

    try {
      if (key != null) {
        ensureConnection();

        transactionId = rltAsyncHelper.obtainTransactionId();

        cmDatPair = new CommandDataPair();
        cmDatPair.setCommand(RHTableCommandCodes.RHTCC_HT_GET_REQUEST);
        cmDatPair.setMultiplexerIndex(transactionId);
        cmDatPair.setData(key);

        //Send and Wait for Response
        rltAsyncHelper.sendAndWait(client.getUserSession(), cmDatPair, transactionId, GET_TIMEOUT);

        if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
          throw new RemoteHashTableException("Remote GET operation FAILED!");
        }

        value = getValueTransactionMap.get(transactionId);
      } //End null key check
    }
    finally {
      rltAsyncHelper.releaseTransactionId(transactionId);
    }

    return value;
  }

  public synchronized void remove(Serializable key) throws RLNetException, IOException, InterruptedException,
      RemoteHashTableException {
    CommandDataPair cmDatPair;
    int transactionId = -1;

    try {
      if (key != null) {
        ensureConnection();

        transactionId = rltAsyncHelper.obtainTransactionId();

        cmDatPair = new CommandDataPair();
        cmDatPair.setCommand(RHTableCommandCodes.RHTCC_HT_REMOVE_REQUEST);
        cmDatPair.setMultiplexerIndex(transactionId);
        cmDatPair.setData(key);

        //Send and Wait for Response
        rltAsyncHelper.sendAndWait(client.getUserSession(), cmDatPair, transactionId, REMOVE_TIMEOUT);

        if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
          throw new RemoteHashTableException("Remote REMOVE operation FAILED!");
        }
      } //End null key check
    }
    finally {
      rltAsyncHelper.releaseTransactionId(transactionId);
    }
  }

  public synchronized void clear() throws RLNetException, IOException, InterruptedException, RemoteHashTableException {
    CommandDataPair cmDatPair;
    int transactionId = -1;

    try {
      ensureConnection();

      transactionId = rltAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(RHTableCommandCodes.RHTCC_HT_CLEAR_REQUEST);
      cmDatPair.setMultiplexerIndex(transactionId);

      //Send and Wait for Response
      rltAsyncHelper.sendAndWait(client.getUserSession(), cmDatPair, transactionId, CLEAR_TIMEOUT);

      if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
        throw new RemoteHashTableException("Remote CLEAR operation FAILED!");
      }
    }
    finally {
      rltAsyncHelper.releaseTransactionId(transactionId);
    }
  }

  public synchronized KeyList getKeyList() throws RLNetException, IOException, InterruptedException,
      RemoteHashTableException {
    CommandDataPair cmDatPair;
    KeyList keyList;
    int transactionId = -1;

    try {
      ensureConnection();

      transactionId = rltAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(RHTableCommandCodes.RHTCC_HT_GET_KEY_LIST_REQUEST);
      cmDatPair.setMultiplexerIndex(transactionId);

      //Send and Wait for Response
      rltAsyncHelper.sendAndWait(client.getUserSession(), cmDatPair, transactionId, GET_KEY_LIST_TIMEOUT);

      if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
        throw new RemoteHashTableException("Remote GET KEY LIST operation FAILED!");
      }

      keyList = keyListTransactionMap.get(transactionId);
    }
    finally {
      rltAsyncHelper.releaseTransactionId(transactionId);
    }

    return keyList;
  }

}
