/**
 * Created Dec 17, 2006
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
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.roguelogic.gui.RLComboBox;
import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.SynchronousTransactionRequestor;
import com.roguelogic.roguenet.Version;
import com.roguelogic.roguenet.plugins.EchoClient;
import com.roguelogic.util.Base64Codec;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class EchoClientDialog extends JFrame implements WindowListener, SynchronousTransactionRequestor {

  private PlugInManager plugInManager;

  private P2PHubPeer[] peerList;

  private JPanel mainPanel, buttonPanel;
  private JLabel peersLbl, statusLbl;
  private JComboBox peersCB;
  private JButton startButton, closeButton, refreshButton;

  private boolean autoEchoing = false;
  private P2PHubPeer selectedPeer;

  private HashMap<String, P2PHubMessage> transactionMap;
  private Object tmLock;

  private static Dimension FrameSize = new Dimension(400, 125);

  public static final int MAX_RANDOM_ECHO_DATA_LEN = 1024;

  public EchoClientDialog(PlugInManager plugInManager) {
    super();

    this.plugInManager = plugInManager;

    transactionMap = new HashMap<String, P2PHubMessage>();
    tmLock = new Object();

    addWindowListener(this);

    if (plugInManager != null) {
      setIconImage(plugInManager.getIcon().getImage());
    }

    initComponents();
  }

  private void initComponents() {
    GridBagLayout frameGbl, mpGbl, buttonGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FrameSize);
    setTitle("Rogue Net - Echo Client");

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

    //Reset everything
    gbc.gridheight = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    //Recipients Row---------------------------------------->
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 2, 5);
    gbc.weightx = 0.0;

    peersLbl = new JLabel("Peers:");
    mpGbl.setConstraints(peersLbl, gbc);
    mainPanel.add(peersLbl);

    dSize = new Dimension(180, 20);
    peersCB = new RLComboBox();
    peersCB.setPreferredSize(dSize);
    peersCB.setMinimumSize(dSize);
    peersCB.setMaximumSize(dSize);
    mpGbl.setConstraints(peersCB, gbc);
    mainPanel.add(peersCB);

    refreshButton = new JButton("Refresh");
    mpGbl.setConstraints(refreshButton, gbc);
    mainPanel.add(refreshButton);

    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshPeers();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Status Row---------------------------------------->
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(2, 5, 2, 5);
    gbc.weightx = 0.0;

    statusLbl = new JLabel("Status: ");
    mpGbl.setConstraints(statusLbl, gbc);
    mainPanel.add(statusLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Button Row---------------------------------------->
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(2, 5, 10, 5);
    gbc.weightx = 0.0;

    buttonGbl = new GridBagLayout();
    buttonPanel = new JPanel(buttonGbl);

    //Send Button
    startButton = new JButton("Start");
    buttonGbl.setConstraints(startButton, gbc);
    buttonPanel.add(startButton);

    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          if (!autoEchoing) {
            startAutoEcho();
          }
          else {
            stopAutoEcho();
          }
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });

    //Close Button
    closeButton = new JButton("Close");
    buttonGbl.setConstraints(closeButton, gbc);
    buttonPanel.add(closeButton);

    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });

    //Add Button Panel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.weightx = 1.0;

    mpGbl.setConstraints(buttonPanel, gbc);
    mainPanel.add(buttonPanel);

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
    refreshPeers();
  }

  public void windowClosed(WindowEvent we) {
    autoEchoing = false;
  }

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  private synchronized void refreshPeers() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Vector<String> data = new Vector<String>();

        if (plugInManager != null) {
          try {
            peerList = plugInManager.getNetAgent().getPeerList();

            if (peerList != null) {
              for (int i = 0; i < peerList.length; i++) {
                if (peerList[i] != null) {
                  data.add(peerList[i].getUsername());
                }
              }
            }

            peersCB.setModel(new DefaultComboBoxModel(data));
          } //End try block
          catch (Exception e) {
            e.printStackTrace();
          }
        } //End null plugInManager check
      }
    });
  }

  private synchronized void startAutoEcho() {
    Thread t;
    Runnable r;

    if (plugInManager != null && plugInManager.getNetAgent() != null && peersCB.getSelectedIndex() >= 0) {
      autoEchoing = true;
      startButton.setText("Stop");
      peersCB.setEnabled(false);
      refreshButton.setEnabled(false);

      selectedPeer = peerList[peersCB.getSelectedIndex()];

      r = new Runnable() {
        public void run() {
          try {
            while (autoEchoing && sendEcho()) {
              SystemUtils.SleepTight(100);
            }
          }
          catch (Exception e) {
            RLErrorDialog.ShowError(e);
          }

          autoEchoing = false;
          startButton.setText("Start");
          peersCB.setEnabled(true);
          refreshButton.setEnabled(true);
        }
      };

      t = new Thread(r);
      t.start();
    }
    else {
      JOptionPane.showMessageDialog(EchoClientDialog.this, "You MUST first select a peer to send an echo request to!", Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, plugInManager.getIcon());
    }

  }

  private synchronized void stopAutoEcho() throws RogueNetException {
    autoEchoing = false;
  }

  private synchronized boolean sendEcho() throws RogueNetException {
    EchoClient echoClient;
    String transId;
    P2PHubMessage mesg;
    byte[] data, echoData;
    boolean echoMatches = false;

    if (plugInManager != null && plugInManager.getNetAgent() != null && selectedPeer != null) {
      echoClient = (EchoClient) plugInManager.getPlugIn(EchoClient.class.getName());

      if (echoClient != null) {
        synchronized (tmLock) {
          data = SystemUtils.GenerateRandomBytes(MAX_RANDOM_ECHO_DATA_LEN);
          statusLbl.setText("Status: Sending " + data.length + " byte(s)...");
          transId = echoClient.sendEcho(selectedPeer, this, data);

          if (transId != null) {
            statusLbl.setText("Status: Sent " + data.length + " byte(s) ; waiting for ECHO...");
            try {
              while (!transactionMap.containsKey(transId)) {
                if (!autoEchoing) {
                  return false; //Auto Echoing was shutoff...
                }

                tmLock.wait(1000);
              } //End transactionMap check
            } //End try block
            catch (InterruptedException e) {
              throw new RogueNetException("Transaction Thread interrupted while waiting!");
            }

            mesg = (P2PHubMessage) transactionMap.remove(transId);
            echoData = Base64Codec.Decode(mesg.getBase64Data(), false);
            echoMatches = SystemUtils.EqualByteArrays(data, echoData);

            statusLbl.setText("Status: Received " + (echoData != null ? echoData.length : -1) + " byte(s) ; " + (echoMatches ? "ECHO Data Matches" : "ECHO Data Mismatch") + "!");

          } //End null transId check
        } //End synchronization block on tmLock
      } //End null echoClient check
    } //End null plugInManager objects and selectedPeer check

    return echoMatches;
  }

  public void processSynchronousTransactionResponse(String transId, P2PHubMessage mesg) {
    synchronized (tmLock) {
      transactionMap.put(transId, mesg);
      tmLock.notifyAll();
    }
  }

}
