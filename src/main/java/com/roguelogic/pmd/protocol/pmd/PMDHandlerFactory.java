/**
 * Created Nov 21, 2007
 */
package com.roguelogic.pmd.protocol.pmd;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * @author Robert C. Ilardi
 *
 */

public class PMDHandlerFactory implements URLStreamHandlerFactory {

  public PMDHandlerFactory() {}

  /* (non-Javadoc)
   * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
   */
  public URLStreamHandler createURLStreamHandler(String protocol) {
    if (Handler.PROTOCOL.equalsIgnoreCase(protocol)) {
      return new Handler();
    }
    else {
      return null;
    }
  }

}
