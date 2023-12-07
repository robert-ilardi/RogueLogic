/**
 * Created: Oct 23, 2008 
 */
package com.roguelogic.mailmybox;

import com.roguelogic.util.RLException;

/**
 * @author Robert C. Ilardi
 * 
 */
public class MMBException extends RLException {

	public MMBException() {
	}

	/**
	 * @param mesg
	 */
	public MMBException(String mesg) {
		super(mesg);
	}

	/**
	 * @param t
	 */
	public MMBException(Throwable t) {
		super(t);
	}

	/**
	 * @param mesg
	 * @param t
	 */
	public MMBException(String mesg, Throwable t) {
		super(mesg, t);
	}

}
