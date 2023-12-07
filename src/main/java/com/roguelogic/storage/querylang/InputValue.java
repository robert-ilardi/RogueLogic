/**
 * Created Sep 20, 2007
 */
package com.roguelogic.storage.querylang;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class InputValue implements QueryLangModelObject {

  public static final int TYPE_UNKNOWN = 0;
  public static final int TYPE_STRING_LITERAL = 1;
  public static final int TYPE_INTEGER_LITERAL = 2;
  public static final int TYPE_DOUBLE_LITERAL = 3;

  private int type;
  private String value;

  public InputValue() {}

  public static InputValue Parse(String stmt) {
    InputValue iValue = new InputValue();
    String tmp;

    if (stmt.charAt(0) == '\'') {
      tmp = stmt.substring(1, (stmt.charAt(stmt.length() - 1) == '\'' ? stmt.length() - 1 : stmt.length()));

      tmp = tmp.replaceAll("\\\\'", "'");

      tmp = tmp.replaceAll("\\\\\\\\", "\\\\");

      iValue.setValue(tmp);
      iValue.setType(TYPE_STRING_LITERAL);
    }
    else if (StringUtils.IsNumeric(stmt)) {
      iValue.setValue(stmt);
      iValue.setType(TYPE_INTEGER_LITERAL);
    }
    else if (StringUtils.IsDouble(stmt)) {
      iValue.setValue(stmt);
      iValue.setType(TYPE_DOUBLE_LITERAL);
    }
    else {
      iValue.setValue(stmt);
      iValue.setType(TYPE_UNKNOWN);
    }

    return iValue;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Object getValueAsTypeObject() {
    Object tObj = null;

    if (value != null) {
      switch (type) {
        case TYPE_STRING_LITERAL:
          tObj = value;
          break;
        case TYPE_INTEGER_LITERAL:
          tObj = new Integer(value.trim());
          break;
        case TYPE_DOUBLE_LITERAL:
          tObj = new Double(value.trim());
          break;
      }
    }

    return tObj;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append(value);

    return sb.toString();
  }

}
