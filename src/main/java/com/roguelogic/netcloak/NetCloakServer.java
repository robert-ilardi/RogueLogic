package com.roguelogic.netcloak;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;

public class NetCloakServer implements SocketProcessorCustomizer {

  public static final String PROP_PORT = "Port";
  public static final String PROP_USERNAME = "Username";
  public static final String PROP_PASSWORD = "Password";
  public static final String PROP_KEY_FILE = "KeyFile";
  public static final String PROP_ALLOWED_PORTS = "AllowedPorts";

  public static final String APP_TITLE = "Net Cloak - Server";
  public static final String VERSION = "2.0";
  public static final String ROGUELOGIC = "RogueLogic 3.0";
  public static final String RL_URL = "http://www.roguelogic.com";
  public static final String COPYRIGHT = "Copyright (c) 2002 - 2006 By: Robert C. Ilardi";

  public static final String GSOBJ_NET_CLOAK_KEY_DATA = "NetCloakKeyData";

  private Properties serverProps;

  private int port;
  private String username;
  private String password;
  private String keyFile;
  private int[] allowedPorts;

  private byte[] keyData;

  private SocketServer sockServer;

  public NetCloakServer(Properties props) throws NetCloakException {
    if (props == null) {
      throw new NetCloakException("Can NOT create Net Cloak Server with NULL Properties!");
    }

    serverProps = props;
  }

  public void start() throws NetCloakException {
    loadProperties();
    loadKeyData();
    startServer();
  }

  private void startServer() throws NetCloakException {
    try {
      sockServer = new SocketServer();

      sockServer.setSocketProcessorClass(NetCloakServerProcessor.class);
      sockServer.putGlobalItem(GSOBJ_NET_CLOAK_KEY_DATA, keyData);
      sockServer.setSocketProcessorCustomizer(this);
      sockServer.setSocketSessionSweeper(new NetCloakServerSessionSweeper());

      sockServer.listen(port);
      System.out.println("Net Cloak Server Running on Port = " + sockServer.getPort());
    }
    catch (Exception e) {
      throw new NetCloakException("Error while attempting to START the Net Cloak Server!", e);
    }
  }

  public void stop() throws NetCloakException {
    try {
      if (sockServer != null) {
        sockServer.close();
      }
    }
    catch (Exception e) {
      throw new NetCloakException("Error while attempting to STOP the Net Cloak Server!", e);
    }
  }

  private void loadProperties() throws NetCloakException {
    String tmp;
    String[] tmpArr;

    try {
      tmp = serverProps.getProperty(PROP_PORT);

      port = Integer.parseInt(tmp);

      username = serverProps.getProperty(PROP_USERNAME);

      password = serverProps.getProperty(PROP_PASSWORD);

      keyFile = serverProps.getProperty(PROP_KEY_FILE);

      tmp = serverProps.getProperty(PROP_ALLOWED_PORTS);

      tmpArr = tmp.split(",");
      allowedPorts = new int[tmpArr.length];
      for (int i = 0; i < tmpArr.length; i++) {
        allowedPorts[i] = Integer.parseInt(tmpArr[i].trim());
      }
    }
    catch (Exception e) {
      throw new NetCloakException("Error while loading Properties!", e);
    }
  }

  private void loadKeyData() throws NetCloakException {
    FileInputStream fis = null;
    ByteArrayOutputStream baos = null;
    byte[] buf;
    int len;

    try {
      fis = new FileInputStream(keyFile);
      buf = new byte[1024];
      baos = new ByteArrayOutputStream();

      len = fis.read(buf);
      while (len > 0) {
        baos.write(buf, 0, len);
        len = fis.read(buf);
      }

      keyData = baos.toByteArray();
    }
    catch (Exception e) {
      throw new NetCloakException("Error while attempting to load Key Data from file: " + keyFile);
    }
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
        fis = null;
      }

      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    NetCloakServerProcessor ncsProcessor;

    try {
      ncsProcessor = (NetCloakServerProcessor) processor;

      ncsProcessor.setUsername(username);
      ncsProcessor.setPassword(password);
      ncsProcessor.setAllowedPorts(allowedPorts);
    }
    catch (Exception e) {
      throw new RLNetException("Could NOT Initialize Net Cloak Server Processor!", e);
    }
  }

  public static void PrintWelcome() {
    StringBuffer sb = new StringBuffer();

    sb.append(APP_TITLE);
    sb.append("\n");

    sb.append("Version: ");
    sb.append(VERSION);
    sb.append("\n");

    sb.append(ROGUELOGIC);
    sb.append("\n");

    sb.append(RL_URL);
    sb.append("\n");

    sb.append(COPYRIGHT);
    sb.append("\n");

    sb.append("\n");
    System.out.print(sb.toString());
  }

  public static void main(String[] args) {
    String propsFile;
    FileInputStream fis = null;
    Properties props;
    boolean failed = false;
    NetCloakServer ncServer = null;

    NetCloakServer.PrintWelcome();

    if (args.length != 1) {
      System.err.println("Usage: java " + NetCloakServer.class.getName() + " [SERVER_PROPERTIES_FILE]");
      System.exit(1);
    }
    else {
      try {
        propsFile = args[0];

        fis = new FileInputStream(propsFile);
        props = new Properties();
        props.load(fis);

        ncServer = new NetCloakServer(props);
        ncServer.start();
      }
      catch (Exception e) {
        e.printStackTrace();
        failed = true;
      }
      finally {
        if (fis != null) {
          try {
            fis.close();
          }
          catch (Exception e) {}
          fis = null;
        }

        if (failed) {
          if (ncServer != null) {
            try {
              ncServer.stop();
            }
            catch (NetCloakException e) {
              e.printStackTrace();
            }
            ncServer = null;
          }
          System.exit(1);
        }
      }
    }
  }

}
