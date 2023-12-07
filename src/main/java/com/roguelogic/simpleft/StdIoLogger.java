package com.roguelogic.simpleft;

public class StdIoLogger implements SftLogger {

	public StdIoLogger() {}

	public void sftLogInfo(LogMessage logMesg) {
		System.out.println(logMesg);
	}

	public void sftLogError(LogMessage logMesg) {
		System.err.println(logMesg);
	}

}
