/**
 * Created Dec 17, 2006
 */
package com.roguelogic.roguenet.plugins;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAConstants;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.SynchronousTransactionRequestor;
import com.roguelogic.roguenet.gui.EchoClientDialog;

/**
 * @author Robert C. Ilardi
 *
 */

public class EchoClient implements RNAPlugIn {

  public static final String ECHO_CLIENT_SUBJECT = "EchoClient.PrivateChannel";
  public static final String PLUG_IN_NAME = "Echo Client";

  private static RNAPlugInInfo EchoClientPII;

  public static final String PPI_LOGICAL_NAME = "Echo Client";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Echo Client integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    EchoClientPII = new RNAPlugInInfo();

    EchoClientPII.setLogicalName(PPI_LOGICAL_NAME);
    EchoClientPII.setVersion(PPI_VERSION);
    EchoClientPII.setDescription(PPI_DESCRIPTION);
    EchoClientPII.setDeveloper(PPI_DEVELOPER);
    EchoClientPII.setUrl(PPI_URL);
    EchoClientPII.setCopyright(PPI_COPYRIGHT);
  }

  private PlugInManager plugInManager;

  private EchoClientDialog controls;

  public EchoClient() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;

    plugInManager.register(this, ECHO_CLIENT_SUBJECT, PLUG_IN_NAME);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, ECHO_CLIENT_SUBJECT, PLUG_IN_NAME);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return EchoClientPII;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {
    if (controls != null && controls.isVisible()) {
      controls.toFront();
    }
    else {
      controls = new EchoClientDialog(plugInManager);
      controls.setVisible(true);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {}

  public synchronized String sendEcho(P2PHubPeer peer, SynchronousTransactionRequestor syncTransReq, byte[] data) throws RogueNetException {
    P2PHubMessage mesg;
    String transId = null;
    SynchronousTransactionReceiver stReceiver;

    if (plugInManager != null && plugInManager.getNetAgent() != null && peer != null && syncTransReq != null) {
      stReceiver = (SynchronousTransactionReceiver) plugInManager.getPlugIn(SynchronousTransactionReceiver.class.getName());

      if (stReceiver != null) {
        mesg = new P2PHubMessage();
        mesg.setSubject(RNAConstants.ECHO_SERVICE_SUBJECT);
        mesg.setRecipients(new String[] { peer.getSessionToken() });
        mesg.setData(data);

        transId = stReceiver.sendSynchronousTransaction(syncTransReq, mesg);
      }
    }

    return transId;
  }

}
