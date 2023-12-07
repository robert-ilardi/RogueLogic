/**
 * Created Jan 15, 2008
 */
package com.roguelogic.hptunnel;

import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_DATA_STREAM;
import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_EK_SHIFT;
import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_LOGIN;
import static com.roguelogic.hptunnel.HPTConstants.STATUS_CODE_SUCESS;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkUtils;
import com.roguelogic.net.rltalk.RLTalkXorCodec;

/**
 * @author Robert C. Ilardi
 *
 */

public class HttpProxyTunnelClient implements SocketProcessorCustomizer, SocketSessionSweeper {

  private HPTLocalProxy localProxy;
  private SocketSession browser;

  private SocketClient sockClient;
  private RLTalkXorCodec codec;

  private Object syncReqLock;

  private boolean loginResRec;
  private boolean loginOk;

  private boolean ekShiftResRec;
  private boolean ekShiftOk;

  public static final long LOGIN_REQUEST_TTL = 60000;
  public static final long EK_SHIFT_REQUEST_TTL = 60000;

  public HttpProxyTunnelClient() {
    syncReqLock = new Object();
  }

  public HPTLocalProxy getLocalProxy() {
    return localProxy;
  }

  public void setLocalProxy(HPTLocalProxy localProxy) {
    this.localProxy = localProxy;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    HttpProxyTunnelClientProcessor htpcPrc;

    if (processor instanceof HttpProxyTunnelClientProcessor) {
      htpcPrc = (HttpProxyTunnelClientProcessor) processor;
      htpcPrc.setClient(this);
    }
  }

  public synchronized void connect(String address, int port) throws HPTException, RLNetException {
    if (sockClient != null) {
      throw new HPTException("Client Already Connected!");
    }

    //Initialize Codec
    byte[] keyData = localProxy.getEncryptionKey();
    codec = new RLTalkXorCodec();
    codec.setKeyData(keyData);

    sockClient = new SocketClient();
    sockClient.setSocketProcessorClass(HttpProxyTunnelClientProcessor.class);
    sockClient.setSocketProcessorCustomizer(this);
    sockClient.setSocketSessionSweeper(this);
    sockClient.setReuseAddress(true);
    sockClient.connect(address, port);
  }

  public synchronized void close() {
    if (sockClient != null) {
      sockClient.close();
      sockClient = null;
    }
  }

  public synchronized boolean isConnected() {
    return sockClient != null ? sockClient.isConnected() : false;
  }

  public void processLogin(CommandDataPair response) throws RLNetException {
    synchronized (syncReqLock) {
      loginResRec = true;

      CommandDataPair plainResponse = codec.decrypt(response);
      loginOk = plainResponse.getStatusCode() == STATUS_CODE_SUCESS;

      syncReqLock.notifyAll();
    }
  }

  public synchronized void login(String username, String password) throws HPTException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    if (sockClient == null) {
      throw new HPTException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(HPT_RLTCMD_LOGIN);
    request.setData(new StringBuffer().append(username).append("|").append(password).toString());

    synchronized (syncReqLock) {
      loginResRec = false;
      loginOk = false;
      startTime = System.currentTimeMillis();

      sendEncrypted(sockClient.getUserSession(), request);

      while (!loginResRec && System.currentTimeMillis() < startTime + LOGIN_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!loginResRec) {
        throw new HPTException("Login Request Timed Out!");
      }

      if (!loginOk) {
        throw new HPTException("Login Failed!");
      }
    }
  }

  private void sendEncrypted(SocketSession userSession, CommandDataPair request) throws RLNetException {
    CommandDataPair cipherRequest = codec.encrypt(request);
    RLTalkUtils.RLTalkSend(userSession, cipherRequest);
  }

  public void setBrowser(SocketSession browser) {
    this.browser = browser;
  }

  public void sendDataThroughTunnel(byte[] data) throws HPTException, RLNetException {
    CommandDataPair request;

    if (sockClient == null) {
      throw new HPTException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(HPT_RLTCMD_DATA_STREAM);
    request.setData(data);

    sendEncrypted(sockClient.getUserSession(), request);
  }

  public void processIncomingDataStream(CommandDataPair cmDatPair) throws RLNetException {
    if (browser != null) {
      CommandDataPair plainCmDatPair = codec.decrypt(cmDatPair);
      browser.send(plainCmDatPair.getData()); //Relay raw data to the browser!
    }
  }

  public void cleanup(SocketSession userSession) {
    userSession.drainRawData();

    System.out.println("Proxy Tunnel Closed?");
    browser.clearUserData();
    browser.endSession();
    browser = null;
  }

  public void processEkShift(CommandDataPair response) throws RLNetException {
    synchronized (syncReqLock) {
      ekShiftResRec = true;

      ekShiftOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      if (ekShiftOk) {
        codec._setKeyIndexes(response.getInt());
      }

      syncReqLock.notifyAll();
    }
  }

  public void determineEncryptionKeyShift() throws HPTException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    if (sockClient == null) {
      throw new HPTException("Client NOT Connected!");
    }

    request = new CommandDataPair();
    request.setCommand(HPT_RLTCMD_EK_SHIFT);

    synchronized (syncReqLock) {
      ekShiftResRec = false;
      ekShiftOk = false;
      startTime = System.currentTimeMillis();

      RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);

      while (!ekShiftResRec && System.currentTimeMillis() < startTime + EK_SHIFT_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!ekShiftResRec) {
        throw new HPTException("EK Shift Request Timed Out!");
      }

      if (!ekShiftOk) {
        throw new HPTException("EK Shift Failed!");
      }
    }
  }

}
