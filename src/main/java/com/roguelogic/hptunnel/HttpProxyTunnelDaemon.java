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

public class HttpProxyTunnelDaemon

implements SocketProcessorCustomizer, SocketSessionSweeper {

  private SocketServer server;

  private int port;
  private String username;
  private String password;
  private byte[] encryptionKey;

  private Object hptLock;

  public HttpProxyTunnelDaemon() {
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
      server.setSocketProcessorClass(HttpProxyTunnelDaemonProcessor.class);
      server.setInitialWorkersCnt(1);
      server.setSocketProcessorCustomizer(this);
      server.setSocketSessionSweeper(this);
      server.setLingerTimeOverride(10);
      server.setReuseAddress(true);
      server.listen(port);

      System.out.println("HTTP Proxy Tunnel Daemon Running on Port = " + port);
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

  public void waitWhileDaemonIsRunning() throws InterruptedException {
    synchronized (hptLock) {
      while (server.isListening()) {
        hptLock.wait(10000);
      }
    }
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public byte[] getEncryptionKey() {
    return encryptionKey;
  }

  public void setEncryptionKey(byte[] encryptionKey) {
    this.encryptionKey = encryptionKey;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    HttpProxyTunnelDaemonProcessor hptdPrc;

    if (processor instanceof HttpProxyTunnelDaemonProcessor) {
      hptdPrc = (HttpProxyTunnelDaemonProcessor) processor;
      hptdPrc.setDaemon(this);
    }
  }

  public void cleanup(SocketSession userSession) {
    userSession.drainRawData();

    System.out.println("Proxy Tunnel Closed?");
  }

  public static void main(String[] args) {
    int retCode;
    HttpProxyTunnelDaemon hptd = null;
    byte[] encryptionKey;

    if (args.length != 4) {
      System.err.println("Usage: java " + HttpProxyTunnelDaemon.class.getName() + " [PORT] [USERNAME] [PASSWORD] [ENCRYPTION_KEY_PATH]");
      retCode = 1;
    }
    else {
      try {
        hptd = new HttpProxyTunnelDaemon();

        hptd.setPort(Integer.parseInt(args[0].trim()));
        hptd.setUsername(args[1].trim());
        hptd.setPassword(args[2].trim());

        encryptionKey = SystemUtils.LoadDataFromFile(args[3].trim());
        hptd.setEncryptionKey(encryptionKey);
        encryptionKey = null;

        hptd.start();

        hptd.waitWhileDaemonIsRunning();

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
