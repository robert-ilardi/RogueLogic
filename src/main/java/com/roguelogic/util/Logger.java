/*
 * Created on Aug 26, 2005
 */

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

package com.roguelogic.util;

/**
 * @author rilardi
 */

public class Logger {

  private static Logger LoggerInstance = null;

  private Logger() {}

  public static synchronized Logger GetInstance() {
    if (LoggerInstance == null) {
      LoggerInstance = new Logger();
    }

    return LoggerInstance;
  }

  public void info(String mesg) {
    System.out.println(mesg);
  }

  public void warn(String mesg) {
    StringBuffer sb = new StringBuffer("WARNING: ");
    sb.append(mesg);
    System.err.println(sb.toString());
    sb = null;
  }

  public void error(String mesg) {
    StringBuffer sb = new StringBuffer("ERROR: ");
    sb.append(mesg);
    System.err.println(sb.toString());
    sb = null;
  }

  public void debug(String mesg) {
    System.out.println(mesg);
  }

}
