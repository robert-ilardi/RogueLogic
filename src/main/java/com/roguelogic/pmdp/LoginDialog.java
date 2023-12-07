/**
 * Created Nov 26, 2007
 */
package com.roguelogic.pmdp;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.pmd.PMDClient;

/**
 * @author Robert C. Ilardi
 *
 */

public class LoginDialog extends JDialog {

  private JPanel mainPanel, cmdButtonPanel;
  private JLabel usernameLbl, passwordLbl, addrLbl, portLbl;
  private JTextField usernameTF, addressTF, portTF;
  private JPasswordField passwordPF;
  private JButton loginButton, cancelButton;

  private PMDClient client;

  private static final Dimension FRAME_SIZE = new Dimension(350, 200);

  public LoginDialog(JFrame parent, PMDClient client) {
    super(parent);

    this.client = client;

    initComponents();
  }

  private void initComponents() {
    GridBagLayout frameGbl, mpGbl, cmdButtonGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FRAME_SIZE);
    setTitle("Single Sign On");

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

    //Address Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    addrLbl = new JLabel("Address:");
    mpGbl.setConstraints(addrLbl, gbc);
    mainPanel.add(addrLbl);

    gbc.weightx = 2.0;
    addressTF = new JTextField();
    addressTF.setPreferredSize(new Dimension(100, 20));
    addressTF.setMinimumSize(new Dimension(100, 20));
    mpGbl.setConstraints(addressTF, gbc);
    mainPanel.add(addressTF);

    gbc.weightx = 0.0;
    portLbl = new JLabel("Port:");
    mpGbl.setConstraints(portLbl, gbc);
    mainPanel.add(portLbl);

    gbc.weightx = 1.0;
    portTF = new JTextField();
    portTF.setPreferredSize(new Dimension(40, 20));
    mpGbl.setConstraints(portTF, gbc);
    mainPanel.add(portTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Username Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    usernameLbl = new JLabel("Username:");
    mpGbl.setConstraints(usernameLbl, gbc);
    mainPanel.add(usernameLbl);

    usernameTF = new JTextField();
    mpGbl.setConstraints(usernameTF, gbc);
    mainPanel.add(usernameTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Password Row------------------------------------>
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    passwordLbl = new JLabel("Password:");
    mpGbl.setConstraints(passwordLbl, gbc);
    mainPanel.add(passwordLbl);

    passwordPF = new JPasswordField();
    passwordPF.setEchoChar('*');
    mpGbl.setConstraints(passwordPF, gbc);
    mainPanel.add(passwordPF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Command Button Row------------------------------------>
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(10, 5, 5, 5);
    gbc.weightx = 0.0;

    cmdButtonGbl = new GridBagLayout();
    cmdButtonPanel = new JPanel(cmdButtonGbl);
    mpGbl.setConstraints(cmdButtonPanel, gbc);
    mainPanel.add(cmdButtonPanel);

    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    cmdButtonGbl.setConstraints(blankPanel, gbc);
    cmdButtonPanel.add(blankPanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 7, 5, 8);
    gbc.weightx = 0.0;

    loginButton = new JButton("Login");
    cmdButtonGbl.setConstraints(loginButton, gbc);
    cmdButtonPanel.add(loginButton);

    loginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        login();
      }
    });

    cancelButton = new JButton("Cancel");
    cmdButtonGbl.setConstraints(cancelButton, gbc);
    cmdButtonPanel.add(cancelButton);

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    cmdButtonGbl.setConstraints(blankPanel, gbc);
    cmdButtonPanel.add(blankPanel);

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

  private void login() {
    try {
      client.close();
      client.connect(addressTF.getText().trim(), Integer.parseInt(portTF.getText().trim()));
      client.login(usernameTF.getText().trim(), String.valueOf(passwordPF.getPassword()).trim());
    }
    catch (Exception e) {
      RLErrorDialog.ShowError(e);
    }
    finally {
      dispose();
    }
  }

}
