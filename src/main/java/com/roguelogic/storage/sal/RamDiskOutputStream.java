/*
 * Created on Aug 31, 2005
 */
package com.roguelogic.storage.sal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * @author rilardi
 */

public class RamDiskOutputStream extends ByteArrayOutputStream {

  private BinHolder file;

  public RamDiskOutputStream() {
    super();
  }

  /**
   * @return Returns the binHolder.
   */
  public BinHolder getFile() {
    return file;
  }

  /**
   * @param file The file to set.
   */
  public void setFile(BinHolder file) {
    this.file = file;
  }

  public void close() throws IOException {
    file.setBin(toByteArray());
    super.close();
  }

}

