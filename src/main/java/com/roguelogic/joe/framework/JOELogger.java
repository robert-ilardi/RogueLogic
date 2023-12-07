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

package com.roguelogic.joe.framework;

/**
 * An interface that represents a Sambuca based Logger.
 * Users can implement this interface to create specialized Loggers which can be plugged into the Sambuca kernel for custom logging of server events and request processing.
 * 
 * @author Robert C. Ilardi
 *
 */

public interface JOELogger {

  /**
   * Implementations of this interface MUST treat messages passed
   * as the parameter to this method as "Info" only. Non-Error Messages.
   * 
   * @param logMesg The LogMessage instance to be logged.
   */
  public void joeLogInfo(LogMessage logMesg);

  /**
   * Implementations of this interface MUST treat messages passed
   * as the parameter to this method as Error Messages which may 
   * or may not contain a throwable.
   * 
   * @param logMesg The LogMessage instance to be logged.
   */
  public void joeLogError(LogMessage logMesg);

}
