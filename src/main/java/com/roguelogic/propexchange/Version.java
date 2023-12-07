package com.roguelogic.propexchange;

public class Version {

  public static final String APP_TITLE_SERVER = "Properties Exchange Server";
  public static final String APP_TITLE_CLIENT = "Properties Exchange Client";
  public static final String VERSION = "1.0";

  public static final String ROGUELOGIC = "RogueLogic 3.0";
  public static final String RL_URL = "http://www.roguelogic.com";
  public static final String COPYRIGHT = "Copyright (c) 1999 - 2006 By: Robert C. Ilardi";

  public static String GetInfo() {
    StringBuffer sb = new StringBuffer();

    sb.append(APP_TITLE_SERVER);
    sb.append("\n");

    sb.append(APP_TITLE_CLIENT);
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
