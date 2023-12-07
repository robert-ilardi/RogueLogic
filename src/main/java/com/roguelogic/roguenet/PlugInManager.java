/**
 * Created Sep 27, 2006
 */
package com.roguelogic.roguenet;

import static com.roguelogic.roguenet.RNAConstants.PROP_EXTERNAL_PLUGIN_CLASSPATH_LIST;
import static com.roguelogic.roguenet.RNAConstants.PROP_RNA_PLUG_IN_CONFIGURATION;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.ImageIcon;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.gui.RNATrayMenu;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class PlugInManager {

  private Properties rnaProps;

  private NetworkAgent netAgent;
  private RNATrayMenu trayMenu;

  private RNAEntitlementsManager entitlementsManager;

  private String rnaPlugInConfigFile;
  private String externPlugInClasspathList;

  private HashMap<String, RNAPlugIn> plugInMap;

  private URLClassLoader externPlugInClassLoader;

  public PlugInManager(Properties rnaProps, NetworkAgent netAgent, RNATrayMenu trayMenu) {
    this.rnaProps = rnaProps;

    this.netAgent = netAgent;
    this.trayMenu = trayMenu;

    plugInMap = new HashMap<String, RNAPlugIn>();

    readProperties();

    try {
      loadDynamicPlugInClassPath(externPlugInClasspathList);
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  private void readProperties() {
    rnaPlugInConfigFile = rnaProps.getProperty(PROP_RNA_PLUG_IN_CONFIGURATION);
    externPlugInClasspathList = rnaProps.getProperty(PROP_EXTERNAL_PLUGIN_CLASSPATH_LIST);
  }

  public void loadPlugIns() throws IOException {
    String[] lines, tokens = null;
    FileInputStream fis = null;
    RNAPlugIn plugIn;

    RNALogger.GetLogger().stdOutPrintln("Loading RNA Plug-In Configuration File: " + rnaPlugInConfigFile);

    try {
      fis = new FileInputStream(rnaPlugInConfigFile);
      lines = StringUtils.ReadLines(fis);

      for (String line : lines) {
        try {
          line = line.trim();

          if (!line.startsWith("#")) {
            tokens = line.split(":", 3);

            if (tokens.length >= 2) {
              //If Plug-In Enabled?
              if ("Y".equalsIgnoreCase(tokens[1])) {
                plugIn = loadPlugInInstance(tokens[0]);
                plugIn.hook(this, (tokens.length >= 3 ? tokens[2] : null));
              } //End plug-in enabled check
            } //End tokens array length check
          } //End check to skip comments
        } //End try block
        catch (ClassNotFoundException e) {
          RNALogger.GetLogger().stdErrPrintln("Plug-In '" + tokens[0] + "' could NOT be loaded and will NOT be available! (ClassNotFoundException): " + e.getMessage());
        }
        catch (InstantiationException e) {
          RNALogger.GetLogger().stdErrPrintln("Plug-In '" + tokens[0] + "' could NOT be loaded and will NOT be available! (InstantiationException): " + e.getMessage());
        }
        catch (IllegalAccessException e) {
          RNALogger.GetLogger().stdErrPrintln("Plug-In '" + tokens[0] + "' could NOT be loaded and will NOT be available! (IllegalAccessException): " + e.getMessage());
        }
        catch (RogueNetException e) {
          RNALogger.GetLogger().stdErrPrintln("Plug-In '" + tokens[0] + "' could NOT be loaded and will NOT be available! (RogueNetException): " + e.getMessage());
          RNALogger.GetLogger().log(e);
        }
        catch (Exception e) {
          RNALogger.GetLogger().stdErrPrintln("Plug-In '" + tokens[0] + "' could NOT be loaded and will NOT be available! (NOT-DIRECTLY-HANDLED EXCEPTION): " + e.getMessage());
          RNALogger.GetLogger().log(e);
        }
      } //End for each line in lines
    } //End try block
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
      }
    }
  }

  public synchronized void register(RNAPlugIn plugIn, String subject, String menuItem) {
    String plugInName;

    plugInName = plugIn.getClass().getName();
    RNALogger.GetLogger().stdOutPrintln("Registering Plug-In: " + plugInName);

    plugInMap.put(plugInName, plugIn);

    if (subject != null && subject.trim().length() > 0) {
      netAgent.registerListener(plugInName, subject.trim());
    }

    if (menuItem != null && menuItem.trim().length() > 0) {
      trayMenu.addPlugin(plugIn, menuItem.trim());
    }
  }

  public synchronized void unregister(RNAPlugIn plugIn, String subject, String menuItem) {
    String plugInName;

    plugInName = plugIn.getClass().getName();
    RNALogger.GetLogger().stdOutPrintln("Unregistering Plug-In: " + plugInName);

    if (subject != null && subject.trim().length() > 0) {
      netAgent.unregisterListener(plugInName, subject);
    }

    if (menuItem != null && menuItem.trim().length() > 0) {
      trayMenu.removePlugin(menuItem.trim());
    }

    plugInMap.remove(plugInName);
  }

  public String getProperty(String name) {
    return rnaProps.getProperty(name);
  }

  public void notifyListener(String listenerName, P2PHubMessage mesg) {
    RNAPlugIn plugIn;

    try {
      plugIn = plugInMap.get(listenerName);
      plugIn.handle(mesg);
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  public RNAPlugIn getPlugIn(String plugInName) {
    return plugInMap.get(plugInName);
  }

  public NetworkAgent getNetAgent() {
    return netAgent;
  }

  public ImageIcon getIcon() {
    return trayMenu.getIcon();
  }

  public ArrayList<String> getPlugInNameList() {
    ArrayList<String> plugInNames = new ArrayList<String>();
    String name;

    for (Iterator iter = plugInMap.keySet().iterator(); iter.hasNext();) {
      name = (String) iter.next();
      plugInNames.add(name);
    }

    return plugInNames;
  }

  public void setEntitlementsManager(RNAEntitlementsManager entitlementsManager) {
    this.entitlementsManager = entitlementsManager;
  }

  public RNAEntitlementsManager getEntitlementsManager() {
    return entitlementsManager;
  }

  public void unloadPlugIns() {
    RNAPlugIn[] plugIns = (RNAPlugIn[]) plugInMap.values().toArray(new RNAPlugIn[plugInMap.size()]);

    for (RNAPlugIn plugIn : plugIns) {
      try {
        plugIn.unhook();
      }
      catch (RogueNetException e) {
        RNALogger.GetLogger().stdErrPrintln("Plug-In '" + plugIn.getClass().getName() + "' could NOT be unloaded! (RogueNetException): " + e.getMessage());
        RNALogger.GetLogger().log(e);
      }
    }
  }

  public void loadDynamicPlugInClassPath(String cpListFile) throws IOException {
    String[] entries;
    FileInputStream fis = null;
    URL[] cpUrls;

    try {
      if (StringUtils.IsNVL(cpListFile)) {
        RNALogger.GetLogger().stdOutPrintln("External Plug-In Class Loader Disabled!");
        externPlugInClassLoader = null;
      }
      else {
        RNALogger.GetLogger().stdOutPrintln("External Plug-In Class Loader Enabled!");

        fis = new FileInputStream(cpListFile);
        entries = StringUtils.ReadLinesIgnoreComments(fis, "#");
        fis.close();
        fis = null;

        if (entries != null && entries.length > 0) {
          cpUrls = new URL[entries.length];

          for (int i = 0; i < entries.length; i++) {
            RNALogger.GetLogger().stdOutPrintln("Adding '" + entries[i].trim() + "' to External Plug-In Class Loader");
            cpUrls[i] = new URL(entries[i].trim());
          }

          externPlugInClassLoader = new URLClassLoader(cpUrls);
        } //End entries null and length check
      } //End else block
    } //End try block
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
      }
    }
  }

  private RNAPlugIn loadPlugInInstance(String piClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    RNAPlugIn plugIn;

    if (externPlugInClassLoader != null) {
      plugIn = (RNAPlugIn) externPlugInClassLoader.loadClass(piClassName).newInstance();
    }
    else {
      plugIn = (RNAPlugIn) Class.forName(piClassName).newInstance();
    }

    return plugIn;
  }

}
