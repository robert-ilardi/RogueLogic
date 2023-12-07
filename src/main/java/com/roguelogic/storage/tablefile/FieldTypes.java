/**
 * Created Sep 14, 2007
 */
package com.roguelogic.storage.tablefile;

import java.util.HashMap;

/**
 * @author Robert C. Ilardi
 *
 */

public enum FieldTypes {
  String(100, "String"), Integer(200, "Integer"), Double(300, "Double");

  private int typeCode;
  private String name;

  private static HashMap<String, FieldTypes> TypeMap;

  static {
    TypeMap = new HashMap<String, FieldTypes>();

    TypeMap.put("STRING", String);
    TypeMap.put("INTEGER", Integer);
  }

  FieldTypes(int typeCode, String name) {
    this.typeCode = typeCode;
    this.name = name;
  }

  public int getTypeCode() {
    return typeCode;
  }

  public String getName() {
    return name;
  }

  public static FieldTypes GetFieldType(String name) {
    return TypeMap.get(name);
  }

}
