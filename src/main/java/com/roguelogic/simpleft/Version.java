package com.roguelogic.simpleft;

/**
 * This class contains Version and Application information for Off Site.
 * 
 * @author Robert C. Ilardi
 * 
 */

public class Version {

	public static final String APP_TITLE = "SimpleFT";
	public static final String VERSION = "1.0";

	public static final String ROGUELOGIC = "RogueLogic 3.5";
	public static final String RL_URL = "http://www.roguelogic.com";
	public static final String COPYRIGHT = "Copyright (c) 1999 - 2010 By: Robert C. Ilardi";

	public static String GetInfo() {
		StringBuffer sb = new StringBuffer();

		sb.append(APP_TITLE);
		sb.append("\n");

		sb.append("Version: ");
		sb.append(VERSION);
		sb.append("\n");

		sb.append(ROGUELOGIC);
		sb.append("\n");

		sb.append(RL_URL);
		sb.append("\n");

		sb.append(COPYRIGHT);
		sb.append("\n");

		sb.append("\n");

		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(GetInfo());
		System.exit(0);
	}

}
