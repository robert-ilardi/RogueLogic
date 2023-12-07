/**
 * Created Nov 22, 2006
 */
package com.roguelogic.roguenet.gui;

import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_DELETE_FILE;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_GET_FILE_LIST;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_GET_USER_SHARED_DIRECTORIES;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_RENAME_FILE;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_FILE_LIST_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_NEW_FILENAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_REQUEST_DIR_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_REQUEST_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_STATUS_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_STATUS_SUCCESS;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_USER_SHARED_DIRECTORY_LIST_PROP;
import static com.roguelogic.roguenet.RNAConstants.SSO_SESSION_TOKEN_PROP;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.roguelogic.gui.RLComboBox;
import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.SynchronousTransactionRequestor;
import com.roguelogic.roguenet.plugins.FileTransferUtility;
import com.roguelogic.roguenet.plugins.SynchronousTransactionReceiver;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileTransferUtilityDialog extends JFrame implements WindowListener, SynchronousTransactionRequestor {

  private PlugInManager plugInManager;

  private P2PHubPeer[] peerList;

  private JPanel mainPanel, agentControlPanel, dirListPanel, fileListPanel;
  private JPanel listsPanel, buttonPanel;
  private JLabel agentsLbl, dirListLbl, fileListLbl;
  private JComboBox agentsCB;
  private JList dirLst, fileLst;
  private JButton refreshAgentsButton, loadButton, downloadButton, uploadButton;
  private JButton closeButton, renameFileButton, deleteFileButton;
  private JScrollPane dirLstSP, fileLstSP;
  private GridBagLayout frameGbl, mpGbl, lpGbl;

  private static final long DIRECTORY_LIST_WAIT_PERIOD = 120000;
  private static final long FILE_RENAME_WAIT_PERIOD = 60000;
  private static final long FILE_DELETE_WAIT_PERIOD = 60000;

  private HashMap<String, P2PHubMessage> transactionMap;
  private Object tmLock;

  private static final Dimension FRAME_SIZE = new Dimension(600, 400);

  public FileTransferUtilityDialog(PlugInManager plugInManager) {
    super();

    transactionMap = new HashMap<String, P2PHubMessage>();
    tmLock = new Object();

    this.plugInManager = plugInManager;

    addWindowListener(this);

    if (plugInManager != null) {
      setIconImage(plugInManager.getIcon().getImage());
    }

    initComponents();
  }

  private void initComponents() {
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FRAME_SIZE);
    super.setTitle("Rogue Net - File Transfer Utility");

    setResizable(false);
    setLocationRelativeTo(null); //Center on Screen

    frameGbl = new GridBagLayout();
    setLayout(frameGbl);

    //Start Main Panel------------------------------>
    paintMainPanelStart();

    //Agent Control Panel---------------------------------------->
    paintAgentControlPanel();

    //Lists Panel----------------------------------->
    paintListsPanel();

    //Button Panel----------------------------------->
    paintButtonPanel();

    //End Main Panel------------------------->
    paintMainPanelEnd();
  }

  private void paintMainPanelStart() {
    GridBagConstraints gbc;

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
  }

  private void paintMainPanelEnd() {
    GridBagConstraints gbc;
    JPanel blankPanel;

    gbc = new GridBagConstraints();

    //Last Row (Main Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);
  }

  private void paintAgentControlPanel() {
    GridBagLayout acpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    acpGbl = new GridBagLayout();
    agentControlPanel = new JPanel(acpGbl);
    agentControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    agentControlPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    mpGbl.setConstraints(agentControlPanel, gbc);
    mainPanel.add(agentControlPanel);

    //Agents Label
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    agentsLbl = new JLabel("Agents:");
    acpGbl.setConstraints(agentsLbl, gbc);
    agentControlPanel.add(agentsLbl);

    //Agents ComboBox
    agentsCB = new RLComboBox();
    dSize = new Dimension(170, 20);
    agentsCB.setMinimumSize(dSize);
    agentsCB.setMaximumSize(dSize);
    agentsCB.setPreferredSize(dSize);
    acpGbl.setConstraints(agentsCB, gbc);
    agentControlPanel.add(agentsCB);

    //Refresh Agents Button
    refreshAgentsButton = new JButton("Refresh");
    acpGbl.setConstraints(refreshAgentsButton, gbc);
    agentControlPanel.add(refreshAgentsButton);
    refreshAgentsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        refreshAgents();
      }
    });

    //Load Agent Button
    loadButton = new JButton("Load");
    acpGbl.setConstraints(loadButton, gbc);
    agentControlPanel.add(loadButton);
    loadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (agentsCB.getSelectedIndex() >= 0) {
              try {
                loadRemoteDirectories(peerList[agentsCB.getSelectedIndex()]);
              }
              catch (Exception e) {
                RLErrorDialog.ShowError(e);
              }
            }
          }
        });
      }
    });

    //End Row (Agent Control Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    acpGbl.setConstraints(blankPanel, gbc);
    agentControlPanel.add(blankPanel);

    //Last Row (Agent Control Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    acpGbl.setConstraints(blankPanel, gbc);
    agentControlPanel.add(blankPanel);
  }

  private void paintListsPanel() {
    GridBagConstraints gbc;
    JPanel blankPanel;

    gbc = new GridBagConstraints();

    //End Row (Main Panel)----------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Lists Panel
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    lpGbl = new GridBagLayout();
    listsPanel = new JPanel(lpGbl);
    listsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    listsPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    mpGbl.setConstraints(listsPanel, gbc);
    mainPanel.add(listsPanel);

    //Dir List Panel---------------------------------------->
    paintDirListPanel();

    //File List Panel---------------------------------------->
    paintFileListPanel();

    //End Row (Main Panel)----------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);
  }

  private void paintDirListPanel() {
    GridBagLayout dlpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    dlpGbl = new GridBagLayout();
    dirListPanel = new JPanel(dlpGbl);
    dirListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    dirListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    lpGbl.setConstraints(dirListPanel, gbc);
    listsPanel.add(dirListPanel);

    //Dir Label
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    dirListLbl = new JLabel("Remote Directories:");
    dlpGbl.setConstraints(dirListLbl, gbc);
    dirListPanel.add(dirListLbl);

    //End Row (Dir List Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    dlpGbl.setConstraints(blankPanel, gbc);
    dirListPanel.add(blankPanel);

    //Dir List
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    dirLst = new JList(new DefaultListModel());
    dirLstSP = new JScrollPane(dirLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(250, 220);
    dirLstSP.setPreferredSize(dSize);
    dirLstSP.setMaximumSize(dSize);
    dirLstSP.setMinimumSize(dSize);
    dlpGbl.setConstraints(dirLstSP, gbc);
    dirListPanel.add(dirLstSP);
    dirLst.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() >= 2) {
          if (dirLst.getSelectedIndex() >= 0 && agentsCB.getSelectedIndex() >= 0) {
            try {
              loadRemoteFiles(peerList[agentsCB.getSelectedIndex()], dirLst.getSelectedValue().toString().trim());
            }
            catch (Exception e) {
              RLErrorDialog.ShowError(e);
            }
          }
        }
      }

      public void mousePressed(MouseEvent evt) {}

      public void mouseReleased(MouseEvent evt) {}

      public void mouseEntered(MouseEvent evt) {}

      public void mouseExited(MouseEvent evt) {}
    });

    //End Row (Dir List Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    dlpGbl.setConstraints(blankPanel, gbc);
    dirListPanel.add(blankPanel);

    //Last Row (Dir List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    dlpGbl.setConstraints(blankPanel, gbc);
    dirListPanel.add(blankPanel);
  }

  private void paintFileListPanel() {
    GridBagLayout flpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    flpGbl = new GridBagLayout();
    fileListPanel = new JPanel(flpGbl);
    fileListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    fileListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    lpGbl.setConstraints(fileListPanel, gbc);
    listsPanel.add(fileListPanel);

    //File Label
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    fileListLbl = new JLabel("Remote Files:");
    flpGbl.setConstraints(fileListLbl, gbc);
    fileListPanel.add(fileListLbl);

    //End Row (File List Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    flpGbl.setConstraints(blankPanel, gbc);
    fileListPanel.add(blankPanel);

    //File List
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    fileLst = new JList(new DefaultListModel());
    fileLstSP = new JScrollPane(fileLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(250, 220);
    fileLstSP.setPreferredSize(dSize);
    fileLstSP.setMaximumSize(dSize);
    fileLstSP.setMinimumSize(dSize);
    flpGbl.setConstraints(fileLstSP, gbc);
    fileListPanel.add(fileLstSP);

    //End Row (File List Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    flpGbl.setConstraints(blankPanel, gbc);
    fileListPanel.add(blankPanel);

    //Last Row (File List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    flpGbl.setConstraints(blankPanel, gbc);
    fileListPanel.add(blankPanel);
  }

  private void paintButtonPanel() {
    GridBagLayout bpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    bpGbl = new GridBagLayout();
    buttonPanel = new JPanel(bpGbl);
    buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    mpGbl.setConstraints(buttonPanel, gbc);
    mainPanel.add(buttonPanel);

    //Download Button
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(1, 5, 1, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    downloadButton = new JButton("Download");
    bpGbl.setConstraints(downloadButton, gbc);
    buttonPanel.add(downloadButton);
    downloadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        performDownload();
      }
    });

    //Upload Button
    uploadButton = new JButton("Upload");
    bpGbl.setConstraints(uploadButton, gbc);
    buttonPanel.add(uploadButton);
    uploadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        performUpload();
      }
    });

    //Rename File Button
    renameFileButton = new JButton("Rename File");
    bpGbl.setConstraints(renameFileButton, gbc);
    buttonPanel.add(renameFileButton);
    renameFileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        performFileRename();
      }
    });

    //Delete File Button
    deleteFileButton = new JButton("Delete File");
    bpGbl.setConstraints(deleteFileButton, gbc);
    buttonPanel.add(deleteFileButton);
    deleteFileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        performFileDelete();
      }
    });

    //Close Button    
    closeButton = new JButton("Close");
    bpGbl.setConstraints(closeButton, gbc);
    buttonPanel.add(closeButton);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        dispose();
      }
    });

    //End Row (Button Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    bpGbl.setConstraints(blankPanel, gbc);
    buttonPanel.add(blankPanel);

    //End Row (Button Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    bpGbl.setConstraints(blankPanel, gbc);
    buttonPanel.add(blankPanel);

    //Last Row (Button Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    bpGbl.setConstraints(blankPanel, gbc);
    buttonPanel.add(blankPanel);

    //End Row (Main Panel)----------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
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

        if (plugInManager != null && plugInManager.getNetAgent() != null) {
          try {
            peerList = plugInManager.getNetAgent().getPeerList();

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

  public void loadRemoteDirectories(P2PHubPeer agent) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    String sessionToken, transId, usdListStr;
    String[] usdList = null;
    SynchronousTransactionReceiver stReceiver;
    long start;
    DefaultListModel dlm;

    dlm = (DefaultListModel) dirLst.getModel();
    dlm.clear();

    if (!verifySSO(agent)) {
      JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "NOT Logged into Agent '" + agent.getUsername() + "'! Please use the Single Sign On option to logon to Agent before continuing.",
          FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(FileTransferUtilityDialog.this.getIconImage()));
      return;
    }

    if (agent != null && plugInManager != null) {
      stReceiver = (SynchronousTransactionReceiver) plugInManager.getPlugIn(SynchronousTransactionReceiver.class.getName());
      sessionToken = plugInManager.getEntitlementsManager().getSSOLedger().getSessionToken(agent.getUsername());

      mesg = new P2PHubMessage();
      mesg.setSubject(FILE_SHARE_SERVICE_SUBJECT);
      mesg.setRecipients(new String[] { agent.getSessionToken() });

      props = new Properties();
      props.setProperty(FILE_SHARE_SERVICE_ACTION_PROP, FILE_SHARE_SERVICE_ACTION_GET_USER_SHARED_DIRECTORIES);
      props.setProperty(SSO_SESSION_TOKEN_PROP, sessionToken);
      mesg.setProperties(props);

      if (stReceiver != null) {
        transId = stReceiver.sendSynchronousTransaction(this, mesg);

        try {
          synchronized (tmLock) {
            start = System.currentTimeMillis();
            while (!transactionMap.containsKey(transId)) {
              if ((start + DIRECTORY_LIST_WAIT_PERIOD) < System.currentTimeMillis()) {
                throw new RogueNetException("Did NOT receive Directory List Response within allowed interval!");
              }

              tmLock.wait(1000);
            }
          }
        }
        catch (InterruptedException e) {
          throw new RogueNetException("Transaction Thread interrupted while waiting!");
        }

        mesg = (P2PHubMessage) transactionMap.remove(transId);

        usdListStr = mesg.getProperty(FILE_SHARE_SERVICE_USER_SHARED_DIRECTORY_LIST_PROP);
        if (!StringUtils.IsNVL(usdListStr)) {
          usdList = usdListStr.split("\\|");

          if (usdList != null) {
            for (String usd : usdList) {
              dlm.addElement(usd);
            }
          }
        } //End usdListStr nvl check
      } //End stReceiver null check
    } //End resource null check
  }

  public void loadRemoteFiles(P2PHubPeer agent, String remoteDir) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    String sessionToken, transId, remoteFileListStr;
    String[] remoteFileList = null;
    SynchronousTransactionReceiver stReceiver;
    long start;
    DefaultListModel dlm;

    dlm = (DefaultListModel) fileLst.getModel();
    dlm.clear();

    if (!verifySSO(agent)) {
      JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "NOT Logged into Agent '" + agent.getUsername() + "'! Please use the Single Sign On option to logon to Agent before continuing.",
          FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(FileTransferUtilityDialog.this.getIconImage()));
      return;
    }

    if (agent != null && plugInManager != null) {
      stReceiver = (SynchronousTransactionReceiver) plugInManager.getPlugIn(SynchronousTransactionReceiver.class.getName());
      sessionToken = plugInManager.getEntitlementsManager().getSSOLedger().getSessionToken(agent.getUsername());

      mesg = new P2PHubMessage();
      mesg.setSubject(FILE_SHARE_SERVICE_SUBJECT);
      mesg.setRecipients(new String[] { agent.getSessionToken() });

      props = new Properties();
      props.setProperty(FILE_SHARE_SERVICE_ACTION_PROP, FILE_SHARE_SERVICE_ACTION_GET_FILE_LIST);
      props.setProperty(FILE_SHARE_SERVICE_REQUEST_DIR_PROP, remoteDir);
      props.setProperty(SSO_SESSION_TOKEN_PROP, sessionToken);
      mesg.setProperties(props);

      if (stReceiver != null) {
        transId = stReceiver.sendSynchronousTransaction(this, mesg);

        try {
          synchronized (tmLock) {
            start = System.currentTimeMillis();
            while (!transactionMap.containsKey(transId)) {
              if ((start + DIRECTORY_LIST_WAIT_PERIOD) < System.currentTimeMillis()) {
                throw new RogueNetException("Did NOT receive Directory List Response within allowed interval!");
              }

              tmLock.wait(1000);
            }
          }
        }
        catch (InterruptedException e) {
          throw new RogueNetException("Transaction Thread interrupted while waiting!");
        }

        mesg = (P2PHubMessage) transactionMap.remove(transId);

        remoteFileListStr = mesg.getProperty(FILE_SHARE_SERVICE_FILE_LIST_PROP);
        if (!StringUtils.IsNVL(remoteFileListStr)) {
          remoteFileList = remoteFileListStr.split("\\|");

          if (remoteFileList != null) {
            for (String remoteFile : remoteFileList) {
              dlm.addElement(remoteFile);
            }
          }
        } //End remoteFileListStr nvl check
      } //End stReceiver null check
    } //End resource null check
  }

  private void download(P2PHubPeer agent, String remoteDir, String remoteFile, String localFilePath) throws RogueNetException {
    FileTransferUtility ftUtility;

    if (agent != null && plugInManager != null) {
      ftUtility = (FileTransferUtility) plugInManager.getPlugIn(FileTransferUtility.class.getName());
      if (ftUtility != null) {
        ftUtility.initDownload(agent, remoteDir, remoteFile, localFilePath, true);
      }
    } //End resource null check
  }

  public void performDownload() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        P2PHubPeer agent = null;
        String localFilePath;

        try {
          if (agentsCB.getSelectedIndex() >= 0) {
            agent = peerList[agentsCB.getSelectedIndex()];
          }

          if (!verifySSO(agent)) {
            JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "NOT Logged into Agent '" + agent.getUsername()
                + "'! Please use the Single Sign On option to logon to Agent before continuing.", FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(
                FileTransferUtilityDialog.this.getIconImage()));
            return;
          }

          if (dirLst.getSelectedIndex() >= 0 && fileLst.getSelectedIndex() >= 0) {
            localFilePath = getLocalFilePath();
            if (!StringUtils.IsNVL(localFilePath)) {
              download(agent, dirLst.getSelectedValue().toString().trim(), fileLst.getSelectedValue().toString().trim(), localFilePath);
            }
          }
          else {
            JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "Please select a Remote Directory and File to Download!", FileTransferUtilityDialog.this.getTitle(),
                JOptionPane.ERROR_MESSAGE, new ImageIcon(FileTransferUtilityDialog.this.getIconImage()));
          }
        } //End try block
        catch (Exception e) {
          RLErrorDialog.ShowError(e);
        } //End catch block
      } //End run method
    });
  }

  public void processSynchronousTransactionResponse(String transId, P2PHubMessage mesg) {
    synchronized (tmLock) {
      transactionMap.put(transId, mesg);
      tmLock.notifyAll();
    }
  }

  private boolean verifySSO(P2PHubPeer agent) throws RogueNetException {
    boolean ssoOk = false;

    if (agent != null && plugInManager != null && plugInManager.getEntitlementsManager() != null && plugInManager.getEntitlementsManager().getSSOController() != null) {
      ssoOk = !StringUtils.IsNVL(plugInManager.getEntitlementsManager().getSSOLedger().getSessionToken(agent.getUsername()));
    }

    return ssoOk;
  }

  public String getLocalFilePath() {
    JFileChooser fc;
    File file;
    String localFilePath = null;
    int retVal;

    fc = new JFileChooser();
    retVal = fc.showOpenDialog(this);

    if (retVal == JFileChooser.APPROVE_OPTION) {
      file = fc.getSelectedFile();
      localFilePath = file.getAbsolutePath();
    }

    return localFilePath;
  }

  private void upload(P2PHubPeer agent, String localFilePath, String remoteDir, String remoteFile) throws RogueNetException {
    FileTransferUtility ftUtility;

    if (agent != null && plugInManager != null) {
      ftUtility = (FileTransferUtility) plugInManager.getPlugIn(FileTransferUtility.class.getName());
      if (ftUtility != null) {
        ftUtility.initUpload(agent, localFilePath, remoteDir, remoteFile, true);
      }
    } //End resource null check
  }

  public void performUpload() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        P2PHubPeer agent = null;
        String localFilePath, remoteFile, remoteDir;

        try {
          if (agentsCB.getSelectedIndex() >= 0) {
            agent = peerList[agentsCB.getSelectedIndex()];
          }

          if (!verifySSO(agent)) {
            JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "NOT Logged into Agent '" + agent.getUsername()
                + "'! Please use the Single Sign On option to logon to Agent before continuing.", FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(
                FileTransferUtilityDialog.this.getIconImage()));
            return;
          }

          if (dirLst.getSelectedIndex() >= 0) {
            localFilePath = getLocalFilePath();
            if (!StringUtils.IsNVL(localFilePath)) {
              remoteDir = dirLst.getSelectedValue().toString().trim();
              remoteFile = StringUtils.ParseFileFromPath(localFilePath);
              upload(agent, localFilePath, remoteDir, remoteFile);
            }
          }
          else {
            JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "Please select a destination Remote Directory for the Upload File!", FileTransferUtilityDialog.this.getTitle(),
                JOptionPane.ERROR_MESSAGE, new ImageIcon(FileTransferUtilityDialog.this.getIconImage()));
          }
        } //End try block
        catch (Exception e) {
          RLErrorDialog.ShowError(e);
        } //End catch block
      } //End run method
    });
  }

  public void performFileRename() {
    P2PHubPeer agent = null;
    String newFilename;

    try {
      if (agentsCB.getSelectedIndex() >= 0) {
        agent = peerList[agentsCB.getSelectedIndex()];
      }

      if (!verifySSO(agent)) {
        JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "NOT Logged into Agent '" + agent.getUsername() + "'! Please use the Single Sign On option to logon to Agent before continuing.",
            FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(FileTransferUtilityDialog.this.getIconImage()));
        return;
      }

      if (dirLst.getSelectedIndex() >= 0 && fileLst.getSelectedIndex() >= 0) {
        newFilename = JOptionPane.showInputDialog(this, "Please Enter New Filename:", fileLst.getSelectedValue().toString().trim());

        if (!StringUtils.IsNVL(newFilename)) {
          renameFile(agent, dirLst.getSelectedValue().toString().trim(), fileLst.getSelectedValue().toString().trim(), newFilename);
        }
      }
      else {
        JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "Please select a Remote File to be Renamed!", FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE,
            new ImageIcon(FileTransferUtilityDialog.this.getIconImage()));
      }
    } //End try block
    catch (Exception e) {
      RLErrorDialog.ShowError(e);
    } //End catch block
  }

  public void performFileDelete() {
    P2PHubPeer agent = null;
    int confirmed;

    try {
      if (agentsCB.getSelectedIndex() >= 0) {
        agent = peerList[agentsCB.getSelectedIndex()];
      }

      if (!verifySSO(agent)) {
        JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "NOT Logged into Agent '" + agent.getUsername() + "'! Please use the Single Sign On option to logon to Agent before continuing.",
            FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(FileTransferUtilityDialog.this.getIconImage()));
        return;
      }

      if (dirLst.getSelectedIndex() >= 0 && fileLst.getSelectedIndex() >= 0) {
        confirmed = JOptionPane.showConfirmDialog(this, "Confirm Deletion of Remote File '" + fileLst.getSelectedValue().toString().trim() + "'", "Confirm Remote File Deletion",
            JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
          deleteFile(agent, dirLst.getSelectedValue().toString().trim(), fileLst.getSelectedValue().toString().trim());
        }
      }
      else {
        JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "Please select a Remote File to be Deleted!", FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE,
            new ImageIcon(FileTransferUtilityDialog.this.getIconImage()));
      }
    } //End try block
    catch (Exception e) {
      RLErrorDialog.ShowError(e);
    } //End catch block
  }

  private void renameFile(P2PHubPeer agent, String remoteDir, String remoteFile, String newFilename) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    String sessionToken, transId, status;
    SynchronousTransactionReceiver stReceiver;
    long start;

    if (agent != null && plugInManager != null) {
      //Send Rename Request
      stReceiver = (SynchronousTransactionReceiver) plugInManager.getPlugIn(SynchronousTransactionReceiver.class.getName());
      sessionToken = plugInManager.getEntitlementsManager().getSSOLedger().getSessionToken(agent.getUsername());

      mesg = new P2PHubMessage();
      mesg.setSubject(FILE_SHARE_SERVICE_SUBJECT);
      mesg.setRecipients(new String[] { agent.getSessionToken() });

      props = new Properties();
      props.setProperty(FILE_SHARE_SERVICE_ACTION_PROP, FILE_SHARE_SERVICE_ACTION_RENAME_FILE);
      props.setProperty(FILE_SHARE_SERVICE_REQUEST_DIR_PROP, remoteDir);
      props.setProperty(FILE_SHARE_SERVICE_REQUEST_FILE_PROP, remoteFile);
      props.setProperty(FILE_SHARE_SERVICE_NEW_FILENAME_PROP, newFilename);
      props.setProperty(SSO_SESSION_TOKEN_PROP, sessionToken);
      mesg.setProperties(props);

      if (stReceiver != null) {
        transId = stReceiver.sendSynchronousTransaction(this, mesg);

        try {
          synchronized (tmLock) {
            start = System.currentTimeMillis();
            while (!transactionMap.containsKey(transId)) {
              if ((start + FILE_RENAME_WAIT_PERIOD) < System.currentTimeMillis()) {
                throw new RogueNetException("Did NOT receive File Rename Response within allowed interval!");
              }

              tmLock.wait(1000);
            }
          }
        }
        catch (InterruptedException e) {
          throw new RogueNetException("Transaction Thread interrupted while waiting!");
        }

        //Check Rename Status
        mesg = (P2PHubMessage) transactionMap.remove(transId);
        status = mesg.getProperty(FILE_SHARE_SERVICE_STATUS_PROP);

        if (!FILE_SHARE_SERVICE_STATUS_SUCCESS.equals(status)) {
          JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "Remote File Rename FAILED!", FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(
              FileTransferUtilityDialog.this.getIconImage()));
        }

        //Refresh Remote Directory
        loadRemoteFiles(agent, remoteDir);
      } //End stReceiver null check
    } //End resource null check
  }

  private void deleteFile(P2PHubPeer agent, String remoteDir, String remoteFile) throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;
    String sessionToken, transId, status;
    SynchronousTransactionReceiver stReceiver;
    long start;

    if (agent != null && plugInManager != null) {
      //Send Rename Request
      stReceiver = (SynchronousTransactionReceiver) plugInManager.getPlugIn(SynchronousTransactionReceiver.class.getName());
      sessionToken = plugInManager.getEntitlementsManager().getSSOLedger().getSessionToken(agent.getUsername());

      mesg = new P2PHubMessage();
      mesg.setSubject(FILE_SHARE_SERVICE_SUBJECT);
      mesg.setRecipients(new String[] { agent.getSessionToken() });

      props = new Properties();
      props.setProperty(FILE_SHARE_SERVICE_ACTION_PROP, FILE_SHARE_SERVICE_ACTION_DELETE_FILE);
      props.setProperty(FILE_SHARE_SERVICE_REQUEST_DIR_PROP, remoteDir);
      props.setProperty(FILE_SHARE_SERVICE_REQUEST_FILE_PROP, remoteFile);
      props.setProperty(SSO_SESSION_TOKEN_PROP, sessionToken);
      mesg.setProperties(props);

      if (stReceiver != null) {
        transId = stReceiver.sendSynchronousTransaction(this, mesg);

        try {
          synchronized (tmLock) {
            start = System.currentTimeMillis();
            while (!transactionMap.containsKey(transId)) {
              if ((start + FILE_DELETE_WAIT_PERIOD) < System.currentTimeMillis()) {
                throw new RogueNetException("Did NOT receive File Deletion Response within allowed interval!");
              }

              tmLock.wait(1000);
            }
          }
        }
        catch (InterruptedException e) {
          throw new RogueNetException("Transaction Thread interrupted while waiting!");
        }

        //Check Rename Status
        mesg = (P2PHubMessage) transactionMap.remove(transId);
        status = mesg.getProperty(FILE_SHARE_SERVICE_STATUS_PROP);

        if (!FILE_SHARE_SERVICE_STATUS_SUCCESS.equals(status)) {
          JOptionPane.showMessageDialog(FileTransferUtilityDialog.this, "Remote File Deletion FAILED!", FileTransferUtilityDialog.this.getTitle(), JOptionPane.ERROR_MESSAGE, new ImageIcon(
              FileTransferUtilityDialog.this.getIconImage()));
        }

        //Refresh Remote Directory
        loadRemoteFiles(agent, remoteDir);
      } //End stReceiver null check
    } //End resource null check
  }

}
