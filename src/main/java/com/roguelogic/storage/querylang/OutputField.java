/**
 * Created Sep 19, 2007
 */
package com.roguelogic.storage.querylang;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class OutputField implements QueryLangModelObject {

  private String tableHandle;
  private String name;
  private String label;

  public OutputField() {}

  public static OutputField Parse(String s) {
    OutputField oField = new OutputField();
    String[] tmpArr;

    if (s.indexOf(".") != -1) {
      tmpArr = s.split("\\.", 2);
      tmpArr = StringUtils.Trim(tmpArr);

      oField.setTableHandle(tmpArr[0]);
      oField.setName(tmpArr[1]);
    }
    else {
      oField.setName(s);
    }

    return oField;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTableHandle() {
    return tableHandle;
  }

  public void setTableHandle(String tableHandle) {
    this.tableHandle = tableHandle;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    if (!StringUtils.IsNVL(tableHandle)) {
      sb.append(tableHandle);
      sb.append("->");
    }

    sb.append(name);

    if (!StringUtils.IsNVL(label)) {
      sb.append(" AS '");
      sb.append(label);
      sb.append("'");
    }

    return sb.toString();
  }

}
