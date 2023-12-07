/**
 * Created Aug 18, 2008
 */
package com.roguelogic.clustering.rfile;

import java.io.File;
import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class RDirEntry implements Serializable {

  public static final int FILE_ENTRY = 0;
  public static final int DIRECTORY_ENTRY = 1;

  private String name;
  private int type;

  public RDirEntry(File f) {
    name = f.getName();
    type = (f.isFile() ? FILE_ENTRY : DIRECTORY_ENTRY);
  }

  public RDirEntry() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public static String GetEntryTypeName(int type) {
    String tyNm = null;

    if (type == FILE_ENTRY) {
      tyNm = "FILE";
    }
    else if (type == DIRECTORY_ENTRY) {
      tyNm = "DIRECTORY";
    }
    else {
      tyNm = "UNKNOWN";
    }

    return tyNm;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[RemoteDirEntry - Name: ");
    sb.append(name);
    sb.append(" ; Type: ");
    sb.append(GetEntryTypeName(type));

    return sb.toString();
  }

}
