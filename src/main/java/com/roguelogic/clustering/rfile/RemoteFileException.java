/**
 * Created Aug 15, 2008
 */
package com.roguelogic.clustering.rfile;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 *
 */

public class RemoteFileException extends RLException {

  public RemoteFileException() {
    super();
  }

  /**
   * @param mesg
   */
  public RemoteFileException(String mesg) {
    super(mesg);

  }

  /**
   * @param t
   */
  public RemoteFileException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public RemoteFileException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
