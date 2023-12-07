/**
 * Created Feb 24, 2009
 */
package com.roguelogic.diskmirror;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 * 
 */

public class DiskMirrorException extends RLException {

	public DiskMirrorException() {}

	/**
	 * @param mesg
	 */
	public DiskMirrorException(String mesg) {
		super(mesg);
	}

	/**
	 * @param t
	 */
	public DiskMirrorException(Throwable t) {
		super(t);
	}

	/**
	 * @param mesg
	 * @param t
	 */
	public DiskMirrorException(String mesg, Throwable t) {
		super(mesg, t);
	}

}
