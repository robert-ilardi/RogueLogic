/**
 * Created Oct 3, 2006
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.roguelogic.gui.RLComboBox;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.Version;
import com.roguelogic.roguenet.plugins.RNMessenger;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNMessengerControls extends JFrame implements WindowListener {

  private PlugInManager plugInManager;

  private P2PHubPeer[] peerList;

  private JPanel mainPanel, buttonPanel;
  private JLabel recipientsLbl;
  private JComboBox recipientsCB;
  private JButton sendButton, muteButton; //optionsButton
  private JButton closeButton, refreshButton;

  private Dimension frameSize = new Dimension(360, 125);

  public RNMessengerControls(PlugInManager plugInManager) {
    super();

    this.plugInManager = plugInManager;

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
    setSize(frameSize);
    setTitle("RN Messenger Controls");

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

    //Recipients / Send Row---------------------------------------->
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.weightx = 0.0;

    recipientsLbl = new JLabel("Recipients:");
    mpGbl.setConstraints(recipientsLbl, gbc);
    mainPanel.add(recipientsLbl);

    dSize = new Dimension(180, 20);
    recipientsCB = new RLComboBox();
    recipientsCB.setPreferredSize(dSize);
    recipientsCB.setMinimumSize(dSize);
    recipientsCB.setMaximumSize(dSize);
    mpGbl.setConstraints(recipientsCB, gbc);
    mainPanel.add(recipientsCB);

    refreshButton = new JButton("Refresh");
    mpGbl.setConstraints(refreshButton, gbc);
    mainPanel.add(refreshButton);

    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshRecipients();
      }
    });

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
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.weightx = 0.0;

    buttonGbl = new GridBagLayout();
    buttonPanel = new JPanel(buttonGbl);

    //Send Button
    sendButton = new JButton("Send");
    buttonGbl.setConstraints(sendButton, gbc);
    buttonPanel.add(sendButton);

    sendButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          sendMessage();
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });

    //Mute Button
    muteButton = new JButton((isMuted() ? "Unmute" : "Mute"));
    buttonGbl.setConstraints(muteButton, gbc);
    buttonPanel.add(muteButton);

    muteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        preformMuteToggle();
      }
    });

    //Options Button
    /*optionsButton = new JButton("Options");
     buttonGbl.setConstraints(optionsButton, gbc);
     buttonPanel.add(optionsButton);

     optionsButton.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {

     }
     });*/

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
    refreshRecipients();
  }

  public void windowClosed(WindowEvent we) {}

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  private synchronized void preformMuteToggle() {
    RNMessenger rnMesnger;

    if (plugInManager != null) {
      rnMesnger = (RNMessenger) plugInManager.getPlugIn(RNMessenger.class.getName());

      if (rnMesnger != null) {
        if (rnMesnger.toggleMute()) {
          muteButton.setText("Unmute");
        }
        else {
          muteButton.setText("Mute");
        }
      }
    }
  }

  private synchronized boolean isMuted() {
    RNMessenger rnMesnger;
    boolean muted = false;

    if (plugInManager != null) {
      rnMesnger = (RNMessenger) plugInManager.getPlugIn(RNMessenger.class.getName());

      if (rnMesnger != null) {
        muted = rnMesnger.isMuted();
      }
    }

    return muted;
  }

  private synchronized void refreshRecipients() {
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

            recipientsCB.setModel(new DefaultComboBoxModel(data));
          } //End try block
          catch (Exception e) {
            e.printStackTrace();
          }
        } //End null plugInManager check
      }
    });
  }

  private synchronized void sendMessage() throws RogueNetException {
    RNMessenger rnMesnger;
    RNMessengerDialog rnMesngerDialog;
    P2PHubPeer peer;

    if (recipientsCB.getSelectedIndex() >= 0) {
      peer = peerList[recipientsCB.getSelectedIndex()];

      if (plugInManager != null) {
        rnMesnger = (RNMessenger) plugInManager.getPlugIn(RNMessenger.class.getName());

        if (rnMesnger != null) {
          rnMesngerDialog = rnMesnger.getConversationDialog(peer.getSessionToken());
          if (!rnMesngerDialog.isVisible()) {
            rnMesngerDialog.setVisible(true);
          }
          else {
            rnMesngerDialog.toFront();
          }
        }
      }
    } //End recipientsCB Selected Index check
    else {
      JOptionPane.showMessageDialog(this, "You MUST first select a Recipient to send a message!", Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, plugInManager.getIcon());
    }
  }

}
