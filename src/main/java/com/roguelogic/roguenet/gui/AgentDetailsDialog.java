/**
 * Created Oct 3, 2006
 */
package com.roguelogic.roguenet.gui;

import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_AGENT_NAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_AVAILABLE_HEAP_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_JVM_VERSION_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_MESSAGES_RECEIVED_CNT_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_MESSAGES_SENT_CNT_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_OS_NAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_P2P_HUB_SESSION_TOKEN_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_P2P_HUB_USERNAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_PLUG_IN_LIST_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_RNA_VERSION_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_TOTAL_HEAP_PROP;
import static com.roguelogic.roguenet.RNAConstants.RNA_INFO_DAEMON_UP_TIME_PROP;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.Version;

/**
 * @author Robert C. Ilardi
 *
 */

public class AgentDetailsDialog extends JFrame implements WindowListener {

  private NetworkExplorer netExplorer;
  private String peer;

  private JPanel mainPanel, cmdButtonPanel, genInfoPanel, plugInPanel;
  private JLabel p2pHubUsernameLbl, p2pHubSessionTokenLbl, agentLbl, rnaVerLbl, upTimeLbl, jvmVersionLbl, osNameLbl;
  private JLabel totalHeapLbl, freeHeapLbl, plugInsLbl, mesgSentCntLbl, mesgReceivedCntLbl;
  private JTextField p2pHubUsernameTF, p2pHubSessionTokenTF, agentTF, rnaVerTF, upTimeTF, jvmVersionTF, osNameTF;
  private JTextField totalHeapTF, freeHeapTF, mesgSentCntTF, mesgReceivedCntTF;
  private JList plugInList;
  private JScrollPane plugInListScrollPane;
  private JButton refreshButton, closeButton, plugInDetailsButton;

  private Dimension frameSize = new Dimension(645, 470);

  public AgentDetailsDialog(NetworkExplorer netExplorer, String peer) {
    super();

    this.netExplorer = netExplorer;
    this.peer = peer;

    addWindowListener(this);

    initComponents();
  }

