/**
 * Created Jan 15, 2006
 */
package com.roguelogic.propexchange;

import static com.roguelogic.propexchange.PropExchangeCommandCodes.PECC_SYNC_SEND_PROPERTIES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkAsyncHelper;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class PropExchangeClient implements SocketProcessorCustomizer, SocketSessionSweeper {

  private String address;
  private int port;

  private SocketClient sockClient;

  private RLTalkAsyncHelper rltAsyncHelper;
  private HashMap<Integer, PropExchangePayload> payloadTransactionMap;

  private ArrayList<PropExchangeObserver> observers;
  private Object observersLock;

  public PropExchangeClient(String address, int port) {
    this.address = address;
    this.port = port;

    observers = new ArrayList<PropExchangeObserver>();
    observersLock = new Object();

    rltAsyncHelper = new RLTalkAsyncHelper();
    payloadTransactionMap = new HashMap<Integer, PropExchangePayload>();
  }

  public void startClient() throws RLNetException {
    sockClient = new SocketClient();
    sockClient.setSocketProcessorClass(PropExchangeClientProcessor.class);
    sockClient.setSocketProcessorCustomizer(this);
    sockClient.setSocketSessionSweeper(this);
    sockClient.connect(address, port);

    rltAsyncHelper.resetAllTransactions();

    System.out.println(Version.APP_TITLE_CLIENT + " Connected to Server Running at: " + address + " on Port = " + port);
  }

  public void stopClient() {
    if (sockClient != null) {
      sockClient.close();
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    PropExchangeClientProcessor pecProcessor;

    try {
      pecProcessor = (PropExchangeClientProcessor) processor;
      pecProcessor.setClient(this);
    }
    catch (Exception e) {
      throw new RLNetException("Could NOT Initialize " + Version.APP_TITLE_CLIENT + " Processor!", e);
    }
  }

  public void cleanup(SocketSession userSession) {
    rltAsyncHelper.killAllTransactions();
  }

  public boolean isConnected() {
    return (sockClient != null ? sockClient.isConnected() : false);
  }

  private synchronized void send(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
    RLTalkUtils.RLTalkSend(sockClient.getUserSession(), cmDatPair);
  }

  private synchronized void sendAndWait(CommandDataPair cmDatPair, int transactionId, int timeout) throws RLTalkException, RLNetException, InterruptedException {
    rltAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transactionId, timeout);
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public void send(Properties props) throws RLTalkException, RLNetException {
    CommandDataPair cmDatPair;
    PropExchangePayload payload;
    String reqId;

    payload = new PropExchangePayload();
    payload.setProps(props);
    payload.setSynchronous(false);

    reqId = PropExchangeUtils.GenerateRequestId();
    payload.setRequestId(reqId);

    cmDatPair = PropExchangeUtils.CreateAsyncCmDatPair(payload);
    send(cmDatPair);
  }

  public PropExchangePayload sendAndWait(Properties props, int timeout) throws RLTalkException, RLNetException, InterruptedException, PropExchangeException {
    CommandDataPair cmDatPair;
    int transactionId = -1;
    PropExchangePayload reply;
    PropExchangePayload payload;
    String reqId;

    try {
      transactionId = rltAsyncHelper.obtainTransactionId();

      payload = new PropExchangePayload();
      payload.setProps(props);

      payload.setSynchronous(true);
      payload.setSyncId(transactionId);

      reqId = PropExchangeUtils.GenerateRequestId();
      payload.setRequestId(reqId);

      cmDatPair = PropExchangeUtils.CreateSyncCmDatPair(payload);
      sendAndWait(cmDatPair, transactionId, timeout);

      if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
        throw new PropExchangeException("Synchronous operation FAILED!");
      }

      reply = payloadTransactionMap.get(transactionId);
    }
    finally {
      rltAsyncHelper.releaseTransactionId(transactionId);
    }

    return reply;
  }

  public void addObserver(PropExchangeObserver observer) {
    synchronized (observersLock) {
      observers.add(observer);
    }
  }

  public void removeObserver(PropExchangeObserver observer) {
    synchronized (observersLock) {
      observers.remove(observer);
    }
  }

  public void removeAllObservers() {
    synchronized (observersLock) {
      observers.clear();
    }
  }

  public void receiveProperties(CommandDataPair cmDatPair) throws RLNetException {
    PropExchangePayload payload = null;
    int transactionId = -1;
    boolean synchronous = false;

    try {
      transactionId = cmDatPair.getMultiplexerIndex();
      synchronous = cmDatPair.getCommand() == PECC_SYNC_SEND_PROPERTIES;

      payload = PropExchangeUtils.DecodePayload(cmDatPair);

      if (synchronous) {
        //Synchronous Transaction
        payloadTransactionMap.put(transactionId, payload);
        rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
      }
      else {
        //Asynchronous Transaction
        synchronized (observersLock) {
          for (PropExchangeObserver observer : observers) {
            observer.receive(payload);
          }
        }
      } //End else block
    } //End try block
    catch (Exception e) {
      if (synchronous) {
        rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
      }

      throw new RLNetException("An error occurred while the Properties Exchange Client attempted to process the Command Data Pair!", e);
    }
  }

}
