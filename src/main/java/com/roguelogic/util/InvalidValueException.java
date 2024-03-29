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

public class InvalidValueException extends RLException {

  public InvalidValueException() {
    super();
  }

  public InvalidValueException(String mesg) {
    super(mesg);
  }

  public InvalidValueException(Throwable t) {
    super(t);
  }

  public InvalidValueException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
