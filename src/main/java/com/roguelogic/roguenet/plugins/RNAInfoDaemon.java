/**
 * Created Sep 28, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.PROP_AGENT_NAME;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_AGENT_NAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_AVAILABLE_HEAP_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_INFO_LEVEL_BASIC;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_INFO_LEVEL_DETAILED;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_INFO_LEVEL_PLUG_IN_DETAILS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_INFO_LEVEL_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_JVM_VERSION_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_MESSAGES_RECEIVED_CNT_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_MESSAGES_SENT_CNT_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_OS_NAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_P2P_HUB_SESSION_TOKEN_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_P2P_HUB_USERNAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_PLUG_IN_LIST_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_REPLY_SUBJECT_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_RNA_VERSION_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_ROUND_TRIP_TOKEN_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_AGENT_NAME_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_AVAILABLE_HEAP_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_FIELD_CNT;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_JVM_VERSION_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_OS_NAME_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_P2P_HUB_SESSION_TOKEN_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_P2P_HUB_USERNAME_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_RNA_VERSION_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_TOTAL_HEAP_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_SUMMARY_UP_TIME_POS;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_TARGET_PLUG_IN;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_TOTAL_HEAP_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_UP_TIME_PROP;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Properties;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.Version;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNAInfoDaemon implements RNAPlugIn {

  private String agentName;

  private PlugInManager plugInManager;

  private static RNAPlugInInfo RNAInfoDaemonPII;

  public static final String PPI_LOGICAL_NAME = "RNA Information Daemon";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Rogue Net Agent Information Services Daemon integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    RNAInfoDaemonPII = new RNAPlugInInfo();

    RNAInfoDaemonPII.setLogicalName(PPI_LOGICAL_NAME);
    RNAInfoDaemonPII.setVersion(PPI_VERSION);
    RNAInfoDaemonPII.setDescription(PPI_DESCRIPTION);
    RNAInfoDaemonPII.setDeveloper(PPI_DEVELOPER);
    RNAInfoDaemonPII.setUrl(PPI_URL);
    RNAInfoDaemonPII.setCopyright(PPI_COPYRIGHT);
  }

  public RNAInfoDaemon() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;

    readProperties();

    plugInManager.register(this, RNA_INFO_DAEMON_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, RNA_INFO_DAEMON_SUBJECT, null);
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
    String infoLevel;

    props = mesg.getProperties();

    if (props != null) {
      infoLevel = props.getProperty(RNA_INFO_DAEMON_INFO_LEVEL_PROP);

      if (RNA_INFO_DAEMON_INFO_LEVEL_BASIC.equalsIgnoreCase(infoLevel)) {
        //Send Basic Information
        sendBasicInfo(mesg);
      }
      else if (RNA_INFO_DAEMON_INFO_LEVEL_DETAILED.equalsIgnoreCase(infoLevel)) {
        //Send Detailed Information
        sendDetailedInfo(mesg);
      }
      else if (RNA_INFO_DAEMON_INFO_LEVEL_PLUG_IN_DETAILS.equalsIgnoreCase(infoLevel)) {
        //Send Plug In Information
        sendPlugInInfo(mesg);
      }
    } //End null props check
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return RNAInfoDaemonPII;
  }

  private void readProperties() {
    agentName = plugInManager.getProperty(PROP_AGENT_NAME);
  }

  private long upTime() {
    long totalTime = 0;

    if (plugInManager.getNetAgent().getActivationTs() != null) {
      totalTime = System.currentTimeMillis() - plugInManager.getNetAgent().getActivationTs().getTime();
    }

    return totalTime;
  }

  public String[] getSummaryArr() {
    String[] summary = new String[RNA_INFO_DAEMON_SUMMARY_FIELD_CNT];

    summary[RNA_INFO_DAEMON_SUMMARY_AGENT_NAME_POS] = getAgentName();
    summary[RNA_INFO_DAEMON_SUMMARY_RNA_VERSION_POS] = getRNAVersion();
    summary[RNA_INFO_DAEMON_SUMMARY_UP_TIME_POS] = getUpTime();
    summary[RNA_INFO_DAEMON_SUMMARY_JVM_VERSION_POS] = getJVMVersion();
    summary[RNA_INFO_DAEMON_SUMMARY_OS_NAME_POS] = getOSName();
    summary[RNA_INFO_DAEMON_SUMMARY_TOTAL_HEAP_POS] = getTotalHeap();
    summary[RNA_INFO_DAEMON_SUMMARY_AVAILABLE_HEAP_POS] = getAvailableHeap();
    summary[RNA_INFO_DAEMON_SUMMARY_P2P_HUB_SESSION_TOKEN_POS] = getP2PHubSessionToken();
    summary[RNA_INFO_DAEMON_SUMMARY_P2P_HUB_USERNAME_POS] = getP2PHubUsername();

    return summary;
  }

  private void sendBasicInfo(P2PHubMessage request) throws RogueNetException {
    P2PHubMessage reply;
    Properties reqProps, repProps;
    String resSubject;

    if (request != null && request.getSender() != null && request.getSender().trim().length() > 0) {
      reqProps = request.getProperties();

      if (reqProps != null) {
        resSubject = reqProps.getProperty(RNA_INFO_DAEMON_REPLY_SUBJECT_PROP);

        if (resSubject != null && resSubject.trim().length() > 0) {
          //Build Message
          reply = new P2PHubMessage();
          reply.setRecipients(new String[] { request.getSender().trim() });
          reply.setSubject(resSubject.trim());

          repProps = new Properties();
          reply.setProperties(repProps);

          repProps.setProperty(RNA_INFO_DAEMON_INFO_LEVEL_PROP, RNA_INFO_DAEMON_INFO_LEVEL_BASIC);

          setOptionalRoundTripToken(reqProps, repProps);

          repProps.setProperty(RNA_INFO_DAEMON_AGENT_NAME_PROP, getAgentName());
          repProps.setProperty(RNA_INFO_DAEMON_RNA_VERSION_PROP, getRNAVersion());
          repProps.setProperty(RNA_INFO_DAEMON_UP_TIME_PROP, getUpTime());
          repProps.setProperty(RNA_INFO_DAEMON_JVM_VERSION_PROP, getJVMVersion());
          repProps.setProperty(RNA_INFO_DAEMON_OS_NAME_PROP, getOSName());
          repProps.setProperty(RNA_INFO_DAEMON_TOTAL_HEAP_PROP, getTotalHeap());
          repProps.setProperty(RNA_INFO_DAEMON_AVAILABLE_HEAP_PROP, getAvailableHeap());
          repProps.setProperty(RNA_INFO_DAEMON_P2P_HUB_SESSION_TOKEN_PROP, getP2PHubSessionToken());
          repProps.setProperty(RNA_INFO_DAEMON_P2P_HUB_USERNAME_PROP, getP2PHubUsername());

          //Send message
          plugInManager.getNetAgent().sendMessage(reply);
        } //End null repSubject check
      } //End null reqProps check
    } //End null request check
  }

  private void sendDetailedInfo(P2PHubMessage request) throws RogueNetException {
    P2PHubMessage reply;
    Properties reqProps, repProps;
    String resSubject;

    if (request != null && request.getSender() != null && request.getSender().trim().length() > 0) {
      reqProps = request.getProperties();

      if (reqProps != null) {
        resSubject = reqProps.getProperty(RNA_INFO_DAEMON_REPLY_SUBJECT_PROP);

        if (resSubject != null && resSubject.trim().length() > 0) {
          //Build Message
          reply = new P2PHubMessage();
          reply.setRecipients(new String[] { request.getSender().trim() });
          reply.setSubject(resSubject.trim());

          repProps = new Properties();
          reply.setProperties(repProps);

          repProps.setProperty(RNA_INFO_DAEMON_INFO_LEVEL_PROP, RNA_INFO_DAEMON_INFO_LEVEL_DETAILED);

          setOptionalRoundTripToken(reqProps, repProps);

          repProps.setProperty(RNA_INFO_DAEMON_AGENT_NAME_PROP, getAgentName());
          repProps.setProperty(RNA_INFO_DAEMON_RNA_VERSION_PROP, getRNAVersion());
          repProps.setProperty(RNA_INFO_DAEMON_UP_TIME_PROP, getUpTime());
          repProps.setProperty(RNA_INFO_DAEMON_JVM_VERSION_PROP, getJVMVersion());
          repProps.setProperty(RNA_INFO_DAEMON_OS_NAME_PROP, getOSName());
          repProps.setProperty(RNA_INFO_DAEMON_TOTAL_HEAP_PROP, getTotalHeap());
          repProps.setProperty(RNA_INFO_DAEMON_AVAILABLE_HEAP_PROP, getAvailableHeap());
          repProps.setProperty(RNA_INFO_DAEMON_P2P_HUB_SESSION_TOKEN_PROP, getP2PHubSessionToken());
          repProps.setProperty(RNA_INFO_DAEMON_P2P_HUB_USERNAME_PROP, getP2PHubUsername());
          repProps.setProperty(RNA_INFO_DAEMON_MESSAGES_SENT_CNT_PROP, getMesgSentCnt());
          repProps.setProperty(RNA_INFO_DAEMON_MESSAGES_RECEIVED_CNT_PROP, getMesgReceivedCnt());
          repProps.setProperty(RNA_INFO_DAEMON_PLUG_IN_LIST_PROP, getPlugInList());

          //Send message
          plugInManager.getNetAgent().sendMessage(reply);
        } //End null repSubject check
      } //End null reqProps check
    } //End null request check
  }

  private void sendPlugInInfo(P2PHubMessage request) throws RogueNetException {
    P2PHubMessage reply;
    Properties reqProps, repProps, piDevProps;
    String resSubject;
    String plugInClassName;
    RNAPlugIn plugIn;
    RNAPlugInInfo piInfo;

    if (request != null && request.getSender() != null && request.getSender().trim().length() > 0) {
      reqProps = request.getProperties();

      if (reqProps != null) {
        plugInClassName = reqProps.getProperty(RNA_INFO_DAEMON_TARGET_PLUG_IN);
        resSubject = reqProps.getProperty(RNA_INFO_DAEMON_REPLY_SUBJECT_PROP);

        if (resSubject != null && resSubject.trim().length() > 0 && plugInClassName != null && plugInClassName.trim().length() > 0) {
          plugIn = plugInManager.getPlugIn(plugInClassName.trim());
          if (plugIn != null) {
            piInfo = plugIn.getPlugInInfo();

            //Build Message
            reply = new P2PHubMessage();
            reply.setRecipients(new String[] { request.getSender().trim() });
            reply.setSubject(resSubject.trim());

            repProps = new Properties();
            reply.setProperties(repProps);

            repProps.setProperty(RNA_INFO_DAEMON_INFO_LEVEL_PROP, RNA_INFO_DAEMON_INFO_LEVEL_PLUG_IN_DETAILS);
            repProps.setProperty(RNA_INFO_DAEMON_TARGET_PLUG_IN, plugInClassName);

            setOptionalRoundTripToken(reqProps, repProps);

            if (piInfo != null) {
              piDevProps = piInfo.getMessageProperties();
              if (piDevProps != null) {
                repProps.putAll(piDevProps);
              }
            }

            //Send message
            plugInManager.getNetAgent().sendMessage(reply);
          } //End plugIn null check
        } //End null repSubject check
      } //End null reqProps check
    } //End null request check
  }

  private String getAgentName() {
    return agentName;
  }

  private String getUpTime() {
    return StringUtils.HumanReadableTime(upTime());
  }

  private String getRNAVersion() {
    StringBuffer ver = new StringBuffer();

    ver.append(Version.SHORT_APP_TITLE);
    ver.append(" ");
    ver.append(Version.VERSION);

    return ver.toString();
  }

  private String getJVMVersion() {
    StringBuffer sb = new StringBuffer();

    sb.append(SystemUtils.GetJavaVersion());
    sb.append(" (");
    sb.append(SystemUtils.GetJavaVendor());
    sb.append(")");

    return sb.toString();
  }

  private String getOSName() {
    StringBuffer sb = new StringBuffer();

    sb.append(SystemUtils.GetOperatingSystemName());
    sb.append(" (");
    sb.append(SystemUtils.GetOperatingSystemVersion());
    sb.append(")");

    return sb.toString();
  }

  private String getTotalHeap() {
    NumberFormat nf = NumberFormat.getInstance();

    nf.setGroupingUsed(true);
    nf.setMaximumFractionDigits(3);

    return nf.format(SystemUtils.GetTotalMemory());
  }

  private String getAvailableHeap() {
    NumberFormat nf = NumberFormat.getInstance();

    nf.setGroupingUsed(true);
    nf.setMaximumFractionDigits(3);

    return nf.format(SystemUtils.GetAvailableMemory());
  }

  private String getP2PHubSessionToken() {
    return (plugInManager != null && plugInManager.getNetAgent() != null ? plugInManager.getNetAgent().getHubSessionToken() : null);
  }

  private String getP2PHubUsername() {
    return (plugInManager != null && plugInManager.getNetAgent() != null ? plugInManager.getNetAgent().getHubUsername() : null);
  }

  private String getMesgSentCnt() {
    return (plugInManager != null && plugInManager.getNetAgent() != null ? String.valueOf(plugInManager.getNetAgent().getMesgSentCnt()) : "0");
  }

  private String getMesgReceivedCnt() {
    return (plugInManager != null && plugInManager.getNetAgent() != null ? String.valueOf(plugInManager.getNetAgent().getMesgReceivedCnt()) : "0");
  }

  public String getPlugInList() {
    StringBuffer piList = new StringBuffer();
    ArrayList<String> piNames;

    piNames = plugInManager.getPlugInNameList();

    for (String piName : piNames) {
      piList.append(piName);
      piList.append(";");
    }

    return piList.toString();
  }

  private void setOptionalRoundTripToken(Properties reqProps, Properties repProps) {
    String rtt;

    if (reqProps != null) {
      rtt = reqProps.getProperty(RNA_INFO_DAEMON_ROUND_TRIP_TOKEN_PROP);
      if (rtt != null) {
        repProps.setProperty(RNA_INFO_DAEMON_ROUND_TRIP_TOKEN_PROP, rtt);
      }
    }
  }

}
