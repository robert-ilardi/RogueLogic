/*
 * Created on Sep 6, 2005
 */
package com.roguelogic.storage.sal;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author rilardi
 */

public class DevNullOutputStream extends OutputStream {

  public DevNullOutputStream() {
    super();
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  public void write(int arg0) throws IOException {}

}

