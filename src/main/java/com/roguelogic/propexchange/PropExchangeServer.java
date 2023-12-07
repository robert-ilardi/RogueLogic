/**
 * Created Jan 15, 2007
 */
package com.roguelogic.propexchange;

import java.util.ArrayList;
import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class PropExchangeServer implements SocketProcessorCustomizer, SocketSessionSweeper {

  private SocketServer server;
  private int port;
  private String bindAddress;

  private ArrayList<PropExchangeObserver> observers;
  private ArrayList<PropExchangeObserver> observersClonedList;
  private Object observersLock;

  public static final int DEFAULT_INIT_WORKER_CNT = 1;
  public static final int DEFAULT_MAX_WORKER_CNT = 25;

  public PropExchangeServer() {
    observers = new ArrayList<PropExchangeObserver>();
    observersLock = new Object();
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setBindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    PropExchangeServerProcessor pesProcessor;

    try {
      pesProcessor = (PropExchangeServerProcessor) processor;
      pesProcessor.setServer(this);
    }
    catch (Exception e) {
      throw new RLNetException("Could NOT Initialize " + Version.APP_TITLE_SERVER + " Processor!", e);
    }
  }

  public void cleanup(SocketSession userSession) {
    if (userSession != null) {
      unregisterSession(userSession);
    }
  }

  public void startServer() throws RLNetException {
    server = new SocketServer();
    server.setSocketProcessorClass(PropExchangeServerProcessor.class);
    server.setInitialWorkersCnt(DEFAULT_INIT_WORKER_CNT);
    server.setMaxWorkersCnt(DEFAULT_MAX_WORKER_CNT);
    server.setSocketProcessorCustomizer(this);
    server.setSocketSessionSweeper(this);
    server.listen(bindAddress, port);
  }

  public void stopServer() {
    if (server != null) {
      server.close();
    }
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

      if (observersClonedList != null) {
        observersClonedList.clear();
      }
    }
  }

  public void registerSession(SocketSession session) {
    PropExchangePayload payload;

    payload = new PropExchangePayload();
    payload.setSession(session);

    synchronized (observersLock) {
      for (PropExchangeObserver observer : observers) {
        observer.register(payload);
      }
    }
  }

  public void unregisterSession(SocketSession session) {
    PropExchangePayload payload;

    payload = new PropExchangePayload();
    payload.setSession(session);

    synchronized (observersLock) {
      for (PropExchangeObserver observer : observers) {
        observer.unregister(payload);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void receiveProperties(SocketSession session, CommandDataPair cmDatPair) throws RLNetException {
    PropExchangePayload payload = null;

    try {
      payload = PropExchangeUtils.DecodePayload(cmDatPair);
      payload.setSession(session);

      synchronized (observersLock) {
        if (observersClonedList == null || observersClonedList.size() != observers.size()) {
          observersClonedList = (ArrayList) observers.clone();
        }
      }

      for (PropExchangeObserver observer : observersClonedList) {
        observer.receive(payload);
      }
    } //End try block
    catch (Exception e) {
      throw new RLNetException("An error occurred while the Properties Exchange Server attempted to process the Command Data Pair!", e);
    }
  }

  public void send(Properties props, SocketSession sockSession) throws RLTalkException, RLNetException {
    send(props, sockSession, -1);
  }

  public void send(Properties props, SocketSession sockSession, int syncId) throws RLTalkException, RLNetException {
    CommandDataPair cmDatPair;
    PropExchangePayload payload;
    String reqId;

    payload = new PropExchangePayload();
    payload.setProps(props);
    payload.setSynchronous((syncId >= 0));
    payload.setSyncId(syncId);

    reqId = PropExchangeUtils.GenerateRequestId();
    payload.setRequestId(reqId);

    if (payload.isSynchronous()) {
      cmDatPair = PropExchangeUtils.CreateSyncCmDatPair(payload);
    }
    else {
      cmDatPair = PropExchangeUtils.CreateAsyncCmDatPair(payload);
    }

    send(cmDatPair, sockSession);
  }

  private synchronized void send(CommandDataPair cmDatPair, SocketSession sockSession) throws RLTalkException, RLNetException {
    RLTalkUtils.RLTalkSend(sockSession, cmDatPair);
  }

}
