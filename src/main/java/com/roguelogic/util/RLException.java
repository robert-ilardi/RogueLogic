/*
 * Created on Aug 22, 2005
 *
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
 * @author Administrator
 * 
 * RogueLogic's ROOT Application Exception. 
 */

public class RLException extends Exception {

  private int errorCode;

  public RLException() {
    super();
  }

  /**
   * @param mesg
   */
  public RLException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public RLException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public RLException(String mesg, Throwable t) {
    super(mesg, t);
  }

  /**
   * @return Returns the errorCode.
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   * @param errorCode The errorCode to set.
   */
  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

}
