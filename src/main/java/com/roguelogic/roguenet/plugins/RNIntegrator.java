/**
 * Created Oct 22, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.RN_INTEGRATOR_ACTION_GET_APP_LIST;
import static com.roguelogic.roguenet.RNAConstants.RN_INTEGRATOR_ACTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.RN_INTEGRATOR_ACTION_SEND_DATA;
import static com.roguelogic.roguenet.RNAConstants.RN_INTEGRATOR_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.roguelogic.net.SocketSession;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.propexchange.PropExchangeObserver;
import com.roguelogic.propexchange.PropExchangePayload;
import com.roguelogic.propexchange.PropExchangeServer;
import com.roguelogic.rni.RNIAppInfo;
import com.roguelogic.rni.RNIClient;
import com.roguelogic.roguenet.MTMQProcessor;
import com.roguelogic.roguenet.MTMQueue;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNALogger;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.SynchronousTransactionRequestor;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNIntegrator implements RNAPlugIn, PropExchangeObserver, SynchronousTransactionRequestor, MTMQProcessor {

  private PlugInManager plugInManager;

  private static RNAPlugInInfo RNIntegratorPII;

  public static final String PPI_LOGICAL_NAME = "Rogue Net Integrator";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Rogue Net Integrator integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    RNIntegratorPII = new RNAPlugInInfo();

    RNIntegratorPII.setLogicalName(PPI_LOGICAL_NAME);
    RNIntegratorPII.setVersion(PPI_VERSION);
    RNIntegratorPII.setDescription(PPI_DESCRIPTION);
    RNIntegratorPII.setDeveloper(PPI_DEVELOPER);
    RNIntegratorPII.setUrl(PPI_URL);
    RNIntegratorPII.setCopyright(PPI_COPYRIGHT);
  }

  public static final String PROP_RNINTEGRATOR_PORT = "RNIntegratorPort";
  public static final String PROP_RNINTEGRATOR_ENABLED = "RNIntegratorEnabled";

  public static final String LOCALHOST = "127.0.0.1";

  public static final long APP_LIST_WAIT_PERIOD = 63000;

  private PropExchangeServer server;

  private boolean rniEnabled;

  private HashMap<String, SocketSession> appMap;
  private Object amLock;

  private HashMap<String, P2PHubMessage> transactionMap;
  private Object tmLock;

  private MTMQueue mtmQueue;

  public RNIntegrator() {
    appMap = new HashMap<String, SocketSession>();
    amLock = new Object();

    transactionMap = new HashMap<String, P2PHubMessage>();
    tmLock = new Object();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    mtmQueue = new MTMQueue();
    mtmQueue.setProcessor(this);
    mtmQueue.start();

    this.plugInManager = plugInManager;
    plugInManager.register(this, RN_INTEGRATOR_SUBJECT, null);

    startServer();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, RN_INTEGRATOR_SUBJECT, null);

    if (mtmQueue != null) {
      mtmQueue.stop();
    }

    stopServer();
  }

  private void startServer() throws RogueNetException {
    String tmp;
    int port;

    try {
      tmp = plugInManager.getProperty(PROP_RNINTEGRATOR_ENABLED);
      rniEnabled = "TRUE".equalsIgnoreCase(tmp);

      if (rniEnabled) {
        tmp = plugInManager.getProperty(PROP_RNINTEGRATOR_PORT);
        port = Integer.parseInt(tmp);

        server = new PropExchangeServer();
        server.setPort(port);
        server.setBindAddress(LOCALHOST);
        server.addObserver(this);
        server.startServer();
      }
    } //End try block
    catch (Exception e) {
      throw new RogueNetException("An error occurred while attempting to start RN Integrator Server!", e);
    }
  }

  private void stopServer() {
    if (server != null) {
      server.stopServer();
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    //Actual processing for the plug-in happens in the processMessageMT method.
    //Because this plug-in may take significant amounts of time to process...
    //So everything must be multi-threaded...
    if (mtmQueue != null) {
      mtmQueue.enqueue(mesg);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return RNIntegratorPII;
  }

  public void processMessageMT(P2PHubMessage mesg) {
    String action;

    action = mesg.getProperty(RN_INTEGRATOR_ACTION_PROP);

    if (RN_INTEGRATOR_ACTION_GET_APP_LIST.equalsIgnoreCase(action)) {
      //Perform Get Local App List
      performGetLocalAppList(mesg);
    }
    else if (RN_INTEGRATOR_ACTION_SEND_DATA.equalsIgnoreCase(action)) {
      //Perform Send Data
      performSendData(mesg);
    }
  }

  private void performGetLocalAppList(P2PHubMessage mesg) {
    P2PHubMessage response;
    Properties resProps;
    int cnt;
    StringBuffer sb;
    Iterator iter;
    String appInfoStr;

    try {
      if (plugInManager != null && plugInManager.getNetAgent() != null) {
        response = new P2PHubMessage();
        response.setRecipients(new String[] { mesg.getSender() });
        response.setSubject(SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT);

        resProps = new Properties();
        response.setProperties(resProps);

        resProps.setProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP, mesg.getProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP));

        synchronized (amLock) {
          cnt = 1;

          iter = appMap.keySet().iterator();

          while (iter.hasNext()) {
            appInfoStr = (String) iter.next();

            sb = new StringBuffer(RNIClient.PROP_PREFIX_APP);
            sb.append(cnt);

            if (!StringUtils.IsNVL(appInfoStr)) {
              resProps.setProperty(sb.toString(), appInfoStr);
            }

            cnt++;
          } //End while iter.hasNext
        } //End synchronized block on amLock

        plugInManager.getNetAgent().sendMessage(response);
      }
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  private void performSendData(P2PHubMessage mesg) {
    Properties props;
    String appStr;
    SocketSession appSession;

    try {
      props = mesg.getProperties();

      if (props != null) {
        appStr = props.getProperty(RNIClient.TARGET_APP_STR_PROP);
        appSession = appMap.get(appStr);

        if (appSession != null) {
          server.send(props, appSession);
        }
      } //End props null check
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  public void register(PropExchangePayload payload) {
  //System.out.println("Connection Established from: " + (payload != null && payload.getSession() != null ? payload.getSession().getRemoteAddressStr() : "[NULL]"));
  }

  public void unregister(PropExchangePayload payload) {
    Iterator iter;
    String key;
    SocketSession session;

    if (payload != null && payload.getSession() != null) {
      synchronized (amLock) {
        iter = appMap.keySet().iterator();

        while (iter.hasNext()) {
          key = (String) iter.next();
          session = appMap.get(key);

          if (session != null && session.equals(payload.getSession())) {
            iter.remove();
          }
          appMap.remove(payload.getSession());
        }
      }
    }
  }

  public void receive(PropExchangePayload payload) {
    Properties props;
    String mesgTy;

    if (payload != null) {
      props = payload.getProps();

      if (props != null) {
        mesgTy = props.getProperty(RNIClient.MESG_TYPE_PROP);

        if (RNIClient.APP_INFO_MESG.equals(mesgTy)) {
          handleAppInfoPEMesg(payload);
        }
        else if (RNIClient.AGENT_DISCOVERY_MESG.equals(mesgTy)) {
          handleAgentDiscoveryMesg(payload);
        }
        else if (RNIClient.APP_DISCOVERY_MESG.equals(mesgTy)) {
          handleAppDiscoveryMesg(payload);
        }
        else if (RNIClient.SEND_DATA_MESG.equals(mesgTy)) {
          handleSendDataMesg(payload);
        }
      } //End null props check
    } //End null payload check
  }

  private void handleAppInfoPEMesg(PropExchangePayload payload) {
    String appInfoStr;

    appInfoStr = RNIAppInfo.AssembleAppInfoStr(payload.getProps());

    if (!StringUtils.IsNVL(appInfoStr)) {
      synchronized (amLock) {
        appMap.put(appInfoStr, payload.getSession());
        System.out.println("RNI> Registered '" + appInfoStr + "' -> " + payload.getSession().getRemoteAddressStr());
      }
    }
  }

  private void handleAgentDiscoveryMesg(PropExchangePayload payload) {
    P2PHubPeer[] peers;
    Properties resProps;
    int cnt;
    StringBuffer sb;

    try {
      peers = plugInManager.getNetAgent().getPeerList();

      resProps = new Properties();
      cnt = 1;

      for (P2PHubPeer peer : peers) {
        sb = new StringBuffer(RNIClient.PROP_PREFIX_AGENT);
        sb.append(cnt);

        resProps.setProperty(sb.toString(), peer.getUsername());

        cnt++;
      }

      server.send(resProps, payload.getSession(), payload.getSyncId());
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  private void handleAppDiscoveryMesg(PropExchangePayload payload) {
    Properties resProps, props;
    String agentName;

    try {
      props = payload.getProps();

      if (props != null) {
        agentName = props.getProperty(RNIClient.TARGET_AGENT_NAME_PROP);
        resProps = getAgentRNIAppList(agentName);

        if (resProps == null) {
          resProps = new Properties();
        }

        server.send(resProps, payload.getSession(), payload.getSyncId());
      } //End props null check
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  private void handleSendDataMesg(PropExchangePayload payload) {
    Properties props;
    String agentName;

    try {
      props = payload.getProps();

      if (props != null) {
        if (plugInManager != null && plugInManager.getNetAgent() != null) {
          props.setProperty(RNIClient.SOURCE_AGENT_NAME_PROP, plugInManager.getNetAgent().getHubUsername()); //Imprint THIS Agent's Name...
          agentName = props.getProperty(RNIClient.TARGET_AGENT_NAME_PROP);
          sendDataToAppAtAgent(agentName, props);
        }
      } //End props null check
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  private Properties getAgentRNIAppList(String agentName) throws RogueNetException {
    P2PHubPeer[] peers;
    P2PHubMessage mesg;
    Properties props, rniAppList = null;
    String transId, peerToken = null, tmp;
    SynchronousTransactionReceiver stReceiver;
    long start;
    Iterator iter;

    if (!StringUtils.IsNVL(agentName) && plugInManager != null) {
      stReceiver = (SynchronousTransactionReceiver) plugInManager.getPlugIn(SynchronousTransactionReceiver.class.getName());

      //Determine Peer Session Token
      peers = plugInManager.getNetAgent().getPeerList();
      for (P2PHubPeer peer : peers) {
        if (peer != null && agentName.equals(peer.getUsername())) {
          peerToken = peer.getSessionToken();
          break;
        }
      }

      if (peerToken != null) {
        mesg = new P2PHubMessage();
        mesg.setSubject(RN_INTEGRATOR_SUBJECT);
        mesg.setRecipients(new String[] { peerToken });

        props = new Properties();
        props.setProperty(RN_INTEGRATOR_ACTION_PROP, RN_INTEGRATOR_ACTION_GET_APP_LIST);
        mesg.setProperties(props);

        if (stReceiver != null) {
          transId = stReceiver.sendSynchronousTransaction(this, mesg);

          try {
            synchronized (tmLock) {
              start = System.currentTimeMillis();
              while (!transactionMap.containsKey(transId)) {
                if ((start + APP_LIST_WAIT_PERIOD) < System.currentTimeMillis()) {
                  throw new RogueNetException("Did NOT receive RNI App List Response within allowed interval!");
                }

                tmLock.wait(1000);
              }
            }
          }
          catch (InterruptedException e) {
            throw new RogueNetException("Transaction Thread interrupted while waiting!");
          }

          mesg = (P2PHubMessage) transactionMap.remove(transId);

          if (mesg != null && mesg.getProperties() != null) {
            rniAppList = mesg.getProperties();

            //Remove non "App" properties
            iter = rniAppList.keySet().iterator();
            while (iter.hasNext()) {
              tmp = (String) iter.next();
              if (tmp == null || !tmp.startsWith(RNIClient.PROP_PREFIX_APP)) {
                iter.remove();
              }
            } //End while iter has next
          } //End null mesg and mesg props check
        } //End stReceiver null check
      } //End peerToken null check
    } //End parameter null check

    return rniAppList;
  }

  public void processSynchronousTransactionResponse(String transId, P2PHubMessage mesg) {
    synchronized (tmLock) {
      transactionMap.put(transId, mesg);
      tmLock.notifyAll();
    }
  }

  private void sendDataToAppAtAgent(String agentName, Properties data) throws RogueNetException {
    P2PHubPeer[] peers;
    P2PHubMessage mesg;
    String peerToken = null;

    if (!StringUtils.IsNVL(agentName) && data != null && plugInManager != null && plugInManager.getNetAgent() != null) {
      //Determine Peer Session Token
      peers = plugInManager.getNetAgent().getPeerList();
      for (P2PHubPeer peer : peers) {
        if (peer != null && agentName.equals(peer.getUsername())) {
          peerToken = peer.getSessionToken();
          break;
        }
      }

      if (peerToken != null) {
        mesg = new P2PHubMessage();
        mesg.setSubject(RN_INTEGRATOR_SUBJECT);
        mesg.setRecipients(new String[] { peerToken });

        data.setProperty(RN_INTEGRATOR_ACTION_PROP, RN_INTEGRATOR_ACTION_SEND_DATA);
        mesg.setProperties(data);

        plugInManager.getNetAgent().sendMessage(mesg);
      } //End peerToken null check
    } //End parameter null check
  }

}
