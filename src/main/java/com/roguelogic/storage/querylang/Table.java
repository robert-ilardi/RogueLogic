/**
 * Created Sep 19, 2007
 */
package com.roguelogic.storage.querylang;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class Table implements QueryLangModelObject {

  private String name;
  private String alias;

  public Table() {}

  public static Table Parse(String s) {
    Table tab = new Table();
    String[] tmpArr;

    if (s.indexOf(" ") != -1) {
      tmpArr = s.split(" ", 2);
      tmpArr = StringUtils.Trim(tmpArr);

      tab.setName(tmpArr[0]);
      tab.setAlias(tmpArr[1]);
    }
    else {
      tab.setName(s);
    }

    return tab;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append(name);

    if (!StringUtils.IsNVL(alias)) {
      sb.append("(");
      sb.append(alias);
      sb.append(")");
    }

    return sb.toString();
  }

}
