/**
 * Created Nov 9, 2006
 */
package com.roguelogic.gui;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RLErrorDialog extends JDialog implements WindowListener {

  private static String AppDefaultTitle = null;

  public static final String DEFAULT_TITLE = "Application Error";
  public static final Dimension FRAME_SIZE = new Dimension(400, 300);

  private JButton okButton;
  private JLabel errorLbl;
  private JTextArea errorTA;
  private JScrollPane errorSP;

  private String errorDetails;

  public RLErrorDialog(String errorDetails) {
    super();

    this.errorDetails = errorDetails;

    addWindowListener(this);

    initComponents();

    errorTA.setText(this.errorDetails);
    errorTA.setCaretPosition(0);
  }

  public static void SetAppDefaultTitle(String adTitle) {
    AppDefaultTitle = adTitle;
  }

  private void initComponents() {
    JPanel mainPanel, blankPanel;
    GridBagLayout frameGbl, mpGbl;
    GridBagConstraints gbc;
    Dimension dSize;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FRAME_SIZE);

    if (!StringUtils.IsNVL(AppDefaultTitle)) {
      setTitle(AppDefaultTitle);
    }
    else {
      setTitle(DEFAULT_TITLE);
    }

    setResizable(false);
    setLocationRelativeTo(null); //Center on Screen

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

    frameGbl = new GridBagLayout();
    frameGbl.setConstraints(mainPanel, gbc);
    add(mainPanel);

    mpGbl = new GridBagLayout();
    mainPanel.setLayout(mpGbl);

    //Reset Constraints
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    //Error Label
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    errorLbl = new JLabel("Error Details:");
    mpGbl.setConstraints(errorLbl, gbc);
    mainPanel.add(errorLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Error Details Text Area
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    errorTA = new JTextArea();
    errorTA.setWrapStyleWord(true);
    errorTA.setLineWrap(true);
    errorTA.setEditable(false);

    dSize = new Dimension(390, 190);
    errorSP = new JScrollPane(errorTA, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    errorSP.setPreferredSize(dSize);
    errorSP.setMaximumSize(dSize);
    errorSP.setMinimumSize(dSize);

    mpGbl.setConstraints(errorSP, gbc);
    mainPanel.add(errorSP);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Start Row
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Ok Button
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    okButton = new JButton("OK");
    mpGbl.setConstraints(okButton, gbc);
    mainPanel.add(okButton);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });

    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

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

  public void windowOpened(WindowEvent we) {}

  public void windowClosing(WindowEvent we) {}

  public void windowClosed(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  public static void ShowError(Throwable t) {
    RLErrorDialog errDialog;

    try {
      errDialog = new RLErrorDialog(StringUtils.GetStackTraceString(t));
      errDialog.setModal(true);
      errDialog.setVisible(true);
    }
    finally {
      //Log Anyway...
      t.printStackTrace();
    }
  }

}
