/**
 * Created Nov 2, 2007
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
 * The interface which represents "business logic" processing classes.
 * Implementations of this interface are used to process incoming HTTP Requests and send reply HTTP Responses.
 * Classes implementing this method MUST contain the "business logic" or at least the entry point for the business logic
 * to process incoming HTTP Requests and write HTTP Responses to be sent back to the client. Implementations of this class *should not*
 * be singletons for performance considerations. Although some very specific implementations in environments with tight resources
 * may consider this type of implementation but should be aware of possible threading issues with the server.
 * 
 * @author Robert C. Ilardi
 *
 */

public interface ServiceHandler {

  /**
   * This method is the entry point by the Sambuca Server instance to pass the control over a complete incoming HTTP Request
   * to user defined code for processing and generation of HTTP Responses.
   * 
   * @param request The instance of the complete incoming HTTP Request read from the network.
   * @param response Essentially a wrapper around a Socket with helper methods to easily write HTTP Response data.
   * @throws SambucaException
   */
  public void handle(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException;

}
