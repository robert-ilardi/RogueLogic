/**
 * Created Aug 15, 2008
 */
package com.roguelogic.clustering.rfile;

import java.io.File;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RemoteFileServer implements SocketProcessorCustomizer, SocketSessionSweeper {

  private String bindAddr;
  private int port;
  private RFileSecurityManager secMan;

  private Object serverLock;

  private SocketServer sockServer;

  public RemoteFileServer(int port, RFileSecurityManager secMan) throws RemoteFileException {
    this("localhost", port, secMan);

    serverLock = new Object();

    listen();
  }

  public RemoteFileServer(String bindAddr, int port, RFileSecurityManager secMan) {
    this.bindAddr = bindAddr;
    this.port = port;
    this.secMan = secMan;
  }

  public String getBindAddr() {
    return bindAddr;
  }

  public int getPort() {
    return port;
  }

  private void listen() throws RemoteFileException {
    try {
      synchronized (serverLock) {
        sockServer = new SocketServer();

        sockServer.setSocketProcessorClass(RemoteFileServerProcessor.class);
        sockServer.setSocketProcessorCustomizer(this);
        sockServer.setSocketSessionSweeper(this);

        if (StringUtils.IsNVL(bindAddr)) {
          sockServer.listen(port);
        }
        else {
          sockServer.listen(bindAddr, port);
        }

        serverLock.notifyAll();
      }
    } //End try block
    catch (Exception e) {
      throw new RemoteFileException("Could NOT start Remote File Server! Message: " + e.getMessage(), e);
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    RemoteFileServerProcessor rfsProc = (RemoteFileServerProcessor) processor;
    rfsProc.setRemoteFileServer(this);
  }

  public void cleanup(SocketSession userSession) {}

  public void shutdown() {
    synchronized (serverLock) {
      if (sockServer != null) {
        sockServer.close();
      }

      serverLock.notifyAll();
    }
  }

  public boolean login(String username, String password) throws RemoteFileException {
    return secMan.login(username, password);
  }

  public boolean exists(String filePath) {
    File f;

    f = new File(filePath);

    return f.exists();
  }

  public boolean isDirectory(String filePath) {
    File f;

    f = new File(filePath);

    return f.isDirectory();
  }

  public boolean isFile(String filePath) {
    File f;

    f = new File(filePath);

    return f.isFile();
  }

  public void createDirectory(String filePath) throws RemoteFileException {
    File f;

    f = new File(filePath);

    if (!f.mkdir()) {
      throw new RemoteFileException("Failed to Create new Directory '" + filePath + "'");
    }
  }

  public void rename(String filePath, String toName) throws RemoteFileException {
    File f1, f2;

    f1 = new File(filePath);
    f2 = new File(toName);

    if (!f1.renameTo(f2)) {
      throw new RemoteFileException("Failed to Rename '" + filePath + "' to '" + toName + "'");
    }
  }

  public void delete(String filePath) throws RemoteFileException {
    File f;

    f = new File(filePath);

    if (!f.delete()) {
      throw new RemoteFileException("Failed to Delete '" + filePath + "'");
    }
  }

  public RDirEntry[] list(String filePath) {
    File f;
    File[] fl;
    RDirEntry[] ls = null;

    f = new File(filePath);

    fl = f.listFiles();

    if (fl != null) {
      ls = new RDirEntry[fl.length];

      for (int i = 0; i < fl.length; i++) {
        ls[i] = new RDirEntry(fl[i]);
      }
    }

    return ls;
  }

}
