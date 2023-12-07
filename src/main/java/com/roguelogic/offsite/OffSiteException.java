/**
 * Created Dec 13, 2008
 */
package com.roguelogic.offsite;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 * 
 */

public class OffSiteException extends RLException {

	public OffSiteException() {}

	/**
	 * @param mesg
	 */
	public OffSiteException(String mesg) {
		super(mesg);
	}

	/**
	 * @param t
	 */
	public OffSiteException(Throwable t) {
		super(t);
	}

	/**
	 * @param mesg
	 * @param t
	 */
	public OffSiteException(String mesg, Throwable t) {
		super(mesg, t);
	}

}
