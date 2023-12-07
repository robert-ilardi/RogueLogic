/**
 * Created Sep 27, 2006
 */
package com.roguelogic.roguenet.plugins;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.RNALogger;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;

/**
 * @author Robert C. Ilardi
 *
 */

public class SimpleMessageLogger implements RNAPlugIn {

  private PlugInManager pluginManager;

  private String subject;

  private static RNAPlugInInfo SimpleMessageLoggerPII;

  public static final String PPI_LOGICAL_NAME = "Simple Message Logger";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Simple Message Logger integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    SimpleMessageLoggerPII = new RNAPlugInInfo();

    SimpleMessageLoggerPII.setLogicalName(PPI_LOGICAL_NAME);
    SimpleMessageLoggerPII.setVersion(PPI_VERSION);
    SimpleMessageLoggerPII.setDescription(PPI_DESCRIPTION);
    SimpleMessageLoggerPII.setDeveloper(PPI_DEVELOPER);
    SimpleMessageLoggerPII.setUrl(PPI_URL);
    SimpleMessageLoggerPII.setCopyright(PPI_COPYRIGHT);
  }

  public SimpleMessageLogger() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager pluginManager, String configStr) throws RogueNetException {
    this.pluginManager = pluginManager;
    subject = configStr;
    pluginManager.register(this, subject, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    pluginManager.unregister(this, subject, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    RNALogger.GetLogger().info("SimpleMessageLogger Plug-In Received Message:\n" + mesg.toXML());
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return SimpleMessageLoggerPII;
  }

}
