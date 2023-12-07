/*
 * Created on Oct 26, 2005
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

public class RLRuntimeException extends RuntimeException {

  public RLRuntimeException() {
    super();
  }

  /**
   * @param mesg
   */
  public RLRuntimeException(String mesg) {
    super(mesg);
  }

  /**
   * @param mesg
   * @param t
   */
  public RLRuntimeException(String mesg, Throwable t) {
    super(mesg, t);
  }

  /**
   * @param t
   */
  public RLRuntimeException(Throwable t) {
    super(t);
  }

}
