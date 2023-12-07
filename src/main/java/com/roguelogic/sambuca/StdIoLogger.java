/**
 * Created Nov 10, 2007
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

package com.roguelogic.sambuca;

/**
 * A simple implementation of the SambucaLogger interface which uses System.out and System.err to log events.
 * 
 * @author Robert C. Ilardi
 *
 */

public class StdIoLogger implements SambucaLogger {

  public StdIoLogger() {}

  public void sambucaLogInfo(LogMessage logMesg) {
    System.out.println(logMesg);
  }

  public void sambucaLogError(LogMessage logMesg) {
    System.err.println(logMesg);
  }

}
