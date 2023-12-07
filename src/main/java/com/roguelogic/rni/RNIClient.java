/**
 * Created Jan 24, 2007
 */
package com.roguelogic.rni;

import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.propexchange.PropExchangeClient;
import com.roguelogic.propexchange.PropExchangeObserver;
import com.roguelogic.propexchange.PropExchangePayload;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNIClient implements PropExchangeObserver {

  public static final int DEFAULT_RNI_PORT = 12479;
  public static final String LOCALHOST = "localhost";

  public static final String MESG_TYPE_PROP = "_RNI.MesgType";
  public static final String TARGET_AGENT_NAME_PROP = "_RNI.TargetAgentName";
  public static final String SOURCE_AGENT_NAME_PROP = "_RNI.SourceAgentName";
  public static final String TARGET_APP_STR_PROP = "_RNI.TargetAppStr";
  public static final String SOURCE_APP_STR_PROP = "_RNI.SourceAppStr";

  public static final String APP_INFO_MESG = "AppInfoMesg";
  public static final String AGENT_DISCOVERY_MESG = "AgentDiscoveryMesg";
  public static final String APP_DISCOVERY_MESG = "AppDiscoveryMesg";
  public static final String SEND_DATA_MESG = "SendDataMesg";

  public static final String PROP_PREFIX_AGENT = "Agent";
  public static final String PROP_PREFIX_APP = "App";

  public static final int AGENT_DISCOVERY_WAIT_INTERVAL = 120;
  public static final int APP_DISCOVERY_WAIT_INTERVAL = 120;

  private PropExchangeClient client;
  private RNIObserver observer;

  private int port;

  private RNIAppInfo appInfo;

  public RNIClient(RNIAppInfo appInfo, RNIObserver observer) {
    this(DEFAULT_RNI_PORT, appInfo, observer);
  }

  public RNIClient(int port, RNIAppInfo appInfo, RNIObserver observer) {
    this.port = port;
    this.appInfo = appInfo;
    this.observer = observer;

    client = new PropExchangeClient(LOCALHOST, port);
    client.addObserver(this);
  }

  public synchronized void connect() throws RNIException {
    try {
      if (!client.isConnected()) {
        client.startClient();
        sendAppInfo();
      }
    }
    catch (Exception e) {
      throw new RNIException("An error occurred while attempting to connect to local Rogue Net Integrator on port = " + port, e);
    }
  }

  public void disconnect() {
    client.stopClient();
  }

  private void sendAppInfo() throws RLNetException {
    Properties mesg;

    mesg = new Properties();

    mesg.setProperty(MESG_TYPE_PROP, APP_INFO_MESG);

    mesg.setProperty(RNIAppInfo.APP_INFO_PROP_NAME, appInfo.getName());
    mesg.setProperty(RNIAppInfo.APP_INFO_PROP_VERSION, appInfo.getVersion());
    mesg.setProperty(RNIAppInfo.APP_INFO_PROP_DEVELOPER, appInfo.getDeveloper());

    if (!StringUtils.IsNVL(appInfo.getUserString())) {
      mesg.setProperty(RNIAppInfo.APP_INFO_PROP_USERSTRING, appInfo.getUserString());
    }

    client.send(mesg);
  }

  public Properties agentDiscovery() throws RNIException {
    Properties mesg;

    mesg = new Properties();
    mesg.setProperty(MESG_TYPE_PROP, AGENT_DISCOVERY_MESG);

    mesg = sendAndWait(mesg, AGENT_DISCOVERY_WAIT_INTERVAL);

    return mesg;
  }

  public Properties appDiscovery(String agentName) throws RNIException {
    Properties mesg = null;

    if (!StringUtils.IsNVL(agentName)) {
      mesg = new Properties();
      mesg.setProperty(MESG_TYPE_PROP, APP_DISCOVERY_MESG);
      mesg.setProperty(TARGET_AGENT_NAME_PROP, agentName);

      mesg = sendAndWait(mesg, APP_DISCOVERY_WAIT_INTERVAL);
    }

    return mesg;
  }

  public void sendAsyncData(Properties data, String targetAgent, RNIAppInfo targetApp) throws RNIException {
    String targetAppStr;

    if (targetApp != null) {
      targetAppStr = targetApp.assembleAppInfoStr();

      if (data != null && data.size() > 0 && !StringUtils.IsNVL(targetAgent) && !StringUtils.IsNVL(targetAppStr)) {
        data.setProperty(MESG_TYPE_PROP, SEND_DATA_MESG);
        data.setProperty(TARGET_AGENT_NAME_PROP, targetAgent);
        data.setProperty(TARGET_APP_STR_PROP, targetAppStr);

        send(data);
      }
    }
  }

  private void send(Properties mesg) throws RNIException {
    try {
      //Add Source App Info String
      if (mesg != null) {
        mesg.put(SOURCE_APP_STR_PROP, appInfo.assembleAppInfoStr());
      }

      //Make sure we are connected
      connect();

      //Send Properties
      client.send(mesg);
    }
    catch (Exception e) {
      throw new RNIException("An error occurred while attempting to send properties message to local Rogue Net Integrator on port = " + port, e);
    }
  }

  private Properties sendAndWait(Properties mesg, int timeoutSeconds) throws RNIException {
    Properties reply = null;
    PropExchangePayload replyPayload;

    try {
      //Add Source App Info String
      if (mesg != null) {
        mesg.put(SOURCE_APP_STR_PROP, appInfo.assembleAppInfoStr());
      }

      //Make sure we are connected
      connect();

      //Send Properties and wait for reply
      replyPayload = client.sendAndWait(mesg, timeoutSeconds);

      if (replyPayload != null) {
        reply = replyPayload.getProps();
      }
    }
    catch (Exception e) {
      throw new RNIException("An error occurred while attempting to send properties message to local Rogue Net Integrator on port = " + port, e);
    }

    return reply;
  }

  public void register(PropExchangePayload payload) {}

  public void unregister(PropExchangePayload payload) {}

  public void receive(PropExchangePayload payload) {
    if (payload != null) {
      observer.receive(payload.getProps());
    }
  }

  public RNIAppInfo getAppInfo() {
    return appInfo;
  }

}
