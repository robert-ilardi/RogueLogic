/**
 * Created Sep 28, 2006
 */
package com.roguelogic.roguenet.gui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.roguelogic.roguenet.RNAConstants;

/**
 * @author Robert C. Ilardi
 *
 */
public class NetExplorerAgentTableModel extends AbstractTableModel {

  private String[] columnNames = RNAConstants.RNA_SUMMARY_INFO_HEADER;
  private Vector<String[]> data;

  public NetExplorerAgentTableModel() {
    data = new Vector<String[]>();
  }

  public void addRow(String[] row) {
    data.add(row);
  }

  public int getColumnCount() {
    return columnNames.length;
  }

  public int getRowCount() {
    return data.size();
  }

  public String getColumnName(int col) {
    return columnNames[col];
  }

  public Object getValueAt(int row, int col) {
    String[] record;

    record = data.get(row);

    return record[col];
  }

  public void clear() {
    data.clear();
  }

}
