/**
 * Created Sep 24, 2006
 */
package com.roguelogic.roguenet.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.NetworkAgent;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class P2PHubMessageComposer extends JFrame {

  private RNATrayMenu rnaTrayMenu;
  private NetworkAgent netAgent;

  private JLabel subjectLbl, recipientsLbl, dataLbl, propertyLbl, equalsSignLbl;
  private JTextField subjectTF, propNameTF, propValueTF;
  private JComboBox recipientsCB;
  private JTextArea dataTA;
  private JScrollPane dataTAScrollPane, propListScrollPane;
  private JPanel mainPanel, commandButtonPanel, propPanel, propButtonPanel;
  private JButton refreshRecipientsButton, sendButton, cancelButton, addPropButton, removePropButton;
  private JList propList;

  public P2PHubMessageComposer(RNATrayMenu rnaTrayMenu, NetworkAgent netAgent) {
    super();

    this.rnaTrayMenu = rnaTrayMenu;
    this.netAgent = netAgent;

    initComponents();
  }

  /*public static void main(String[] args) {
   P2PHubMessageComposer composer;

   composer = new P2PHubMessageComposer(null);
   composer.setVisible(true);
   composer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }*/

  private void initComponents() {
    GridBagLayout frameGbl, mpGbl, cmdButtonGbl, propPanelGbl, propButtonPanelGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(360, 400);
    setTitle("P2P Hub Message Composer");

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

    //Row 1---------------------------------------->
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.weightx = 0.0;

    //Subject Label
    subjectLbl = new JLabel("Subject: ");
    mpGbl.setConstraints(subjectLbl, gbc);
    mainPanel.add(subjectLbl);

    //Subject Text Field
    subjectTF = new JTextField();
    subjectTF.setPreferredSize(new Dimension(150, 20));
    subjectTF.setMaximumSize(new Dimension(150, 20));
    subjectTF.setMinimumSize(new Dimension(150, 20));
    mpGbl.setConstraints(subjectTF, gbc);
    mainPanel.add(subjectTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Row 2---------------------------------------->
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.weightx = 0.0;

    //Recipient Label
    recipientsLbl = new JLabel("Recipient: ");
    mpGbl.setConstraints(recipientsLbl, gbc);
    mainPanel.add(recipientsLbl);

    //Recipients Combo Box
    gbc.gridwidth = 2;
    recipientsCB = new JComboBox(new DefaultComboBoxModel());
    recipientsCB.setPreferredSize(new Dimension(180, 20));
    recipientsCB.setMaximumSize(new Dimension(180, 20));
    recipientsCB.setMinimumSize(new Dimension(180, 20));

    recipientsCB.setUI(new MetalComboBoxUI() {
      protected ComboPopup createPopup() {
        BasicComboPopup popup = new BasicComboPopup(comboBox) {
          protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
            FontMetrics fm = comboBox.getGraphics().getFontMetrics();
            int valWidth = 0, tmp;
            DefaultComboBoxModel cModel;

            cModel = (DefaultComboBoxModel) comboBox.getModel();
            for (int i = 0; i < cModel.getSize(); i++) {
              tmp = fm.stringWidth(comboBox.getSelectedItem().toString().trim()) + 5;

              if (tmp > valWidth) {
                valWidth = tmp;
              }
            }

            return super.computePopupBounds(px, py, Math.max(valWidth, pw), ph);
          }
        };
        popup.getAccessibleContext().setAccessibleParent(comboBox);
        return popup;
      }
    });

    mpGbl.setConstraints(recipientsCB, gbc);
    mainPanel.add(recipientsCB);

    gbc.gridwidth = 1;
    refreshRecipientsButton = new JButton("Refresh");
    mpGbl.setConstraints(refreshRecipientsButton, gbc);
    mainPanel.add(refreshRecipientsButton);

    refreshRecipientsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        try {
          P2PHubMessageComposer.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          refreshRecipients();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        finally {
          P2PHubMessageComposer.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Row 3---------------------------------------->
    //Property Panel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 0, 10, 0);
    gbc.weightx = 0.0;

    propPanel = new JPanel();
    propPanelGbl = new GridBagLayout();
    propPanel.setLayout(propPanelGbl);
    mpGbl.setConstraints(propPanel, gbc);
    mainPanel.add(propPanel);

    //Reset for Internal Property Panel Components
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.weightx = 0.0;

    //Prop Name Label
    propertyLbl = new JLabel("Property: ");
    propPanelGbl.setConstraints(propertyLbl, gbc);
    propPanel.add(propertyLbl);

    //Prop Name Text Field
    propNameTF = new JTextField();
    propNameTF.setPreferredSize(new Dimension(100, 20));
    propNameTF.setMaximumSize(new Dimension(100, 20));
    propNameTF.setMinimumSize(new Dimension(100, 20));
    propPanelGbl.setConstraints(propNameTF, gbc);
    propPanel.add(propNameTF);

    //Equals Sign
    equalsSignLbl = new JLabel("=");
    propPanelGbl.setConstraints(equalsSignLbl, gbc);
    propPanel.add(equalsSignLbl);

    //Prop Value Text Field
    propValueTF = new JTextField();
    propValueTF.setPreferredSize(new Dimension(100, 20));
    propValueTF.setMaximumSize(new Dimension(100, 20));
    propValueTF.setMinimumSize(new Dimension(100, 20));
    propPanelGbl.setConstraints(propValueTF, gbc);
    propPanel.add(propValueTF);

    //End Property Panel Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    propPanelGbl.setConstraints(blankPanel, gbc);
    propPanel.add(blankPanel);

    //Next Property Panel Row
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.weightx = 0.0;

    //Prop Buttons Panel
    propButtonPanel = new JPanel();
    propButtonPanelGbl = new GridBagLayout();
    propButtonPanel.setLayout(propButtonPanelGbl);
    propPanel.add(propButtonPanel);

    //Add Button
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;

    addPropButton = new JButton("Add >");
    propButtonPanelGbl.setConstraints(addPropButton, gbc);
    propButtonPanel.add(addPropButton);

    addPropButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        StringBuffer prop = new StringBuffer();
        DefaultListModel lModel = (DefaultListModel) propList.getModel();

        prop.append(propNameTF.getText().trim());
        prop.append(" = ");
        prop.append(propValueTF.getText().trim());
        lModel.addElement(prop.toString());

        propNameTF.setText("");
        propValueTF.setText("");
      }
    });

    //Remove Button
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;

    removePropButton = new JButton("< Remove");
    propButtonPanelGbl.setConstraints(removePropButton, gbc);
    propButtonPanel.add(removePropButton);

    removePropButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        DefaultListModel lModel = (DefaultListModel) propList.getModel();

        if (propList.getSelectedIndex() >= 0) {
          lModel.remove(propList.getSelectedIndex());
        }
      }
    });

    //Property List
    gbc.gridwidth = 3;
    propList = new JList(new DefaultListModel());
    propListScrollPane = new JScrollPane(propList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    propListScrollPane.setPreferredSize(new Dimension(200, 50));
    propListScrollPane.setMaximumSize(new Dimension(200, 50));
    propListScrollPane.setMinimumSize(new Dimension(200, 50));
    propPanelGbl.setConstraints(propListScrollPane, gbc);
    propPanel.add(propListScrollPane);

    //End Property Panel Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    propPanelGbl.setConstraints(blankPanel, gbc);
    propPanel.add(blankPanel);

    //Next Property Panel Row
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.weightx = 0.0;

    //Row 4---------------------------------------->
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.weightx = 0.0;

    //Data Label
    dataLbl = new JLabel("Data: ");
    mpGbl.setConstraints(dataLbl, gbc);
    mainPanel.add(dataLbl);

    //Data Text Area
    gbc.gridwidth = 3;
    gbc.gridheight = 2;
    dataTA = new JTextArea();
    dataTAScrollPane = new JScrollPane(dataTA, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    dataTAScrollPane.setPreferredSize(new Dimension(250, 100));
    dataTAScrollPane.setMaximumSize(new Dimension(250, 100));
    dataTAScrollPane.setMinimumSize(new Dimension(250, 100));
    mpGbl.setConstraints(dataTAScrollPane, gbc);
    mainPanel.add(dataTAScrollPane);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Row 5---------------------------------------->
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.weightx = 0.0;

    commandButtonPanel = new JPanel();
    cmdButtonGbl = new GridBagLayout();
    commandButtonPanel.setLayout(cmdButtonGbl);

    //Send Button
    sendButton = new JButton("Send");
    cmdButtonGbl.setConstraints(sendButton, gbc);
    commandButtonPanel.add(sendButton);

    sendButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        try {
          P2PHubMessageComposer.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          sendMessage();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        finally {
          P2PHubMessageComposer.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });

    //Cancel Button
    cancelButton = new JButton("Cancel");
    cmdButtonGbl.setConstraints(cancelButton, gbc);
    commandButtonPanel.add(cancelButton);

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        P2PHubMessageComposer.this.dispose();
      }
    });

    //Add Command Button Panel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.weightx = 1.0;

    mpGbl.setConstraints(commandButtonPanel, gbc);
    mainPanel.add(commandButtonPanel);

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

  private void sendMessage() throws RogueNetException {
    P2PHubMessage mesg = new P2PHubMessage();
    DefaultListModel lModel;
    String[] tmpArr;
    String tmp;
    Properties props;

    mesg.setSubject(subjectTF.getText().trim());

    if (recipientsCB.getSelectedIndex() >= 0) {
      mesg.setRecipients(new String[] { recipientsCB.getSelectedItem().toString().trim() });
    }

    if (propList.getModel().getSize() > 0) {
      lModel = (DefaultListModel) propList.getModel();
      props = new Properties();

      for (int i = 0; i < lModel.getSize(); i++) {
        tmp = (String) lModel.getElementAt(i);
        tmpArr = tmp.split("=", 2);
        tmpArr = StringUtils.Trim(tmpArr);

        props.setProperty(tmpArr[0], tmpArr[1]);
      }

      mesg.setProperties(props);
    }

    mesg.setData(dataTA.getText().getBytes());

    netAgent.sendMessage(mesg);
  }

  private void refreshRecipients() throws RogueNetException {
    P2PHubPeer[] peerList = netAgent.getPeerList();
    Vector<String> data = new Vector<String>();

    if (peerList != null) {
      for (int i = 0; i < peerList.length; i++) {
        if (peerList[i] != null) {
          data.add(peerList[i].getSessionToken());
        }
      }
    }

    recipientsCB.setModel(new DefaultComboBoxModel(data));
  }

}
