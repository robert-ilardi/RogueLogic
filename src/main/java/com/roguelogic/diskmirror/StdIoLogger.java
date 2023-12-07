package com.roguelogic.diskmirror;

public class StdIoLogger implements DmLogger {

	public StdIoLogger() {}

	public void offsLogInfo(LogMessage logMesg) {
		System.out.println(logMesg);
	}

	public void offsLogError(LogMessage logMesg) {
		System.err.println(logMesg);
	}

}
