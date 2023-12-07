/**
 * Created Sep 28, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.NAMING_ENTRY_PROP_PREFIX;
import static com.roguelogic.roguenet.RNAConstants.P2P_HUB_USERNAME_NAMING_ENTRY_NAME;
import static com.roguelogic.roguenet.RNAConstants.PROP_AGENT_NAME;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_QUERY_ENTIRE_REGISTRY;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_QUERY_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_QUERY_SPECIFIC_ENTRY;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_REPLY_SUBJECT_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_ROUND_TRIP_TOKEN_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_TARGET_ENTRY_NAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_TARGET_ENTRY_VALUE_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_ENTRY_NAME;

import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNANamingDaemon implements RNAPlugIn {

  private String agentName;

  private PlugInManager plugInManager;

  private HashMap<String, String> nameRegistry;

  private static RNAPlugInInfo RNANamingDaemonPII;

  public static final String PPI_LOGICAL_NAME = "RNA Naming Daemon";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Rogue Net Agent Naming Services Daemon integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    RNANamingDaemonPII = new RNAPlugInInfo();

    RNANamingDaemonPII.setLogicalName(PPI_LOGICAL_NAME);
    RNANamingDaemonPII.setVersion(PPI_VERSION);
    RNANamingDaemonPII.setDescription(PPI_DESCRIPTION);
    RNANamingDaemonPII.setDeveloper(PPI_DEVELOPER);
    RNANamingDaemonPII.setUrl(PPI_URL);
    RNANamingDaemonPII.setCopyright(PPI_COPYRIGHT);
  }

  public RNANamingDaemon() {
    nameRegistry = new HashMap<String, String>();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;

    readProperties();

    addBuiltInNamesToRegistry();

    plugInManager.register(this, RNA_NAMING_DAEMON_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, RNA_NAMING_DAEMON_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    Properties props;
    String query;

    props = mesg.getProperties();

    if (props != null) {
      query = props.getProperty(RNA_NAMING_DAEMON_QUERY_PROP);

      if (RNA_NAMING_DAEMON_QUERY_ENTIRE_REGISTRY.equalsIgnoreCase(query)) {
        //Send Entire Name Registry
        sendNameRegistry(mesg);
      }
      else if (RNA_NAMING_DAEMON_QUERY_SPECIFIC_ENTRY.equalsIgnoreCase(query)) {
        //Send a specific Entry if found
        sendSpecificEntry(mesg);
      }
    } //End null props check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return RNANamingDaemonPII;
  }

  private void readProperties() {
    agentName = plugInManager.getProperty(PROP_AGENT_NAME);
  }

  private void setOptionalRoundTripToken(Properties reqProps, Properties repProps) {
    String rtt;

    if (reqProps != null) {
      rtt = reqProps.getProperty(RNA_NAMING_DAEMON_ROUND_TRIP_TOKEN_PROP);
      if (rtt != null) {
        repProps.setProperty(RNA_NAMING_DAEMON_ROUND_TRIP_TOKEN_PROP, rtt);
      }
    }
  }

  public synchronized void registerName(String entryName, String name) {
    if (entryName != null && name != null) {
      nameRegistry.put(entryName, name);
    }
  }

  public synchronized Properties getNameRegistryAsMesgProps() {
    Properties props = new Properties();

    props.putAll(nameRegistry);
    props = StringUtils.PrefixPropNames(props, NAMING_ENTRY_PROP_PREFIX);

    return props;
  }

  public synchronized String getNameFromRegistry(String entryName) {
    String name = null;

    if (entryName != null) {
      name = nameRegistry.get(entryName);
    }

    return name;
  }

  private void addBuiltInNamesToRegistry() {
    registerName(RNA_NAMING_ENTRY_NAME, agentName);

    if (plugInManager != null && plugInManager.getNetAgent() != null) {
      registerName(P2P_HUB_USERNAME_NAMING_ENTRY_NAME, plugInManager.getNetAgent().getHubUsername());
    }
  }

  public void sendNameRegistry(P2PHubMessage request) throws RogueNetException {
    P2PHubMessage reply;
    Properties reqProps, repProps;
    String resSubject;

    if (request != null && request.getSender() != null && request.getSender().trim().length() > 0) {
      reqProps = request.getProperties();

      if (reqProps != null) {
        resSubject = reqProps.getProperty(RNA_NAMING_DAEMON_REPLY_SUBJECT_PROP);

        if (resSubject != null && resSubject.trim().length() > 0) {
          //Build Message
          reply = new P2PHubMessage();
          reply.setRecipients(new String[] { request.getSender().trim() });
          reply.setSubject(resSubject.trim());

          repProps = new Properties();
          reply.setProperties(repProps);

          repProps.setProperty(RNA_NAMING_DAEMON_QUERY_PROP, RNA_NAMING_DAEMON_QUERY_ENTIRE_REGISTRY);

          setOptionalRoundTripToken(reqProps, repProps);

          //Set Naming Properties
          repProps.putAll(getNameRegistryAsMesgProps());

          //Send message
          plugInManager.getNetAgent().sendMessage(reply);
        } //End null repSubject check
      } //End null reqProps check
    } //End null request check
  }

  public void sendSpecificEntry(P2PHubMessage request) throws RogueNetException {
    P2PHubMessage reply;
    Properties reqProps, repProps;
    String resSubject, targetEntryName, targetEntryValue;

    if (request != null && request.getSender() != null && request.getSender().trim().length() > 0) {
      reqProps = request.getProperties();

      if (reqProps != null) {
        resSubject = reqProps.getProperty(RNA_NAMING_DAEMON_REPLY_SUBJECT_PROP);

        if (resSubject != null && resSubject.trim().length() > 0) {
          targetEntryName = reqProps.getProperty(RNA_NAMING_DAEMON_TARGET_ENTRY_NAME_PROP);

          if (targetEntryName != null && targetEntryName.trim().length() > 0) {
            //Build Message
            reply = new P2PHubMessage();
            reply.setRecipients(new String[] { request.getSender().trim() });
            reply.setSubject(resSubject.trim());

            repProps = new Properties();
            reply.setProperties(repProps);

            repProps.setProperty(RNA_NAMING_DAEMON_QUERY_PROP, RNA_NAMING_DAEMON_QUERY_ENTIRE_REGISTRY);

            setOptionalRoundTripToken(reqProps, repProps);

            repProps.setProperty(RNA_NAMING_DAEMON_TARGET_ENTRY_NAME_PROP, targetEntryName);

            targetEntryValue = getNameFromRegistry(targetEntryName);
            if (targetEntryValue != null) {
              repProps.setProperty(RNA_NAMING_DAEMON_TARGET_ENTRY_VALUE_PROP, targetEntryValue);
            }

            //Send message
            plugInManager.getNetAgent().sendMessage(reply);
          } //End targetEntryName null check
        } //End null repSubject check
      } //End null reqProps check
    } //End null request check
  }

}
