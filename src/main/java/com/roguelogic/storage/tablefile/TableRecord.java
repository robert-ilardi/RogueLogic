/**
 * Created Sep 18, 2007
 */
package com.roguelogic.storage.tablefile;

import java.io.Serializable;
import java.util.HashMap;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class TableRecord implements Serializable {

  public static final byte SPACE_BYTE = " ".getBytes()[0];

  private String[] fields;
  private Object[] values;

  private long position = -1;

  public TableRecord() {}

  public String[] getFields() {
    return fields;
  }

  public void setFields(String[] fields) {
    this.fields = fields;
  }

  public Object[] getValues() {
    return values;
  }

  public void setValues(Object[] values) {
    this.values = values;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("Record[");

    if (fields != null) {
      for (int i = 0; i < fields.length; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(fields[i]);
      }
    }

    sb.append("]{");

    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          sb.append("; ");
        }
        sb.append(values[i]);
      }
    }

    sb.append("}");

    if (position != -1) {
      sb.append(" ; Position = ");
      sb.append(position);
    }

    return sb.toString();
  }

  public byte[] toByteArray(TableDefinition tabDef) throws TableFileException {
    byte[] bRec, tmp;
    TFField fDef;
    String fName, val;

    bRec = new byte[tabDef.getRecordLen()];
    SystemUtils.FillArray(bRec, SPACE_BYTE);

    for (int i = 0; i < fields.length && i < values.length; i++) {
      fName = fields[i].toUpperCase();
      fDef = tabDef.getField(fName);

      val = values[i].toString();
      val = StringUtils.RPad(val, ' ', fDef.getLength());
      tmp = val.getBytes();

      if (tmp.length > fDef.getLength()) {
        throw new TableFileException("Value for Field '" + fName + "' is too long! Expected Size = " + fDef.getLength() + " ; Actual Size = " + tmp.length);
      }
      else {
        System.arraycopy(tmp, 0, bRec, fDef.getStartIndex(), fDef.getLength());
      }
    } //End for i loop through fields

    return bRec;
  }

  public static TableRecord GetRecord(TableDefinition def, String line, boolean dataOnly) {
    TableRecord rec = new TableRecord();
    String[] fields = null;
    TFField[] tfFields;
    Object[] values;

    tfFields = def.getFields();

    if (tfFields != null) {
      if (!dataOnly) {
        fields = new String[tfFields.length];
      }

      values = new Object[tfFields.length];

      rec.setFields(fields);
      rec.setValues(values);

      for (int i = 0; i < tfFields.length; i++) {
        if (!dataOnly) {
          fields[i] = tfFields[i].getName();
        }

        if (tfFields[i].getType() == FieldTypes.String) {
          values[i] = line.substring(tfFields[i].getStartIndex(), tfFields[i].getEndIndex()).trim();
        }
        else if (tfFields[i].getType() == FieldTypes.Integer) {
          values[i] = new Integer(line.substring(tfFields[i].getStartIndex(), tfFields[i].getEndIndex()).trim());
        }
        else if (tfFields[i].getType() == FieldTypes.Double) {
          values[i] = new Double(line.substring(tfFields[i].getStartIndex(), tfFields[i].getEndIndex()).trim());
        }
      }
    }

    return rec;
  }

  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

  public void update(HashMap<String, Object> updateMap) {
    Object val;

    for (int i = 0; i < fields.length; i++) {
      if (updateMap.containsKey(fields[i])) {
        val = updateMap.get(fields[i]);
        values[i] = val;
      }
    }
  }

}
