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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.roguelogic.net.SocketSession;

/**
 * This class represents a HTTP Response to be "sent" to the client.
 * However it is not a simple value-object, it is a hybrid object
 * which has method to not only store data about the response but
 * actually write that data back to the client over the network.
 * 
 * @author Robert C. Ilardi
 *
 */

public class SambucaHttpResponse implements Serializable {

  public static final String HTTP_VERSION = "HTTP/1.1";

  public static final String HEADER_CONTENT_TYPE = "Content-Type";
  public static final String HEADER_SERVER = "Server";
  public static final String HEADER_DATE = "Date";
  public static final String HEADER_CONNECTION = "Connection";
  public static final String HEADER_CONTENT_LENGTH = "Content-Length";

  public static final String CONNECTION_CLOSE = "close";

  public static final String CONTENT_TYPE_HTML_TEXT = "text/html";
  public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
  public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
  public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";

  public static final String STATUS_CODE_OK = "200 OK";
  public static final String STATUS_CODE_REDIRECT = "307 Temporary Redirect";
  public static final String STATUS_CODE_BAD_REQUEST = "400 Bad Request";
  public static final String STATUS_CODE_FORBIDDEN = "403 Forbidden";
  public static final String STATUS_CODE_NOT_FOUND = "404 Not Found";
  public static final String STATUS_CODE_METHOD_NOT_ALLOWED = "405 Method Not Allowed";
  public static final String STATUS_CODE_INTERNAL_SERVER_ERROR = "500 Internal Server Error";
  public static final String STATUS_CODE_NOT_IMPLEMENTED = "501 Not Implemented";
  public static final String STATUS_CODE_SERVICE_UNAVAILABLE = "503 Service Unavailable";
  public static final String STATUS_CODE_HTTP_VERSION_NOT_SUPPORTED = "505 HTTP Version not supported";

  private static final SimpleDateFormat DateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

  private SocketSession sockSession;

  private boolean sentHeaders;
  private boolean sentReplyStatus;

  private HashMap<String, String> headers;
  private String statusCd;

  public SambucaHttpResponse() {
    sentHeaders = false;
    sentReplyStatus = false;

    statusCd = STATUS_CODE_INTERNAL_SERVER_ERROR; //Default if the handler does not set one!

    headers = new HashMap<String, String>();
    addDefaultHeaders();
  }

  /**
   * This method adds default headers to the header map.
   *
   */
  private void addDefaultHeaders() {
    addHeader(HEADER_SERVER, Version.SERVER_HEADER);
    addHeader(HEADER_DATE, getTimestamp());
    addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_TEXT_PLAIN); //Default Content Type to Plain Text
    addHeader(HEADER_CONNECTION, CONNECTION_CLOSE); //Since Sambuca 1.0 Connection Closes after every transaction
  }

  /**
   * 
   * @return The current date-time as a HTTP Header formatted string.
   */
  private String getTimestamp() {
    String ts;

    ts = DateFormat.format(new Date());

    return ts;
  }

  /**
   * Sets the connection that this response object is wrapping. Only the server
   * should be able to set this, so the method id protected.
   * 
   * @param sockSession The RogueLogic Socket "connection" wrapper.
   */
  protected void setSocketSession(SocketSession sockSession) {
    this.sockSession = sockSession;
  }

  /**
   * 
   * @param contentType The content type string to be used as the Content-Type header.
   */
  public synchronized void setContentType(String contentType) {
    addHeader(HEADER_CONTENT_TYPE, contentType);
  }

  /**
   * Sets the status code of the HTTP Response to be written. A list of these
   * constants (most common ones/not a complete set) are available in this class. See the constants: STATUS_CODE_* 
   * @param statusCd The HTTP Status Code string such as "200 OK"
   */
  public synchronized void setStatusCode(String statusCd) {
    this.statusCd = statusCd;
  }

  /**
   * This method can be used to add http headers to the response. Headers
   * are not written out to the socket until the first send(byte[]) method is invoked
   * or the sendHeaders() method is invoked.
   * 
   * @param header The name of the header to be added.
   * @param value The value of the header corresponding to the header name to be added.
   */
  public synchronized void addHeader(String header, String value) {
    headers.put(header, value);
  }

  /**
   * Sends the status code to the client. Will be invoked automatically by the first invocation of sendHeaders.
   * 
   * @throws SambucaException
   */
  public synchronized void sendStatusCode() throws SambucaException {
    StringBuffer buf = new StringBuffer();

    try {
      buf.append(HTTP_VERSION);
      buf.append(" ");
      buf.append(statusCd);
      buf.append("\r\n");

      sockSession.send(buf.toString().getBytes());

      sentReplyStatus = true;
    }
    catch (Exception e) {
      throw new SambucaException("An error occurred while attempting to send Status Code!", e);
    }
  }

  /**
   * Sends the header set to the client. If it is the first time this method is invoked,
   * the sendStatusCode() method is automatically called first.
   * @throws SambucaException
   */
  public synchronized void sendHeaders() throws SambucaException {
    StringBuffer headerBuf;
    byte[] bArr;
    Iterator<String> iter;
    String header, value;

    if (!sentReplyStatus) {
      sendStatusCode();
    }

    try {
      headerBuf = new StringBuffer();
      iter = headers.keySet().iterator();

      while (iter.hasNext()) {
        header = iter.next();
        value = headers.get(header);

        headerBuf.append(header);
        headerBuf.append(": ");
        headerBuf.append(value);
        headerBuf.append("\r\n");
      }

      headerBuf.append("\r\n");

      bArr = headerBuf.toString().getBytes();

      sockSession.send(bArr);
      sentHeaders = true;
    }
    catch (Exception e) {
      throw new SambucaException("An error occurred while attempting to Send Headers!", e);
    }
  }

  /**
   * This method can be called multiple times by the user. If it is the first invocation of this method,
   * then the sendHeaders() method will be called automatically.
   * 
   * @param data
   * @throws SambucaException
   */
  public synchronized void send(byte[] data) throws SambucaException {
    if (!sentHeaders) {
      sendHeaders();
    }

    try {
      sockSession.send(data);
    }
    catch (Exception e) {
      throw new SambucaException("An error occurred while attempting to Send Data!", e);
    }
  }

  /**
   * This method closes the connection which this HTTP Response represents.
   *
   */
  public synchronized void close() {
    if (!sockSession.wasPeerForcedClosed() && !sockSession.isZombie()) {
      sockSession.endSession();
    }
  }

}
