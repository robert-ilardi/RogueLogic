/**
 * Created Jan 20, 2007
 */
package com.roguelogic.propexchange;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Properties;

import com.roguelogic.net.SocketSession;

/**
 * @author Robert C. Ilardi
 *
 */

public class PropExchangePayload implements Serializable {

  private Properties props;

  private SocketSession session;

  private boolean synchronous;
  private int syncId = -1;

  private String requestId;

  public PropExchangePayload() {}

  public Properties getProps() {
    return props;
  }

  public void setProps(Properties props) {
    this.props = props;
  }

  public SocketSession getSession() {
    return session;
  }

  public void setSession(SocketSession session) {
    this.session = session;
  }

  public boolean isSynchronous() {
    return synchronous;
  }

  public void setSynchronous(boolean synchronous) {
    this.synchronous = synchronous;
  }

  public int getSyncId() {
    return syncId;
  }

  public void setSyncId(int transactionId) {
    this.syncId = transactionId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestTransId) {
    this.requestId = requestTransId;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    Iterator iter;
    String name, value;

    sb.append("\n~~~~~~~~~~~~~~~~Properties Exchange Payload~~~~~~~~~~~~~~~~\n");

    sb.append("Request Id: ");
    sb.append(requestId);
    sb.append("\n");

    sb.append("Synchronous? ");
    sb.append((synchronous ? "YES" : "NO"));
    sb.append("\n");

    sb.append("Sync Id: ");
    sb.append(syncId);
    sb.append("\n");

    sb.append("Properties (Cnt = ");
    sb.append((props != null ? props.size() : -1));
    sb.append(") - \n");

    if (props != null) {
      iter = props.keySet().iterator();

      while (iter.hasNext()) {
        name = (String) iter.next();

        if (name != null) {
          value = props.getProperty(name);

          sb.append(name);
          sb.append(" = ");
          sb.append(value);
          sb.append("\n");
        }
      }
    }

    sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

    return sb.toString();
  }

}
