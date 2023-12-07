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

import com.roguelogic.sambuca.SambucaHttpServer;
import com.roguelogic.sambuca.SambucaLogger;
import com.roguelogic.sambuca.StdIoLogger;
import com.roguelogic.util.StringUtils;

/**
 * A simple file based HTTP Server Demo class.
 * 
 * @author Robert C. Ilardi
 *
 */

public class WebServiceAutoMounter {

  public static void main(String[] args) {
    SambucaHttpServer httpServer = null;
    int exitCd, port;
    String slClass, wsFacadeClassName;
    WSAutoMounterServiceHandlerFactory factory;
    SambucaLogger logger;

    if (args.length != 2) {
      System.err.println("Usage: java <-DSambucaLogger=[CLASS_NAME]> " + WebServiceAutoMounter.class.getName() + " [WEB_SERVICE_FACADE_CLASS_NAME] [PORT]");
      exitCd = 1;
    }
    else {
      try {
        wsFacadeClassName = args[0];
        port = Integer.parseInt(args[1]);

        slClass = System.getProperty("SambucaLogger");

        if (!StringUtils.IsNVL(slClass)) {
          logger = (SambucaLogger) Class.forName(slClass.trim()).newInstance();
        }
        else {
          logger = new StdIoLogger();
        }

        httpServer = new SambucaHttpServer();
        httpServer.setPort(port);
        httpServer.setLogger(logger);

        factory = new WSAutoMounterServiceHandlerFactory();
        factory.setWsFacadeClassName(wsFacadeClassName);
        factory.setLogger(logger);
        factory.mountFacade();

        httpServer.setHandlerFactory(factory);

        httpServer.listen();

        System.out.println("Serving Facade Class '" + factory.getWsFacadeClassName() + "' as Web Service on Port = " + httpServer.getPort());

        httpServer.waitWhileListening();

        exitCd = 0;
      } //End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
      finally {
        if (httpServer != null) {
          try {
            httpServer.shutdown();
            httpServer = null;
          }
          catch (Exception e) {}
        }
      }
    }

    System.exit(exitCd);
  }

}
