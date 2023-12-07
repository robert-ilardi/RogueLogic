/**
 * Created Apr 5, 2008
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

package com.roguelogic.sambuca.websrvcs;

import com.roguelogic.sambuca.SambucaException;

/**
 * @author Robert C. Ilardi
 *
 */

public class SambucaWebServiceException extends SambucaException {

  public SambucaWebServiceException() {
    super();
  }

  /**
   * @param mesg
   */
  public SambucaWebServiceException(String mesg) {
    super(mesg);
  }

  /**
   * @param t
   */
  public SambucaWebServiceException(Throwable t) {
    super(t);
  }

  /**
   * @param mesg
   * @param t
   */
  public SambucaWebServiceException(String mesg, Throwable t) {
    super(mesg, t);
  }

}
