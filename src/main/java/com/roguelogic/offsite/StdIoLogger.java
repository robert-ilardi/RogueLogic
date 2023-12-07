package com.roguelogic.offsite;

public class StdIoLogger implements OffsLogger {

	public StdIoLogger() {}

	public void offsLogInfo(LogMessage logMesg) {
		System.out.println(logMesg);
	}

	public void offsLogError(LogMessage logMesg) {
		System.err.println(logMesg);
	}

}
