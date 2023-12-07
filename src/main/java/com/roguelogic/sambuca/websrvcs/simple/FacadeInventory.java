/**
 * Created Apr 5, 2008
 */

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

package com.roguelogic.sambuca.websrvcs.simple;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Robert C. Ilardi
 *
 */

public class FacadeInventory {

  private String facadeClassName;

  private ArrayList<Method> methods;
  private HashSet<Class> parameters;

  public FacadeInventory(String facadeClassName) {
    this.facadeClassName = facadeClassName;

    methods = new ArrayList<Method>();
    parameters = new HashSet<Class>();
  }

  public String getFacadeClassName() {
    return facadeClassName;
  }

  public void addMethod(Method m) {
    methods.add(m);
  }

  public ArrayList<Method> getMethods() {
    return methods;
  }

  public void addParameter(Class c) {
    parameters.add(c);
  }

  public HashSet<Class> getParameters() {
    return parameters;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    Class[] params;

    sb.append("[FacadeInventory - ClassName=");
    sb.append(facadeClassName);
    sb.append("] {\n");

    sb.append("  Methods:\n");

    for (Method mthd : methods) {
      sb.append("    ");

      sb.append(mthd.getReturnType().getName());

      sb.append(" ");
      sb.append(mthd.getName());
      sb.append("(");

      params = mthd.getParameterTypes();
      for (int i = 0; i < params.length; i++) {
        if (i > 0) {
          sb.append(", ");
        }

        if (params[i].isArray()) {
          sb.append(params[i].getComponentType().getName());
          sb.append("[]");
        }
        else {
          sb.append(params[i].getName());
        }
      }

      sb.append(");\n");
    }

    sb.append("  Parameters:\n");
    for (Class param : parameters) {
      sb.append("    ");
      sb.append(param.getName());
      sb.append("\n");
    }

    sb.append("}");

    return sb.toString();
  }

  public String getXMLDescriptor() {
    StringBuffer xml = new StringBuffer();
    Class[] params;

    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

    xml.append("<Facade-Inventory facade=\"");
    xml.append(facadeClassName);
    xml.append("\" version=\"");
    xml.append(Version.VERSION);
    xml.append("\">\n");

    xml.append("\t<Methods>\n");

    for (Method mthd : methods) {
      xml.append("\t\t<Method name=\"");
      xml.append(mthd.getName());

      xml.append("\" return-type=\"");
      if (mthd.getReturnType().isArray()) {
        xml.append(mthd.getReturnType().getComponentType().getName());
      }
      else {
        xml.append(mthd.getReturnType().getName());
      }

      xml.append("\" return-type-is-array=\"");
      xml.append(mthd.getReturnType().isArray() ? "TRUE" : "FALSE");

      xml.append("\" signature-key=\"");
      xml.append(WSFacadeScanner.GetSignatureKey(mthd));
      xml.append("\">\n");

      params = mthd.getParameterTypes();

      for (int i = 0; i < params.length; i++) {
        xml.append("\t\t\t<Parameter index=\"");
        xml.append(i + 1);

        xml.append("\" type=\"");

        if (params[i].isArray()) {
          xml.append(params[i].getComponentType().getName());
        }
        else {
          xml.append(params[i].getName());
        }

        xml.append("\" is-array=\"");
        xml.append(params[i].isArray() ? "TRUE" : "FALSE");
        xml.append("\"/>\n");
      } //End params for i loop

      xml.append("\t\t</Method>\n");
    } //End method for each loop

    xml.append("\t</Methods>\n");

    xml.append("</Facade-Inventory>\n");

    return xml.toString();
  }

}
