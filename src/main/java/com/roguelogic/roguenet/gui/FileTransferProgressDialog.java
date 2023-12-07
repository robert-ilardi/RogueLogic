/**
 * Created Nov 30, 2006
 */
package com.roguelogic.roguenet.gui;

import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_GET_FILE_LIST;
import static com.roguelogic.roguenet.RNAConstants.FILE_SHARE_SERVICE_ACTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_ACTION_ABORT_TRANSFER;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_ACTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_STATUS_ABORTED;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_STATUS_PROP;
import static com.roguelogic.roguenet.RNAConstants.FILE_STREAM_TRANSMITTER_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.FILE_TRANSFER_UTILITY_SUBJECT;
import static com.roguelogic.roguenet.RNAConstants.SSO_SESSION_TOKEN_PROP;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.roguenet.DownloadFile;
import com.roguelogic.roguenet.PlugInManager;
import com.roguelogic.roguenet.RogueNetException;
import com.roguelogic.roguenet.plugins.FileStreamTransmitter;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileTransferProgressDialog extends JFrame implements WindowListener {
  private JProgressBar bar;
  private JLabel statusLbl;
  private JLabel fileSizeLbl;
  private JLabel curSizeLbl;
  private JLabel speedLbl;
  private JLabel elapsedTimeLbl;
  private JButton cancelBtn;
  private JPanel mainPanel;
  private JPanel pbPanel;

  private DownloadFile dload;

  private Thread monitorThread;

  private PlugInManager plugInManager;

  private static final Dimension FRAME_SIZE = new Dimension(400, 220);

  private FileTransferProgressDialog(PlugInManager plugInManager) {
    super();

    addWindowListener(this);

    this.plugInManager = plugInManager;

    if (plugInManager != null) {
      setIconImage(plugInManager.getIcon().getImage());
    }
  }

  public FileTransferProgressDialog(PlugInManager plugInManager, DownloadFile dload) {
    this(plugInManager);

    this.dload = dload;

    initDownloadMonitor();

    setVisible(true);
  }

  private Runnable dloadMonitor = new Runnable() {
    public void run() {
      double startTime, now, prevNow, totalTime = 1.0, kbsTransfered, tRate;
      long curSize;

      if (dload != null) {
        while (StringUtils.IsNVL(dload.getServerTransferId())) {
          SystemUtils.SleepTight(50); //Since we cannot be notified by dload we have to poll and sleep instead of properly waiting :( 
        }

        cancelBtn.setEnabled(true);

        startTime = System.currentTimeMillis();
        prevNow = startTime;

        while (!dload.isAborted() && dload.getCurrentSize() < dload.getTotalSize()) {
          curSize = dload.getCurrentSize();

          fileSizeLbl.setText("File Size: " + dload.getTotalSize() + " Bytes");
          curSizeLbl.setText("Progress: " + curSize + " Bytes");

          SystemUtils.SleepTight(100);

          now = System.currentTimeMillis();

          if (now > (prevNow + 1000)) {
            prevNow = now;
            totalTime = (now - startTime) / 1000;

            kbsTransfered = (curSize / 1000.0);
            tRate = kbsTransfered / totalTime;
            speedLbl.setText("Avg. Transfer Rate: " + StringUtils.FormatDouble(tRate, 2) + " KBps");

            elapsedTimeLbl.setText("Elapsed Time: " + StringUtils.HumanReadableTime((long) (now - startTime)));
          }
        }

        cancelBtn.setEnabled(false);

        curSize = dload.getCurrentSize();

        fileSizeLbl.setText("File Size: " + dload.getTotalSize() + " Bytes");
        curSizeLbl.setText("Progress: " + curSize + " Bytes");

        kbsTransfered = (curSize / 1000.0);
        tRate = kbsTransfered / totalTime;
        speedLbl.setText("Avg. Transfer Rate: " + StringUtils.FormatDouble(tRate, 2) + " KBps");

        SystemUtils.Sleep(5); //Wait a few seconds

        close();
      }
    }
  };

  private void initDownloadMonitor() {
    monitorThread = new Thread(dloadMonitor);
    initComponents((dload.isReverse() ? "Uploading..." : "Downloading..."), (dload.isReverse() ? "Uploading: " : "Downloading: ") + (dload != null ? dload.getRemoteFile() : "[UNKNOWN]"), "File Size: "
        + (dload != null ? String.valueOf(dload.getTotalSize()) + " Bytes" : "[UNKNOWN]"));
  }

  private void initComponents(String title, String status, String fileSize) {
    GridBagLayout frameGbl, mpGbl, pbGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FRAME_SIZE);
    setTitle(title);

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

    //File Name Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    statusLbl = new JLabel(status);
    mpGbl.setConstraints(statusLbl, gbc);
    mainPanel.add(statusLbl);

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

    fileSizeLbl = new JLabel(fileSize);
    mpGbl.setConstraints(fileSizeLbl, gbc);
    mainPanel.add(fileSizeLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Current Size Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    curSizeLbl = new JLabel("Progress:");
    mpGbl.setConstraints(curSizeLbl, gbc);
    mainPanel.add(curSizeLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Transfer Rate Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    speedLbl = new JLabel("Avg. Transfer Rate:");
    mpGbl.setConstraints(speedLbl, gbc);
    mainPanel.add(speedLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Elapsed Time Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    elapsedTimeLbl = new JLabel("Elapsed Time:");
    mpGbl.setConstraints(elapsedTimeLbl, gbc);
    mainPanel.add(elapsedTimeLbl);

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
        sendAbort();
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

  public void windowOpened(WindowEvent we) {
    monitorThread.start();
  }

  public void windowClosed(WindowEvent we) {}

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  public void sendAbort() {
    statusLbl.setText("Aborting...");

    try {
      if (plugInManager != null) {
        if (dload != null) {
          if (dload.isReverse()) {
            abortReverseDownload();
          }
          else {
            sendAbortDownload();
          }
        }
      }
    }
    catch (Exception e) {
      RLErrorDialog.ShowError(e);
    }
  }

  private void sendAbortDownload() throws RogueNetException {
    P2PHubMessage mesg;
    Properties props;

    mesg = new P2PHubMessage();
    mesg.setSubject(FILE_STREAM_TRANSMITTER_SUBJECT);
    mesg.setRecipients(new String[] { dload.getP2phSessionToken() });

    props = new Properties();
    props.setProperty(FILE_SHARE_SERVICE_ACTION_PROP, FILE_SHARE_SERVICE_ACTION_GET_FILE_LIST);
    props.setProperty(SSO_SESSION_TOKEN_PROP, dload.getSsoSessionToken());
    props.setProperty(FILE_STREAM_TRANSMITTER_ACTION_PROP, FILE_STREAM_TRANSMITTER_ACTION_ABORT_TRANSFER);
    props.setProperty(FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP, FILE_TRANSFER_UTILITY_SUBJECT);
    props.setProperty(FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP, dload.getServerTransferId());
    props.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, dload.getClientTransferId());
    mesg.setProperties(props);

    plugInManager.getNetAgent().sendMessage(mesg);
  }

  private void abortReverseDownload() throws RogueNetException {
    FileStreamTransmitter fsTrans;
    P2PHubMessage mesg;
    Properties props;

    //Simulate Abort Message
    mesg = new P2PHubMessage();
    mesg.setSender(dload.getP2phSessionToken());
    mesg.setRecipients(new String[] { dload.getP2phSessionToken() });
    mesg.setSubject(dload.getReplySubject());

    props = new Properties();
    mesg.setProperties(props);

    props.setProperty(FILE_STREAM_TRANSMITTER_REPLY_SUBJECT_PROP, dload.getReplySubject());
    props.setProperty(FILE_STREAM_TRANSMITTER_SERVER_TRANSFER_ID_PROP, dload.getServerTransferId());
    props.setProperty(FILE_STREAM_TRANSMITTER_CLIENT_TRANSFER_ID_PROP, dload.getClientTransferId());
    props.setProperty(FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_PROP, FILE_STREAM_TRANSMITTER_PAYLOAD_TYPE_ACCESS_STATUS);
    props.setProperty(FILE_STREAM_TRANSMITTER_STATUS_PROP, FILE_STREAM_TRANSMITTER_STATUS_ABORTED);

    if (!StringUtils.IsNVL(dload.getRemoteFile())) {
      props.setProperty(FILE_STREAM_TRANSMITTER_REQUEST_FILE_PROP, dload.getRemoteFile());
    }

    fsTrans = (FileStreamTransmitter) plugInManager.getPlugIn(FileStreamTransmitter.class.getName());
    fsTrans.abortFileTransfer(mesg);
  }

}
