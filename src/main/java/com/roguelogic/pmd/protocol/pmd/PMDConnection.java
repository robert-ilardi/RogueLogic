/**
 * Created Nov 21, 2007
 */
package com.roguelogic.pmd.protocol.pmd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.roguelogic.pmd.PMDClient;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */
public class PMDConnection extends URLConnection {

  private PMDClient client;

  /**
   * @param url
   */
  public PMDConnection(URL url) {
    super(url);
    this.setAllowUserInteraction(true);
    this.setDoInput(true);
  }

  /* (non-Javadoc)
   * @see java.net.URLConnection#connect()
   */
  @Override
  public synchronized void connect() throws IOException {
    String username = "", password = "";
    String[] tmpArr;

    if (client != null && client.isConnected()) {
      return;
    }

    try {
      if (!StringUtils.IsNVL(url.getUserInfo())) {
        tmpArr = url.getUserInfo().trim().split("\\|", 2);

        if (tmpArr.length >= 1) {
          username = tmpArr[0].trim();
        }

        if (tmpArr.length >= 2) {
          password = tmpArr[1].trim();
        }
      }

      client = new PMDClient();
      client.connect(url.getHost(), url.getPort());
      client.login(username, password);
    }
    catch (Exception e) {
      throw new IOException(StringUtils.GetStackTraceString(e));
    }
  }

  public synchronized void disconnect() {
    if (client == null || !client.isConnected()) {
      return;
    }

    client.close();
    client = null;
  }

  public String getContentType() {
    //return "audio/mpeg3";
    return "audio/mpeg";
  }

  public int getContentLength() {
    if (client == null || !client.isConnected()) {
      return -1;
    }

    try {
      return client.getFileLength(url.getFile());
    }
    catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  public synchronized InputStream getInputStream() throws IOException {
    if (client == null || !client.isConnected()) {
      throw new IOException("PMD Client NOT Connected to Daemon!");
    }

    try {
      client.openFile(url.getFile());
    }
    catch (Exception e) {
      throw new IOException(StringUtils.GetStackTraceString(e));
    }

    return client.getRemoteInputStream();
  }

}
