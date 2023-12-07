/**
 * Created Oct 23, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.*;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;

/**
 * @author Robert C. Ilardi
 *
 */

public class PlugInRemoteInstallDaemon implements RNAPlugIn {

  private PlugInManager plugInManager;

  private static RNAPlugInInfo PlugInRemoteInstallDaemonPII;

  public static final String PPI_LOGICAL_NAME = "Plug-In Remote Installation Daemon";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Plug-In Remote Installation Daemon integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    PlugInRemoteInstallDaemonPII = new RNAPlugInInfo();

    PlugInRemoteInstallDaemonPII.setLogicalName(PPI_LOGICAL_NAME);
    PlugInRemoteInstallDaemonPII.setVersion(PPI_VERSION);
    PlugInRemoteInstallDaemonPII.setDescription(PPI_DESCRIPTION);
    PlugInRemoteInstallDaemonPII.setDeveloper(PPI_DEVELOPER);
    PlugInRemoteInstallDaemonPII.setUrl(PPI_URL);
    PlugInRemoteInstallDaemonPII.setCopyright(PPI_COPYRIGHT);
  }

  public PlugInRemoteInstallDaemon() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, PLUG_IN_REMOTE_INSTALL_DAEMON_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, PLUG_IN_REMOTE_INSTALL_DAEMON_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return PlugInRemoteInstallDaemonPII;
  }

}
