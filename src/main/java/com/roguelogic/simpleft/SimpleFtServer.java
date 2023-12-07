/**
 * Created Mar 31, 2009
 */
package com.roguelogic.simpleft;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */
public class SimpleFtServer implements SocketProcessorCustomizer, SocketSessionSweeper {

  public static final String SERVER_APP_TITLE = "SimpleFT Server";

  public static final String PROP_ROOT_LOGGER = "RootLogger";
  public static final String PROP_BIND_ADDRESS = "BindAddress";
  public static final String PROP_PORT = "Port";

  public static final String PROP_USERNAME = "Username";
  public static final String PROP_PASSWORD = "Password";

  public static final String PROP_TRANSPORT_XOR_FILE = "TransportXorFile";
  public static final String PROP_ACCESS_CONTROL_LIST = "AccessControlList";

  private Properties sftsProps;

  private SftLogger logger;

  private String bindAddr;
  private int port;

  private String username;
  private String password;

  private Object serverLock;

  private SocketServer sockServer;

  private String transportXorFile;
  private byte[] transportXorKey;

  private String accessControlListFile;
  private AccessControlList acl;

  public SimpleFtServer(Properties sftsProps) {
    this.sftsProps = sftsProps;

    serverLock = new Object();
  }

  public void boot() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
    System.out.println("Booting " + SERVER_APP_TITLE + " at: " + StringUtils.GetTimeStamp());

    readProperties();
    initRootLogger();
    loadTransportXorKey();
    loadAccessControlList();

    log(acl.toString());
  }

  private void readProperties() {
    String tmp;

    tmp = sftsProps.getProperty(PROP_PORT);
    port = Integer.parseInt(tmp);

    bindAddr = sftsProps.getProperty(PROP_BIND_ADDRESS);

    username = sftsProps.getProperty(PROP_USERNAME);
    password = sftsProps.getProperty(PROP_PASSWORD);

    transportXorFile = sftsProps.getProperty(PROP_TRANSPORT_XOR_FILE);
    accessControlListFile = sftsProps.getProperty(PROP_ACCESS_CONTROL_LIST);
  }

  private void initRootLogger() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    String rootLoggerClass = sftsProps.getProperty(PROP_ROOT_LOGGER);

    System.out.println("Loading Root Logger: " + rootLoggerClass);

    logger = (SftLogger) Class.forName(rootLoggerClass).newInstance();

    System.out.println();
  }

  private void loadTransportXorKey() throws IOException {
    transportXorKey = SystemUtils.LoadDataFromFile(transportXorFile);
  }

  private void loadAccessControlList() throws IOException {
    FileInputStream fis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    String tmp;
    String[] tmpArr;
    Share s;

    try {
      fis = new FileInputStream(accessControlListFile);
      isr = new InputStreamReader(fis);
      br = new BufferedReader(isr);

      acl = new AccessControlList();

      tmp = br.readLine();
      while (tmp != null) {
        tmpArr = tmp.trim().split("\\|");
        tmpArr = StringUtils.Trim(tmpArr);

        s = new Share();
        acl.addShare(s);

        s.setName(tmpArr[0]);

        // Set Options
        for (int i = 1; i < tmpArr.length; i++) {
          tmp = tmpArr[i].trim().toUpperCase();

          if ("R".equals(tmp)) {
            s.setRead(true);
          }
          else if ("W".equals(tmp)) {
            s.setWrite(true);
          }
          else if ("D".equals(tmp)) {
            s.setDelete(true);
          }
          else if ("M".equals(tmp)) {
            s.setMakeDir(true);
          }
        }

        tmp = br.readLine();
      }
    } // End try block
    finally {
      if (br != null) {
        try {
          br.close();
        }
        catch (Exception e) {}
      }

      if (isr != null) {
        try {
          isr.close();
        }
        catch (Exception e) {}
      }

      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
      }
    }
  }

  public static void PrintWelcome() {
    System.out.print(Version.GetInfo());
  }

  public void listen() throws RLNetException {
    synchronized (serverLock) {
      sockServer = new SocketServer();

      sockServer.setSocketProcessorClass(SimpleFtServerProcessor.class);
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
  }

  public void shutdown() {
    synchronized (serverLock) {
      if (sockServer == null) {
        return;
      }

      sockServer.close();
      sockServer = null;

      serverLock.notifyAll();
    }
  }

  public void waitWhileListening() throws InterruptedException {
    synchronized (serverLock) {
      while (sockServer.isListening()) {
        serverLock.wait(10000);
      }
    }
  }

  public byte[] getTransportXorKey() {
    return transportXorKey;
  }

  public void log(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.sftLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  public void log(Exception e) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setThrowable(e);

    if (logger != null) {
      logger.sftLogError(lMesg);
    }
    else {
      System.err.println(lMesg);
    }
  }

  public void logErr(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.sftLogError(lMesg);
    }
    else {
      System.err.println(lMesg);
    }
  }

  public String getProperty(String propName) {
    return sftsProps.getProperty(propName);
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    SimpleFtServerProcessor sftsPrc;

    if (processor instanceof SimpleFtServerProcessor) {
      sftsPrc = (SimpleFtServerProcessor) processor;
      sftsPrc.setServer(this);
    }

  }

  public void cleanup(SocketSession userSession) {
    BufferedOutputStream bos = (BufferedOutputStream) userSession.removeUserItem(SimpleFtServerProcessor.USOBJ_UPLOAD_HANDLE);

    if (bos != null) {
      try {
        bos.close();
      }
      catch (Exception e) {
        log(e);
      }
    }

    BufferedInputStream bis = (BufferedInputStream) userSession.removeUserItem(SimpleFtServerProcessor.USOBJ_DOWNLOAD_HANDLE);

    if (bis != null) {
      try {
        bis.close();
      }
      catch (Exception e) {
        log(e);
      }
    }
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public AccessControlList getAccessControlList() {
    return acl;
  }

  public static void main(String[] args) {
    int exitCd;
    Properties props;
    FileInputStream fis = null;
    SimpleFtServer server;

    if (args.length != 1) {
      System.err.println("Usage: java " + SimpleFtServer.class.getName() + " [SERVER_PROPERTIES_FILE]");
      exitCd = 1;
    }
    else {
      try {
        PrintWelcome();

        System.out.println("Using " + SERVER_APP_TITLE + " Properties File: " + args[0]);

        fis = new FileInputStream(args[0]);
        props = new Properties();
        props.load(fis);
        fis.close();
        fis = null;

        server = new SimpleFtServer(props);
        server.boot();

        server.listen();

        server.waitWhileListening();

        exitCd = 0;
      }
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
      finally {
        if (fis != null) {
          try {
            fis.close();
          }
          catch (Exception e) {}
        }
      }
    }

    System.exit(exitCd);
  }

}
