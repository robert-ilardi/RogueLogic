/**
 * Created Oct 2, 2006
 */
package com.roguelogic.roguenet.plugins;

import static com.roguelogic.roguenet.RNAConstants.*;

import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RNAPlugInInfo;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.SynchronousTransactionRequestor;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class SynchronousTransactionReceiver implements RNAPlugIn {

  private PlugInManager plugInManager;

  private HashMap<String, SynchronousTransactionRequestor> stRequestorMap;
  private Object strmLock;

  private static RNAPlugInInfo SynchronousTransactionReceiverPII;

  public static final String PPI_LOGICAL_NAME = "Synchronous Transaction Receiver";
  public static final String PPI_VERSION = "1.0";
  public static final String PPI_DESCRIPTION = "Synchronous Transaction Receiver integrated Plug-In.";
  public static final String PPI_DEVELOPER = "RogueLogic";
  public static final String PPI_URL = "http://www.roguelogic.com";
  public static final String PPI_COPYRIGHT = "(c) 2006 By: Robert C. Ilardi";

  static {
    SynchronousTransactionReceiverPII = new RNAPlugInInfo();

    SynchronousTransactionReceiverPII.setLogicalName(PPI_LOGICAL_NAME);
    SynchronousTransactionReceiverPII.setVersion(PPI_VERSION);
    SynchronousTransactionReceiverPII.setDescription(PPI_DESCRIPTION);
    SynchronousTransactionReceiverPII.setDeveloper(PPI_DEVELOPER);
    SynchronousTransactionReceiverPII.setUrl(PPI_URL);
    SynchronousTransactionReceiverPII.setCopyright(PPI_COPYRIGHT);
  }

  public SynchronousTransactionReceiver() {
    stRequestorMap = new HashMap<String, SynchronousTransactionRequestor>();
    strmLock = new Object();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#hook(com.roguelogic.roguenet.PlugInManager, java.lang.String)
   */
  public void hook(PlugInManager plugInManager, String configStr) throws RogueNetException {
    this.plugInManager = plugInManager;
    plugInManager.register(this, SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT, null);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#unhook()
   */
  public void unhook() throws RogueNetException {
    plugInManager.unregister(this, SYNCHRONOUS_TRANSACTION_RECEIVER_SUBJECT, null);
    stRequestorMap.clear();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handleMenuExec()
   */
  public void handleMenuExec() {}

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#handle(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void handle(P2PHubMessage mesg) throws RogueNetException {
    String transId;
    SynchronousTransactionRequestor requestor;

    transId = mesg.getProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP);

    synchronized (strmLock) {
      requestor = stRequestorMap.remove(transId);
    }

    if (requestor != null) {
      requestor.processSynchronousTransactionResponse(transId, mesg);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.roguenet.RNAPlugIn#getPlugInInfo()
   */
  public RNAPlugInInfo getPlugInInfo() {
    return SynchronousTransactionReceiverPII;
  }

  public String sendSynchronousTransaction(SynchronousTransactionRequestor requestor, P2PHubMessage mesg) throws RogueNetException {
    String transId = null;
    Properties mesgProps;
    boolean sent = false;

    if (requestor != null && mesg != null) {
      try {
        transId = StringUtils.GenerateTimeUniqueId();
        synchronized (strmLock) {
          stRequestorMap.put(transId, requestor);
        }

        mesgProps = mesg.getProperties();
        if (mesgProps == null) {
          mesgProps = new Properties();
          mesg.setProperties(mesgProps);
        }

        mesgProps.setProperty(SYNCHRONOUS_TRANSACTION_RECEIVER_TRANS_ID_PROP, transId);

        if (plugInManager != null && plugInManager.getNetAgent() != null) {
          sent = plugInManager.getNetAgent().sendMessage(mesg);
        }
      } //End try block
      finally {
        if (!sent) {
          synchronized (strmLock) {
            stRequestorMap.remove(transId);
          }
        }
      }
    } //End null input param check

    return transId;
  }

}
