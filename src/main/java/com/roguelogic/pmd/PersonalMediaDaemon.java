/**
 * Created Nov 20, 2007
 */
package com.roguelogic.pmd;

import java.io.File;
import java.util.ArrayList;

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

public class PersonalMediaDaemon implements SocketProcessorCustomizer, SocketSessionSweeper {

  private SocketServer server;

  private int port;
  private String username;
  private String password;
  private String[] shareDirs;
  private String[] expandedSharedDirs;

  private Object pmdLock;

  public PersonalMediaDaemon() {
    pmdLock = new Object();
  }

  public void start() throws RLNetException, PMDException {
    synchronized (pmdLock) {
      if (server != null) {
        return;
      }

      if (password == null || password.indexOf("|") >= 0) {
        throw new PMDException("Password can not be NULL or contain PIPEs!");
      }

      if (username == null || username.indexOf("|") >= 0) {
        throw new PMDException("Username can not be NULL or contain PIPEs!");
      }

      if (shareDirs == null || shareDirs.length == 0) {
        throw new PMDException("There MUST be at least 1 Shared Directory!");
      }

      server = new SocketServer();
      server.setSocketProcessorClass(PersonalMediaDaemonProcessor.class);
      server.setInitialWorkersCnt(1);
      server.setSocketProcessorCustomizer(this);
      server.setSocketSessionSweeper(this);
      server.listen(port);

      System.out.println("Personal Media Daemon Running on Port = " + port);
      pmdLock.notifyAll();
    }
  }

  public void stop() {
    synchronized (pmdLock) {
      if (server == null) {
        return;
      }

      server.close();
      server = null;

      pmdLock.notifyAll();
    }
  }

  public void waitWhileDaemonIsRunning() throws InterruptedException {
    synchronized (pmdLock) {
      while (server.isListening()) {
        pmdLock.wait(10000);
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

  public void setShareDirs(String[] shareDirs) {
    this.shareDirs = shareDirs;

    if (this.shareDirs != null) {
      for (int i = 0; i < this.shareDirs.length; i++) {
        if (!StringUtils.IsNVL(this.shareDirs[i])) {
          this.shareDirs[i] = this.shareDirs[i].trim();
          if (!this.shareDirs[i].endsWith("/")) {
            this.shareDirs[i] = new StringBuffer().append(this.shareDirs[i]).append("/").toString();
          }
        }
      }
    }
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public String[] getShareDirs() {
    synchronized (pmdLock) {
      if (expandedSharedDirs != null) {
        return expandedSharedDirs;
      }

      ArrayList<String> flatShareTree = new ArrayList<String>();
      String[] flatShareTreeArr;

      for (String share : shareDirs) {
        flatShareTree.add(share);
        getFlatShareTree(share, flatShareTree);
      }

      flatShareTreeArr = new String[flatShareTree.size()];
      flatShareTreeArr = (String[]) flatShareTree.toArray(flatShareTreeArr);

      expandedSharedDirs = flatShareTreeArr;

      return flatShareTreeArr;
    }
  }

  private void getFlatShareTree(String root, ArrayList<String> flatTree) {
    File dir;
    File[] ls;

    dir = new File(root);

    if (dir.isDirectory()) {
      ls = dir.listFiles();

      for (File f : ls) {
        if (f.isDirectory()) {
          flatTree.add(f.getPath());
          getFlatShareTree(f.getPath(), flatTree);
        }
      }
    }
  }

  public String getUsername() {
    return username;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    PersonalMediaDaemonProcessor pmdPrc;

    if (processor instanceof PersonalMediaDaemonProcessor) {
      pmdPrc = (PersonalMediaDaemonProcessor) processor;
      pmdPrc.setDaemon(this);
    }
  }

  public void cleanup(SocketSession userSession) {}

  public static void main(String[] args) {
    int retCode;
    String[] tmpArr;
    PersonalMediaDaemon pmd = null;

    if (args.length < 4) {
      System.err.println("Usage: java " + PersonalMediaDaemon.class.getName() + " [PORT] [USERNAME] [PASSWORD] [SHARED_DIR1] <SHARED_DIR2> ... <SHARED_DIR(N)>");
      retCode = 1;
    }
    else {
      try {
        pmd = new PersonalMediaDaemon();

        pmd.setPort(Integer.parseInt(args[0].trim()));
        pmd.setUsername(args[1].trim());
        pmd.setPassword(args[2].trim());

        tmpArr = new String[args.length - 3];
        System.arraycopy(args, 3, tmpArr, 0, tmpArr.length);
        pmd.setShareDirs(tmpArr);

        pmd.start();

        pmd.waitWhileDaemonIsRunning();

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
