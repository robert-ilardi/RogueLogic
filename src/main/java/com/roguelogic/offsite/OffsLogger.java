package com.roguelogic.offsite;

/**
 * 
 * @author Robert C. Ilardi
 * 
 */

public interface OffsLogger {

	/**
	 * Implementations of this interface MUST treat messages passed as the parameter to this method as "Info" only. Non-Error Messages.
	 * 
	 * @param logMesg
	 *          The LogMessage instance to be logged.
	 */
	public void offsLogInfo(LogMessage logMesg);

	/**
	 * Implementations of this interface MUST treat messages passed as the parameter to this method as Error Messages which may or may not contain a throwable.
	 * 
	 * @param logMesg
	 *          The LogMessage instance to be logged.
	 */
	public void offsLogError(LogMessage logMesg);

}
