/**
 * Created Oct 23, 2006
 */
package com.roguelogic.roguenet.plugins;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;

/**
 * @author Robert C. Ilardi
 *
 */

public class PlugInRemoteInstaller implements RNAPlugIn {

  public static final String PLUG_IN_NAME = "Remote Plug-In Installer";
  public static final String REMOTE_INSTALLER_SUBJECT = "PlugInRemoteInstaller.ResponseChannel";

  private PlugInManager plugInManager;

  private static RNAPlugInInfo PlugInRemoteInstallerPII;

  public static final String PPI_LOGICAL_NAME = "Plug-In Remote Installer";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Plug-In Remote Installer integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    PlugInRemoteInstallerPII = new RNAPlugInInfo();

    PlugInRemoteInstallerPII.setLogicalName(PPI_LOGICAL_NAME);
    PlugInRemoteInstallerPII.setVersion(PPI_VERSION);
    PlugInRemoteInstallerPII.setDescription(PPI_DESCRIPTION);
    PlugInRemoteInstallerPII.setDeveloper(PPI_DEVELOPER);
    PlugInRemoteInstallerPII.setUrl(PPI_URL);
    PlugInRemoteInstallerPII.setCopyright(PPI_COPYRIGHT);
  }

  public PlugInRemoteInstaller() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, REMOTE_INSTALLER_SUBJECT, PLUG_IN_NAME);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, REMOTE_INSTALLER_SUBJECT, PLUG_IN_NAME);
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
    return PlugInRemoteInstallerPII;
  }

}
