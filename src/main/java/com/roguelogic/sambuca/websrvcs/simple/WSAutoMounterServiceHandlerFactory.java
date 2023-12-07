/**
 * Created Apr 3, 2008
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

package com.roguelogic.sambuca.websrvcs.simple;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.roguelogic.sambuca.LogMessage;
import com.roguelogic.sambuca.SambucaLogger;
import com.roguelogic.sambuca.ServiceHandler;
import com.roguelogic.sambuca.ServiceHandlerFactory;
import com.roguelogic.sambuca.websrvcs.SambucaWebServiceException;

/**
 * The ServiceHandlerFactory implementation used by the WebServiceAutoMounter to create WSAutoMounterServiceHandlers.
 * 
 * @author Robert C. Ilardi
 *
 */

public class WSAutoMounterServiceHandlerFactory implements ServiceHandlerFactory {

  private String wsFacadeClassName;
  private SambucaLogger logger;

  private Class wsFacadeClass;
  private FacadeInventory inventory;
  private HashMap<String, Method> methodMap;

  public WSAutoMounterServiceHandlerFactory() {}

  public String getWsFacadeClassName() {
    return wsFacadeClassName;
  }

  public void setWsFacadeClassName(String wsFacadeClassName) {
    this.wsFacadeClassName = wsFacadeClassName;
  }

  public SambucaLogger getLogger() {
    return logger;
  }

  public void setLogger(SambucaLogger logger) {
    this.logger = logger;
  }

  public void mountFacade() throws SambucaWebServiceException {
    WSFacadeScanner scanner;

    try {
      print("Mounting Web Service Facade...");

      print("Loading Facade Class: " + wsFacadeClassName);
      wsFacadeClass = Class.forName(wsFacadeClassName);
      wsFacadeClass.newInstance(); //Check if class can be instanciated...

      scanner = new WSFacadeScanner(wsFacadeClass);
      scanner.setLogger(logger);

      inventory = scanner.scan();
      methodMap = scanner.buildMethodMap();

      print("Facade Inventory XML Descriptor:\n" + inventory.getXMLDescriptor());
    } //End try block
    catch (Exception e) {
      throw new SambucaWebServiceException("An error occurred while attempting to mount the web service Facade. Class = '" + wsFacadeClassName + "'. System Message: " + e.getMessage(), e);
    }
  }

  private void print(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.sambucaLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.sambuca.ServiceHandlerFactory#createHandler()
   */
  public ServiceHandler createHandler() {
    WSAutoMounterServiceHandler handler;

    handler = new WSAutoMounterServiceHandler();
    handler.setWsFacadeClass(wsFacadeClass);
    handler.setLogger(logger);
    handler.setInventory(inventory);
    handler.setMethodMap(methodMap);

    return handler;
  }

}
