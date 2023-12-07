/**
 * Created Sep 20, 2007
 */
package com.roguelogic.storage.querylang;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class InputField implements QueryLangModelObject {

  private String tableHandle;
  private String name;

  public InputField() {}

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

    return sb.toString();
  }

}
