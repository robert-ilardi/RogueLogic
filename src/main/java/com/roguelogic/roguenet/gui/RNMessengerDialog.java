/**
 * Created Oct 15, 2006
 */
package com.roguelogic.roguenet.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.plugins.RNMessenger;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNMessengerDialog extends JFrame implements WindowListener {

  private RNMessenger rnMsngr;
  private P2PHubPeer peer;

  private JPanel mainPanel, buttonPanel;
  private JLabel recipientLbl;
  private JTextField recipientTF;
  private JTextArea conversationTA, mesgTA;
  private JButton sendButton, closeButton;
  private JScrollPane conversationTAScrollPane, mesgTAScrollPane;

  private Dimension frameSize = new Dimension(365, 340);

  public static final int MESG_TYPE_SELF = 0;
  public static final int MESG_TYPE_PEER = 1;
  public static final int MESG_TYPE_SYSTEM = 2;

  public RNMessengerDialog(RNMessenger rnMsngr, P2PHubPeer peer) {
    super();

    this.rnMsngr = rnMsngr;
    this.peer = peer;

    addWindowListener(this);

    if (rnMsngr != null && rnMsngr.getPlugInManager() != null) {
      setIconImage(rnMsngr.getPlugInManager().getIcon().getImage());
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
    setTitle("RN Messenger");

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

    //Recipient Label Row---------------------------------------->
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    recipientLbl = new JLabel("Conversation with:");
    mpGbl.setConstraints(recipientLbl, gbc);
    mainPanel.add(recipientLbl);

    recipientTF = new JTextField(peer.getUsername());
    recipientTF.setEditable(false);
    recipientTF.setCaretPosition(0);

    dSize = new Dimension(230, 20);
    recipientTF.setPreferredSize(dSize);
    recipientTF.setMinimumSize(dSize);
    recipientTF.setMaximumSize(dSize);

    mpGbl.setConstraints(recipientTF, gbc);
    mainPanel.add(recipientTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Conversation Text Area Row---------------------------------------->
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    conversationTA = new JTextArea();
    conversationTA.setWrapStyleWord(true);
    conversationTA.setLineWrap(true);
    conversationTA.setEditable(false);

    dSize = new Dimension(250, 100);
    conversationTAScrollPane = new JScrollPane(conversationTA, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    conversationTAScrollPane.setPreferredSize(dSize);
    conversationTAScrollPane.setMaximumSize(dSize);
    conversationTAScrollPane.setMinimumSize(dSize);

    mpGbl.setConstraints(conversationTAScrollPane, gbc);
    mainPanel.add(conversationTAScrollPane);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Message Text Area Row---------------------------------------->
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    mesgTA = new JTextArea();
    mesgTA.setWrapStyleWord(true);
    mesgTA.setLineWrap(true);

    mesgTA.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent ke) {}

      public void keyReleased(KeyEvent ke) {}

      public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }
      }
    });

    dSize = new Dimension(250, 100);
    mesgTAScrollPane = new JScrollPane(mesgTA, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    mesgTAScrollPane.setPreferredSize(dSize);
    mesgTAScrollPane.setMaximumSize(dSize);
    mesgTAScrollPane.setMinimumSize(dSize);

    mpGbl.setConstraints(mesgTAScrollPane, gbc);
    mainPanel.add(mesgTAScrollPane);

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
        sendMessage();
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

  public void windowOpened(WindowEvent we) {}

  public void windowClosed(WindowEvent we) {
    removeConversationDialog();
  }

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  private synchronized void sendMessage() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        String txt = mesgTA.getText();
        boolean received;

        if (rnMsngr != null && !StringUtils.IsNVL(txt)) {
          try {
            txt = txt.replaceAll("\n", " ").trim();

            appendConversationText(txt, MESG_TYPE_SELF);
            mesgTA.setText("");
            mesgTA.grabFocus();

            received = rnMsngr.sendMessage(peer.getSessionToken(), txt);

            if (!received) {
              appendConversationText("Message NOT Sent to Recipient! User not logged in?", MESG_TYPE_SYSTEM);
            }
          }
          catch (Exception e) {
            appendConversationText("Message NOT Sent to Recipient! COMMUNICATIONS ERROR!!!", MESG_TYPE_SYSTEM);
            e.printStackTrace();
          }
        }
        else {
          mesgTA.setText("");
        }
      }
    });
  }

  public void appendConversationText(String txt, int mesgType) {
    StringBuffer sb = new StringBuffer();

    sb.append("\n");

    switch (mesgType) {
      case MESG_TYPE_PEER:
        sb.append("Received at ");
        break;
      case MESG_TYPE_SELF:
        sb.append("You sent at ");
        break;
      case MESG_TYPE_SYSTEM:
        sb.append("System Message ");
        break;
    }

    sb.append(StringUtils.QuickDateFormat("MMM dd h:mm:ss a"));
    sb.append("> ");
    sb.append(txt);
    sb.append("\n");

    conversationTA.append(sb.toString());
    conversationTA.setCaretPosition(conversationTA.getText().length());
  }

  private void removeConversationDialog() {
    if (rnMsngr != null) {
      rnMsngr.removeConversationDialog(peer.getSessionToken());
    }
  }

}
