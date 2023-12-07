/**
 * Created Nov 3, 2007
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
 * The SambucaHttpServer uses an instnace of a ServiceHandlerFactory to create instances ServicesHandlers.
 * The reason why we have this factory intermmediate class instead of just passing the class name of the ServiceHandler Implementation
 * right to the SambucaHttpServer so the server itself could create new ServiceHandler instances is because, forcing users to define
 * a Factory outside of the SambucaHttpServer itself, allows users to create ServiceHandler instances that can be easily configured
 * by external code. Otherwise, the ServiceHandler implementations whould have to use roundabout ways to obtain references to data
 * and classes to configure itself, making the full initialization process of a ServiceHandler much more difficult.
 * 
 * Also, we do not just set a single instance of the ServiceHandler implementation into the Server directly as well since
 * a single instance will have possible threading and performance issues. It is possible for the server to clone this single instance
 * if cloneable was supported by the ServerHandler, but this would not obvious to users and may cause unforeseen issues when
 * developing custom ServiceHandler implementations. The Factory pattern just provides a cleaner picture to developer of what
 * the server is really doing.
 *   
 * Class that implement ServiceHandlerFactory will be used by the SambucaHttpServer instance to create new instances
 * of the User Defined ServiceHandler Implementation as needed by the server.
 * 
 * @author Robert C. Ilardi
 *
 */

public interface ServiceHandlerFactory {

  /**
   * Implementations of this method is used by the SambucaHttpServer to create ServiceHandler instances as needed.
   *   
   * @return An instance of the User Defined ServiceHandler Implementation. For most
   * implementations this instance SHOULD NOT be a singleton. See documentation on ServiceHandlers for more information. 
   * 
   */
  public ServiceHandler createHandler();

}
