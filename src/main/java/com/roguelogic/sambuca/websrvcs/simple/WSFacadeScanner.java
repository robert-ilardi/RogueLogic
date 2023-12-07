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

package com.roguelogic.sambuca.websrvcs.simple;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.roguelogic.sambuca.LogMessage;
import com.roguelogic.sambuca.SambucaLogger;

/**
 * @author Robert C. Ilardi
 *
 */

public class WSFacadeScanner {

  private SambucaLogger logger;

  private Class facade;

  public WSFacadeScanner(Class facade) {
    this.facade = facade;
  }

  public void setLogger(SambucaLogger logger) {
    this.logger = logger;
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

  public FacadeInventory scan() {
    FacadeInventory inventory = null;
    Method[] methods;
    Class[] params;

    print("Scanning Facade: " + facade.getName());

    inventory = new FacadeInventory(facade.getName());

    //Build Method inventory
    methods = facade.getMethods();

    for (Method m : methods) {
      //Ignore methods from super class Object and any non-public methods...
      if (!m.getDeclaringClass().equals(Object.class) && Modifier.isPublic(m.getModifiers())) {
        inventory.addMethod(m);

        //Get all parameters from the method signature...
        params = m.getParameterTypes();
        for (Class param : params) {

          if (param.isArray()) {
            inventory.addParameter(param.getComponentType());
          }
          else {
            inventory.addParameter(param);
          }
        }

        //Get return type
        if (m.getReturnType().isArray()) {
          inventory.addParameter(m.getReturnType().getComponentType());
        }
        else {
          inventory.addParameter(m.getReturnType());
        }
      }
    }

    return inventory;
  }

  public HashMap<String, Method> buildMethodMap() {
    HashMap<String, Method> methodMap = new HashMap<String, Method>();
    Method[] methods;
    String signatureKey;

    print("Building Method Map of Facade: " + facade.getName());

    //Build Method inventory
    methods = facade.getMethods();

    for (Method m : methods) {
      signatureKey = GetSignatureKey(m);
      methodMap.put(signatureKey, m);
    }

    return methodMap;
  }

  public static String GetSignatureKey(Method m) {
    StringBuffer signatureKey = new StringBuffer();
    Class[] params;

    signatureKey.append(m.getName());

    params = m.getParameterTypes();

    for (Class param : params) {
      signatureKey.append(",");

      if (param.isArray()) {
        signatureKey.append(param.getComponentType().getName());
        signatureKey.append("[]");
      }
      else {
        signatureKey.append(param.getName());
      }

    }

    return signatureKey.toString();
  }

}
