/**
 * Created Nov 21, 2007
 */
package com.roguelogic.pmd.protocol.pmd;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author Robert C. Ilardi
 *
 */
public class Handler extends URLStreamHandler {

  public static final String PROTOCOL = "PMD";

  public Handler() {
    super();
  }

  /* (non-Javadoc)
   * @see java.net.URLStreamHandler#openConnection(java.net.URL)
   */
  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    System.out.println("Protocol: " + url.getProtocol());
    System.out.println("Host: " + url.getHost());
    System.out.println("Port: " + url.getPort());
    System.out.println("File: " + url.getFile());
    System.out.println("UserInfo: " + url.getUserInfo());
    return new PMDConnection(url);
  }

  public void parseURL(URL u, String spec, int start, int limit) {
    String protocol = u.getProtocol();
    String[] tmpArr;
    String host, tmp, file, userInfo = null;
    int port;

    if (PROTOCOL.equalsIgnoreCase(protocol)) {
      tmp = spec.substring(start + 2, limit);

      tmpArr = tmp.split(":", 2);
      host = tmpArr[0];

      tmpArr = tmpArr[1].split("/", 2);
      port = Integer.parseInt(tmpArr[0]);

      file = tmpArr[1];

      if (host.indexOf("@") >= 0) {
        tmpArr = host.split("@", 2);
        userInfo = tmpArr[0].trim();
        host = tmpArr[1].trim();
      }

      /*System.out.println("URL: " + spec);
      System.out.println("Protocol: " + protocol);
      System.out.println("Host: " + host);
      System.out.println("Port: " + port);
      System.out.println("File: " + file);*/

      this.setURL(u, protocol, host, port, null, userInfo, file, null, null);
    }
  }

}
