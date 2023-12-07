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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.roguelogic.util.StringUtils;

/**
 * A simple file based HTTP Server Demo class.
 * 
 * @author Robert C. Ilardi
 *
 */

public class SambucaHttpServerDemo {

  public static final String SAMBUCA_MIME_PROP_FILE = "com/roguelogic/sambuca/sambuca_mimetypes.properties";

  public static void main(String[] args) {
    SambucaHttpServer httpServer = null;
    int exitCd, port;
    String wwwRoot, mtFile, slClass;
    SimpleDirServiceHandlerFactory factory;
    InputStream ins = null;
    Properties mtProps = null;
    SambucaLogger logger;

    if (args.length != 2) {
      System.err.println("Usage: java <-DMimeTypes=[MIME_TYPES.PROPERTIES]> <-DSambucaLogger=[CLASS_NAME]>" + SambucaHttpServerDemo.class.getName() + " [WWW_ROOT_DIR] [PORT]");
      exitCd = 1;
    }
    else {
      try {
        wwwRoot = args[0];
        port = Integer.parseInt(args[1]);

        mtFile = System.getProperty("MimeTypes");

        if (!StringUtils.IsNVL(mtFile)) {
          System.out.println("Using Mime Type Override File: " + mtFile);

          ins = new FileInputStream(mtFile);
          mtProps = new Properties();
          mtProps.load(ins);
          ins.close();
          ins = null;
        }
        else {
          System.out.println("Using Default Mime Type File from Classpath: " + SAMBUCA_MIME_PROP_FILE);

          ins = SambucaHttpServerDemo.class.getClassLoader().getResourceAsStream(SAMBUCA_MIME_PROP_FILE);
          mtProps = new Properties();
          mtProps.load(ins);
          ins.close();
          ins = null;
        }

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

        factory = new SimpleDirServiceHandlerFactory();
        factory.setWwwRoot(wwwRoot);
        factory.setIndexFile("index.htm");
        factory.setMimeTypes(mtProps);
        factory.setLogger(logger);

        httpServer.setHandlerFactory(factory);

        httpServer.listen();

        System.out.println("Serving Directory '" + factory.getWwwRoot() + "' on Port = " + httpServer.getPort());

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

        if (ins != null) {
          try {
            ins.close();
          }
          catch (Exception e) {}
        }
      }
    }

    System.exit(exitCd);
  }

}
