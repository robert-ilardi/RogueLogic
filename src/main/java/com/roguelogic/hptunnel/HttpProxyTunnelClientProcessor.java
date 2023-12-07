/**
 * Created Jan 15, 2008
 */
package com.roguelogic.hptunnel;

import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_DATA_STREAM;
import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_EK_SHIFT;
import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_LOGIN;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 *
 */

public class HttpProxyTunnelClientProcessor extends RLTalkSocketProcessor {

  private HttpProxyTunnelClient client;

  public HttpProxyTunnelClientProcessor() {
    super();
  }

  public void setClient(HttpProxyTunnelClient client) {
    this.client = client;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandshake()
   */
  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandle(com.roguelogic.net.rltalk.CommandDataPair)
   */
  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    switch (cmDatPair.getCommand()) {
      case HPT_RLTCMD_EK_SHIFT:
        client.processEkShift(cmDatPair);
        break;
      case HPT_RLTCMD_LOGIN:
        client.processLogin(cmDatPair);
        break;
      case HPT_RLTCMD_DATA_STREAM:
        client.processIncomingDataStream(cmDatPair);
        break;
    }
  }

}
