/**
 * Created Sep 27, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.RNA_NAMING_DAEMON_ROUND_TRIP_TOKEN_PROP;

import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.gui.RNMessengerControls;
import com.roguelogic.roguenet.gui.RNMessengerDialog;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNMessenger implements RNAPlugIn {

  public static final String PLUG_IN_NAME = "RN Messenger";
  public static final String RN_MESSENGER_SUBJECT = "RNMessenger.DataChannel";

  public static final String RN_MESSENGER_MESG_TEXT = "RNMessenger.MessageText";

  private PlugInManager plugInManager;

  private static RNAPlugInInfo RNMessengerPII;

  public static final String PPI_LOGICAL_NAME = "Rogue Net Messenger";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Rogue Net Messenger service integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    RNMessengerPII = new RNAPlugInInfo();

    RNMessengerPII.setLogicalName(PPI_LOGICAL_NAME);
    RNMessengerPII.setVersion(PPI_VERSION);
    RNMessengerPII.setDescription(PPI_DESCRIPTION);
    RNMessengerPII.setDeveloper(PPI_DEVELOPER);
    RNMessengerPII.setUrl(PPI_URL);
    RNMessengerPII.setCopyright(PPI_COPYRIGHT);
  }

  private boolean muted;

  private RNMessengerControls controls;

  private HashMap<String, RNMessengerDialog> conversationMap;
  private Object conversationMapLock;

  private HashMap<String, P2PHubPeer> peerMap;
  private Object peerMapLock;

  public RNMessenger() {
    muted = false;

    conversationMap = new HashMap<String, RNMessengerDialog>();
    conversationMapLock = new Object();

    peerMap = new HashMap<String, P2PHubPeer>();
    peerMapLock = new Object();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, RN_MESSENGER_SUBJECT, PLUG_IN_NAME);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, RN_MESSENGER_SUBJECT, PLUG_IN_NAME);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {
    if (controls != null && controls.isVisible()) {
      controls.toFront();
    }
    else {
      controls = new RNMessengerControls(plugInManager);
      controls.setVisible(true);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    if (mesg != null && mesg.getProperties() != null) {
      if (isNamingMesg(mesg)) {
        handleNamingMesg(mesg);
      }
      else {
        handleTextMesg(mesg);
      }
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return RNMessengerPII;
  }

  public synchronized boolean toggleMute() {
    muted = !muted;

    return muted;
  }

  public synchronized boolean isMuted() {
    return muted;
  }

  private boolean isNamingMesg(P2PHubMessage mesg) {
    return !StringUtils.IsNVL(mesg.getProperty(RNA_NAMING_DAEMON_ROUND_TRIP_TOKEN_PROP));
  }

  private void handleNamingMesg(P2PHubMessage mesg) {}

  public RNMessengerDialog getConversationDialog(String sessionToken) throws RogueNetException {
    RNMessengerDialog conversation;

    synchronized (conversationMapLock) {
      conversation = conversationMap.get(sessionToken);

      if (conversation == null) {
        conversation = new RNMessengerDialog(this, getPeer(sessionToken));
        conversationMap.put(sessionToken, conversation);
      }
    }

    return conversation;
  }

  public void removeConversationDialog(String sessionToken) {
    synchronized (conversationMapLock) {
      conversationMap.remove(sessionToken);
    }
  }

  private void handleTextMesg(P2PHubMessage mesg) {
    RNMessengerDialog conversation;
    String txt;

    if (!isMuted()) {
      try {
        conversation = getConversationDialog(mesg.getSender());

        if (!conversation.isVisible()) {
          conversation.setVisible(true);
        }

        txt = mesg.getProperty(RN_MESSENGER_MESG_TEXT);
        if (!StringUtils.IsNVL(txt)) {
          conversation.appendConversationText(txt, RNMessengerDialog.MESG_TYPE_PEER);
        }
      } //End try block
      catch (Exception e) {
        e.printStackTrace();
      }
    } //End !isMuted check
  }

  public PlugInManager getPlugInManager() {
    return plugInManager;
  }

  public boolean sendMessage(String sessionToken, String mesgText) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    boolean received = false;

    if (!StringUtils.IsNVL(sessionToken) && !StringUtils.IsNVL(mesgText)) {
      mesg = new P2PHubMessage();
      mesg.setSubject(RN_MESSENGER_SUBJECT);
      mesg.setRecipients(new String[] { sessionToken });

      props = new Properties();
      props.setProperty(RN_MESSENGER_MESG_TEXT, mesgText.trim());
      mesg.setProperties(props);

      if (plugInManager != null && plugInManager.getNetAgent() != null) {
        received = plugInManager.getNetAgent().sendMessage(mesg);
      }
    }

    return received;
  }

  public P2PHubPeer getPeer(String sessionToken) throws RogueNetException {
    P2PHubPeer peer = null;

    synchronized (peerMapLock) {
      peer = peerMap.get(sessionToken);

      if (peer == null) {
        //If null make ONE refresh attempt
        refreshPeerMap();

        peer = peerMap.get(sessionToken);
      } //End peer null check
    }

    return peer;
  }

  public void refreshPeerMap() throws RogueNetException {
    P2PHubPeer[] peerList;

    synchronized (peerMapLock) {
      if (plugInManager != null && plugInManager.getNetAgent() != null) {
        peerList = plugInManager.getNetAgent().getPeerList();
        peerMap.clear();

        for (P2PHubPeer p : peerList) {
          peerMap.put(p.getSessionToken(), p);
        }
      }
    }
  }

}
