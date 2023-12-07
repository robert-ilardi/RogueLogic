/*
 Copyright 2008 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*
 * Created on Apr 11, 2008
 */
package com.roguelogic.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author rilardi
 */

public class JBean implements Serializable {

  private String packageName;
  private String name;
  private ArrayList<JBeanField> fields;

  public JBean() {}

  /**
   * @return Returns the packageName.
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * @param packageName The packageName to set.
   */
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  /**
   * @return Returns the fields.
   */
  public ArrayList<JBeanField> getFields() {
    return fields;
  }

  /**
   * @param fields The fields to set.
   */
  public void setFields(ArrayList<JBeanField> fields) {
    this.fields = fields;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  public void addField(JBeanField field) {
    fields.add(field);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[JBean - Name: ");
    sb.append(name);

    sb.append("; Package: ");
    sb.append(packageName);

    sb.append("]");

    if (fields != null) {
      for (JBeanField field : fields) {
        sb.append("\n  ");
        sb.append(field);
      }
    }

    return sb.toString();
  }

}
