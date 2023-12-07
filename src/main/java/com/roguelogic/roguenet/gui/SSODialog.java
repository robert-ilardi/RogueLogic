/**
 * Created Oct 29, 2006
 */
package com.roguelogic.roguenet.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.roguelogic.gui.RLComboBox;
import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.NetworkAgent;
import com.roguelogic.roguenet.RNAEntitlementsManager;
import com.roguelogic.roguenet.RNALogger;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class SSODialog extends JFrame implements WindowListener {

  private RNATrayMenu rnaTrayMenu;
  private RNAEntitlementsManager rnaEntitlementsManager;
  private NetworkAgent netAgent;

  private P2PHubPeer[] peerList;

  private JPanel mainPanel, cmdButtonPanel;
  private JLabel usernameLbl, passwordLbl, agentsLbl;
  private JComboBox agentsCB;
  private JTextField usernameTF;
  private JPasswordField passwordPF;
  private JTextArea ssoTA;
  private JButton loginButton, logoutButton, cancelButton, refreshButton;

  private static final Dimension FRAME_SIZE = new Dimension(350, 300);

  public SSODialog(RNATrayMenu rnaTrayMenu, RNAEntitlementsManager rnaEntitlementsManager, NetworkAgent netAgent) {
    super();

    this.rnaTrayMenu = rnaTrayMenu;
    this.rnaEntitlementsManager = rnaEntitlementsManager;
    this.netAgent = netAgent;

    addWindowListener(this);

    initComponents();
  }

  private void initComponents() {
    GridBagLayout frameGbl, mpGbl, cmdButtonGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FRAME_SIZE);
    setTitle("Single Sign On");

    if (rnaTrayMenu != null) {
      setIconImage(rnaTrayMenu.getIcon().getImage());
    }

    setResizable(false);
    setLocationRelativeTo(null); //Center on Screen

    frameGbl = new GridBagLayout();
    setLayout(frameGbl);

    gbc = new GridBagConstraints();

    //Add the main panel
    mainPanel = new JPanel();
    mainPanel.setPreferredSize(getSize());
    mainPanel.setMaximumSize(getSize());
    mainPanel.setMinimumSize(getSize());
    mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;

    frameGbl.setConstraints(mainPanel, gbc);
    add(mainPanel);

    mpGbl = new GridBagLayout();
    mainPanel.setLayout(mpGbl);

    //Info Row------------------------------------>
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    ssoTA = new JTextArea("Perform Login for Single Sign On to ALL Secure Remote Plug-Ins on TARGET Agent.");
    ssoTA.setLineWrap(true);
    ssoTA.setWrapStyleWord(true);
    ssoTA.setEditable(false);
    ssoTA.setBackground(this.getBackground());
    dSize = new Dimension(FRAME_SIZE.width - 20, 50);
    ssoTA.setMinimumSize(dSize);
    ssoTA.setMaximumSize(dSize);
    ssoTA.setPreferredSize(dSize);
    mpGbl.setConstraints(ssoTA, gbc);
    mainPanel.add(ssoTA);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Agents Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    agentsLbl = new JLabel("Agents:");
    mpGbl.setConstraints(agentsLbl, gbc);
    mainPanel.add(agentsLbl);

    agentsCB = new RLComboBox();
    dSize = new Dimension(170, 20);
    agentsCB.setMinimumSize(dSize);
    agentsCB.setMaximumSize(dSize);
    agentsCB.setPreferredSize(dSize);
    mpGbl.setConstraints(agentsCB, gbc);
    mainPanel.add(agentsCB);

    refreshButton = new JButton("Refresh");
    mpGbl.setConstraints(refreshButton, gbc);
    mainPanel.add(refreshButton);

    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshAgents();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Username Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    usernameLbl = new JLabel("Username:");
    mpGbl.setConstraints(usernameLbl, gbc);
    mainPanel.add(usernameLbl);

    usernameTF = new JTextField();
    mpGbl.setConstraints(usernameTF, gbc);
    mainPanel.add(usernameTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Password Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    passwordLbl = new JLabel("Password:");
    mpGbl.setConstraints(passwordLbl, gbc);
    mainPanel.add(passwordLbl);

    passwordPF = new JPasswordField();
    passwordPF.setEchoChar('*');
    mpGbl.setConstraints(passwordPF, gbc);
    mainPanel.add(passwordPF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Command Button Row------------------------------------>
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    cmdButtonGbl = new GridBagLayout();
    cmdButtonPanel = new JPanel(cmdButtonGbl);
    mpGbl.setConstraints(cmdButtonPanel, gbc);
    mainPanel.add(cmdButtonPanel);

    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    cmdButtonGbl.setConstraints(blankPanel, gbc);
    cmdButtonPanel.add(blankPanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 7, 5, 8);
    gbc.weightx = 0.0;

    loginButton = new JButton("Login");
    cmdButtonGbl.setConstraints(loginButton, gbc);
    cmdButtonPanel.add(loginButton);

    loginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        login();
      }
    });

    logoutButton = new JButton("Logout");
    cmdButtonGbl.setConstraints(logoutButton, gbc);
    cmdButtonPanel.add(logoutButton);

    logoutButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        logout();
      }
    });

    cancelButton = new JButton("Cancel");
    cmdButtonGbl.setConstraints(cancelButton, gbc);
    cmdButtonPanel.add(cancelButton);

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SSODialog.this.dispose();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    cmdButtonGbl.setConstraints(blankPanel, gbc);
    cmdButtonPanel.add(blankPanel);

    //Last Row------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);
  }

  public void windowOpened(WindowEvent we) {
    refreshAgents();
  }

  public void windowClosed(WindowEvent we) {}

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  public void refreshAgents() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Vector<String> data = new Vector<String>();

        if (netAgent != null) {
          try {
            peerList = netAgent.getPeerList();

            if (peerList != null) {
              for (int i = 0; i < peerList.length; i++) {
                if (peerList[i] != null) {
                  data.add(peerList[i].getUsername());
                }
              }
            }

            agentsCB.setModel(new DefaultComboBoxModel(data));
          } //End try block
          catch (Exception e) {
            e.printStackTrace();
          }
        } //End null plugInManager check
      }
    });
  }

  public void login() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        String username, password, peer = null;
        String sessionToken, p2pHubUsername = null;

        username = usernameTF.getText().trim();
        password = String.valueOf(passwordPF.getPassword());

        if (agentsCB.getSelectedIndex() >= 0) {
          p2pHubUsername = peerList[agentsCB.getSelectedIndex()].getUsername();
          peer = peerList[agentsCB.getSelectedIndex()].getSessionToken();
        }

        if (StringUtils.IsNVL(peer)) {
          JOptionPane.showMessageDialog(SSODialog.this, "Please select an Agent!", SSODialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(SSODialog.this.getIconImage()));
          return;
        }

        if (StringUtils.IsNVL(username)) {
          JOptionPane.showMessageDialog(SSODialog.this, "Please enter a username!", SSODialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(SSODialog.this.getIconImage()));
          return;
        }

        if (StringUtils.IsNVL(password)) {
          JOptionPane.showMessageDialog(SSODialog.this, "Please enter a password!", SSODialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(SSODialog.this.getIconImage()));
          return;
        }

        if (rnaEntitlementsManager != null) {
          try {
            sessionToken = rnaEntitlementsManager.ssoLogin(username, password, peer);

            if (!StringUtils.IsNVL(sessionToken)) {
              rnaEntitlementsManager.getSSOLedger().removeLink(p2pHubUsername); //Remove old login
              rnaEntitlementsManager.getSSOLedger().addLink(p2pHubUsername, sessionToken);

              JOptionPane.showMessageDialog(SSODialog.this, "Single Sign On Login on Agent '" + p2pHubUsername + "' using username '" + username + "' SUCCESSFUL!", SSODialog.this.getTitle(),
                  JOptionPane.INFORMATION_MESSAGE, new ImageIcon(SSODialog.this.getIconImage()));
            }
          }
          catch (Exception e) {
            //RLErrorDialog.ShowError(e);
            RNALogger.GetLogger().log(e);

            JOptionPane.showMessageDialog(SSODialog.this, "Single Sign On Login on Agent '" + p2pHubUsername + "' using username '" + username + "' FAILED!", SSODialog.this.getTitle(),
                JOptionPane.ERROR_MESSAGE, new ImageIcon(SSODialog.this.getIconImage()));
          }

          usernameTF.setText("");
          passwordPF.setText("");
        } //End null rnaEntitlementsManager check
      }
    });
  }

  public void logout() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        String peer = null, p2pHubUsername = null, sessionToken;

        if (agentsCB.getSelectedIndex() >= 0) {
          p2pHubUsername = peerList[agentsCB.getSelectedIndex()].getUsername();
          peer = peerList[agentsCB.getSelectedIndex()].getSessionToken();
        }

        if (StringUtils.IsNVL(peer)) {
          JOptionPane.showMessageDialog(SSODialog.this, "Please select an Agent!", SSODialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(SSODialog.this.getIconImage()));
          return;
        }

        if (rnaEntitlementsManager != null) {
          try {
            sessionToken = rnaEntitlementsManager.getSSOLedger().getSessionToken(p2pHubUsername);

            if (StringUtils.IsNVL(sessionToken)) {
              JOptionPane.showMessageDialog(SSODialog.this, "A Single Sign On Session is NOT Available on this Agent in the local ledger!", SSODialog.this.getTitle(), JOptionPane.ERROR_MESSAGE,
                  new ImageIcon(SSODialog.this.getIconImage()));
              return;
            }

            rnaEntitlementsManager.ssoLogout(sessionToken, peer);
            rnaEntitlementsManager.getSSOLedger().removeLink(p2pHubUsername);

            JOptionPane.showMessageDialog(SSODialog.this, "Single Sign On Session Logout on Agent '" + p2pHubUsername + "' Successful!", SSODialog.this.getTitle(), JOptionPane.ERROR_MESSAGE,
                new ImageIcon(SSODialog.this.getIconImage()));
          }
          catch (Exception e) {
            RLErrorDialog.ShowError(e);
          }
        }
      }
    });
  }
}
