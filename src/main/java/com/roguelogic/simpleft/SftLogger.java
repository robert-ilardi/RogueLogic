package com.roguelogic.simpleft;

/**
 * 
 * @author Robert C. Ilardi
 * 
 */

public interface SftLogger {

	/**
	 * Implementations of this interface MUST treat messages passed as the parameter to this method as "Info" only. Non-Error Messages.
	 * 
	 * @param logMesg
	 *          The LogMessage instance to be logged.
	 */
	public void sftLogInfo(LogMessage logMesg);

	/**
	 * Implementations of this interface MUST treat messages passed as the parameter to this method as Error Messages which may or may not contain a throwable.
	 * 
	 * @param logMesg
	 *          The LogMessage instance to be logged.
	 */
	public void sftLogError(LogMessage logMesg);

}
