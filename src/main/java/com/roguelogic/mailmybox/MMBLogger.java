/**
 * Created Oct 23, 2008
 */

/*
 Copyright 2008 Robert C. Ilardi

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

package com.roguelogic.mailmybox;

/**
 * An interface that represents a Mail My Box based Logger.
 * 
 * @author Robert C. Ilardi
 * 
 */

public interface MMBLogger {

	/**
	 * Implementations of this interface MUST treat messages passed as the parameter to this method as "Info" only. Non-Error Messages.
	 * 
	 * @param logMesg
	 *          The LogMessage instance to be logged.
	 */
	public void mmbLogInfo(LogMessage logMesg);

	/**
	 * Implementations of this interface MUST treat messages passed as the parameter to this method as Error Messages which may or may not contain a throwable.
	 * 
	 * @param logMesg
	 *          The LogMessage instance to be logged.
	 */
	public void mmbLogError(LogMessage logMesg);

}
