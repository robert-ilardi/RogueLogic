/**
 * Created Sep 18, 2007
 */
package com.roguelogic.storage.tablefile;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class TableRecordSet implements Serializable {

  private TableRecord[] records;
  private TFField[] fields;

  public TableRecordSet() {}

  public TableRecord[] getRecords() {
    return records;
  }

  public void setRecords(TableRecord[] records) {
    this.records = records;
  }

  public TFField[] getFields() {
    return fields;
  }

  public void setFields(TFField[] fields) {
    this.fields = fields;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    Object[] values;

    if (fields != null) {
      for (int i = 0; i < fields.length; i++) {
        if (i > 0) {
          sb.append("\t");
        }

        sb.append(fields[i].getName().toUpperCase().trim());
      }
    }

    if (records != null) {
      sb.append("\n");

      for (int i = 0; i < records.length; i++) {
        if (i > 0) {
          sb.append("\n");
        }

        values = records[i].getValues();
        if (values != null)
          for (int j = 0; j < values.length; j++) {
            if (j > 0) {
              sb.append("\t");
            }

            sb.append((values[j] != null ? values[j].toString().trim() : "NULL"));
          }
      }
    }

    return sb.toString();
  }

}
