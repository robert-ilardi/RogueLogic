/**
 * Created Oct 2, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.*;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.gui.NetworkExplorer;

/**
 * @author Robert C. Ilardi
 *
 */

public class NetworkExplorerReceiver implements RNAPlugIn {

  private PlugInManager plugInManager;

  private NetworkExplorer netExplorer;

  private static RNAPlugInInfo NetworkExplorerReceiverPII;

  public static final String PPI_LOGICAL_NAME = "Network Explorer Receiver";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Network Explorer Receiver integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    NetworkExplorerReceiverPII = new RNAPlugInInfo();

    NetworkExplorerReceiverPII.setLogicalName(PPI_LOGICAL_NAME);
    NetworkExplorerReceiverPII.setVersion(PPI_VERSION);
    NetworkExplorerReceiverPII.setDescription(PPI_DESCRIPTION);
    NetworkExplorerReceiverPII.setDeveloper(PPI_DEVELOPER);
    NetworkExplorerReceiverPII.setUrl(PPI_URL);
    NetworkExplorerReceiverPII.setCopyright(PPI_COPYRIGHT);
  }

  public NetworkExplorerReceiver() {}

  public void setNetworkExplorer(NetworkExplorer netExplorer) {
    this.netExplorer = netExplorer;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, NETWORK_EXPLORER_REPLY_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, NETWORK_EXPLORER_REPLY_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    if (netExplorer != null) {
      netExplorer.receiveUpdate(mesg);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return NetworkExplorerReceiverPII;
  }

}
