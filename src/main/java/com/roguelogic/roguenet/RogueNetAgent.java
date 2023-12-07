/**
 * Created Sep 23, 2006
 */
package com.roguelogic.roguenet;

import static com.roguelogic.roguenet.RNAConstants.PROP_ENTITLEMENTS_MANAGER_IMPL;
import static com.roguelogic.roguenet.RNAConstants.PROP_INTEGRATED_P2P_HUB_SERVER_AUTO_START;
import static com.roguelogic.roguenet.RNAConstants.PROP_INTEGRATED_P2P_HUB_SERVER_ENABLED;
import static com.roguelogic.roguenet.RNAConstants.PROP_INTEGRATED_P2P_HUB_SERVER_HEART_BEAT_TTL;
import static com.roguelogic.roguenet.RNAConstants.PROP_INTEGRATED_P2P_HUB_SERVER_KEY_FILE;
import static com.roguelogic.roguenet.RNAConstants.PROP_INTEGRATED_P2P_HUB_SERVER_NAME;
import static com.roguelogic.roguenet.RNAConstants.PROP_INTEGRATED_P2P_HUB_SERVER_PEER_FILE;
import static com.roguelogic.roguenet.RNAConstants.PROP_INTEGRATED_P2P_HUB_SERVER_PORT;
import static com.roguelogic.roguenet.RNAConstants.PROP_PLUG_INS_PROPERTIES_FILE;
import static com.roguelogic.roguenet.RNAConstants.PROP_ROOT_PROPERTIES_FILE;
import static com.roguelogic.roguenet.RNAConstants.PROP_STDERR_FILE;
import static com.roguelogic.roguenet.RNAConstants.PROP_STDOUT_FILE;
import static com.roguelogic.roguenet.RNAConstants.PROP_USER_PROPERTIES_FILE;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.p2phub.P2PHubException;
import com.roguelogic.p2phub.server.P2PHubServer;
import com.roguelogic.roguenet.gui.RNATrayMenu;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RogueNetAgent {

  private String rnaPropFile;
  private Properties rnaProps;

  private Date bootTs;
  private Date shutdownTs;

  private NetworkAgent netAgent;
  private RNATrayMenu trayMenu;
  private PlugInManager plugInManager;
  private P2PHubServer integratedHubServer;
  private RNAEntitlementsManager entitlementsManager;

  private Object agentUpLock = new Object();
  private boolean userShutdown;

  private boolean integratedHubServerEnabled;
  private boolean integratedHubServerAutoStart;

  private String stdOutFile;
  private String stdErrFile;

  private String entitlementsManagerClass;

  public RogueNetAgent(String rnaPropFile) {
    this.rnaPropFile = rnaPropFile;
  }

  public RogueNetAgent(Properties rnaProps) {
    this.rnaProps = rnaProps;
  }

  private static void PrintWelcome() {
    StringBuffer sb = new StringBuffer();

    sb.append("\n** ");
    sb.append(Version.APP_TITLE);
    sb.append(" **\n\n");

    sb.append("Version Info:\n");
    sb.append(Version.GetInfo());
    sb.append("\n");

    RNALogger.GetLogger().stdOutPrintln(sb.toString());
  }

  private static void PrintUsage() {
    StringBuffer sb = new StringBuffer();

    sb.append("java ");
    sb.append(RogueNetAgent.class.getName());
    sb.append(" [RNA_PROPERTIES_FILE]\n");

    RNALogger.GetLogger().warn(sb.toString());
  }

  public void boot() throws RogueNetException, IOException, P2PHubException, ClassCastException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    bootTs = new Date();

    RLErrorDialog.SetAppDefaultTitle("Rogue Net Error");

    RNALogger.GetLogger().stdOutPrintln("Booting " + Version.APP_TITLE + "........");
    RNALogger.GetLogger().stdOutPrintln("Start Up Time: " + bootTs);

    loadPropertiesFiles();

    readProperties();

    createLogger();

    createEntitlementsController();

    createNetworkAgent();

    initRNAGui();

    createPlugInManager();

    createIntegratedHubServer(); //Optionally Enable Integrated P2P Hub Server

    autoStartIntegratedHubServer(); //Optionally Auto Start the Integrated P2P Hub Server

    netAgent.activate(); //Active the Network Agent (Connect to the "Rogue Net" Network!)

    RNALogger.GetLogger().stdOutPrint("\n" + Version.APP_TITLE + " Ready:\n\n");
  }

  public void shutdown() throws RogueNetException, P2PHubException {
    shutdownTs = new Date();

    RNALogger.GetLogger().stdOutPrintln("Shutting Down " + Version.APP_TITLE + "........");
    RNALogger.GetLogger().stdOutPrintln("Shutdown Time: " + shutdownTs);

    if (netAgent != null) {
      RNALogger.GetLogger().stdOutPrintln("\nDeactivating Network Agent:");
      netAgent.deactivate();
    }

    if (integratedHubServer != null) {
      RNALogger.GetLogger().stdOutPrintln("\nStopping Integrated Hub Server:");
      integratedHubServer.stop();
    }
  }

  public void waitWhileAgentIsUp() {
    synchronized (agentUpLock) {
      //while (!userShutdown && netAgent.isUp()) {
      while (!userShutdown) {
        try {
          agentUpLock.wait();
        }
        catch (Exception e) {
          SystemUtils.Sleep(1);
        }
      }
    }
  }

  private void loadPropertiesFiles() throws IOException {
    String propFile, tmp;
    FileInputStream fis = null;
    StringBuffer sb;

    //Load Specified RNA Properties File
    if (rnaPropFile != null) {
      RNALogger.GetLogger().stdOutPrintln("Loading RNA Properties File: '" + rnaPropFile + "'");
      rnaProps = SystemUtils.LoadPropertiesFile(rnaPropFile);
    }

    //Load ROOT Properties File
    if (rnaProps != null) {
      try {
        propFile = rnaProps.getProperty(PROP_ROOT_PROPERTIES_FILE);

        if (!StringUtils.IsNVL(propFile)) {
          RNALogger.GetLogger().stdOutPrintln("Loading ROOT Properties File: '" + propFile + "'");
          fis = new FileInputStream(propFile);
          rnaProps.load(fis);
        }
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

    //Load Optional Plug-Ins Properties File
    if (rnaProps != null) {
      try {
        propFile = rnaProps.getProperty(PROP_PLUG_INS_PROPERTIES_FILE);

        if (!StringUtils.IsNVL(propFile)) {
          RNALogger.GetLogger().stdOutPrintln("Loading Optional PLUG-INS Properties File (If Available): '" + propFile + "'");

          if (SystemUtils.FileExists(propFile)) {
            RNALogger.GetLogger().stdOutPrintln("PLUG-INS Properties available, loading...");
            fis = new FileInputStream(propFile);
            rnaProps.load(fis);
          }
          else {
            RNALogger.GetLogger().stdOutPrintln("PLUG-INS Properties NOT available, skipping...");
          }
        }
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

    //Optionally Load User Properties File
    if (rnaProps != null) {
      try {
        propFile = rnaProps.getProperty(PROP_USER_PROPERTIES_FILE);

        if (!StringUtils.IsNVL(propFile)) {
          tmp = SystemUtils.GetHomeDirectory();

          if (!StringUtils.IsNVL(tmp)) {
            sb = new StringBuffer();
            sb.append(tmp);

            if (!tmp.endsWith("/") && !tmp.endsWith("\\")) {
              sb.append("/");
            }

            sb.append(propFile);
            propFile = sb.toString();

            RNALogger.GetLogger().stdOutPrintln("Loading Optional USER Properties File (If Available): '" + propFile + "'");
            if (SystemUtils.FileExists(propFile)) {
              RNALogger.GetLogger().stdOutPrintln("USER Properties available, loading...");
              fis = new FileInputStream(propFile);
              rnaProps.load(fis);
            }
            else {
              RNALogger.GetLogger().stdOutPrintln("USER Properties NOT available, skipping...");
            }
          }
        }
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
  }

  private void readProperties() {
    String tmp;

    RNALogger.GetLogger().stdOutPrintln("Reading Properties");

    tmp = rnaProps.getProperty(PROP_INTEGRATED_P2P_HUB_SERVER_ENABLED);
    integratedHubServerEnabled = "TRUE".equalsIgnoreCase(tmp);

    tmp = rnaProps.getProperty(PROP_INTEGRATED_P2P_HUB_SERVER_AUTO_START);
    integratedHubServerAutoStart = "TRUE".equalsIgnoreCase(tmp);

    stdOutFile = rnaProps.getProperty(PROP_STDOUT_FILE);
    stdErrFile = rnaProps.getProperty(PROP_STDERR_FILE);

    entitlementsManagerClass = rnaProps.getProperty(PROP_ENTITLEMENTS_MANAGER_IMPL);
  }

  private void createLogger() throws IOException {
    RNALogger.GetLogger().stdOutPrintln("Creating Logger");
    RNALogger.CreateLogger(rnaProps);

    RNALogger.GetLogger().stdOutPrintln("Checking for optional Std Out/Err Redirects...");

    if (!StringUtils.IsNVL(stdOutFile)) {
      RNALogger.GetLogger().stdOutPrint("Redirecting STDOUT to: " + stdOutFile.trim());
      SystemUtils.RedirectStdOut(stdOutFile.trim());
      RNALogger.GetLogger().stdOutPrint("STDOUT for " + P2PHubServer.class.getName() + " Redirected to: " + stdOutFile.trim() + "\n---------------------------\n\n");
    }

    if (!StringUtils.IsNVL(stdErrFile)) {
      RNALogger.GetLogger().stdOutPrint("Redirecting STDERR to: " + stdErrFile.trim());
      SystemUtils.RedirectStdErr(stdErrFile.trim());
      RNALogger.GetLogger().stdOutPrint("STDERR for " + P2PHubServer.class.getName() + " Redirected to: " + stdErrFile.trim() + "\n---------------------------\n\n");
    }

  }

  private void createEntitlementsController() throws ClassCastException, InstantiationException, IllegalAccessException, ClassNotFoundException, RogueNetException {
    RNALogger.GetLogger().stdOutPrintln("Creating Entitlements Controller");
    entitlementsManager = (RNAEntitlementsManager) Class.forName(entitlementsManagerClass).newInstance();
    entitlementsManager.initEntitlementsManager(rnaProps);
  }

  public RNAEntitlementsManager getRNAEntitlementsManager() {
    return entitlementsManager;
  }

  private void createNetworkAgent() throws RogueNetException {
    RNALogger.GetLogger().stdOutPrintln("Creating Network Agent");
    netAgent = new NetworkAgent(rnaProps);
  }

  private void initRNAGui() {
    try {
      RNALogger.GetLogger().stdOutPrintln("Initializing GUI");
      trayMenu = new RNATrayMenu(rnaProps, this, netAgent);
      trayMenu.installSysTrayIcon();
    }
    catch (Throwable t) {
      RNALogger.GetLogger().log(t);
    }
  }

  private void createPlugInManager() {
    RNALogger.GetLogger().stdOutPrintln("Creating Plug-In Manager");

    plugInManager = new PlugInManager(rnaProps, netAgent, trayMenu);
    plugInManager.setEntitlementsManager(entitlementsManager);
    netAgent.setPlugInManager(plugInManager);
    entitlementsManager.setPlugInManager(plugInManager);
  }

  private void createIntegratedHubServer() throws RogueNetException, P2PHubException {
    Properties p2pHubServerProps;

    if (integratedHubServerEnabled) {
      RNALogger.GetLogger().stdOutPrintln("Creating Integrated P2P Hub Server");

      //Translate Rogue Net Agent properties to P2P Hub Server properties
      p2pHubServerProps = new Properties();
      p2pHubServerProps.setProperty(P2PHubServer.PROP_SERVER_NAME, rnaProps.getProperty(PROP_INTEGRATED_P2P_HUB_SERVER_NAME));
      p2pHubServerProps.setProperty(P2PHubServer.PROP_PORT, rnaProps.getProperty(PROP_INTEGRATED_P2P_HUB_SERVER_PORT));
      p2pHubServerProps.setProperty(P2PHubServer.PROP_KEY_FILE, rnaProps.getProperty(PROP_INTEGRATED_P2P_HUB_SERVER_KEY_FILE));
      p2pHubServerProps.setProperty(P2PHubServer.PROP_PEER_FILE, rnaProps.getProperty(PROP_INTEGRATED_P2P_HUB_SERVER_PEER_FILE));
      p2pHubServerProps.setProperty(P2PHubServer.PROP_HEART_BEAT_TTL, rnaProps.getProperty(PROP_INTEGRATED_P2P_HUB_SERVER_HEART_BEAT_TTL));

      integratedHubServer = new P2PHubServer(p2pHubServerProps);
    }
    else {
      RNALogger.GetLogger().stdOutPrintln("NOTICE: Integrated P2P Hub Server is DISABLE!");
    }
  }

  private void autoStartIntegratedHubServer() throws P2PHubException {
    if (integratedHubServerEnabled && integratedHubServerAutoStart) {
      RNALogger.GetLogger().stdOutPrintln("Integrated P2P Hub Server Auto Start Enable...");
      integratedHubServer.start();
    }
  }

  public String getProperty(String name) {
    return (rnaProps != null ? rnaProps.getProperty(name) : "");
  }

  public boolean isIntegratedHubServerEnabled() {
    return integratedHubServerEnabled;
  }

  public void forceExit() {
    synchronized (agentUpLock) {
      userShutdown = true;
      agentUpLock.notifyAll();
    }
  }

  public boolean isIntegratedHubServerUp() {
    return integratedHubServer != null && integratedHubServer.isServerUp();
  }

  public void startIntegratedHubServer() throws P2PHubException {
    if (integratedHubServer != null) {
      integratedHubServer.start();
    }
  }

  public void stopIntegratedHubServer() throws P2PHubException {
    if (integratedHubServer != null) {
      integratedHubServer.stop();
    }
  }

  public static void main(String[] args) {
    RogueNetAgent rnAgent = null;
    String rnaPropFile;
    int exitCode;

    PrintWelcome();

    if (args.length != 1) {
      PrintUsage();
      exitCode = 1;
    }
    else {
      try {
        rnaPropFile = args[0];

        rnAgent = new RogueNetAgent(rnaPropFile);
        rnAgent.boot();

        rnAgent.waitWhileAgentIsUp();

        exitCode = 0;
      } //End try block
      catch (Throwable t) {
        RNALogger.GetLogger().log(t);
        exitCode = 1;
      }
      finally {
        if (rnAgent != null) {
          try {
            rnAgent.shutdown();
          }
          catch (Throwable t) {
            RNALogger.GetLogger().log(t);
          }
          rnAgent = null;
        }
      }
    }

    RNALogger.GetLogger().stdOutPrintln("\n" + Version.APP_TITLE + " - Exit Code: " + exitCode);
    System.exit(exitCode);
  }

}
