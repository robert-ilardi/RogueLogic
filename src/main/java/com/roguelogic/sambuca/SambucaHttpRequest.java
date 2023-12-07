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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class represents an HTTP Request read by the server.
 * 
 * @author Robert C. Ilardi
 *
 */

public class SambucaHttpRequest implements Serializable {

  public static final int METHOD_UNKNOWN = -1;
  public static final int METHOD_GET = 0;
  public static final int METHOD_POST = 1;

  private int method;
  private String url;
  private HashMap<String, String> parameters;
  private int contentLen;

  private String serverAddress;
  private int serverPort;

  private String remoteAddress;
  private int remotePort;

  public SambucaHttpRequest() {
    contentLen = -1;
    parameters = new HashMap<String, String>();
  }

  /**
   * 
   * @return An integer representing a HTTP Request Method. Use the METHOD_* constants within this class to
   * determine which type of request method it is. 
   */
  public int getMethod() {
    return method;
  }

  /**
   * 
   * @param method Sets the integer code representing the HTTP Request method.
   */
  protected void setMethod(int method) {
    this.method = method;
  }

  /**
   * 
   * @return The URL string requested in the HTTP Request.
   */
  public String getUrl() {
    return url;
  }

  /**
   * 
   * @param url Sets the URL string requested in the HTTP Request.
   */
  protected void setUrl(String url) {
    this.url = url;
  }

  /**
   * This method basically wraps the "put" method of a properties object.
   * It is used to store the data passed as name value pairs in an HTTP Get or Post Request.
   * 
   * @param name Sets the name of the name-value pair.
   * @param value Sets the value of the name-value pair.
   */
  protected void setParameter(String name, String value) {
    parameters.put((name != null ? name.trim().toUpperCase() : null), value);
  }

  /**
   * This method basically wraps the "get" method of a properties object.
   * It is used to obtain the data passed as name value pairs in an HTTP Get or Post Request.
   * 
   * @param name Sets the name of the value of the name-value pair to be retrieved.
   * @return The value of the name-value pair represented by the name key passed to this method.
   */
  public String getParameter(String name) {
    return parameters.get((name != null ? name.trim().toUpperCase() : null));
  }

  /**
   * 
   * @return The content length header parameter of an HTTP Request
   */
  public int getContentLen() {
    return contentLen;
  }

  /**
   * 
   * @param contentLen Sets the content length header parameter of an HTTP Request
   */
  public void setContentLen(int contentLen) {
    this.contentLen = contentLen;
  }

  /**
   * 
   * @return The Server Address (mostly used for debugging a request). Useful for multi-homed systems.
   */
  public String getServerAddress() {
    return serverAddress;
  }

  /**
   * 
   * @param serverAddress Sets the server address for which an incoming HTTP Request was read from.
   */
  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  /**
   * 
   * @return The port number the Server is listening on.
   */
  public int getServerPort() {
    return serverPort;
  }

  /**
   * 
   * @param serverPort Sets the port number the Server is listening on.
   */
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  /**
   * 
   * @return The address of the remote client connecting and making the HTTP Request
   */
  public String getRemoteAddress() {
    return remoteAddress;
  }

  /**
   * 
   * @param remoteAddress Set the address of the remote client connecting and making the HTTP Request
   */
  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  /**
   * 
   * @return The port of the remote client connecting and making the HTTP Request
   */
  public int getRemotePort() {
    return remotePort;
  }

  /**
   * 
   * @param reportPort Set the port of the remote client connecting and making the HTTP Request
   */
  public void setRemotePort(int reportPort) {
    this.remotePort = reportPort;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    Iterator<String> iter;
    String name, value;

    sb.append("**Start Sambuca Http Request**\n");

    sb.append("Method = ");

    switch (method) {
      case METHOD_GET:
        sb.append("GET");
        break;
      case METHOD_POST:
        sb.append("POST");
        break;
      default:
        sb.append("[UNKNOWN]");
    }

    sb.append("\n");

    sb.append("URL = ");
    sb.append(url);
    sb.append("\n");

    sb.append("ContentLen = ");
    sb.append(contentLen);
    sb.append("\n");

    sb.append("Parameters:\n");

    iter = parameters.keySet().iterator();

    while (iter.hasNext()) {
      name = iter.next();
      value = parameters.get(name);

      sb.append(name);
      sb.append(" = ");
      sb.append(value);
      sb.append("\n");
    }

    sb.append("**End Sambuca Http Request**");

    return sb.toString();
  }

}
