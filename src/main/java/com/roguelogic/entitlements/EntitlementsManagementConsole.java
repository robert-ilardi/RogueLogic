/**
 * Created Oct 30, 2006
 */
package com.roguelogic.entitlements;

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
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.roguelogic.gui.RLErrorDialog;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class EntitlementsManagementConsole extends JFrame implements WindowListener {

  private EntitlementsController controller;

  //Main Components
  private JPanel mainPanel;
  private JTabbedPane tabs;
  private GridBagLayout frameGbl, mpGbl;

  //User Tab's Components
  private JPanel userControlPanel, userListPanel, userDetailsPanel, userButtonPanel;
  private JLabel usersLbl, usernameLbl, passwordLbl, confirmLbl;
  private JList userLst;
  private JTextField usernameTF;
  private JPasswordField passwordPF, confirmPF;
  private JScrollPane userLstSP;
  private JButton userUpdateButton, userDeleteButton, userClearButton, userRefreshButton, userDBMaintainButton;

  //Groups Tab's Components
  private JPanel groupControlPanel, groupListPanel, groupDetailsPanel, groupButtonPanel;
  private JLabel groupsLbl, groupCodeLbl, groupDescriptionLbl;
  private JList groupLst;
  private JTextField groupCodeTF, groupDescriptionTF;
  private JScrollPane groupLstSP;
  private JButton groupUpdateButton, groupDeleteButton, groupClearButton, groupRefreshButton, groupDBMaintainButton;

  //Items Tab's Components
  private JPanel itemControlPanel, itemListPanel, itemDetailsPanel, itemCheckBoxPanel, itemButtonPanel;
  private JLabel itemsLbl, itemCodeLbl, itemTypeLbl, itemDescriptionLbl, itemValueLbl;
  private JList itemLst;
  private JTextField itemCodeTF, itemTypeTF, itemDescriptionTF, itemValueTF;
  private JScrollPane itemLstSP;
  private JButton itemUpdateButton, itemDeleteButton, itemClearButton, itemRefreshButton, itemDBMaintainButton;
  private JCheckBox itemReadCHB, itemWriteCHB, itemExecuteCHB;

  //User-Groups Tab's Components
  private JPanel userGroupControlPanel, userGroupGroupListPanel, userGroupUserListPanel, userGroupGroupUsersListPanel, userGroupButtonPanel;
  private JLabel userGroupGroupsLbl, userGroupUsersLbl, userGroupGroupUsersLbl;
  private JList userGroupGroupLst, userGroupUserLst, userGroupGroupUsersLst;
  private JScrollPane userGroupGroupLstSP, userGroupUserLstSP, userGroupGroupUsersLstSP;
  private JButton userGroupAddButton, userGroupRemoveButton, userGroupRefreshButton, userGroupDBMaintainButton;

  //Group-Items Tab's Components
  private JPanel groupItemControlPanel, groupItemGroupListPanel, groupItemItemListPanel, groupItemGroupItemsListPanel, groupItemButtonPanel;
  private JLabel groupItemGroupsLbl, groupItemItemsLbl, groupItemGroupItemsLbl;
  private JList groupItemGroupLst, groupItemItemLst, groupItemGroupItemsLst;
  private JScrollPane groupItemGroupLstSP, groupItemItemLstSP, groupItemGroupItemsLstSP;
  private JButton groupItemAddButton, groupItemRemoveButton, groupItemRefreshButton, groupItemDBMaintainButton;

  private static final Dimension FRAME_SIZE = new Dimension(700, 500);
  private static final String MAIN_TITLE = "RL Entitlements Management Console";

  public EntitlementsManagementConsole(EntitlementsController controller) {
    super();

    this.controller = controller;

    addWindowListener(this);

    initComponents();
  }

  private void initComponents() {
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(FRAME_SIZE);
    super.setTitle(MAIN_TITLE);

    setResizable(false);
    setLocationRelativeTo(null); //Center on Screen

    frameGbl = new GridBagLayout();
    setLayout(frameGbl);

    paintMainPanelStart();

    paintUserControlPanel();

    paintGroupControlPanel();

    paintItemControlPanel();

    paintUserGroupControlPanel();

    paintGroupItemControlPanel();

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

    //Tabs
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.insets = new Insets(1, 1, 17, 4);
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;

    tabs = new JTabbedPane();
    mpGbl.setConstraints(tabs, gbc);
    mainPanel.add(tabs);
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

  private void paintUserControlPanel() {
    GridBagLayout ucpGbl, ulpGbl, udpGbl, ubpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ucpGbl = new GridBagLayout();
    userControlPanel = new JPanel(ucpGbl);
    userControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    userControlPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    mpGbl.setConstraints(userControlPanel, gbc);
    //mainPanel.add(userControlPanel);
    tabs.addTab("Users", userControlPanel);

    //User List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ulpGbl = new GridBagLayout();
    userListPanel = new JPanel(ulpGbl);
    userListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    userListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    ucpGbl.setConstraints(userListPanel, gbc);
    userControlPanel.add(userListPanel);

    //Users Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    usersLbl = new JLabel("Users:");
    ulpGbl.setConstraints(usersLbl, gbc);
    userListPanel.add(usersLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ulpGbl.setConstraints(blankPanel, gbc);
    userListPanel.add(blankPanel);

    //User List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    userLst = new JList(new DefaultListModel());
    userLstSP = new JScrollPane(userLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(250, 220);
    userLstSP.setPreferredSize(dSize);
    userLstSP.setMaximumSize(dSize);
    userLstSP.setMinimumSize(dSize);
    ulpGbl.setConstraints(userLstSP, gbc);
    userListPanel.add(userLstSP);
    userLst.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() >= 2) {
          if (userLst.getSelectedIndex() >= 0) {
            loadUserDetails(userLst.getSelectedValue().toString().trim());
          }
        }
      }

      public void mousePressed(MouseEvent evt) {}

      public void mouseReleased(MouseEvent evt) {}

      public void mouseEntered(MouseEvent evt) {}

      public void mouseExited(MouseEvent evt) {}
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ulpGbl.setConstraints(blankPanel, gbc);
    userListPanel.add(blankPanel);

    //Last Row (User List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ulpGbl.setConstraints(blankPanel, gbc);
    userListPanel.add(blankPanel);

    //User Details Panel------------------------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 2, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    udpGbl = new GridBagLayout();
    userDetailsPanel = new JPanel(udpGbl);
    userDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    userDetailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    ucpGbl.setConstraints(userDetailsPanel, gbc);
    userControlPanel.add(userDetailsPanel);

    //Username Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(30, 0, 5, 5);
    gbc.weightx = 0.0;

    usernameLbl = new JLabel("Username:");
    udpGbl.setConstraints(usernameLbl, gbc);
    userDetailsPanel.add(usernameLbl);

    //Username Text Field
    dSize = new Dimension(150, 20);
    usernameTF = new JTextField();
    usernameTF.setPreferredSize(dSize);
    udpGbl.setConstraints(usernameTF, gbc);
    userDetailsPanel.add(usernameTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    udpGbl.setConstraints(blankPanel, gbc);
    userDetailsPanel.add(blankPanel);

    //Password Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 0, 5, 5);
    gbc.weightx = 0.0;

    passwordLbl = new JLabel("Password:");
    udpGbl.setConstraints(passwordLbl, gbc);
    userDetailsPanel.add(passwordLbl);

    //Password Password Field
    dSize = new Dimension(150, 20);
    passwordPF = new JPasswordField();
    passwordPF.setPreferredSize(dSize);
    udpGbl.setConstraints(passwordPF, gbc);
    userDetailsPanel.add(passwordPF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    udpGbl.setConstraints(blankPanel, gbc);
    userDetailsPanel.add(blankPanel);

    //Password Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 0, 5, 5);
    gbc.weightx = 0.0;

    confirmLbl = new JLabel("Confirm:");
    udpGbl.setConstraints(confirmLbl, gbc);
    userDetailsPanel.add(confirmLbl);

    //Confirm Password Password Field
    dSize = new Dimension(150, 20);
    confirmPF = new JPasswordField();
    confirmPF.setPreferredSize(dSize);
    udpGbl.setConstraints(confirmPF, gbc);
    userDetailsPanel.add(confirmPF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    udpGbl.setConstraints(blankPanel, gbc);
    userDetailsPanel.add(blankPanel);

    //Last Row (User Details Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    udpGbl.setConstraints(blankPanel, gbc);
    userDetailsPanel.add(blankPanel);

    //End Row (User Control Panel)----------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ucpGbl.setConstraints(blankPanel, gbc);
    userControlPanel.add(blankPanel);

    //User Button Panel-------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ubpGbl = new GridBagLayout();
    userButtonPanel = new JPanel(ubpGbl);
    userButtonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    userButtonPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    ucpGbl.setConstraints(userButtonPanel, gbc);
    userControlPanel.add(userButtonPanel);

    //Start Row
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ubpGbl.setConstraints(blankPanel, gbc);
    userButtonPanel.add(blankPanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    //Update Button
    userUpdateButton = new JButton("Update");
    ubpGbl.setConstraints(userUpdateButton, gbc);
    userButtonPanel.add(userUpdateButton);
    userUpdateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ucpUpdateUser();
      }
    });

    //Delete Button
    userDeleteButton = new JButton("Delete");
    ubpGbl.setConstraints(userDeleteButton, gbc);
    userButtonPanel.add(userDeleteButton);
    userDeleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ucpDeleteUser();
      }
    });

    //Clear Button
    userClearButton = new JButton("Clear");
    ubpGbl.setConstraints(userClearButton, gbc);
    userButtonPanel.add(userClearButton);
    userClearButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ucpClear();
      }
    });

    //Refresh Button
    userRefreshButton = new JButton("Refresh");
    ubpGbl.setConstraints(userRefreshButton, gbc);
    userButtonPanel.add(userRefreshButton);
    userRefreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ucpRefreshUserList();
      }
    });

    //User DB Maintenance Button
    userDBMaintainButton = new JButton("DB Maintenance");
    ubpGbl.setConstraints(userDBMaintainButton, gbc);
    userButtonPanel.add(userDBMaintainButton);
    userDBMaintainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ucpUserDbMaintenance();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ubpGbl.setConstraints(blankPanel, gbc);
    userButtonPanel.add(blankPanel);

    //Last Row (User Button Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ubpGbl.setConstraints(blankPanel, gbc);
    userButtonPanel.add(blankPanel);

    //Last Row (User Control Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ucpGbl.setConstraints(blankPanel, gbc);
    userControlPanel.add(blankPanel);
  }

  private void paintGroupControlPanel() {
    GridBagLayout gcpGbl, glpGbl, gdpGbl, gbpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    gcpGbl = new GridBagLayout();
    groupControlPanel = new JPanel(gcpGbl);
    groupControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    groupControlPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    mpGbl.setConstraints(groupControlPanel, gbc);
    //mainPanel.add(groupControlPanel);
    tabs.addTab("Groups", groupControlPanel);

    //Group List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    glpGbl = new GridBagLayout();
    groupListPanel = new JPanel(glpGbl);
    groupListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    groupListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gcpGbl.setConstraints(groupListPanel, gbc);
    groupControlPanel.add(groupListPanel);

    //Groups Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    groupsLbl = new JLabel("Groups:");
    glpGbl.setConstraints(groupsLbl, gbc);
    groupListPanel.add(groupsLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    glpGbl.setConstraints(blankPanel, gbc);
    groupListPanel.add(blankPanel);

    //Group List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    groupLst = new JList(new DefaultListModel());
    groupLstSP = new JScrollPane(groupLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(250, 220);
    groupLstSP.setPreferredSize(dSize);
    groupLstSP.setMaximumSize(dSize);
    groupLstSP.setMinimumSize(dSize);
    glpGbl.setConstraints(groupLstSP, gbc);
    groupListPanel.add(groupLstSP);
    groupLst.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() >= 2) {
          if (groupLst.getSelectedIndex() >= 0) {
            loadGroupDetails(groupLst.getSelectedValue().toString().trim());
          }
        }
      }

      public void mousePressed(MouseEvent evt) {}

      public void mouseReleased(MouseEvent evt) {}

      public void mouseEntered(MouseEvent evt) {}

      public void mouseExited(MouseEvent evt) {}
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    glpGbl.setConstraints(blankPanel, gbc);
    groupListPanel.add(blankPanel);

    //Last Row (Group List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    glpGbl.setConstraints(blankPanel, gbc);
    groupListPanel.add(blankPanel);

    //Group Details Panel------------------------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 2, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    gdpGbl = new GridBagLayout();
    groupDetailsPanel = new JPanel(gdpGbl);
    groupDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    groupDetailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gcpGbl.setConstraints(groupDetailsPanel, gbc);
    groupControlPanel.add(groupDetailsPanel);

    //Group Code Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(30, 0, 5, 5);
    gbc.weightx = 0.0;

    groupCodeLbl = new JLabel("Group Code:");
    gdpGbl.setConstraints(groupCodeLbl, gbc);
    groupDetailsPanel.add(groupCodeLbl);

    //Group Code Text Field
    dSize = new Dimension(150, 20);
    groupCodeTF = new JTextField();
    groupCodeTF.setPreferredSize(dSize);
    gdpGbl.setConstraints(groupCodeTF, gbc);
    groupDetailsPanel.add(groupCodeTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gdpGbl.setConstraints(blankPanel, gbc);
    groupDetailsPanel.add(blankPanel);

    //Description Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 0, 5, 5);
    gbc.weightx = 0.0;

    groupDescriptionLbl = new JLabel("Description:");
    gdpGbl.setConstraints(groupDescriptionLbl, gbc);
    groupDetailsPanel.add(groupDescriptionLbl);

    //Description Text Field
    dSize = new Dimension(150, 20);
    groupDescriptionTF = new JTextField();
    groupDescriptionTF.setPreferredSize(dSize);
    gdpGbl.setConstraints(groupDescriptionTF, gbc);
    groupDetailsPanel.add(groupDescriptionTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gdpGbl.setConstraints(blankPanel, gbc);
    groupDetailsPanel.add(blankPanel);

    //Last Row (Group Details Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gdpGbl.setConstraints(blankPanel, gbc);
    groupDetailsPanel.add(blankPanel);

    //End Row (Group Control Panel)----------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gcpGbl.setConstraints(blankPanel, gbc);
    groupControlPanel.add(blankPanel);

    //Group Button Panel-------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    gbpGbl = new GridBagLayout();
    groupButtonPanel = new JPanel(gbpGbl);
    groupButtonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    groupButtonPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gcpGbl.setConstraints(groupButtonPanel, gbc);
    groupControlPanel.add(groupButtonPanel);

    //Start Row
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gbpGbl.setConstraints(blankPanel, gbc);
    groupButtonPanel.add(blankPanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    //Update Button
    groupUpdateButton = new JButton("Update");
    gbpGbl.setConstraints(groupUpdateButton, gbc);
    groupButtonPanel.add(groupUpdateButton);
    groupUpdateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gcpUpdateGroup();
      }
    });

    //Delete Button
    groupDeleteButton = new JButton("Delete");
    gbpGbl.setConstraints(groupDeleteButton, gbc);
    groupButtonPanel.add(groupDeleteButton);
    groupDeleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gcpDeleteGroup();
      }
    });

    //Clear Button
    groupClearButton = new JButton("Clear");
    gbpGbl.setConstraints(groupClearButton, gbc);
    groupButtonPanel.add(groupClearButton);
    groupClearButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gcpClear();
      }
    });

    //Refresh Button
    groupRefreshButton = new JButton("Refresh");
    gbpGbl.setConstraints(groupRefreshButton, gbc);
    groupButtonPanel.add(groupRefreshButton);
    groupRefreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gcpRefreshGroupList();
      }
    });

    //Group DB Maintenance Button
    groupDBMaintainButton = new JButton("DB Maintenance");
    gbpGbl.setConstraints(groupDBMaintainButton, gbc);
    groupButtonPanel.add(groupDBMaintainButton);
    groupDBMaintainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gcpGroupDbMaintenance();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gbpGbl.setConstraints(blankPanel, gbc);
    groupButtonPanel.add(blankPanel);

    //Last Row (Group Button Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gbpGbl.setConstraints(blankPanel, gbc);
    groupButtonPanel.add(blankPanel);

    //Last Row (Group Control Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gcpGbl.setConstraints(blankPanel, gbc);
    groupControlPanel.add(blankPanel);
  }

  private void paintItemControlPanel() {
    GridBagLayout icpGbl, ilpGbl, idpGbl, icbpGbl, ibpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    icpGbl = new GridBagLayout();
    itemControlPanel = new JPanel(icpGbl);
    itemControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    itemControlPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    mpGbl.setConstraints(itemControlPanel, gbc);
    //mainPanel.add(itemControlPanel);
    tabs.addTab("Items", itemControlPanel);

    //Item List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ilpGbl = new GridBagLayout();
    itemListPanel = new JPanel(ilpGbl);
    itemListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    itemListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    icpGbl.setConstraints(itemListPanel, gbc);
    itemControlPanel.add(itemListPanel);

    //Items Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    itemsLbl = new JLabel("Items:");
    ilpGbl.setConstraints(itemsLbl, gbc);
    itemListPanel.add(itemsLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ilpGbl.setConstraints(blankPanel, gbc);
    itemListPanel.add(blankPanel);

    //Item List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    itemLst = new JList(new DefaultListModel());
    itemLstSP = new JScrollPane(itemLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(250, 220);
    itemLstSP.setPreferredSize(dSize);
    itemLstSP.setMaximumSize(dSize);
    itemLstSP.setMinimumSize(dSize);
    ilpGbl.setConstraints(itemLstSP, gbc);
    itemListPanel.add(itemLstSP);
    itemLst.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() >= 2) {
          if (itemLst.getSelectedIndex() >= 0) {
            loadItemDetails(itemLst.getSelectedValue().toString().trim());
          }
        }
      }

      public void mousePressed(MouseEvent evt) {}

      public void mouseReleased(MouseEvent evt) {}

      public void mouseEntered(MouseEvent evt) {}

      public void mouseExited(MouseEvent evt) {}
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ilpGbl.setConstraints(blankPanel, gbc);
    itemListPanel.add(blankPanel);

    //Last Row (Item List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ilpGbl.setConstraints(blankPanel, gbc);
    itemListPanel.add(blankPanel);

    //Item Details Panel------------------------------------------------------>
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 2, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    idpGbl = new GridBagLayout();
    itemDetailsPanel = new JPanel(idpGbl);
    itemDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    itemDetailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    icpGbl.setConstraints(itemDetailsPanel, gbc);
    itemControlPanel.add(itemDetailsPanel);

    //Item Code Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(30, 0, 5, 5);
    gbc.weightx = 0.0;

    itemCodeLbl = new JLabel("Item Code:");
    idpGbl.setConstraints(itemCodeLbl, gbc);
    itemDetailsPanel.add(itemCodeLbl);

    //Item Code Text Field
    dSize = new Dimension(150, 20);
    itemCodeTF = new JTextField();
    itemCodeTF.setPreferredSize(dSize);
    idpGbl.setConstraints(itemCodeTF, gbc);
    itemDetailsPanel.add(itemCodeTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    idpGbl.setConstraints(blankPanel, gbc);
    itemDetailsPanel.add(blankPanel);

    //Item Type Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 0, 5, 5);
    gbc.weightx = 0.0;

    itemTypeLbl = new JLabel("Item Type:");
    idpGbl.setConstraints(itemTypeLbl, gbc);
    itemDetailsPanel.add(itemTypeLbl);

    //Item Type Text Field
    dSize = new Dimension(150, 20);
    itemTypeTF = new JTextField();
    itemTypeTF.setPreferredSize(dSize);
    idpGbl.setConstraints(itemTypeTF, gbc);
    itemDetailsPanel.add(itemTypeTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    idpGbl.setConstraints(blankPanel, gbc);
    itemDetailsPanel.add(blankPanel);

    //Description Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 0, 5, 5);
    gbc.weightx = 0.0;

    itemDescriptionLbl = new JLabel("Description:");
    idpGbl.setConstraints(itemDescriptionLbl, gbc);
    itemDetailsPanel.add(itemDescriptionLbl);

    //Description Text Field
    dSize = new Dimension(150, 20);
    itemDescriptionTF = new JTextField();
    itemDescriptionTF.setPreferredSize(dSize);
    idpGbl.setConstraints(itemDescriptionTF, gbc);
    itemDetailsPanel.add(itemDescriptionTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    idpGbl.setConstraints(blankPanel, gbc);
    itemDetailsPanel.add(blankPanel);

    //Value Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 0, 5, 5);
    gbc.weightx = 0.0;

    itemValueLbl = new JLabel("Value:");
    idpGbl.setConstraints(itemValueLbl, gbc);
    itemDetailsPanel.add(itemValueLbl);

    //Value Text Field
    dSize = new Dimension(150, 20);
    itemValueTF = new JTextField();
    itemValueTF.setPreferredSize(dSize);
    idpGbl.setConstraints(itemValueTF, gbc);
    itemDetailsPanel.add(itemValueTF);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    idpGbl.setConstraints(blankPanel, gbc);
    itemDetailsPanel.add(blankPanel);

    //Permission Check Box Panel
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 0, 5, 5);
    gbc.weightx = 0.0;

    icbpGbl = new GridBagLayout();
    itemCheckBoxPanel = new JPanel(icbpGbl);
    idpGbl.setConstraints(itemCheckBoxPanel, gbc);
    itemDetailsPanel.add(itemCheckBoxPanel);

    //Check Boxes
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 0, 5, 5);
    gbc.weightx = 0.0;

    itemReadCHB = new JCheckBox("Read");
    icbpGbl.setConstraints(itemReadCHB, gbc);
    itemCheckBoxPanel.add(itemReadCHB);

    itemWriteCHB = new JCheckBox("Write");
    icbpGbl.setConstraints(itemWriteCHB, gbc);
    itemCheckBoxPanel.add(itemWriteCHB);

    itemExecuteCHB = new JCheckBox("Execute");
    icbpGbl.setConstraints(itemExecuteCHB, gbc);
    itemCheckBoxPanel.add(itemExecuteCHB);

    //End Row (Check Box Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    icbpGbl.setConstraints(blankPanel, gbc);
    itemCheckBoxPanel.add(blankPanel);

    //End Row (Item Details Panel)
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    idpGbl.setConstraints(blankPanel, gbc);
    itemDetailsPanel.add(blankPanel);

    //Last Row (Item Details Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    idpGbl.setConstraints(blankPanel, gbc);
    itemDetailsPanel.add(blankPanel);

    //End Row (Item Control Panel)----------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    icpGbl.setConstraints(blankPanel, gbc);
    itemControlPanel.add(blankPanel);

    //Item Button Panel-------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ibpGbl = new GridBagLayout();
    itemButtonPanel = new JPanel(ibpGbl);
    itemButtonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    itemButtonPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    icpGbl.setConstraints(itemButtonPanel, gbc);
    itemControlPanel.add(itemButtonPanel);

    //Start Row
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ibpGbl.setConstraints(blankPanel, gbc);
    itemButtonPanel.add(blankPanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    //Update Button
    itemUpdateButton = new JButton("Update");
    ibpGbl.setConstraints(itemUpdateButton, gbc);
    itemButtonPanel.add(itemUpdateButton);
    itemUpdateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        icpUpdateItem();
      }
    });

    //Delete Button
    itemDeleteButton = new JButton("Delete");
    ibpGbl.setConstraints(itemDeleteButton, gbc);
    itemButtonPanel.add(itemDeleteButton);
    itemDeleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        icpDeleteItem();
      }
    });

    //Clear Button
    itemClearButton = new JButton("Clear");
    ibpGbl.setConstraints(itemClearButton, gbc);
    itemButtonPanel.add(itemClearButton);
    itemClearButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        icpClear();
      }
    });

    //Refresh Button
    itemRefreshButton = new JButton("Refresh");
    ibpGbl.setConstraints(itemRefreshButton, gbc);
    itemButtonPanel.add(itemRefreshButton);
    itemRefreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        icpRefreshItemList();
      }
    });

    //Refresh Button
    itemDBMaintainButton = new JButton("DB Maintenance");
    ibpGbl.setConstraints(itemDBMaintainButton, gbc);
    itemButtonPanel.add(itemDBMaintainButton);
    itemDBMaintainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        icpItemDbMaintenance();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ibpGbl.setConstraints(blankPanel, gbc);
    itemButtonPanel.add(blankPanel);

    //Last Row (Item Button Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ibpGbl.setConstraints(blankPanel, gbc);
    itemButtonPanel.add(blankPanel);

    //Last Row (Item Control Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    icpGbl.setConstraints(blankPanel, gbc);
    itemControlPanel.add(blankPanel);
  }

  private void paintUserGroupControlPanel() {
    GridBagLayout ugcpGbl, ugglpGbl, ugulpGbl, uggulpGbl, ugbpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ugcpGbl = new GridBagLayout();
    userGroupControlPanel = new JPanel(ugcpGbl);
    userGroupControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    userGroupControlPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    mpGbl.setConstraints(userGroupControlPanel, gbc);
    //mainPanel.add(userGroupControlPanel);
    tabs.addTab("User Groups", userGroupControlPanel);

    //User Groups Group List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ugglpGbl = new GridBagLayout();
    userGroupGroupListPanel = new JPanel(ugglpGbl);
    userGroupGroupListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    userGroupGroupListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    ugcpGbl.setConstraints(userGroupGroupListPanel, gbc);
    userGroupControlPanel.add(userGroupGroupListPanel);

    //User Group Group Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    userGroupGroupsLbl = new JLabel("Groups:");
    ugglpGbl.setConstraints(userGroupGroupsLbl, gbc);
    userGroupGroupListPanel.add(userGroupGroupsLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugglpGbl.setConstraints(blankPanel, gbc);
    userGroupGroupListPanel.add(blankPanel);

    //Group List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    userGroupGroupLst = new JList(new DefaultListModel());
    userGroupGroupLstSP = new JScrollPane(userGroupGroupLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(200, 220);
    userGroupGroupLstSP.setPreferredSize(dSize);
    userGroupGroupLstSP.setMaximumSize(dSize);
    userGroupGroupLstSP.setMinimumSize(dSize);
    ugglpGbl.setConstraints(userGroupGroupLstSP, gbc);
    userGroupGroupListPanel.add(userGroupGroupLstSP);
    userGroupGroupLst.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent evt) {
        if (userGroupGroupLst.getSelectedIndex() >= 0) {
          ugcpRefreshGroupUsersList();
        }
      }

      public void mousePressed(MouseEvent evt) {}

      public void mouseReleased(MouseEvent evt) {}

      public void mouseEntered(MouseEvent evt) {}

      public void mouseExited(MouseEvent evt) {}
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugglpGbl.setConstraints(blankPanel, gbc);
    userGroupGroupListPanel.add(blankPanel);

    //Last Row (User Groups Group List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugglpGbl.setConstraints(blankPanel, gbc);
    userGroupGroupListPanel.add(blankPanel);

    //User Groups User List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ugulpGbl = new GridBagLayout();
    userGroupUserListPanel = new JPanel(ugulpGbl);
    userGroupUserListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    userGroupUserListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    ugcpGbl.setConstraints(userGroupUserListPanel, gbc);
    userGroupControlPanel.add(userGroupUserListPanel);

    //User Group Group Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    userGroupUsersLbl = new JLabel("Users:");
    ugulpGbl.setConstraints(userGroupUsersLbl, gbc);
    userGroupUserListPanel.add(userGroupUsersLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugulpGbl.setConstraints(blankPanel, gbc);
    userGroupUserListPanel.add(blankPanel);

    //User List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    userGroupUserLst = new JList(new DefaultListModel());
    userGroupUserLstSP = new JScrollPane(userGroupUserLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(200, 220);
    userGroupUserLstSP.setPreferredSize(dSize);
    userGroupUserLstSP.setMaximumSize(dSize);
    userGroupUserLstSP.setMinimumSize(dSize);
    ugulpGbl.setConstraints(userGroupUserLstSP, gbc);
    userGroupUserListPanel.add(userGroupUserLstSP);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugulpGbl.setConstraints(blankPanel, gbc);
    userGroupUserListPanel.add(blankPanel);

    //Last Row (User Groups User List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugulpGbl.setConstraints(blankPanel, gbc);
    userGroupUserListPanel.add(blankPanel);

    //User Groups Group Users List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    uggulpGbl = new GridBagLayout();
    userGroupGroupUsersListPanel = new JPanel(uggulpGbl);
    userGroupGroupUsersListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    userGroupGroupUsersListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    ugcpGbl.setConstraints(userGroupGroupUsersListPanel, gbc);
    userGroupControlPanel.add(userGroupGroupUsersListPanel);

    //User Group Group Users Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    userGroupGroupUsersLbl = new JLabel("Group Users:");
    uggulpGbl.setConstraints(userGroupGroupUsersLbl, gbc);
    userGroupGroupUsersListPanel.add(userGroupGroupUsersLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    uggulpGbl.setConstraints(blankPanel, gbc);
    userGroupGroupUsersListPanel.add(blankPanel);

    //User List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    userGroupGroupUsersLst = new JList(new DefaultListModel());
    userGroupGroupUsersLstSP = new JScrollPane(userGroupGroupUsersLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(200, 220);
    userGroupGroupUsersLstSP.setPreferredSize(dSize);
    userGroupGroupUsersLstSP.setMaximumSize(dSize);
    userGroupGroupUsersLstSP.setMinimumSize(dSize);
    uggulpGbl.setConstraints(userGroupGroupUsersLstSP, gbc);
    userGroupGroupUsersListPanel.add(userGroupGroupUsersLstSP);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    uggulpGbl.setConstraints(blankPanel, gbc);
    userGroupGroupUsersListPanel.add(blankPanel);

    //Last Row (User Groups Group Users List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    uggulpGbl.setConstraints(blankPanel, gbc);
    userGroupGroupUsersListPanel.add(blankPanel);

    //End Row (User Group Control Panel)----------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugcpGbl.setConstraints(blankPanel, gbc);
    userGroupControlPanel.add(blankPanel);

    //User Groups Button Panel-------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    ugbpGbl = new GridBagLayout();
    userGroupButtonPanel = new JPanel(ugbpGbl);
    userGroupButtonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    userGroupButtonPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    ugcpGbl.setConstraints(userGroupButtonPanel, gbc);
    userGroupControlPanel.add(userGroupButtonPanel);

    //Start Row
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugbpGbl.setConstraints(blankPanel, gbc);
    userGroupButtonPanel.add(blankPanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    //Update Button
    userGroupAddButton = new JButton("Add");
    ugbpGbl.setConstraints(userGroupAddButton, gbc);
    userGroupButtonPanel.add(userGroupAddButton);
    userGroupAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ugcpAddLink();
      }
    });

    //Delete Button
    userGroupRemoveButton = new JButton("Remove");
    ugbpGbl.setConstraints(userGroupRemoveButton, gbc);
    userGroupButtonPanel.add(userGroupRemoveButton);
    userGroupRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ugcpRemoveUser();
      }
    });

    //Refresh Button
    userGroupRefreshButton = new JButton("Refresh");
    ugbpGbl.setConstraints(userGroupRefreshButton, gbc);
    userGroupButtonPanel.add(userGroupRefreshButton);
    userGroupRefreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ugcpRefreshLists();
      }
    });

    //User Group DB Maintenance Button
    userGroupDBMaintainButton = new JButton("DB Maintenance");
    ugbpGbl.setConstraints(userGroupDBMaintainButton, gbc);
    userGroupButtonPanel.add(userGroupDBMaintainButton);
    userGroupDBMaintainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ugcpUserGroupDbMaintenanceGroup();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugbpGbl.setConstraints(blankPanel, gbc);
    userGroupButtonPanel.add(blankPanel);

    //Last Row (Group Button Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugbpGbl.setConstraints(blankPanel, gbc);
    userGroupButtonPanel.add(blankPanel);

    //Last Row (Group Control Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    ugcpGbl.setConstraints(blankPanel, gbc);
    userGroupControlPanel.add(blankPanel);
  }

  private void paintGroupItemControlPanel() {
    GridBagLayout gicpGbl, giglpGbl, giilpGbl, gigilpGbl, gibpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    gicpGbl = new GridBagLayout();
    groupItemControlPanel = new JPanel(gicpGbl);
    groupItemControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    groupItemControlPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    mpGbl.setConstraints(groupItemControlPanel, gbc);
    //mainPanel.add(groupItemControlPanel);
    tabs.addTab("Group Items", groupItemControlPanel);

    //Group Items Group List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    giglpGbl = new GridBagLayout();
    groupItemGroupListPanel = new JPanel(giglpGbl);
    groupItemGroupListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    groupItemGroupListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gicpGbl.setConstraints(groupItemGroupListPanel, gbc);
    groupItemControlPanel.add(groupItemGroupListPanel);

    //Group Items Group Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    groupItemGroupsLbl = new JLabel("Groups:");
    giglpGbl.setConstraints(groupItemGroupsLbl, gbc);
    groupItemGroupListPanel.add(groupItemGroupsLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    giglpGbl.setConstraints(blankPanel, gbc);
    groupItemGroupListPanel.add(blankPanel);

    //Group List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    groupItemGroupLst = new JList(new DefaultListModel());
    groupItemGroupLstSP = new JScrollPane(groupItemGroupLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(200, 220);
    groupItemGroupLstSP.setPreferredSize(dSize);
    groupItemGroupLstSP.setMaximumSize(dSize);
    groupItemGroupLstSP.setMinimumSize(dSize);
    giglpGbl.setConstraints(groupItemGroupLstSP, gbc);
    groupItemGroupListPanel.add(groupItemGroupLstSP);
    groupItemGroupLst.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent evt) {
        if (groupItemGroupLst.getSelectedIndex() >= 0) {
          gicpRefreshGroupItemsList();
        }
      }

      public void mousePressed(MouseEvent evt) {}

      public void mouseReleased(MouseEvent evt) {}

      public void mouseEntered(MouseEvent evt) {}

      public void mouseExited(MouseEvent evt) {}
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    giglpGbl.setConstraints(blankPanel, gbc);
    groupItemGroupListPanel.add(blankPanel);

    //Last Row (Group Items Group List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    giglpGbl.setConstraints(blankPanel, gbc);
    groupItemGroupListPanel.add(blankPanel);

    //Group Items Item List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    giilpGbl = new GridBagLayout();
    groupItemItemListPanel = new JPanel(giilpGbl);
    groupItemItemListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    groupItemItemListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gicpGbl.setConstraints(groupItemItemListPanel, gbc);
    groupItemControlPanel.add(groupItemItemListPanel);

    //Group Items Items Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    groupItemItemsLbl = new JLabel("Items:");
    giilpGbl.setConstraints(groupItemItemsLbl, gbc);
    groupItemItemListPanel.add(groupItemItemsLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    giilpGbl.setConstraints(blankPanel, gbc);
    groupItemItemListPanel.add(blankPanel);

    //Item List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    groupItemItemLst = new JList(new DefaultListModel());
    groupItemItemLstSP = new JScrollPane(groupItemItemLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(200, 220);
    groupItemItemLstSP.setPreferredSize(dSize);
    groupItemItemLstSP.setMaximumSize(dSize);
    groupItemItemLstSP.setMinimumSize(dSize);
    giilpGbl.setConstraints(groupItemItemLstSP, gbc);
    groupItemItemListPanel.add(groupItemItemLstSP);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    giilpGbl.setConstraints(blankPanel, gbc);
    groupItemItemListPanel.add(blankPanel);

    //Last Row (Item Groups Item List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    giilpGbl.setConstraints(blankPanel, gbc);
    groupItemItemListPanel.add(blankPanel);

    //Item Groups Group Items List Panel-------------------------------------------------->
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    gigilpGbl = new GridBagLayout();
    groupItemGroupItemsListPanel = new JPanel(gigilpGbl);
    groupItemGroupItemsListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    groupItemGroupItemsListPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gicpGbl.setConstraints(groupItemGroupItemsListPanel, gbc);
    groupItemControlPanel.add(groupItemGroupItemsListPanel);

    //Group Items Group Items Label
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    groupItemGroupItemsLbl = new JLabel("Group Items:");
    gigilpGbl.setConstraints(groupItemGroupItemsLbl, gbc);
    groupItemGroupItemsListPanel.add(groupItemGroupItemsLbl);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gigilpGbl.setConstraints(blankPanel, gbc);
    groupItemGroupItemsListPanel.add(blankPanel);

    //Group Items List
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    groupItemGroupItemsLst = new JList(new DefaultListModel());
    groupItemGroupItemsLstSP = new JScrollPane(groupItemGroupItemsLst, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dSize = new Dimension(200, 220);
    groupItemGroupItemsLstSP.setPreferredSize(dSize);
    groupItemGroupItemsLstSP.setMaximumSize(dSize);
    groupItemGroupItemsLstSP.setMinimumSize(dSize);
    gigilpGbl.setConstraints(groupItemGroupItemsLstSP, gbc);
    groupItemGroupItemsListPanel.add(groupItemGroupItemsLstSP);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gigilpGbl.setConstraints(blankPanel, gbc);
    groupItemGroupItemsListPanel.add(blankPanel);

    //Last Row (Group Items Group Items List Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gigilpGbl.setConstraints(blankPanel, gbc);
    groupItemGroupItemsListPanel.add(blankPanel);

    //End Row (Group Items Control Panel)----------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gicpGbl.setConstraints(blankPanel, gbc);
    groupItemControlPanel.add(blankPanel);

    //Item Groups Button Panel-------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    gibpGbl = new GridBagLayout();
    groupItemButtonPanel = new JPanel(gibpGbl);
    groupItemButtonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    groupItemButtonPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gicpGbl.setConstraints(groupItemButtonPanel, gbc);
    groupItemControlPanel.add(groupItemButtonPanel);

    //Start Row
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gibpGbl.setConstraints(blankPanel, gbc);
    groupItemButtonPanel.add(blankPanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    //Update Button
    groupItemAddButton = new JButton("Add");
    gibpGbl.setConstraints(groupItemAddButton, gbc);
    groupItemButtonPanel.add(groupItemAddButton);
    groupItemAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gicpAddLink();
      }
    });

    //Delete Button
    groupItemRemoveButton = new JButton("Remove");
    gibpGbl.setConstraints(groupItemRemoveButton, gbc);
    groupItemButtonPanel.add(groupItemRemoveButton);
    groupItemRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gicpRemoveItem();
      }
    });

    //Refresh Button
    groupItemRefreshButton = new JButton("Refresh");
    gibpGbl.setConstraints(groupItemRefreshButton, gbc);
    groupItemButtonPanel.add(groupItemRefreshButton);
    groupItemRefreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gicpRefreshLists();
      }
    });

    //Item Group DB Maintenance Button
    groupItemDBMaintainButton = new JButton("DB Maintenance");
    gibpGbl.setConstraints(groupItemDBMaintainButton, gbc);
    groupItemButtonPanel.add(groupItemDBMaintainButton);
    groupItemDBMaintainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        gicpGroupItemDbMaintenanceGroup();
      }
    });

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gibpGbl.setConstraints(blankPanel, gbc);
    groupItemButtonPanel.add(blankPanel);

    //Last Row (Group Button Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gibpGbl.setConstraints(blankPanel, gbc);
    groupItemButtonPanel.add(blankPanel);

    //Last Row (Group Control Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    gicpGbl.setConstraints(blankPanel, gbc);
    groupItemControlPanel.add(blankPanel);
  }

  public void windowOpened(WindowEvent we) {
    ucpRefreshUserList();
    gcpRefreshGroupList();
    icpRefreshItemList();
    ugcpRefreshLists();
    gicpRefreshLists();
  }

  public void windowClosing(WindowEvent we) {}

  public void windowClosed(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

  public void setTitle(String title) {
    StringBuffer sb;

    if (!StringUtils.IsNVL(title)) {
      sb = new StringBuffer();
      sb.append(MAIN_TITLE);
      sb.append(" - ");
      sb.append(title.trim());

      super.setTitle(sb.toString());
    }
  }

  private synchronized void ucpRefreshUserList() {
    User[] users;
    DefaultListModel dlm;

    dlm = (DefaultListModel) userLst.getModel();
    dlm.clear();

    if (controller != null) {
      try {
        users = controller.listUsers();

        if (users != null && users.length > 0) {
          for (User user : users) {
            dlm.addElement(user.getUsername());
          }
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void ucpClear() {
    usernameTF.setText("");
    passwordPF.setText("");
    confirmPF.setText("");
  }

  private synchronized void ucpUpdateUser() {
    User user;
    String username, password, confirmPassword;

    if (controller != null) {
      try {
        username = usernameTF.getText().trim();
        password = String.valueOf(passwordPF.getPassword());
        confirmPassword = String.valueOf(confirmPF.getPassword());

        if (StringUtils.IsNVL(username)) {
          JOptionPane.showMessageDialog(this, "Please enter a username!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        if (StringUtils.IsNVL(password)) {
          JOptionPane.showMessageDialog(this, "Please enter a password!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        if (!password.equals(confirmPassword)) {
          JOptionPane.showMessageDialog(this, "Passwords must match!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          passwordPF.setText("");
          confirmPF.setText("");
          return;
        }

        user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCreated(new Date());
        user.setLastMod(new Date());
        user.setStatus(EntitlementsController.ENTRY_ACTIVE);

        if (controller.userExists(user)) {
          controller.updateUser(user);
        }
        else {
          controller.createUser(user);
        }

        ucpClear();
        ucpRefreshUserList();
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void ucpDeleteUser() {
    User user;
    String username;

    if (controller != null) {
      try {
        username = usernameTF.getText().trim();

        if (StringUtils.IsNVL(username)) {
          JOptionPane.showMessageDialog(this, "Please enter a username!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        user = new User();
        user.setUsername(username);

        controller.deleteUser(user);
        ucpClear();
        ucpRefreshUserList();
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void ucpUserDbMaintenance() {
    if (controller != null) {
      try {
        controller.preformUserDBMaintenance(null);
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void gcpRefreshGroupList() {
    Group[] groups;
    DefaultListModel dlm;

    dlm = (DefaultListModel) groupLst.getModel();
    dlm.clear();

    if (controller != null) {
      try {
        groups = controller.listGroups();

        if (groups != null && groups.length > 0) {
          for (Group group : groups) {
            dlm.addElement(group.getCode());
          }
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void gcpClear() {
    groupCodeTF.setText("");
    groupDescriptionTF.setText("");
  }

  private synchronized void gcpUpdateGroup() {
    Group group;
    String code, description;

    if (controller != null) {
      try {
        code = groupCodeTF.getText().trim();
        description = groupDescriptionTF.getText().trim();

        if (StringUtils.IsNVL(code)) {
          JOptionPane.showMessageDialog(this, "Please enter a Group Code!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        if (StringUtils.IsNVL(description)) {
          JOptionPane.showMessageDialog(this, "Please enter the description!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        group = new Group();
        group.setCode(code);
        group.setDescription(description);
        group.setCreated(new Date());
        group.setLastMod(new Date());
        group.setStatus(EntitlementsController.ENTRY_ACTIVE);

        if (controller.groupExists(group)) {
          controller.updateGroup(group);
        }
        else {
          controller.createGroup(group);
        }

        gcpClear();
        gcpRefreshGroupList();
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void gcpDeleteGroup() {
    Group group;
    String code;

    if (controller != null) {
      try {
        code = groupCodeTF.getText().trim();

        if (StringUtils.IsNVL(code)) {
          JOptionPane.showMessageDialog(this, "Please enter a Group Code!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        group = new Group();
        group.setCode(code);

        controller.deleteGroup(group);
        gcpClear();
        gcpRefreshGroupList();
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void gcpGroupDbMaintenance() {
    if (controller != null) {
      try {
        controller.preformGroupDBMaintenance(null);
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void icpRefreshItemList() {
    Item[] items;
    DefaultListModel dlm;

    dlm = (DefaultListModel) itemLst.getModel();
    dlm.clear();

    if (controller != null) {
      try {
        items = controller.listItems();

        if (items != null && items.length > 0) {
          for (Item item : items) {
            dlm.addElement(item.getCode());
          }
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void icpClear() {
    itemCodeTF.setText("");
    itemTypeTF.setText("");
    itemDescriptionTF.setText("");
    itemValueTF.setText("");
    itemReadCHB.setSelected(false);
    itemWriteCHB.setSelected(false);
    itemExecuteCHB.setSelected(false);
  }

  private synchronized void icpUpdateItem() {
    Item item;
    String code, type, description, value;
    boolean read, write, execute;

    if (controller != null) {
      try {
        code = itemCodeTF.getText().trim();
        type = itemTypeTF.getText().trim();
        description = itemDescriptionTF.getText().trim();
        value = itemValueTF.getText().trim();
        read = itemReadCHB.isSelected();
        write = itemWriteCHB.isSelected();
        execute = itemExecuteCHB.isSelected();

        if (StringUtils.IsNVL(code)) {
          JOptionPane.showMessageDialog(this, "Please enter an Item Code!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        if (StringUtils.IsNVL(description)) {
          JOptionPane.showMessageDialog(this, "Please enter the description!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        item = new Item();
        item.setCode(code);
        item.setDescription(description);
        item.setValue(value);
        item.setType(type);
        item.setRead(read);
        item.setWrite(write);
        item.setExecute(execute);
        item.setCreated(new Date());
        item.setLastMod(new Date());
        item.setStatus(EntitlementsController.ENTRY_ACTIVE);

        if (controller.itemExists(item)) {
          controller.updateItem(item);
        }
        else {
          controller.createItem(item);
        }

        icpClear();
        icpRefreshItemList();
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void icpDeleteItem() {
    Item item;
    String code;

    if (controller != null) {
      try {
        code = itemCodeTF.getText().trim();

        if (StringUtils.IsNVL(code)) {
          JOptionPane.showMessageDialog(this, "Please enter an Item Code!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
          return;
        }

        item = new Item();
        item.setCode(code);

        controller.deleteItem(item);
        icpClear();
        icpRefreshItemList();
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void icpItemDbMaintenance() {
    if (controller != null) {
      try {
        controller.preformItemDBMaintenance(null);
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void ugcpAddLink() {
    String username, groupCode;
    UserGroupLink link;

    if (userGroupGroupLst.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(this, "Please select a Group!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
      return;
    }

    if (userGroupUserLst.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(this, "Please select an User!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
      return;
    }

    groupCode = userGroupGroupLst.getSelectedValue().toString().trim();
    username = userGroupUserLst.getSelectedValue().toString().trim();

    if (controller != null) {
      try {
        link = new UserGroupLink();
        link.setUsername(username);
        link.setGroupCode(groupCode);
        link.setCreated(new Date());
        link.setLastMod(new Date());
        link.setStatus(EntitlementsController.ENTRY_ACTIVE);

        if (controller.userLinkExists(link)) {
          JOptionPane.showMessageDialog(this, "User '" + username + "' is already linked to Group '" + groupCode + "'!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
        }
        else {
          controller.linkUser(link);
          ugcpRefreshGroupUsersList();
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void ugcpRemoveUser() {
    String username, groupCode;
    UserGroupLink link;

    if (userGroupGroupLst.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(this, "Please select a Group!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
      return;
    }

    if (userGroupGroupUsersLst.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(this, "Please select a linked User!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
      return;
    }

    groupCode = userGroupGroupLst.getSelectedValue().toString().trim();
    username = userGroupGroupUsersLst.getSelectedValue().toString().trim();

    if (controller != null) {
      try {
        link = new UserGroupLink();
        link.setUsername(username);
        link.setGroupCode(groupCode);

        controller.unlinkUser(link);
        ugcpRefreshGroupUsersList();
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void ugcpRefreshLists() {
    ugcpRefreshGroupList();
    ugcpRefreshUserList();
    ugcpRefreshGroupUsersList();
  }

  private synchronized void ugcpRefreshGroupList() {
    Group[] groups;
    DefaultListModel dlm;

    dlm = (DefaultListModel) userGroupGroupLst.getModel();
    dlm.clear();

    if (controller != null) {
      try {
        groups = controller.listGroups();

        if (groups != null && groups.length > 0) {
          for (Group group : groups) {
            dlm.addElement(group.getCode());
          }
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void ugcpRefreshUserList() {
    User[] users;
    DefaultListModel dlm;

    dlm = (DefaultListModel) userGroupUserLst.getModel();
    dlm.clear();

    if (controller != null) {
      try {
        users = controller.listUsers();

        if (users != null && users.length > 0) {
          for (User user : users) {
            dlm.addElement(user.getUsername());
          }
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void ugcpRefreshGroupUsersList() {
    UserGroupLink[] links;
    DefaultListModel dlm;
    String selGroup;

    dlm = (DefaultListModel) userGroupGroupUsersLst.getModel();
    dlm.clear();

    if (userGroupGroupLst.getSelectedIndex() >= 0) {
      selGroup = userGroupGroupLst.getSelectedValue().toString().trim();

      if (controller != null) {
        try {
          links = controller.listUserGroupLinks();

          if (links != null && links.length > 0) {
            for (UserGroupLink link : links) {
              if (selGroup.equals(link.getGroupCode())) {
                dlm.addElement(link.getUsername());
              }
            }
          }
        } //End try block
        catch (Exception e) {
          RLErrorDialog.ShowError(e);
        }
      } //End null controller check
    } //End userGroupGroupLst select index test
  }

  private synchronized void ugcpUserGroupDbMaintenanceGroup() {
    if (controller != null) {
      try {
        controller.preformUserGroupDBMaintenance(null);
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check    
  }

  private synchronized void gicpAddLink() {
    String groupCode, itemCode;
    GroupItemLink link;

    if (groupItemGroupLst.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(this, "Please select a Group!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
      return;
    }

    if (groupItemItemLst.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(this, "Please select an Item!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
      return;
    }

    groupCode = groupItemGroupLst.getSelectedValue().toString().trim();
    itemCode = groupItemItemLst.getSelectedValue().toString().trim();

    if (controller != null) {
      try {
        link = new GroupItemLink();
        link.setItemCode(itemCode);
        link.setGroupCode(groupCode);
        link.setCreated(new Date());
        link.setLastMod(new Date());
        link.setStatus(EntitlementsController.ENTRY_ACTIVE);

        if (controller.itemLinkExists(link)) {
          JOptionPane.showMessageDialog(this, "Item '" + itemCode + "' is already linked to Group '" + groupCode + "'!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
        }
        else {
          controller.linkItem(link);
          gicpRefreshGroupItemsList();
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void gicpRemoveItem() {
    String itemCode, groupCode;
    GroupItemLink link;

    if (groupItemGroupLst.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(this, "Please select a Group!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
      return;
    }

    if (groupItemGroupItemsLst.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(this, "Please select a linked Item!", MAIN_TITLE, JOptionPane.ERROR_MESSAGE, new ImageIcon(this.getIconImage()));
      return;
    }

    groupCode = groupItemGroupLst.getSelectedValue().toString().trim();
    itemCode = groupItemGroupItemsLst.getSelectedValue().toString().trim();

    if (controller != null) {
      try {
        link = new GroupItemLink();
        link.setItemCode(itemCode);
        link.setGroupCode(groupCode);

        controller.unlinkItem(link);
        gicpRefreshGroupItemsList();
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void gicpRefreshLists() {
    gicpRefreshGroupList();
    gicpRefreshItemList();
    gicpRefreshGroupItemsList();
  }

  private synchronized void gicpRefreshGroupList() {
    Group[] groups;
    DefaultListModel dlm;

    dlm = (DefaultListModel) groupItemGroupLst.getModel();
    dlm.clear();

    if (controller != null) {
      try {
        groups = controller.listGroups();

        if (groups != null && groups.length > 0) {
          for (Group group : groups) {
            dlm.addElement(group.getCode());
          }
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void gicpRefreshItemList() {
    Item[] items;
    DefaultListModel dlm;

    dlm = (DefaultListModel) groupItemItemLst.getModel();
    dlm.clear();

    if (controller != null) {
      try {
        items = controller.listItems();

        if (items != null && items.length > 0) {
          for (Item item : items) {
            dlm.addElement(item.getCode());
          }
        }
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check
  }

  private synchronized void gicpRefreshGroupItemsList() {
    GroupItemLink[] links;
    DefaultListModel dlm;
    String selGroup;

    dlm = (DefaultListModel) groupItemGroupItemsLst.getModel();
    dlm.clear();

    if (groupItemGroupLst.getSelectedIndex() >= 0) {
      selGroup = groupItemGroupLst.getSelectedValue().toString().trim();

      if (controller != null) {
        try {
          links = controller.listGroupItemLinks();

          if (links != null && links.length > 0) {
            for (GroupItemLink link : links) {
              if (selGroup.equals(link.getGroupCode())) {
                dlm.addElement(link.getItemCode());
              }
            }
          }
        } //End try block
        catch (Exception e) {
          RLErrorDialog.ShowError(e);
        }
      } //End null controller check
    } //End groupItemGroupLst select index test
  }

  private synchronized void gicpGroupItemDbMaintenanceGroup() {
    if (controller != null) {
      try {
        controller.preformGroupItemDBMaintenance(null);
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controller check    
  }

  private synchronized void loadUserDetails(String username) {
    User user;

    ucpClear();

    if (controller != null) {
      try {
        user = new User();
        user.setUsername(username);

        user = controller.getUser(user, false);

        usernameTF.setText(user.getUsername());
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controll check
  }

  private synchronized void loadGroupDetails(String groupCode) {
    Group group;

    gcpClear();

    if (controller != null) {
      try {
        group = new Group();
        group.setCode(groupCode);

        group = controller.getGroup(group, false);

        groupCodeTF.setText(group.getCode());
        groupDescriptionTF.setText(group.getDescription());
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controll check
  }

  private synchronized void loadItemDetails(String itemCode) {
    Item item;

    icpClear();

    if (controller != null) {
      try {
        item = new Item();
        item.setCode(itemCode);

        item = controller.getItem(item);

        itemCodeTF.setText(item.getCode());
        itemTypeTF.setText(item.getType());
        itemDescriptionTF.setText(item.getDescription());
        itemValueTF.setText(item.getValue());
        itemReadCHB.setSelected(item.isRead());
        itemWriteCHB.setSelected(item.isWrite());
        itemExecuteCHB.setSelected(item.isExecute());
      } //End try block
      catch (Exception e) {
        RLErrorDialog.ShowError(e);
      }
    } //End null controll check
  }

}
