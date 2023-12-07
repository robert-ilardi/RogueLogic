/*
 Copyright 2007 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.roguelogic.sambuca;

import com.roguelogic.util.SystemUtils;

/**
 * This class contains Version and Application information for the Sambuca HTTP Framework.
 * 
 * @author Robert C. Ilardi
 *
 */

public class Version {

  public static final String APP_TITLE = "Sambuca Embedded-Java HTTP Server";
  public static final String VERSION = "1.0.5";

  public static final String APP_TITLE_SHORT = "Sambuca HTTP Server";

  public static final String SERVER_HEADER = new StringBuffer().append(APP_TITLE).append("/").append(VERSION).append(" (").append(SystemUtils.GetOperatingSystemName()).append(")").toString();

  public static final String ROGUELOGIC = "RogueLogic 3.5";
  public static final String RL_URL = "http://www.roguelogic.com";
  public static final String COPYRIGHT = "Copyright (c) 1999 - 2008 By: Robert C. Ilardi";

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
