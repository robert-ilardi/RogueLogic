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

/**
 * @author rilardi
 */

public class JBeanField implements Serializable {

  private String name;
  private String dataType;
  private boolean array;
  private String setter;
  private String getter;

  public JBeanField() {}

  /**
   * @return Returns the dataType.
   */
  public String getDataType() {
    return dataType;
  }

  /**
   * @param dataType The dataType to set.
   */
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  /**
   * @return Returns the getter.
   */
  public String getGetter() {
    return getter;
  }

  /**
   * @param getter The getter to set.
   */
  public void setGetter(String getter) {
    this.getter = getter;
  }

  /**
   * @return Returns the isArray.
   */
  public boolean isArray() {
    return array;
  }

  /**
   * @param isArray The isArray to set.
   */
  public void setArray(boolean isArray) {
    this.array = isArray;
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

  /**
   * @return Returns the setter.
   */
  public String getSetter() {
    return setter;
  }

  /**
   * @param setter The setter to set.
   */
  public void setSetter(String setter) {
    this.setter = setter;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[JBeanField - Name: ");
    sb.append(name);

    sb.append("; DataType: ");
    sb.append(dataType);

    sb.append("; IsArray: ");
    sb.append((array ? "YES" : "NO"));

    sb.append("; Setter: ");
    sb.append(setter);

    sb.append("; Getter: ");
    sb.append(getter);

    sb.append("]");

    return sb.toString();
  }

}
