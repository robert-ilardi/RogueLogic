/**
 * Created Sep 16, 2006
 */
package com.roguelogic.p2phub.server;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.p2phub.P2PHubException;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.p2phub.P2PHubUtils;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class P2PHubServer implements SocketProcessorCustomizer {

  public static final String PROP_SERVER_NAME = "ServerName";
  public static final String PROP_PORT = "Port";
  public static final String PROP_PEER_FILE = "PeerFile";
  public static final String PROP_KEY_FILE = "KeyFile";
  public static final String PROP_HEART_BEAT_TTL = "HeartBeatTTL";

  public static final String PROP_STDOUT_FILE = "StdOutFile";
  public static final String PROP_STDERR_FILE = "StdErrFile";

  private Properties serverProps;

  private String serverName;
  private int port;
  private String peerFile;
  private String keyFile;

  private byte[] keyData;

  private String stdOutFile = null;
  private String stdErrFile = null;

  private ArrayList<P2PHubPeer> peers;

  private SocketServer sockServer;

  private HashMap<P2PHubPeer, SocketSession> peerSessionMap;
  private Object psMapLock;

  private long uniqueSessionId = 1000;
  private Object sessionIdGeneratorLock;

  private HashMap<String, Long> peerHeatBeatMap;
  private Object phbMapLock;

  private int heartBeatTTL;

  public P2PHubServer(Properties props) throws P2PHubException {
    if (props == null) {
      throw new P2PHubException("Can NOT create " + Version.APP_TITLE + " with NULL Properties!");
    }

    serverProps = props;

    peers = new ArrayList<P2PHubPeer>();

    peerSessionMap = new HashMap<P2PHubPeer, SocketSession>();
    psMapLock = new Object();

    sessionIdGeneratorLock = new Object();

    peerHeatBeatMap = new HashMap<String, Long>();
    phbMapLock = new Object();

    heartBeatTTL = 0;
  }

  public void start() throws P2PHubException {
    System.out.println("Initializing " + Version.APP_TITLE + "........");

    loadProperties();
    doStdRedirects();
    loadKeyData();
    loadPeerFile();
    startServer();
  }

  private void startServer() throws P2PHubException {
    P2PHubServerSessionSweeper sweeper;

    try {
      sockServer = new SocketServer();

      sockServer.setSocketProcessorClass(P2PHubServerProcessor.class);
      sockServer.setSocketProcessorCustomizer(this);

      sweeper = new P2PHubServerSessionSweeper();
      sweeper.setServer(this);
      sockServer.setSocketSessionSweeper(sweeper);

      sockServer.listen(port);
      System.out.println(Version.APP_TITLE + " Running on Port = " + sockServer.getPort());

      System.out.println("Server Startup Time: " + StringUtils.GetTimeStamp());
    }
    catch (Exception e) {
      throw new P2PHubException("Error while attempting to START the " + Version.APP_TITLE + "!", e);
    }
  }

  public void stop() throws P2PHubException {
    try {
      if (sockServer != null) {
        sockServer.close();
        sockServer = null;
      }

      peers.clear();
      peerSessionMap.clear();
    }
    catch (Exception e) {
      throw new P2PHubException("Error while attempting to STOP the " + Version.APP_TITLE + "!", e);
    }
  }

  private void loadProperties() throws P2PHubException {
    String tmp;

    try {
      System.out.print("Loading Properties - ");

      serverName = serverProps.getProperty(PROP_SERVER_NAME);

      tmp = serverProps.getProperty(PROP_PORT);
      port = Integer.parseInt(tmp);

      keyFile = serverProps.getProperty(PROP_KEY_FILE);

      peerFile = serverProps.getProperty(PROP_PEER_FILE);

      stdOutFile = serverProps.getProperty(PROP_STDOUT_FILE);
      stdErrFile = serverProps.getProperty(PROP_STDERR_FILE);

      tmp = serverProps.getProperty(PROP_HEART_BEAT_TTL);
      if (!StringUtils.IsNVL(tmp)) {
        heartBeatTTL = Integer.parseInt(tmp);
      }
      else {
        heartBeatTTL = 0;
      }

      System.out.println("OK");
    }
    catch (Exception e) {
      System.out.println("FAILED");
      throw new P2PHubException("Error while loading Properties!", e);
    }
  }

  private void loadKeyData() throws P2PHubException {
    try {
      System.out.print("Loading Key Data - ");
      keyData = P2PHubUtils.LoadKeyData(keyFile);
      System.out.println("OK");
    }
    catch (Exception e) {
      System.out.println("FAILED");
      throw new P2PHubException("Error while attempting to load Key Data from file: " + keyFile);
    }
  }

  private void loadPeerFile() throws P2PHubException {
    String[] lines;
    FileInputStream fis = null;
    P2PHubPeer peer;

    try {
      System.out.print("Loading Peer Information - ");

      fis = new FileInputStream(peerFile);
      lines = StringUtils.ReadLines(fis);

      if (lines != null) {
        for (int i = 0; i < lines.length; i++) {
          lines[i] = lines[i].trim();

          if (!lines[i].startsWith("#")) {
            peer = P2PHubPeer.Decode(lines[i]);

            if (peer != null) {
              peers.add(peer);
            }
          }
        }
      }

      System.out.println("OK ; Peer Cnt = " + peers.size());
    } //End try block
    catch (Exception e) {
      System.out.println("FAILED");
      throw new P2PHubException("Error while attempting to load Peer Information from file: " + peerFile);
    }
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
        fis = null;
      }
    }
  }

  private void doStdRedirects() throws P2PHubException {
    try {
      System.out.println("Checking for optional Std Out/Err Redirects...");

      if (!StringUtils.IsNVL(stdOutFile)) {
        System.out.println("Redirecting STDOUT to: " + stdOutFile.trim());
        SystemUtils.RedirectStdOut(stdOutFile.trim());
        System.out.print("STDOUT for " + P2PHubServer.class.getName() + " Redirected to: " + stdOutFile.trim() + "\n---------------------------\n\n");
      }

      if (!StringUtils.IsNVL(stdErrFile)) {
        System.out.println("Redirecting STDERR to: " + stdErrFile.trim());
        SystemUtils.RedirectStdErr(stdErrFile.trim());
        System.err.println("STDERR for " + P2PHubServer.class.getName() + " Redirected to: " + stdErrFile.trim() + "\n---------------------------\n\n");
      }
    }
    catch (Exception e) {
      throw new P2PHubException("An error occurred while attempting to Redirect Std Out/Err!", e);
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    P2PHubServerProcessor p2phsProcessor;

    try {
      p2phsProcessor = (P2PHubServerProcessor) processor;
      p2phsProcessor.setServer(this);
    }
    catch (Exception e) {
      throw new RLNetException("Could NOT Initialize " + Version.APP_TITLE + " Processor!", e);
    }
  }

  public static void PrintWelcome() {
    System.out.print(Version.GetInfo());
  }

  protected byte[] getKeyData() {
    return keyData;
  }

  protected ArrayList<P2PHubPeer> getPeers() {
    return peers;
  }

  public void associateSessionWithPeer(SocketSession userSession, P2PHubPeer peer) {
    synchronized (psMapLock) {
      peerSessionMap.put(peer, userSession);
    }
  }

  public SocketSession getSessionAssociatedWithPeer(P2PHubPeer peer) {
    synchronized (psMapLock) {
      return peerSessionMap.get(peer);
    }
  }

  public void removeSessionPeerAssociation(P2PHubPeer peer) {
    synchronized (psMapLock) {
      peerSessionMap.remove(peer);
    }
  }

  public P2PHubPeer[] getLoggedInPeers() {
    P2PHubPeer[] peers = null;
    Iterator iter;
    int cnt = 0;

    synchronized (psMapLock) {
      peers = new P2PHubPeer[peerSessionMap.size()];

      iter = peerSessionMap.keySet().iterator();

      while (iter.hasNext()) {
        peers[cnt] = (P2PHubPeer) iter.next();
        cnt++;
      }
    }

    return peers;
  }

  public void setSessionToken(P2PHubPeer peer) {
    StringBuffer token;

    synchronized (sessionIdGeneratorLock) {
      if (peer != null) {
        token = new StringBuffer();

        token.append(serverName);
        token.append(".");
        token.append(uniqueSessionId);
        token.append(".");
        token.append(peer.getUsername());
        token.append(".");
        token.append(StringUtils.GenerateTimeUniqueId());

        peer.setSessionToken(token.toString());

        uniqueSessionId++;

        //If we sleep for 1 second between each logins, it will take over 31 years to reach 1 billion logins.
        //This helps prevent people from trying to launch a DOS attack...
        SystemUtils.Sleep(1);
      }
    }
  }

  public boolean isLoggedIn(P2PHubPeer peer) {
    boolean peerFound;
    long hbTs = -1;

    synchronized (psMapLock) {
      peerFound = peerSessionMap.containsKey(peer);
    }

    if (heartBeatTTL > 0) {
      if (peerFound) {
        hbTs = getHeartBeatEntry(peer);
      }

      return (hbTs != -1 && ((hbTs + (heartBeatTTL * 1000)) >= System.currentTimeMillis()));
    }
    else {
      return peerFound;
    }
  }

  public boolean isServerUp() {
    return (sockServer != null && sockServer.isListening());
  }

  public void updateHeartBeatEntry(P2PHubPeer peer, long hbTs) {
    if (peer != null) {
      synchronized (phbMapLock) {
        //System.out.println("Update HB Entry for user '" + peer.getUsername() + "' ; HB = " + hbTs);
        peerHeatBeatMap.put(peer.getUsername(), System.currentTimeMillis()); //Use server's time because we don't know if everyone's clock is in sync...
      }
    }
  }

  public void removeHeartBeatEntry(P2PHubPeer peer) {
    if (peer != null) {
      removeHeartBeatEntry(peer.getUsername());
    }
  }

  public void removeHeartBeatEntry(String peerUsername) {
    if (peerUsername != null) {
      synchronized (phbMapLock) {
        peerHeatBeatMap.remove(peerUsername);
      }
    }
  }

  public long getHeartBeatEntry(P2PHubPeer peer) {
    long hbTs = -1;

    if (peer != null) {
      hbTs = getHeartBeatEntry(peer.getUsername());
    }

    return hbTs;
  }

  public long getHeartBeatEntry(String p2phUsername) {
    Long entry;
    long hbTs = -1;

    synchronized (phbMapLock) {
      entry = peerHeatBeatMap.get(p2phUsername);
    }

    if (entry != null) {
      hbTs = entry.longValue();
    }

    return hbTs;
  }

  public static void main(String[] args) {
    String propsFile;
    FileInputStream fis = null;
    Properties props;
    boolean failed = false;
    P2PHubServer hubServer = null;

    P2PHubServer.PrintWelcome();

    if (args.length != 1) {
      System.err.println("Usage: java " + P2PHubServer.class.getName() + " [SERVER_PROPERTIES_FILE]");
      System.exit(1);
    }
    else {
      try {
        propsFile = args[0];

        fis = new FileInputStream(propsFile);
        props = new Properties();
        props.load(fis);

        hubServer = new P2PHubServer(props);
        hubServer.start();
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
          if (hubServer != null) {
            try {
              hubServer.stop();
            }
            catch (P2PHubException e) {
              e.printStackTrace();
            }
            hubServer = null;
          }
          System.exit(1);
        }
      }
    }
  }

}
