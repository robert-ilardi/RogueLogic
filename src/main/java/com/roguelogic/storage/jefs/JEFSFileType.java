/**
 * Created Jan 23, 2008
 */
package com.roguelogic.storage.jefs;

/**
 * @author Robert C. Ilardi
 *
 */

public enum JEFSFileType {
  Directory(0, "Directory", 'D'), File(1, "File", 'F'), Deletion(2, "Deletion", 'X');

  private int typeCode;
  private String typeName;
  private char typeIndicator;

  JEFSFileType(int typeCode, String typeName, char typeIndicator) {
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
