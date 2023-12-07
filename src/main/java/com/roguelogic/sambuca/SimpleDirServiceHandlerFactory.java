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

import java.util.Properties;

/**
 * A simple ServiceHandlerFactory Demo class used by the SambucaHttpServerDemo to create SimpleDirServiceHandlers.
 * 
 * @author Robert C. Ilardi
 *
 */

public class SimpleDirServiceHandlerFactory implements ServiceHandlerFactory {

  private String wwwRoot;
  private String indexFile;
  private Properties mimeTypes;
  private SambucaLogger logger;

  public SimpleDirServiceHandlerFactory() {}

  public String getWwwRoot() {
    return wwwRoot;
  }

  public void setWwwRoot(String wwwRoot) {
    this.wwwRoot = wwwRoot;
  }

  public String getIndexFile() {
    return indexFile;
  }

  public void setIndexFile(String indexFile) {
    this.indexFile = indexFile;
  }

  public void setMimeTypes(Properties mimeTypes) {
    this.mimeTypes = mimeTypes;
  }

  public Properties getMimeTypes() {
    return mimeTypes;
  }

  public SambucaLogger getLogger() {
    return logger;
  }

  public void setLogger(SambucaLogger logger) {
    this.logger = logger;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.sambuca.ServiceHandlerFactory#createHandler()
   */
  public ServiceHandler createHandler() {
    SimpleDirServiceHandler handler;

    handler = new SimpleDirServiceHandler();
    handler.setWwwRoot(wwwRoot);
    handler.setIndexFile(indexFile);
    handler.setMimeTypes(mimeTypes);
    handler.setLogger(logger);

    return handler;
  }

}
