/**
 * Created Sep 14, 2007
 */
package com.roguelogic.storage.tablefile;

import java.io.Serializable;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class TFField implements Serializable {

  private FieldTypes type;
  private int length;
  private int index;
  private String name;

  private int startIndex;
  private int endIndex;

  public TFField() {}

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FieldTypes getType() {
    return type;
  }

  public void setType(FieldTypes type) {
    this.type = type;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public void setEndIndex(int endIndex) {
    this.endIndex = endIndex;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("Field: ");
    sb.append(name);
    sb.append("\n");

    sb.append("Index: ");
    sb.append(index);
    sb.append("\n");

    sb.append("Type: ");
    sb.append(type.getName());

    if (length > 0) {
      sb.append("(");
      sb.append(length);
      sb.append(")");
    }

    sb.append("\n");

    return sb.toString();
  }

  public boolean validateType(Object obj) {
    boolean valid = false;

    if (type == FieldTypes.Integer) {
      valid = obj instanceof Long || obj instanceof Integer || StringUtils.IsNumeric((String) obj);
    }
    else if (type == FieldTypes.Double) {
      valid = obj instanceof Double || StringUtils.IsDouble((String) obj);
    }
    else if (type == FieldTypes.String) {
      valid = obj instanceof String;
    }

    return valid;
  }

}
