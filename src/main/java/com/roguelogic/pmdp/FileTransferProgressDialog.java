/**
 * Created Nov 30, 2006
 */
package com.roguelogic.pmdp;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.pmd.PMDClient;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileTransferProgressDialog extends JDialog implements WindowListener {
  private JProgressBar bar;
  private JLabel statusLbl, filenameLbl, fileSizeLbl;
  private JButton cancelBtn;
  private JPanel mainPanel;
  private JPanel pbPanel;

  private PMDClient client;

  private String mesg;
  private String filename;
  private int fileSize;

  private static final Dimension FRAME_SIZE = new Dimension(400, 220);

  public FileTransferProgressDialog(JFrame parent, PMDClient client, String mesg, String fileName, int fileSize) {
    super(parent);

    this.client = client;
    this.mesg = mesg;
    this.filename = fileName;
    this.fileSize = fileSize;

    initComponents();
  }

  private void initComponents() {
    GridBagLayout frameGbl, mpGbl, pbGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FRAME_SIZE);
    setTitle("Remote File Transfer");

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

    //Mesg Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    statusLbl = new JLabel(mesg);
    mpGbl.setConstraints(statusLbl, gbc);
    mainPanel.add(statusLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Filename Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    filenameLbl = new JLabel("Filename: " + filename);
    mpGbl.setConstraints(filenameLbl, gbc);
    mainPanel.add(filenameLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //File Size Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    fileSizeLbl = new JLabel("File Size: " + fileSize);
    mpGbl.setConstraints(fileSizeLbl, gbc);
    mainPanel.add(fileSizeLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Progress Bar Row------------------------------------>
    pbGbl = new GridBagLayout();
    pbPanel = new JPanel(pbGbl);
    mpGbl.setConstraints(pbPanel, gbc);
    mainPanel.add(pbPanel);

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 2.0;
    gbc.weighty = 0.0;

    bar = new JProgressBar();
    dSize = new Dimension((int) (FRAME_SIZE.getWidth() - 90), 25);
    bar.setPreferredSize(dSize);
    bar.setMaximumSize(dSize);
    bar.setMinimumSize(dSize);
    bar.setIndeterminate(true);
    bar.setString("");
    bar.setStringPainted(true);
    pbGbl.setConstraints(bar, gbc);
    pbPanel.add(bar);

    //Cancel Button
    gbc.weightx = 0.0;
    cancelBtn = new JButton("Cancel");
    cancelBtn.setEnabled(false);
    dSize = new Dimension(80, 25);
    cancelBtn.setPreferredSize(dSize);
    cancelBtn.setMaximumSize(dSize);
    cancelBtn.setMinimumSize(dSize);
    pbGbl.setConstraints(cancelBtn, gbc);
    pbPanel.add(cancelBtn);

    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          client.closeFile();
          close();
        }
        catch (Exception e) {
          RLErrorDialog.ShowError(e);
        }
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    pbGbl.setConstraints(blankPanel, gbc);
    pbPanel.add(blankPanel);

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

  public void close() {
    if (bar != null) {
      bar.setIndeterminate(false);
    }

    bar = null;
    dispose();
  }

  public void windowOpened(WindowEvent we) {}

  public void windowClosed(WindowEvent we) {}

  public void windowClosing(WindowEvent we) {
    try {
      client.closeFile();
      close();
    }
    catch (Exception e) {
      RLErrorDialog.ShowError(e);
    }
  }

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

}
