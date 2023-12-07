/**
 * Created Mar 31, 2009
 */
package com.roguelogic.simpleft;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 * 
 */

public class SimpleFtException extends RLException {

	public SimpleFtException() {}

	/**
	 * @param mesg
	 */
	public SimpleFtException(String mesg) {
		super(mesg);
	}

	/**
	 * @param t
	 */
	public SimpleFtException(Throwable t) {
		super(t);
	}

	/**
	 * @param mesg
	 * @param t
	 */
	public SimpleFtException(String mesg, Throwable t) {
		super(mesg, t);
	}

}
