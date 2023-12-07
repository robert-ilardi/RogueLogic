/**
 * Created Jan 15, 2008
 */
package com.roguelogic.hptunnel;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class HPTLocalProxy implements SocketProcessorCustomizer, SocketSessionSweeper {

  private SocketServer server;

  private int proxyPort;
  private String remoteAddress;
  private int remotePort;
  private String username;
  private String password;
  private byte[] encryptionKey;

  private Object hptLock;

  public HPTLocalProxy() {
    hptLock = new Object();
  }

  public void start() throws RLNetException, HPTException {
    synchronized (hptLock) {
      if (server != null) {
        return;
      }

      if (password == null || password.indexOf("|") >= 0) {
        throw new HPTException("Password can not be NULL or contain PIPEs!");
      }

      if (username == null || username.indexOf("|") >= 0) {
        throw new HPTException("Username can not be NULL or contain PIPEs!");
      }

      if (encryptionKey == null || encryptionKey.length == 0) {
        throw new HPTException("Encryption Key must be greater than 0 bytes in length!");
      }

      server = new SocketServer();
      server.setSocketProcessorClass(HPTLocalProxyProcessor.class);
      server.setInitialWorkersCnt(1);
      server.setSocketProcessorCustomizer(this);
      server.setSocketSessionSweeper(this);
      server.setLingerTimeOverride(10);
      server.setReuseAddress(true);
      server.listen("127.0.0.1", proxyPort);

      System.out.println("HTTP Proxy Tunnel Local Proxy Running on Port = " + proxyPort);
      hptLock.notifyAll();
    }
  }

  public void stop() {
    synchronized (hptLock) {
      if (server == null) {
        return;
      }

      server.close();
      server = null;

      hptLock.notifyAll();
    }
  }

  public void waitWhileProxyIsRunning() throws InterruptedException {
    synchronized (hptLock) {
      while (server.isListening()) {
        hptLock.wait(10000);
      }
    }
  }

  public byte[] getEncryptionKey() {
    return encryptionKey;
  }

  public void setEncryptionKey(byte[] encryptionKey) {
    this.encryptionKey = encryptionKey;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    HPTLocalProxyProcessor hptlpPrc;

    if (processor instanceof HPTLocalProxyProcessor) {
      hptlpPrc = (HPTLocalProxyProcessor) processor;
      hptlpPrc.setProxy(this);
    }
  }

  public void cleanup(SocketSession userSession) {
    userSession.drainRawData();

    System.out.println("Browser Closed?");

    HttpProxyTunnelClient client = (HttpProxyTunnelClient) userSession.getUserItem(HPTLocalProxyProcessor.USOBJ_HPTCLIENT);

    if (client != null) {
      client.close();
    }

    userSession.clearUserData();
  }

  public static void main(String[] args) {
    int retCode;
    HPTLocalProxy hptc = null;
    byte[] encryptionKey;

    if (args.length != 6) {
      System.err.println("Usage: java " + HPTLocalProxy.class.getName() + " [PROXY_PORT] [REMOTE_ADDRESS] [REMOTE_PORT] [USERNAME] [PASSWORD] [ENCRYPTION_KEY_PATH]");
      retCode = 1;
    }
    else {
      try {
        hptc = new HPTLocalProxy();

        hptc.setProxyPort(Integer.parseInt(args[0].trim()));
        hptc.setRemoteAddress(args[1].trim());
        hptc.setRemotePort(Integer.parseInt(args[2].trim()));
        hptc.setUsername(args[3].trim());
        hptc.setPassword(args[4].trim());

        encryptionKey = SystemUtils.LoadDataFromFile(args[5].trim());
        hptc.setEncryptionKey(encryptionKey);
        encryptionKey = null;

        hptc.start();

        hptc.waitWhileProxyIsRunning();

        retCode = 0;
      }
      catch (Throwable t) {
        retCode = 1;
        t.printStackTrace();
      }
    }

    System.exit(retCode);
  }

}
