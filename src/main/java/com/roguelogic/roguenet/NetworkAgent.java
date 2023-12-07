/**
 * Created Sep 23, 2006
 */
package com.roguelogic.roguenet;

import static com.roguelogic.roguenet.RNAConstants.PROP_P2PHUB_ADDRESS;
import static com.roguelogic.roguenet.RNAConstants.PROP_P2PHUB_HEART_BEAT_INTERVAL;
import static com.roguelogic.roguenet.RNAConstants.PROP_P2PHUB_KEY_FILE;
import static com.roguelogic.roguenet.RNAConstants.PROP_P2PHUB_PASSWORD;
import static com.roguelogic.roguenet.RNAConstants.PROP_P2PHUB_PORT;
import static com.roguelogic.roguenet.RNAConstants.PROP_P2PHUB_USERNAME;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.p2phub.P2PHubUtils;
import com.roguelogic.p2phub.client.P2PHubClient;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class NetworkAgent {

  private Properties rnaProps;

  private String p2pHubAddress;
  private int p2pHubPort;
  private String p2pHubKeyFile;
  private String p2pHubUsername;
  private String p2pHubPassword;
  private int p2pHubHBIntervalSecs;

  private Date activationTs;
  private Date deactivationTs;

  private P2PHubClient p2phClient;
  private ConnectionMonitor connMonitor;
  private MessageProcessor mesgProcessor;
  private PlugInManager plugInManager;

  private long mesgSentCnt;

  public NetworkAgent(Properties rnaProps) {
    this.rnaProps = rnaProps;
  }

  private void readProperties() {
    String tmp;

    p2pHubAddress = rnaProps.getProperty(PROP_P2PHUB_ADDRESS);

    tmp = rnaProps.getProperty(PROP_P2PHUB_PORT);
    p2pHubPort = Integer.parseInt(tmp);

    p2pHubKeyFile = rnaProps.getProperty(PROP_P2PHUB_KEY_FILE);
    p2pHubUsername = rnaProps.getProperty(PROP_P2PHUB_USERNAME);
    p2pHubPassword = rnaProps.getProperty(PROP_P2PHUB_PASSWORD);

    tmp = rnaProps.getProperty(PROP_P2PHUB_HEART_BEAT_INTERVAL);
    p2pHubHBIntervalSecs = Integer.parseInt(tmp);
  }

  public synchronized void activate() throws RogueNetException {
    if (p2phClient != null) {
      throw new RogueNetException("Network Agent already participating on a P2P Network...");
    }

    try {
      readProperties();

      startConnectionDaemon();

      deactivationTs = null;
      activationTs = new Date();

      RNALogger.GetLogger().stdOutPrintln("Agent Activation Time: " + activationTs + "\n");
    } //End try block
    catch (Exception e) {
      throw new RogueNetException("An error occurred while attempting to Activate the Network Agent.", e);
    }
  }

  public synchronized void deactivate() throws RogueNetException {
    try {
      stopConnectionDaemon();

      activationTs = null;
      deactivationTs = new Date();

      RNALogger.GetLogger().stdOutPrintln("Agent Deactivation Time: " + deactivationTs);
    } //End try block
    catch (Exception e) {
      throw new RogueNetException("An error occurred while attempting to Activate the Network Agent.", e);
    }
  }

  public boolean isUp() {
    return (p2phClient != null && p2phClient.isConnected());
  }

  public void setPlugInManager(PlugInManager plugInManager) {
    this.plugInManager = plugInManager;
  }

  private void startConnectionDaemon() throws IOException, InterruptedException, RogueNetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    P2PHubPeer peer;
    byte[] keyData;

    keyData = P2PHubUtils.LoadKeyData(p2pHubKeyFile);

    peer = new P2PHubPeer();
    peer.setUsername(p2pHubUsername);
    peer.setPassword(p2pHubPassword);

    mesgSentCnt = 0;

    mesgProcessor = new MessageProcessor();
    mesgProcessor.start();

    if (plugInManager != null) {
      mesgProcessor.setPlugInManager(plugInManager);
      plugInManager.loadPlugIns();
    }
    else {
      RNALogger.GetLogger().warn(">>>> WARNING!!! Plug-In Manager NOT Loaded... ALL Messages will be ignored!");
    }

    p2phClient = new P2PHubClient(p2pHubAddress, p2pHubPort, peer, keyData, mesgProcessor);
    p2phClient.enableHeartBeating(p2pHubHBIntervalSecs);

    connMonitor = new ConnectionMonitor(this, rnaProps);
    connMonitor.start();
  }

  private void stopConnectionDaemon() throws IOException, InterruptedException {
    if (mesgProcessor != null) {
      mesgProcessor.setHalt(true);
    }

    if (connMonitor != null) {
      try {
        connMonitor.stop();
      }
      catch (Exception e) {
        RNALogger.GetLogger().log(e);
      }
    }

    if (p2phClient != null) {
      try {
        p2phClient.stop();
        p2phClient = null;
      }
      catch (Exception e) {
        RNALogger.GetLogger().log(e);
      }
    }

    if (plugInManager != null) {
      plugInManager.unloadPlugIns();
    }
  }

  protected P2PHubClient getP2PHubclient() {
    return p2phClient;
  }

  protected synchronized void ensureConnection() throws RogueNetException {
    try {
      if (!p2phClient.isConnected()) {
        RNALogger.GetLogger().stdOutPrintln("Ensure Connection> NOT Connected to P2P Hub Network... Attempting to establish connection. (" + StringUtils.GetTimeStamp() + ")");

        p2phClient.stop();
        p2phClient.start();

        if (!p2phClient.login()) {
          throw new RogueNetException("Ensure Connection : Login to P2P Hub Server failed!");
        }
      } //End isConnected check
    } //End try block
    catch (RogueNetException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RogueNetException("Ensure Connection Failed!", e);
    }
  }

  public synchronized P2PHubPeer[] getPeerList() throws RogueNetException {
    try {
      ensureConnection();

      return p2phClient.getPeerList();
    }
    catch (Exception e) {
      throw new RogueNetException("Get Peer List from P2P Hub Server Failed!", e);
    }
  }

  public synchronized boolean sendMessage(P2PHubMessage mesg) throws RogueNetException {
    boolean sent;
    try {
      ensureConnection();

      sent = p2phClient.sendMessage(mesg);
      mesgSentCnt++;

      return sent;
    }
    catch (Exception e) {
      throw new RogueNetException("Send P2P Hub Message failed! Message = \n" + mesg.toXML(), e);
    }
  }

  public void registerListener(String listenerName, String subject) {
    mesgProcessor.registerListener(listenerName, subject);
  }

  public void unregisterListener(String listenerName, String subject) {
    mesgProcessor.unregisterListener(listenerName, subject);
  }

  public boolean isConnected() {
    return (p2phClient != null ? p2phClient.isConnected() : false);
  }

  public String getHubAddress() {
    return (p2phClient != null ? p2phClient.getAddress() : p2pHubAddress);
  }

  public int getHubPort() {
    return (p2phClient != null ? p2phClient.getPort() : p2pHubPort);
  }

  public RNAPlugIn getPlugIn(String plugInName) {
    RNAPlugIn plugIn = null;

    if (plugInManager != null) {
      plugIn = plugInManager.getPlugIn(plugInName);
    }

    return plugIn;
  }

  public Date getActivationTs() {
    return activationTs;
  }

  public Date getDeactivationTs() {
    return deactivationTs;
  }

  public String getHubUsername() {
    return p2pHubUsername;
  }

  public String getHubSessionToken() {
    return (p2phClient != null && p2phClient.getSelf() != null ? p2phClient.getSelf().getSessionToken() : null);
  }

  public long getMesgSentCnt() {
    return mesgSentCnt;
  }

  public long getMesgReceivedCnt() {
    return (mesgProcessor != null ? mesgProcessor.getMesgReceivedCnt() : 0);
  }

}
