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

public class HttpClientProcessor implements SocketProcessor {

  private HttpClient client;

  public HttpClientProcessor() {}

  public void setClient(HttpClient client) {
    this.client = client;
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
    if (userSession.wasHandshook() && data != null && data.length > 0) {
      client.sendDataThroughTunnel(data);
    }
    else {
      userSession.setHandshookStatus(true);
    }
  }

}
