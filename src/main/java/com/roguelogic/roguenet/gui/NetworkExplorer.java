/**
 * Created Sep 28, 2006
 */
package com.roguelogic.roguenet.gui;

import static com.roguelogic.roguenet.RNAConstants.*;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.NetworkAgent;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.Version;
import com.roguelogic.roguenet.plugins.NetworkExplorerReceiver;
import com.roguelogic.roguenet.plugins.RNAInfoDaemon;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class NetworkExplorer extends JFrame implements WindowListener {

  private Properties rnaProps;

  private boolean doLocalLoop;

  private RNATrayMenu rnaTrayMenu;
  private NetworkAgent netAgent;

  private JPanel mainPanel, cmdButtonPanel;
  private JLabel networkLbl;
  private JTable agentTable;
  private JScrollPane agentTablePane;
  private JButton refreshButton, closeButton, detailsButton;
  private Dimension frameSize = new Dimension(500, 400);

  private NetExplorerAgentTableModel agentTableModel;

  private Hashtable<String, AgentDetailsDialog> rttToADDMap;

  private P2PHubPeer[] peers;

  public NetworkExplorer(Properties rnaProps, RNATrayMenu rnaTrayMenu, NetworkAgent netAgent) {
    super();

    this.rnaProps = rnaProps;
    this.rnaTrayMenu = rnaTrayMenu;
    this.netAgent = netAgent;

    rttToADDMap = new Hashtable<String, AgentDetailsDialog>();

    addWindowListener(this);

    readProperties();

    initComponents();

    try {
      hookReceiver();
      loadNetworkInfo();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void readProperties() {
    String tmp;

    tmp = rnaProps.getProperty(PROP_NET_EXPLORER_LOCAL_LOOPING);
    doLocalLoop = "TRUE".equalsIgnoreCase(tmp);
  }

  private void defaultHeaderWidths() {
    String tmp;
    FontMetrics fm;
    int width;
    TableColumn tc;

    fm = getGraphics().getFontMetrics();

    for (int i = 0; i < agentTable.getColumnCount(); i++) {
      tmp = agentTable.getColumnName(i);
      tc = agentTable.getColumn(tmp);

      width = fm.stringWidth(tmp) + (2 * agentTable.getColumnModel().getColumnMargin() + 5);
      //tc.setPreferredWidth(width);
      tc.setPreferredWidth(Math.max(width, 100));
    }
  }

  private void initComponents() {
    GridBagLayout frameGbl, mpGbl, cmdButtonGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(frameSize);
    setTitle("Network Explorer");

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

    //Row 1------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    networkLbl = new JLabel("P2P Hub Server - [NOT CONNECTED]");
    mpGbl.setConstraints(networkLbl, gbc);
    mainPanel.add(networkLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Row 2------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 10, 5);
    gbc.weightx = 0.0;

    agentTableModel = new NetExplorerAgentTableModel();

    dSize = new Dimension((int) frameSize.getWidth() - 15, 250);
    agentTable = new JTable(agentTableModel);
    agentTablePane = new JScrollPane();
    agentTablePane.setPreferredSize(dSize);
    agentTablePane.setMaximumSize(dSize);
    agentTablePane.setMinimumSize(dSize);
    agentTable.setPreferredScrollableViewportSize(agentTablePane.getPreferredSize());
    agentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    agentTablePane.setViewportView(agentTable);

    mpGbl.setConstraints(agentTablePane, gbc);
    mainPanel.add(agentTablePane);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Row 3------------------------------------>
    //Command Button Panel
    cmdButtonGbl = new GridBagLayout();
    cmdButtonPanel = new JPanel();
    cmdButtonPanel.setLayout(cmdButtonGbl);

    //For the buttons
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.weightx = 0.0;

    //Refresh Button
    refreshButton = new JButton("Refresh");
    cmdButtonGbl.setConstraints(refreshButton, gbc);
    cmdButtonPanel.add(refreshButton);

    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        NetworkExplorer.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            try {
              loadNetworkInfo();
            }
            catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        NetworkExplorer.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });

    //Details Button
    detailsButton = new JButton("Details");
    cmdButtonGbl.setConstraints(detailsButton, gbc);
    cmdButtonPanel.add(detailsButton);

    detailsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        NetworkExplorer.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
          showAgentDetails();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        finally {
          NetworkExplorer.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });

    //Close Button
    closeButton = new JButton("Close");
    cmdButtonGbl.setConstraints(closeButton, gbc);
    cmdButtonPanel.add(closeButton);

    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        NetworkExplorer.this.dispose();
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

  private void loadNetworkInfo() throws RogueNetException {
    StringBuffer sb;
    RNAInfoDaemon localInfoDaemon;

    //Update Network Label
    sb = new StringBuffer("P2P Hub Server - ");

    if (netAgent != null && netAgent.isConnected()) {
      sb.append(netAgent.getHubAddress());
      sb.append(" : ");
      sb.append(netAgent.getHubPort());
    }
    else {
      sb.append("[NOT CONNECTED]");
    }

    networkLbl.setText(sb.toString());

    //Refresh Agent Table...
    agentTable.clearSelection();
    agentTableModel.clear();

    //First add the local Agent
    if (!doLocalLoop) {
      //Manually get Local Agent info...
      localInfoDaemon = (RNAInfoDaemon) netAgent.getPlugIn(RNAInfoDaemon.class.getName());
      if (localInfoDaemon != null) {
        agentTableModel.addRow(localInfoDaemon.getSummaryArr());
        agentTableModel.fireTableRowsInserted(0, 1);
      }
    }

    //Next add ALL currently connected peer nodes via the Hub Server
    //This is an asynchronous process...
    //We must request all peers to send us the data...
    peers = netAgent.getPeerList();
    if (peers != null && peers.length > 0) {
      for (int i = 0; i < peers.length; i++) {
        if (doLocalLoop || peers[i].equals(netAgent.getHubUsername()))
          if (peers[i] != null) {
            sendGetSummaryInfoRequest(peers[i].getSessionToken());
          }
      }
    }
  }

  public void sendGetSummaryInfoRequest(String peer) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;

    if (peer != null && peer.trim().length() > 0) {
      mesg = new P2PHubMessage();

      mesg.setSubject(RNA_INFO_DAEMON_SUBJECT);
      mesg.setRecipients(new String[] { peer.trim() });

      props = new Properties();
      props.setProperty(RNA_INFO_DAEMON_INFO_LEVEL_PROP, RNA_INFO_DAEMON_INFO_LEVEL_BASIC);
      props.setProperty(RNA_INFO_DAEMON_REPLY_SUBJECT_PROP, NETWORK_EXPLORER_REPLY_SUBJECT);

      mesg.setProperties(props);

      netAgent.sendMessage(mesg);
    }
  }

  public void sendGetDetailedInfoRequest(AgentDetailsDialog add, String peer) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    String rtt;

    if (add != null && peer != null && peer.trim().length() > 0) {
      rtt = getRoundTripToken();
      rttToADDMap.put(rtt, add);

      mesg = new P2PHubMessage();

      mesg.setSubject(RNA_INFO_DAEMON_SUBJECT);
      mesg.setRecipients(new String[] { peer.trim() });

      props = new Properties();
      props.setProperty(RNA_INFO_DAEMON_INFO_LEVEL_PROP, RNA_INFO_DAEMON_INFO_LEVEL_DETAILED);
      props.setProperty(RNA_INFO_DAEMON_REPLY_SUBJECT_PROP, NETWORK_EXPLORER_REPLY_SUBJECT);
      props.setProperty(RNA_INFO_DAEMON_ROUND_TRIP_TOKEN_PROP, rtt);

      mesg.setProperties(props);

      netAgent.sendMessage(mesg);
    }
  }

  public void sendGetPlugInDetailsRequest(String peer, String plugInName) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;

    if (peer != null && peer.trim().length() > 0) {
      mesg = new P2PHubMessage();

      mesg.setSubject(RNA_INFO_DAEMON_SUBJECT);
      mesg.setRecipients(new String[] { peer.trim() });

      props = new Properties();
      props.setProperty(RNA_INFO_DAEMON_INFO_LEVEL_PROP, RNA_INFO_DAEMON_INFO_LEVEL_PLUG_IN_DETAILS);
      props.setProperty(RNA_INFO_DAEMON_REPLY_SUBJECT_PROP, NETWORK_EXPLORER_REPLY_SUBJECT);
      props.setProperty(RNA_INFO_DAEMON_TARGET_PLUG_IN, plugInName);

      mesg.setProperties(props);

      netAgent.sendMessage(mesg);
    }
  }

  public synchronized void receiveUpdate(P2PHubMessage mesg) {
    String[] summary;
    int cnt;
    Properties props;
    String infoLevel;
    AgentDetailsDialog add;
    String roundTripToken;

    props = mesg.getProperties();
    if (props != null) {
      infoLevel = props.getProperty(RNA_INFO_DAEMON_INFO_LEVEL_PROP);

      if (RNA_INFO_DAEMON_INFO_LEVEL_BASIC.equals(infoLevel)) {
        summary = convertToSummaryArr(mesg);
        agentTableModel.addRow(summary);
        cnt = agentTableModel.getRowCount();
        agentTableModel.fireTableRowsInserted(cnt - 1, cnt);
      }
      else if (RNA_INFO_DAEMON_INFO_LEVEL_DETAILED.equals(infoLevel)) {
        roundTripToken = props.getProperty(RNA_INFO_DAEMON_ROUND_TRIP_TOKEN_PROP);
        add = rttToADDMap.remove(roundTripToken);

        if (add != null && add.isVisible()) {
          add.receiveUpdate(mesg);
        }
      }
      else if (RNA_INFO_DAEMON_INFO_LEVEL_PLUG_IN_DETAILS.equals(infoLevel)) {
        showPlugInDetailPropertiesTable(mesg);
      }
    }
  }

  private void showPlugInDetailPropertiesTable(P2PHubMessage mesg) {
    PlugInDetailsPropTableDialog pidPtd;
    Properties props;

    props = mesg.getProperties();

    if (props != null && props.size() > 0) {
      pidPtd = new PlugInDetailsPropTableDialog(props, NETWORK_EXPLORER_PLUG_IN_DETAILS_PROP_TABLE_TITLE, getIconImage());
      pidPtd.setVisible(true);
    }
  }

  private String[] convertToSummaryArr(P2PHubMessage mesg) {
    String[] summary;
    Properties props;

    summary = new String[RNA_INFO_DAEMON_SUMMARY_FIELD_CNT];
    props = mesg.getProperties();

    summary[RNA_INFO_DAEMON_SUMMARY_AGENT_NAME_POS] = props.getProperty(RNA_INFO_DAEMON_AGENT_NAME_PROP);
    summary[RNA_INFO_DAEMON_SUMMARY_RNA_VERSION_POS] = props.getProperty(RNA_INFO_DAEMON_RNA_VERSION_PROP);
    summary[RNA_INFO_DAEMON_SUMMARY_UP_TIME_POS] = props.getProperty(RNA_INFO_DAEMON_UP_TIME_PROP);
    summary[RNA_INFO_DAEMON_SUMMARY_JVM_VERSION_POS] = props.getProperty(RNA_INFO_DAEMON_JVM_VERSION_PROP);
    summary[RNA_INFO_DAEMON_SUMMARY_OS_NAME_POS] = props.getProperty(RNA_INFO_DAEMON_OS_NAME_PROP);
    summary[RNA_INFO_DAEMON_SUMMARY_TOTAL_HEAP_POS] = props.getProperty(RNA_INFO_DAEMON_TOTAL_HEAP_PROP);
    summary[RNA_INFO_DAEMON_SUMMARY_AVAILABLE_HEAP_POS] = props.getProperty(RNA_INFO_DAEMON_AVAILABLE_HEAP_PROP);
    summary[RNA_INFO_DAEMON_SUMMARY_P2P_HUB_SESSION_TOKEN_POS] = props.getProperty(RNA_INFO_DAEMON_P2P_HUB_SESSION_TOKEN_PROP);
    summary[RNA_INFO_DAEMON_SUMMARY_P2P_HUB_USERNAME_POS] = props.getProperty(RNA_INFO_DAEMON_P2P_HUB_USERNAME_PROP);

    return summary;
  }

  private void showAgentDetails() {
    String peer;
    AgentDetailsDialog add;

    if (agentTable.getSelectedRow() < 0) {
      JOptionPane.showMessageDialog(this, "You MUST first select an Agent from the table to view details!", Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, rnaTrayMenu.getIcon());
    }
    else {
      peer = (String) agentTableModel.getValueAt(agentTable.getSelectedRow(), RNA_INFO_DAEMON_SUMMARY_P2P_HUB_SESSION_TOKEN_POS);

      if (peer == null || peer.trim().length() == 0) {
        JOptionPane.showMessageDialog(this, "You MUST first select an Agent from the table to view details!", Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE, rnaTrayMenu.getIcon());
      }
      else {
        add = new AgentDetailsDialog(this, peer);
        add.setVisible(true);
      }
    }
  }

  private void hookReceiver() {
    NetworkExplorerReceiver receiver = (NetworkExplorerReceiver) netAgent.getPlugIn(NetworkExplorerReceiver.class.getName());
    receiver.setNetworkExplorer(this);
  }

  private void unhookReceiver() {
    NetworkExplorerReceiver receiver = (NetworkExplorerReceiver) netAgent.getPlugIn(NetworkExplorerReceiver.class.getName());
    receiver.setNetworkExplorer(null);
  }

  public void windowOpened(WindowEvent we) {
    defaultHeaderWidths();
  }

  public void windowClosed(WindowEvent we) {
    unhookReceiver();
  }

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  public RNATrayMenu getRNATrayMenu() {
    return rnaTrayMenu;
  }

  private String getRoundTripToken() {
    return StringUtils.GenerateTimeUniqueId();
  }

}
