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

package com.roguelogic.util;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class JBeanDescriptionXMLParser extends DefaultHandler {

  public static final String ELMT_JBEAN_DESCRIBER = "RL-JBEAN-DESCRIBER";
  public static final String ELMT_JBEAN = "JBEAN";
  public static final String ELMT_FIELD = "FIELD";

  public static final String ATTR_NAME = "name";
  public static final String ATTR_PACKAGE = "package";
  public static final String ATTR_DATA_TYPE = "data-type";
  public static final String ATTR_IS_ARRAY = "is-array";
  public static final String ATTR_SETTER = "setter";
  public static final String ATTR_GETTER = "getter";

  private ArrayList<JBean> jBeans;
  private JBean curBean;

  public JBeanDescriptionXMLParser() {
    super();
  }

  public ArrayList<JBean> getJBeans() {
    return jBeans;
  }

  public void startDocument() {
    jBeans = null;
  }

  public void endDocument() {}

  public void startElement(String uri, String localName, String qName, Attributes attrs) {
    String elementName = (uri == null || uri.trim().length() == 0 ? qName : localName);
    JBeanField jbf;

    //System.out.println("!!!!!!!!!!!!!> Processing Element: " + elementName);

    if (ELMT_JBEAN_DESCRIBER.equalsIgnoreCase(elementName)) {
      jBeans = new ArrayList<JBean>();
    }
    else if (ELMT_JBEAN.equalsIgnoreCase(elementName)) {
      curBean = new JBean();
      curBean.setFields(new ArrayList<JBeanField>());

      curBean.setPackageName(attrs.getValue(ATTR_PACKAGE));
      curBean.setName(attrs.getValue(ATTR_NAME));

      jBeans.add(curBean);
    }
    else if (ELMT_FIELD.equalsIgnoreCase(elementName)) {
      jbf = new JBeanField();

      jbf.setName(attrs.getValue(ATTR_NAME));
      jbf.setDataType(attrs.getValue(ATTR_DATA_TYPE));
      jbf.setArray("TRUE".equalsIgnoreCase(attrs.getValue(ATTR_IS_ARRAY)));
      jbf.setSetter(attrs.getValue(ATTR_SETTER));
      jbf.setGetter(attrs.getValue(ATTR_GETTER));

      curBean.addField(jbf);
    }
  }

  public void endElement(String uri, String localName, String qName) {}

}