  private void initComponents() {
    GridBagLayout frameGbl, mpGbl, cmdButtonGbl, gipGbl, pipGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(frameSize);
    setTitle("Agent Details");
    setIconImage(netExplorer.getIconImage());

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

    //Create General Info Panel
    genInfoPanel = new JPanel();
    genInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    genInfoPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;

    mpGbl.setConstraints(genInfoPanel, gbc);
    mainPanel.add(genInfoPanel);

    gipGbl = new GridBagLayout();
    genInfoPanel.setLayout(gipGbl);

    //Create Plug-In Panel
    plugInPanel = new JPanel();
    plugInPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    plugInPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHEAST;

    mpGbl.setConstraints(plugInPanel, gbc);
    mainPanel.add(plugInPanel);

    pipGbl = new GridBagLayout();
    plugInPanel.setLayout(pipGbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Start General Info Panel------------------------------------>

    gbc.gridheight = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    //Row 1------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    agentLbl = new JLabel("Agent Name: ");
    gipGbl.setConstraints(agentLbl, gbc);
    genInfoPanel.add(agentLbl);

    agentTF = new JTextField();
    agentTF.setEditable(false);
    dSize = new Dimension(200, 20);
    agentTF.setPreferredSize(dSize);
    agentTF.setMinimumSize(dSize);
    agentTF.setMaximumSize(dSize);
    gipGbl.setConstraints(agentTF, gbc);
    genInfoPanel.add(agentTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 2------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    p2pHubUsernameLbl = new JLabel("Username: ");
    gipGbl.setConstraints(p2pHubUsernameLbl, gbc);
    genInfoPanel.add(p2pHubUsernameLbl);

    p2pHubUsernameTF = new JTextField();
    p2pHubUsernameTF.setEditable(false);
    p2pHubUsernameTF.setText(peer);

    dSize = new Dimension(200, 20);
    p2pHubUsernameTF.setPreferredSize(dSize);
    p2pHubUsernameTF.setMinimumSize(dSize);
    p2pHubUsernameTF.setMaximumSize(dSize);
    gipGbl.setConstraints(p2pHubUsernameTF, gbc);
    genInfoPanel.add(p2pHubUsernameTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 3------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    p2pHubSessionTokenLbl = new JLabel("Session Token: ");
    gipGbl.setConstraints(p2pHubSessionTokenLbl, gbc);
    genInfoPanel.add(p2pHubSessionTokenLbl);

    p2pHubSessionTokenTF = new JTextField();
    p2pHubSessionTokenTF.setEditable(false);
    p2pHubSessionTokenTF.setText(peer);

    dSize = new Dimension(200, 20);
    p2pHubSessionTokenTF.setPreferredSize(dSize);
    p2pHubSessionTokenTF.setMinimumSize(dSize);
    p2pHubSessionTokenTF.setMaximumSize(dSize);
    gipGbl.setConstraints(p2pHubSessionTokenTF, gbc);
    genInfoPanel.add(p2pHubSessionTokenTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 4------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    rnaVerLbl = new JLabel("RNA Version: ");
    gipGbl.setConstraints(rnaVerLbl, gbc);
    genInfoPanel.add(rnaVerLbl);

    rnaVerTF = new JTextField();
    rnaVerTF.setEditable(false);
    dSize = new Dimension(200, 20);
    rnaVerTF.setPreferredSize(dSize);
    rnaVerTF.setMinimumSize(dSize);
    rnaVerTF.setMaximumSize(dSize);
    gipGbl.setConstraints(rnaVerTF, gbc);
    genInfoPanel.add(rnaVerTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 5------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    upTimeLbl = new JLabel("Up Time: ");
    gipGbl.setConstraints(upTimeLbl, gbc);
    genInfoPanel.add(upTimeLbl);

    upTimeTF = new JTextField();
    upTimeTF.setEditable(false);
    dSize = new Dimension(230, 20);
    upTimeTF.setPreferredSize(dSize);
    upTimeTF.setMinimumSize(dSize);
    upTimeTF.setMaximumSize(dSize);
    gipGbl.setConstraints(upTimeTF, gbc);
    genInfoPanel.add(upTimeTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 6------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    jvmVersionLbl = new JLabel("JVM Version: ");
    gipGbl.setConstraints(jvmVersionLbl, gbc);
    genInfoPanel.add(jvmVersionLbl);

    jvmVersionTF = new JTextField();
    jvmVersionTF.setEditable(false);
    dSize = new Dimension(200, 20);
    jvmVersionTF.setPreferredSize(dSize);
    jvmVersionTF.setMinimumSize(dSize);
    jvmVersionTF.setMaximumSize(dSize);
    gipGbl.setConstraints(jvmVersionTF, gbc);
    genInfoPanel.add(jvmVersionTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 7------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    osNameLbl = new JLabel("Operating System: ");
    gipGbl.setConstraints(osNameLbl, gbc);
    genInfoPanel.add(osNameLbl);

    osNameTF = new JTextField();
    osNameTF.setEditable(false);
    dSize = new Dimension(200, 20);
    osNameTF.setPreferredSize(dSize);
    osNameTF.setMinimumSize(dSize);
    osNameTF.setMaximumSize(dSize);
    gipGbl.setConstraints(osNameTF, gbc);
    genInfoPanel.add(osNameTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 8------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    totalHeapLbl = new JLabel("Total Heap: ");
    gipGbl.setConstraints(totalHeapLbl, gbc);
    genInfoPanel.add(totalHeapLbl);

    totalHeapTF = new JTextField();
    totalHeapTF.setEditable(false);
    dSize = new Dimension(200, 20);
    totalHeapTF.setPreferredSize(dSize);
    totalHeapTF.setMinimumSize(dSize);
    totalHeapTF.setMaximumSize(dSize);
    gipGbl.setConstraints(totalHeapTF, gbc);
    genInfoPanel.add(totalHeapTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 9------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    freeHeapLbl = new JLabel("Free Heap: ");
    gipGbl.setConstraints(freeHeapLbl, gbc);
    genInfoPanel.add(freeHeapLbl);

    freeHeapTF = new JTextField();
    freeHeapTF.setEditable(false);
    dSize = new Dimension(200, 20);
    freeHeapTF.setPreferredSize(dSize);
    freeHeapTF.setMinimumSize(dSize);
    freeHeapTF.setMaximumSize(dSize);
    gipGbl.setConstraints(freeHeapTF, gbc);
    genInfoPanel.add(freeHeapTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 10------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    mesgSentCntLbl = new JLabel("Sent Messages: ");
    gipGbl.setConstraints(mesgSentCntLbl, gbc);
    genInfoPanel.add(mesgSentCntLbl);

    mesgSentCntTF = new JTextField();
    mesgSentCntTF.setEditable(false);
    dSize = new Dimension(200, 20);
    mesgSentCntTF.setPreferredSize(dSize);
    mesgSentCntTF.setMinimumSize(dSize);
    mesgSentCntTF.setMaximumSize(dSize);
    gipGbl.setConstraints(mesgSentCntTF, gbc);
    genInfoPanel.add(mesgSentCntTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Row 11------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    mesgReceivedCntLbl = new JLabel("Received Messages: ");
    gipGbl.setConstraints(mesgReceivedCntLbl, gbc);
    genInfoPanel.add(mesgReceivedCntLbl);

    mesgReceivedCntTF = new JTextField();
    mesgReceivedCntTF.setEditable(false);
    dSize = new Dimension(200, 20);
    mesgReceivedCntTF.setPreferredSize(dSize);
    mesgReceivedCntTF.setMinimumSize(dSize);
    mesgReceivedCntTF.setMaximumSize(dSize);
    gipGbl.setConstraints(mesgReceivedCntTF, gbc);
    genInfoPanel.add(mesgReceivedCntTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gipGbl.setConstraints(blankPanel, gbc);
    genInfoPanel.add(blankPanel);

    //Last Row------------------------------------------------------->
    /*gbc.gridwidth = GridBagConstraints.REMAINDER;
     gbc.gridheight = GridBagConstraints.REMAINDER;
     gbc.weightx = 1.0;
     gbc.weighty = 1.0;
     blankPanel = new JPanel();
     blankPanel.setPreferredSize(new Dimension(1, 1));
     gipGbl.setConstraints(blankPanel, gbc);
     genInfoPanel.add(blankPanel);*/

    //Start Plug In Panel------------------------------------>
    gbc.gridheight = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.insets = null;

    //Row 1------------------------------------>
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 1, 5);
    gbc.weightx = 0.0;

    plugInsLbl = new JLabel("Installed Plug-Ins:");
    pipGbl.setConstraints(plugInsLbl, gbc);
    plugInPanel.add(plugInsLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    pipGbl.setConstraints(blankPanel, gbc);
    plugInPanel.add(blankPanel);

    //Row 2------------------------------------>
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 5, 5);
    gbc.weightx = 1.0;

    plugInList = new JList(new DefaultListModel());
    plugInListScrollPane = new JScrollPane(plugInList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    dSize = new Dimension(250, 220);
    plugInListScrollPane.setPreferredSize(dSize);
    plugInListScrollPane.setMaximumSize(dSize);
    plugInListScrollPane.setMinimumSize(dSize);
    pipGbl.setConstraints(plugInListScrollPane, gbc);
    plugInPanel.add(plugInListScrollPane);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    pipGbl.setConstraints(blankPanel, gbc);
    plugInPanel.add(blankPanel);

    //Row 3------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 5, 5);

    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    pipGbl.setConstraints(blankPanel, gbc);
    plugInPanel.add(blankPanel);

    gbc.weightx = 0.0;
    plugInDetailsButton = new JButton("Plug-In Details");
    pipGbl.setConstraints(plugInDetailsButton, gbc);
    plugInPanel.add(plugInDetailsButton);

    plugInDetailsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        AgentDetailsDialog.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        showPlugInDetails();
        AgentDetailsDialog.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });

    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    pipGbl.setConstraints(blankPanel, gbc);
    plugInPanel.add(blankPanel);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    pipGbl.setConstraints(blankPanel, gbc);
    plugInPanel.add(blankPanel);

    //Last Row------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    pipGbl.setConstraints(blankPanel, gbc);
    plugInPanel.add(blankPanel);

    //Finish Up Main Panel------------------------------------------->

    gbc.gridheight = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.insets = new Insets(20, 10, 10, 10);

    //Button Row------------------------------------>
    //Command Button Panel
    cmdButtonGbl = new GridBagLayout();
    cmdButtonPanel = new JPanel();
    cmdButtonPanel.setLayout(cmdButtonGbl);

    //For the buttons
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(10, 10, 10, 10);

    //Refresh Button
    refreshButton = new JButton("Refresh");
    cmdButtonGbl.setConstraints(refreshButton, gbc);
    cmdButtonPanel.add(refreshButton);

    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        AgentDetailsDialog.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loadAgentInfo();
        AgentDetailsDialog.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });

    //Close Button
    closeButton = new JButton("Close");
    cmdButtonGbl.setConstraints(closeButton, gbc);
    cmdButtonPanel.add(closeButton);

    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        AgentDetailsDialog.this.dispose();
      }
    });

    //Add Command Button Panel
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    mpGbl.setConstraints(cmdButtonPanel, gbc);
    mainPanel.add(cmdButtonPanel);

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

  private void loadAgentInfo() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          if (netExplorer != null) {
            netExplorer.sendGetDetailedInfoRequest(AgentDetailsDialog.this, peer);
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void windowOpened(WindowEvent we) {
    loadAgentInfo();
  }

  public void windowClosed(WindowEvent we) {}

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  public synchronized void receiveUpdate(final P2PHubMessage mesg) {
    if (mesg != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          Properties props;
          String[] tmpArr;
          String tmp;
          DefaultListModel lModel;

          props = mesg.getProperties();

          //Populate Scalar Info
          p2pHubUsernameTF.setText(props.getProperty(RNA_INFO_DAEMON_P2P_HUB_USERNAME_PROP));
          p2pHubUsernameTF.setCaretPosition(0);

          p2pHubSessionTokenTF.setText(props.getProperty(RNA_INFO_DAEMON_P2P_HUB_SESSION_TOKEN_PROP));
          p2pHubSessionTokenTF.setCaretPosition(0);

          agentTF.setText(props.getProperty(RNA_INFO_DAEMON_AGENT_NAME_PROP));
          rnaVerTF.setText(props.getProperty(RNA_INFO_DAEMON_RNA_VERSION_PROP));
          upTimeTF.setText(props.getProperty(RNA_INFO_DAEMON_UP_TIME_PROP));
          jvmVersionTF.setText(props.getProperty(RNA_INFO_DAEMON_JVM_VERSION_PROP));
          osNameTF.setText(props.getProperty(RNA_INFO_DAEMON_OS_NAME_PROP));
          totalHeapTF.setText(props.getProperty(RNA_INFO_DAEMON_TOTAL_HEAP_PROP));
          freeHeapTF.setText(props.getProperty(RNA_INFO_DAEMON_AVAILABLE_HEAP_PROP));
          mesgSentCntTF.setText(props.getProperty(RNA_INFO_DAEMON_MESSAGES_SENT_CNT_PROP));
          mesgReceivedCntTF.setText(props.getProperty(RNA_INFO_DAEMON_MESSAGES_RECEIVED_CNT_PROP));

          //Populate Plug In List
          lModel = (DefaultListModel) plugInList.getModel();
          lModel.clear();

          tmp = props.getProperty(RNA_INFO_DAEMON_PLUG_IN_LIST_PROP);
          if (tmp != null) {
            tmpArr = tmp.trim().split(";");

            plugInsLbl.setText("Installed Plug-Ins: (" + tmpArr.length + ")");

            for (int i = 0; i < tmpArr.length; i++) {
              lModel.addElement(tmpArr[i].trim());
            }
          }
          else {
            plugInsLbl.setText("Installed Plug-Ins: (0)");
          }
        }
      });
    } //End null message check
  }

  public void showPlugInDetails() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          String plugInName;

          if (netExplorer != null) {
            plugInName = (String) plugInList.getSelectedValue();

            if (plugInName != null && plugInName.trim().length() > 0) {
              netExplorer.sendGetPlugInDetailsRequest(peer, plugInName.trim());
            }
            else {
              JOptionPane.showMessageDialog(AgentDetailsDialog.this, "You MUST first select Plug-In from the list to view details!", Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, netExplorer
                  .getRNATrayMenu().getIcon());
            }
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

}
