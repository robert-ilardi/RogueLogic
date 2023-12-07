/**
 * Created Nov 22, 2006
 */
package com.roguelogic.pmdp;

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
import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.pmd.PMDClient;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileTransferUtilityDialog extends JDialog implements WindowListener {

  private JPanel mainPanel, dirListPanel, fileListPanel;
  private JPanel listsPanel, buttonPanel;
  private JLabel dirListLbl, fileListLbl;
  private JList dirLst, fileLst;
  private JButton downloadButton, closeButton;
  private JScrollPane dirLstSP, fileLstSP;
  private GridBagLayout frameGbl, mpGbl, lpGbl;

  private PMDClient client;
  private String[] remoteShares;

  private static final Dimension FRAME_SIZE = new Dimension(600, 400);

  private String selectedFile;

  public FileTransferUtilityDialog(PMDClient client, JFrame parent) {
    super(parent);

    this.client = client;

    addWindowListener(this);

    initComponents();

    loadRemoteShares();
  }

  private void initComponents() {
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FRAME_SIZE);
    super.setTitle("Remote File Chooser");

    setResizable(false);
    setLocationRelativeTo(null); //Center on Screen

    frameGbl = new GridBagLayout();
    setLayout(frameGbl);

    //Start Main Panel------------------------------>
    paintMainPanelStart();

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
          if (dirLst.getSelectedIndex() >= 0) {
            try {
              loadRemoteFiles(dirLst.getSelectedValue().toString().trim());
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
        StringBuffer path;
        String remoteDir = dirLst.getSelectedValue().toString().trim();
        String remoteFile = fileLst.getSelectedValue().toString().trim();

        path = new StringBuffer();
        path.append(remoteDir);

        if (!remoteDir.endsWith("\\") && !remoteDir.endsWith("/")) {
          path.append("/");
        }

        path.append(remoteFile);

        FileTransferUtilityDialog.this.selectedFile = path.toString();

        dispose();
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

  public void windowOpened(WindowEvent we) {}

  public void windowClosed(WindowEvent we) {}

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  public void loadRemoteFiles(String remoteDir) {
    String[] remoteFileList = null;
    DefaultListModel dlm;

    try {
      dlm = (DefaultListModel) fileLst.getModel();
      dlm.clear();

      remoteFileList = client.getRemoteFileList(remoteDir);

      if (remoteFileList != null) {
        Arrays.sort(remoteFileList);

        for (String remoteFile : remoteFileList) {
          dlm.addElement(remoteFile);
        }
      }
    }
    catch (Exception e) {
      RLErrorDialog.ShowError(e);
    }
  }

  private void loadRemoteShares() {
    DefaultListModel dlm;

    try {
      dlm = (DefaultListModel) dirLst.getModel();
      dlm.clear();

      if (remoteShares == null) {
        remoteShares = client.getRemoteShares();
        Arrays.sort(remoteShares);
      }

      if (remoteShares != null) {
        for (String share : remoteShares) {
          dlm.addElement(share);
        }
      }
    }
    catch (Exception e) {
      RLErrorDialog.ShowError(e);
    }
  }

  public String getSelectedFile() {
    return selectedFile;
  }

}
