/**
 * Created Jan 23, 2008
 */
package com.roguelogic.storage.jefs;

/**
 * @author Robert C. Ilardi
 *
 */

public enum JEFSPageType {
  Free(0, "Free Space", 'F'), Tracking(1, "Tracking", 'T'), Data(2, "Data", 'D');

  private int typeCode;
  private String typeName;
  private char typeIndicator;

  JEFSPageType(int typeCode, String typeName, char typeIndicator) {
    this.typeCode = typeCode;
    this.typeName = typeName;
    this.typeIndicator = typeIndicator;
  }

  public int getTypeCode() {
    return typeCode;
  }

  public String getTypeName() {
    return typeName;
  }

  public char getTypeIndicator() {
    return typeIndicator;
  }

}
