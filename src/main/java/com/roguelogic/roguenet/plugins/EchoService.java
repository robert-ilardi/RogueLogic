/**
 * Created Dec 17, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.ECHO_SERVICE_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP;

import java.util.Properties;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNALogger;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class EchoService implements RNAPlugIn {

  private static RNAPlugInInfo EchoServicePII;

  public static final String PPI_LOGICAL_NAME = "Echo Service";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Echo Service integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    EchoServicePII = new RNAPlugInInfo();

    EchoServicePII.setLogicalName(PPI_LOGICAL_NAME);
    EchoServicePII.setVersion(PPI_VERSION);
    EchoServicePII.setDescription(PPI_DESCRIPTION);
    EchoServicePII.setDeveloper(PPI_DEVELOPER);
    EchoServicePII.setUrl(PPI_URL);
    EchoServicePII.setCopyright(PPI_COPYRIGHT);
  }

  private PlugInManager plugInManager;

  public EchoService() {}

  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;

    plugInManager.register(this, ECHO_SERVICE_SUBJECT, null);
  }

  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, ECHO_SERVICE_SUBJECT, null);
  }

  public void handleMenuExec() {}

  public RNAPlugInInfo getPlugInInfo() {
    return EchoServicePII;
  }

  public void handle(P2PHubMessage mesg) throws RogueNetException {
    P2PHubMessage response;
    Properties resProps;
    String syncTransId;

    syncTransId = mesg.getProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP);

    if (plugInManager != null && plugInManager.getNetAgent() != null && !StringUtils.IsNVL(syncTransId)) {
      try {

        response = new P2PHubMessage();
        response.setRecipients(new String[] { mesg.getSender() });
        response.setSubject(SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT);

        resProps = new Properties();
        response.setProperties(resProps);

        resProps.setProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP, syncTransId);

        response.setBase64Data(mesg.getBase64Data());

        plugInManager.getNetAgent().sendMessage(response);
      }
      catch (Exception e) {
        RNALogger.GetLogger().log(e);
      }
    } //End null parameter check
  }

}
