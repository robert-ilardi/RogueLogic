/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

import java.io.IOException;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkUtils;

/**
 * @author Robert C. Ilardi
 * 
 */
public class IpcClient implements SocketProcessorCustomizer, SocketSessionSweeper {

  private String address;
  private int port;
  private SocketClient sockClient;
  private IpcProcessHost processHost;

  private String lastEventId;

  public IpcClient(IpcProcessHost processHost, String address, int port) {
    this.address = address;
    this.port = port;
    this.processHost = processHost;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    IpcClientSockProcessor aicsp = (IpcClientSockProcessor) processor;
    aicsp.setClient(this);
  }

  private synchronized void connect() throws RLNetException {
    sockClient = new SocketClient();
    sockClient.setSocketProcessorClass(IpcClientSockProcessor.class);
    sockClient.setSocketProcessorCustomizer(this);
    sockClient.setSocketSessionSweeper(this);
    sockClient.connect(address, port);
  }

  public synchronized void disconnect() throws RLNetException {
    if (sockClient != null) {
      sockClient.close();
      sockClient = null;
    }
  }

  public synchronized void ensureConnection() throws RLNetException {
    if (sockClient == null || !sockClient.isConnected()) {
      disconnect();
      connect();
    }
  }

  public synchronized void send(IpcEvent event) throws RLNetException, IOException {
    CommandDataPair cmDatPair;

    lastEventId = event.getEventId();

    ensureConnection();

    cmDatPair = new CommandDataPair();
    cmDatPair.setCommand(IpcConstants.AMG_TLK_CMD_SEND_EVENT);
    cmDatPair.setData(event);

    RLTalkUtils.RLTalkSend(sockClient.getUserSession(), cmDatPair);
  }

  public void processIpcAck(CommandDataPair cmDatPair) {
    processHost.processIpcAck(cmDatPair);
  }

  public boolean isAvailable() {
    boolean processUp;

    try {
      ensureConnection();
      processUp = true;
    }
    catch (Exception e) {
      try {
        disconnect();
      }
      catch (Exception e2) {
      }

      processUp = false;
    }

    return processUp;
  }

  public void cleanup(SocketSession userSession) {
    processHost.processLostConnection(lastEventId);
  }

}
