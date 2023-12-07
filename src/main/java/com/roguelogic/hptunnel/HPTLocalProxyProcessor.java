/**
 * Created Jan 15, 2008
 */
package com.roguelogic.hptunnel;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketSession;

/**
 * @author Robert C. Ilardi
 *
 */

public class HPTLocalProxyProcessor implements SocketProcessor {

  public static final String USOBJ_HPTCLIENT = "HPTClient";

  private HPTLocalProxy proxy;

  public HPTLocalProxyProcessor() {}

  public void setProxy(HPTLocalProxy proxy) {
    this.proxy = proxy;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.net.SocketProcessor#clearSession()
   */
  public void clearSession() {}

  /* (non-Javadoc)
   * @see com.roguelogic.net.SocketProcessor#destroyProcessor()
   */
  public void destroyProcessor() {}

  /* (non-Javadoc)
   * @see com.roguelogic.net.SocketProcessor#process(com.roguelogic.net.SocketSession, byte[])
   */
  public void process(SocketSession userSession, byte[] data) throws RLNetException {
    if (!userSession.wasHandshook()) {
      //Create Tunnel Connection
      createTunnelConnection(userSession);
      userSession.setHandshookStatus(true);
    }
    else {
      //Send data through tunnel
      sendDataThroughTunnel(userSession, data);
    }
  }

  private void createTunnelConnection(SocketSession userSession) throws RLNetException {
    HttpProxyTunnelClient client;

    try {
      client = new HttpProxyTunnelClient();
      client.setLocalProxy(proxy);
      client.setBrowser(userSession);

      client.connect(proxy.getRemoteAddress(), proxy.getRemotePort());
      client.determineEncryptionKeyShift();
      client.login(proxy.getUsername(), proxy.getPassword());

      userSession.putUserItem(USOBJ_HPTCLIENT, client);
    }
    catch (HPTException e) {
      throw new RLNetException(e);
    }
    catch (InterruptedException e) {
      throw new RLNetException(e);
    }
  }

  private void sendDataThroughTunnel(SocketSession userSession, byte[] data) throws RLNetException {
    HttpProxyTunnelClient client;

    try {
      client = (HttpProxyTunnelClient) userSession.getUserItem(USOBJ_HPTCLIENT);
      client.sendDataThroughTunnel(data);
    }
    catch (HPTException e) {
      System.out.println("|" + new String(data) + "|");
      throw new RLNetException(e);
    }
  }

}
