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

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class InventoryXMLParser extends DefaultHandler {

  public static final String ELMT_FACADE_INVENTORY = "FACADE-INVENTORY";
  public static final String ELMT_METHOD = "METHOD";
  public static final String ELMT_PARAMETER = "PARAMETER";

  public static final String ATTR_FACADE = "facade";
  public static final String ATTR_VERSION = "version";
  public static final String ATTR_NAME = "name";
  public static final String ATTR_RETURN_TYPE = "return-type";
  public static final String ATTR_RETURN_TYPE_IS_ARRAY = "return-type-is-array";
  public static final String ATTR_TYPE = "type";
  public static final String ATTR_IS_ARRAY = "is-array";
  public static final String ATTR_SIGNATURE_KEY = "signature-key";

  private ClientInventory inventory;
  private WSMethod curMethod;

  public InventoryXMLParser() {
    super();
  }

  public ClientInventory getInventory() {
    return inventory;
  }

  public void startDocument() {
    inventory = null;
  }

  public void endDocument() {}

  public void startElement(String uri, String localName, String qName, Attributes attrs) {
    String elementName = (uri == null || uri.trim().length() == 0 ? qName : localName);
    WSParameter param;

    //System.out.println("!!!!!!!!!!!!!> Processing Element: " + elementName);

    if (ELMT_FACADE_INVENTORY.equalsIgnoreCase(elementName)) {
      inventory = new ClientInventory();

      inventory.setFacadeName(attrs.getValue(ATTR_FACADE));
      inventory.setVersion(attrs.getValue(ATTR_VERSION));
    }
    else if (ELMT_METHOD.equalsIgnoreCase(elementName)) {
      curMethod = new WSMethod();
      inventory.addMethod(curMethod);

      curMethod.setName(attrs.getValue(ATTR_NAME));

      param = new WSParameter();
      param.setTypeName(attrs.getValue(ATTR_RETURN_TYPE));
      param.setArray("TRUE".equalsIgnoreCase(attrs.getValue(ATTR_RETURN_TYPE_IS_ARRAY)));

      curMethod.setReturnType(param);

      curMethod.setSignatureKey(attrs.getValue(ATTR_SIGNATURE_KEY));
    }
    else if (ELMT_PARAMETER.equalsIgnoreCase(elementName)) {
      param = new WSParameter();
      param.setTypeName(attrs.getValue(ATTR_TYPE));
      param.setArray("TRUE".equalsIgnoreCase(attrs.getValue(ATTR_IS_ARRAY)));

      curMethod.addParameter(param);
    }
  }

  public void endElement(String uri, String localName, String qName) {}

}
