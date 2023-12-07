/**
 * Created Oct 3, 2006
 */
package com.roguelogic.roguenet.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * @author Robert C. Ilardi
 *
 */

public class PropertiesTableDialog extends JFrame implements WindowListener {

  private Properties props;
  private String ptdTitle;

  private JPanel mainPanel;
  private JTable propTable;
  private PropertiesTableModel propTableModel;
  private JScrollPane propTablePane;
  private JButton closeButton;

  private Dimension frameSize = new Dimension(400, 300);

  public PropertiesTableDialog(Properties props, String ptdTitle, Image icon) {
    super();

    this.props = props;
    this.ptdTitle = ptdTitle;

    addWindowListener(this);

    setIconImage(icon);

    initComponents();
  }

  private void initComponents() {
    GridBagLayout frameGbl, mpGbl;
    GridBagConstraints gbc;
    JPanel blankPanel;
    Dimension dSize;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(frameSize);
    setTitle(ptdTitle);

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

    //Reset everything
    gbc.gridheight = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    //Row 1------------------------------------>
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 10, 10, 5);
    gbc.weightx = 0.0;

    dSize = new Dimension((int) frameSize.getWidth(), 200);
    propTableModel = new PropertiesTableModel();
    propTable = new JTable(propTableModel);
    propTablePane = new JScrollPane();
    propTablePane.setPreferredSize(dSize);
    propTablePane.setMaximumSize(dSize);
    propTablePane.setMinimumSize(dSize);
    propTable.setPreferredScrollableViewportSize(propTablePane.getPreferredSize());
    propTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    propTablePane.setViewportView(propTable);

    mpGbl.setConstraints(propTablePane, gbc);
    mainPanel.add(propTablePane);

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

    //Left Padding
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Close Button
    gbc.weightx = 0.0;
    closeButton = new JButton("Close");
    mpGbl.setConstraints(closeButton, gbc);
    mainPanel.add(closeButton);

    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evnt) {
        PropertiesTableDialog.this.dispose();
      }
    });

    //Right Padding
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
  }

  public void windowOpened(WindowEvent we) {
    try {
      populateTable();
      defaultHeaderWidths();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void populateTable() {
    Iterator iter;
    String name, value;
    int cnt;

    propTableModel.clear();

    if (props != null) {
      iter = props.keySet().iterator();

      while (iter.hasNext()) {
        name = (String) iter.next();

        if (!filteredOut(name)) {
          value = props.getProperty(name);

          propTableModel.addRow(new String[] { name, value });
        }
      }
    }

    cnt = propTableModel.getRowCount();
    propTableModel.fireTableRowsInserted(cnt - 1, cnt);
  }

  protected boolean filteredOut(String propName) {
    return false;
  }

  protected void defaultHeaderWidths() {
    String tmp;
    FontMetrics fm;
    int width, maxWidth;
    TableColumn tc;

    fm = getGraphics().getFontMetrics();

    for (int i = 0; i < propTable.getColumnCount(); i++) {
      tc = propTable.getColumn(propTable.getColumnName(i));
      maxWidth = 0;

      for (int j = 0; j < propTable.getModel().getRowCount(); j++) {
        tmp = (String) propTable.getModel().getValueAt(j, i);

        width = fm.stringWidth(tmp) + (2 * propTable.getColumnModel().getColumnMargin() + 5);

        if (width > maxWidth) {
          maxWidth = width;
        }
      }

      tc.setPreferredWidth(Math.max(maxWidth, 150));
    }
  }

  public void windowClosed(WindowEvent we) {}

  public void windowClosing(WindowEvent we) {}

  public void windowIconified(WindowEvent we) {}

  public void windowDeiconified(WindowEvent we) {}

  public void windowActivated(WindowEvent we) {}

  public void windowDeactivated(WindowEvent we) {}

}
