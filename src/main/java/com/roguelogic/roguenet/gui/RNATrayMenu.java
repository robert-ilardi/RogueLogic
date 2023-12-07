/**
 * Created Sep 24, 2006
 */
package com.roguelogic.roguenet.gui;

import static com.roguelogic.roguenet.RNAConstants.PROP_DEVELOPER_MODE;
import static com.roguelogic.roguenet.RNAConstants.PROP_SHOW_EXIT;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import com.roguelogic.entitlements.EntitlementsController;
import com.roguelogic.entitlements.EntitlementsManagementConsole;
import com.roguelogic.roguenet.NetworkAgent;
import com.roguelogic.roguenet.RNAConstants;
import com.roguelogic.roguenet.RNAEntitlementsManager;
import com.roguelogic.roguenet.RNALogger;
import com.roguelogic.roguenet.RNAPlugIn;
import com.roguelogic.roguenet.RogueNetAgent;
import com.roguelogic.roguenet.Version;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNATrayMenu {

  private ImageIcon rnaIcon;

  private boolean developerMode;
  private boolean showExit;

  private boolean aboutVisible = false;
  private boolean startServerInProgress = false;
  private boolean stopServerInProgress = false;

  private Properties rnaProps;

  private RogueNetAgent rnAgent;
  private NetworkAgent netAgent;
  private SSODialog ssoDialog;
  private EntitlementsManagementConsole emConsole;

  private HashMap<String, JMenuItem> plugInMenuItemMap;
  private JMenu plugInSubMenu;

  private JMenuItem startServerMenuItem;
  private JMenuItem stopServerMenuItem;

  private NetworkExplorer netExplorer;

  public RNATrayMenu(Properties rnaProps, RogueNetAgent rnAgent, NetworkAgent netAgent) throws IOException {
    this.rnaProps = rnaProps;
    this.rnAgent = rnAgent;
    this.netAgent = netAgent;

    rnaIcon = new ImageIcon(SystemUtils.LoadDataFromClassLoader(RNAConstants.RN_ICON_CLASSPATH));

    plugInMenuItemMap = new HashMap<String, JMenuItem>();

    readProperties();
  }

  private void readProperties() {
    String tmp;

    tmp = rnaProps.getProperty(PROP_DEVELOPER_MODE);
    developerMode = "TRUE".equalsIgnoreCase(tmp);

    tmp = rnaProps.getProperty(PROP_SHOW_EXIT);
    showExit = "TRUE".equalsIgnoreCase(tmp);
  }

  public void installSysTrayIcon() {
    TrayIcon trayIcon;
    JPopupMenu menu;

    menu = loadMenu();

    trayIcon = new TrayIcon(rnaIcon);
    trayIcon.setCaption(Version.APP_TITLE);
    trayIcon.setPopupMenu(menu);

    SystemTray.getDefaultSystemTray().addTrayIcon(trayIcon);
  }

  public ImageIcon getIcon() {
    return rnaIcon;
  }

  private JPopupMenu loadMenu() {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem item;
    JMenu p2pHubServerSubMenu, optionsSubMenu;

    //Add Basic Menu Items

    //Start Rogue Net Tools--------------------------------->

    //Network Explorer
    item = new JMenuItem("Network Explorer");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        launchMenuItem("netExplorerAction");
      }
    });
    menu.add(item);

    //Integrated P2P Hub Server Controls
    if (rnAgent.isIntegratedHubServerEnabled()) {
      p2pHubServerSubMenu = new JMenu("P2P Hub Server");
      menu.add(p2pHubServerSubMenu);

      startServerMenuItem = new JMenuItem("Start Server");
      startServerMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          launchMenuItem("startServerAction");
        }
      });
      p2pHubServerSubMenu.add(startServerMenuItem);

      stopServerMenuItem = new JMenuItem("Stop Server");
      stopServerMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          launchMenuItem("stopServerAction");
        }
      });
      p2pHubServerSubMenu.add(stopServerMenuItem);
    }

    //Options Sub Menu
    optionsSubMenu = new JMenu("Options");
    menu.add(optionsSubMenu);

    //Options Sub Menu -> Preferences
    /*item = new JMenuItem("Preferences");
     item.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent arg0) {
     launchMenuItem("preferencesAction");
     }
     });
     optionsSubMenu.add(item);*/

    //Options Sub Menu -> Plug-Ins Management
    /*item = new JMenuItem("Plug-Ins Management");
     item.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent arg0) {
     launchMenuItem("pluginsManagementAction");
     }
     });
     optionsSubMenu.add(item);*/

    //Options Sub Menu -> Entitlements Management
    item = new JMenuItem("Entitlements Management");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        launchMenuItem("entitlementsManagementAction");
      }
    });
    optionsSubMenu.add(item);

    //Single Sign On
    item = new JMenuItem("Single Sign On");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        launchMenuItem("ssoAction");
      }
    });
    menu.add(item);

    //RN Raw Message Composer
    if (developerMode) {
      item = new JMenuItem("Raw Mesg Composer");
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          launchMenuItem("p2pHubMesgComposerAction");
        }
      });
      menu.add(item);
    }

    //End Rogue Net Tools--------------------------------->

    menu.addSeparator();

    //Start Plug-Ins Sub Menu--------------------------------->

    plugInSubMenu = new JMenu("Plug-Ins");
    menu.add(plugInSubMenu);

    //End Plug-Ins Sub Menu--------------------------------->

    menu.addSeparator();

    //Start Generic GUI Options--------------------------------->

    //About Menu Item
    item = new JMenuItem("About");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        launchMenuItem("aboutAction");
      }
    });
    menu.add(item);

    //RogueLogic
    item = new JMenuItem("Visit RogueLogic.com");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        launchMenuItem("visitRogueLogicAction");
      }
    });
    menu.add(item);

    //End Generic GUI Options--------------------------------->

    //Exit Menu
    if (developerMode || showExit) {
      menu.addSeparator();

      item = new JMenuItem("Exit");
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          launchMenuItem("exitAction");
        }
      });
      menu.add(item);
    }

    return menu;
  }

  private void launchMenuItem(final String actionMethod) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          Method miAm = RNATrayMenu.class.getMethod(actionMethod, new Class[] {});
          miAm.invoke(RNATrayMenu.this, new Object[] {});
        }
        catch (Exception e) {
          RNALogger.GetLogger().log(e);
        }
      }
    });
  }

  public void aboutAction() {
    if (aboutVisible) {
      return;
    }

    try {
      aboutVisible = true;
      JOptionPane.showMessageDialog(null, Version.GetInfo(), Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, rnaIcon);
    }
    finally {
      aboutVisible = false;
    }
  }

  public void visitRogueLogicAction() {
    try {
      Desktop.browse(new URL(Version.RL_URL));
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
  }

  public void exitAction() {
    rnAgent.forceExit();
  }

  public void netExplorerAction() {
    if (netExplorer != null && netExplorer.isVisible()) {
      netExplorer.toFront();
    }
    else {
      netExplorer = new NetworkExplorer(rnaProps, this, netAgent);
      netExplorer.setVisible(true);
    }
  }

  public void p2pHubMesgComposerAction() {
    P2PHubMessageComposer composer;

    composer = new P2PHubMessageComposer(this, netAgent);
    composer.setVisible(true);
  }

  public void addPlugin(final RNAPlugIn plugIn, String menuItemName) {
    JMenuItem item = new JMenuItem(menuItemName);
    item.addActionListener(new ActionListener() {
      private RNAPlugIn plugInRef = plugIn;

      public void actionPerformed(ActionEvent arg0) {
        plugInRef.handleMenuExec();
      }
    });

    plugInMenuItemMap.put(menuItemName, item);
    plugInSubMenu.add(item);
  }

  public void removePlugin(String menuItemName) {
    JMenuItem item;

    item = plugInMenuItemMap.get(menuItemName);

    if (item != null) {
      plugInSubMenu.remove(item);
      plugInMenuItemMap.remove(item);
    }
  }

  public void startServerAction() {
    if (startServerInProgress) {
      return;
    }

    try {
      startServerInProgress = true;

      if (!rnAgent.isIntegratedHubServerUp()) {
        rnAgent.startIntegratedHubServer();
        JOptionPane.showMessageDialog(null, "Integrated P2P Hub Server started!", Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, rnaIcon);
      }
      else {
        JOptionPane.showMessageDialog(null, "Integrated P2P Hub Server is already running!", Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, rnaIcon);
      }
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
    finally {
      startServerInProgress = false;
    }
  }

  public void stopServerAction() {
    if (stopServerInProgress) {
      return;
    }

    try {
      stopServerInProgress = true;

      rnAgent.stopIntegratedHubServer();
      JOptionPane.showMessageDialog(null, "Integrated P2P Hub Server stopped!", Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, rnaIcon);
    }
    catch (Exception e) {
      RNALogger.GetLogger().log(e);
    }
    finally {
      stopServerInProgress = false;
    }
  }

  public void ssoAction() {
    if (ssoDialog != null && ssoDialog.isVisible()) {
      ssoDialog.toFront();
    }
    else {
      ssoDialog = new SSODialog(this, rnAgent.getRNAEntitlementsManager(), netAgent);
      ssoDialog.setVisible(true);
    }
  }

  public void entitlementsManagementAction() {
    RNAEntitlementsManager rnaEM;
    EntitlementsController entlCntrlr = null;

    if (emConsole != null && emConsole.isVisible()) {
      emConsole.toFront();
    }
    else {
      rnaEM = rnAgent.getRNAEntitlementsManager();
      if (rnaEM != null) {
        entlCntrlr = rnaEM.getEntitlementsController();
      }

      emConsole = new EntitlementsManagementConsole(entlCntrlr);
      emConsole.setIconImage(rnaIcon.getImage());
      emConsole.setTitle(Version.APP_TITLE);
      emConsole.setVisible(true);
    }
  }

  public void preferencesAction() {}

  public void pluginsManagementAction() {}

}
