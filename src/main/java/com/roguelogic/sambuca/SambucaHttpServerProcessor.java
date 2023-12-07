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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketSession;
import com.roguelogic.util.StringUtils;

/**
 * The HTTP protocol implementation of the RogueLogic Socket Wrapper Framework "SocketWorker" concept.
 * This class contains all the logic to parse and handle at a low level HTTP data read from the socket.
 * 
 * @author Robert C. Ilardi
 *
 */

public class SambucaHttpServerProcessor implements SocketProcessor {

  public static final byte SPACE_BYTE = (byte) 32;
  public static final byte QUESTION_MARK_BYTE = (byte) 63;
  public static final byte AMPERSAND_BYTE = (byte) 38;

  public static final String USOBJ_PACKET_QUEUE = "PacketQueue";
  public static final String USOBJ_TOUCH_TIME = "TouchTime";
  public static final String USOBJ_PENDING_HTTP_REQUEST = "PendingHttpRequest";

  private ServiceHandler handler;
  private SambucaHttpServer server;
  private SambucaLogger logger;

  public SambucaHttpServerProcessor() {}

  public void clearSession() {}

  public void destroyProcessor() {}

  /**
   * This method is the implementation of the SocketProcess method.
   * It is used to process low level socket data and is the entry point
   * to convert that data into complete HTTP Requests. Once a complete HTTP Request
   * is available, this method is pass executing and the HTTP Request to the user defined ServiceHandler implementation.
   */
  public void process(SocketSession userSession, byte[] rawData) throws RLNetException {
    SambucaHttpRequest request = null;
    SambucaHttpResponse response = null;
    PacketQueue queue = null;
    byte[] headerData;

    userSession.putUserItem(USOBJ_TOUCH_TIME, new Long(System.currentTimeMillis()));

    if (userSession.wasHandshook()) {
      //System.out.println(new String(rawData));
      queue = (PacketQueue) userSession.getUserItem(USOBJ_PACKET_QUEUE);

      queue.enqueue(rawData);

      //correctCorruptHeader(queue); //In case we have a HTTP client that doesn't follow the rules!

      request = (SambucaHttpRequest) userSession.getUserItem(USOBJ_PENDING_HTTP_REQUEST);

      if (request != null) {
        //Has pending request continuing processing...
        //Right now only POST requests can be pending!
        if (queue.hasEnoughHttpData(request.getContentLen())) {
          userSession.removeUserItem(USOBJ_PACKET_QUEUE); //So we don't reprocess

          parsePostData(request, queue);
          response = createResponse(userSession);

          handle(request, response);
        }
      } //End request!=null check
      else if (queue.hasCompleteHttpHeader()) {
        headerData = queue.dequeueHttpHeader();

        request = createRequest(headerData, userSession);

        switch (request.getMethod()) {
          case SambucaHttpRequest.METHOD_GET:
            response = createResponse(userSession);
            handle(request, response);
            break;
          case SambucaHttpRequest.METHOD_POST:
            if (queue.hasEnoughHttpData(request.getContentLen())) {
              parsePostData(request, queue);
              response = createResponse(userSession);
              handle(request, response);
            }
            else {
              userSession.putUserItem(USOBJ_PENDING_HTTP_REQUEST, request);
            }
            break;
          default:
            response = createResponse(userSession);
            response.setStatusCode(SambucaHttpResponse.STATUS_CODE_METHOD_NOT_ALLOWED);
            sendError(response);
        } //End switch on request.getMethod()
      } //End check if hasCompleteHttpHeader
    } //End userSession.wasHandshook() check
    else {
      server.registerConnection(userSession);

      queue = new PacketQueue();
      userSession.putUserItem(USOBJ_PACKET_QUEUE, queue);

      userSession.setHandshookStatus(true);

      logNewConnection(userSession);
    }
  }

  /**
   * 
   * @param handler The ServiceHandler instance to use for processing complete HTTP Requests.
   */
  public void setHandler(ServiceHandler handler) {
    this.handler = handler;
  }

  /**
   * Creates a Sambuca HTTP Request out of raw socket data.
   * 
   * @param headerData - Raw HTTP Header Data read from the socket.
   * @param userSession - The RogueLogic Socket Wrapper Framework's "Connection" representation.
   * @return A HTTP Request object. At this point the request object may not be fully initialized. Meaning not all data may be
   * populated in the request. Perhaps more socket reads are needed to complete the request. The process method will ensure only
   * complete HTTP Requests are passed to the ServiceHandler implementation.
   * @throws SambucaException
   */
  private SambucaHttpRequest createRequest(byte[] headerData, SocketSession userSession) throws SambucaException {
    SambucaHttpRequest request = new SambucaHttpRequest();
    String methodStr;
    int method;

    request.setServerAddress(userSession.getServerAddress());
    request.setServerPort(userSession.getServerPort());

    request.setRemoteAddress(userSession.getRemoteAddress());
    request.setRemotePort(userSession.getRemotePort());

    methodStr = parseMethodString(headerData);
    method = determineMethodCode(methodStr);
    request.setMethod(method);

    switch (method) {
      case SambucaHttpRequest.METHOD_GET:
        parseGetUrl(headerData, request);
        break;
      case SambucaHttpRequest.METHOD_POST:
        parsePostUrl(headerData, request);
        parseContentLen(headerData, request);
        break;
      default:
    //throw new SambucaException("Unsupported Method '" + methodStr + "'");
    }

    return request;
  }

  /**
   * Parses HTTP Header data to obtain the requested URL
   * 
   * @param headerData Raw HTTP Header data
   * @param request The Request object to set the URL into.
   * @throws SambucaException
   */
  private void parseGetUrl(byte[] headerData, SambucaHttpRequest request) throws SambucaException {
    String url = null;
    ByteArrayOutputStream baos;

    baos = new ByteArrayOutputStream();

    for (int i = 4; i < headerData.length; i++) {
      if (headerData[i] == SPACE_BYTE) {
        //Found space
        break;
      }
      else if (headerData[i] == QUESTION_MARK_BYTE) {
        //Found question mark
        parseGetQueryString(headerData, i + 1, request);
        break;
      }
      else {
        baos.write(headerData[i]);
      }
    }

    url = new String(baos.toByteArray());

    try {
      url = URLDecoder.decode(url, "UTF-8");
    }
    catch (Exception e) {
      throw new SambucaException(e);
    }

    request.setUrl(url.trim());
  }

  /**
   * Parses raw HTTP Request data to obtain a Get Query String.
   * 
   * @param headerData The raw HTTP data to be read.
   * @param startIndex The index in the headerData array to start reading
   * @param request The request object to set the Query String's name-value pair parameters into.
   * @throws SambucaException
   */
  private void parseGetQueryString(byte[] headerData, int startIndex, SambucaHttpRequest request) throws SambucaException {
    ByteArrayOutputStream baos;

    baos = new ByteArrayOutputStream();

    for (int i = startIndex; i < headerData.length; i++) {
      if (headerData[i] == SPACE_BYTE) {
        //Found space
        addGetParameter(baos.toByteArray(), request);
        baos.reset();
        break;
      }
      else if (headerData[i] == AMPERSAND_BYTE) {
        //Found ampersand
        addGetParameter(baos.toByteArray(), request);
        baos.reset();
      }
      else {
        baos.write(headerData[i]);
      }
    }

    if (baos.size() > 0) {
      addGetParameter(baos.toByteArray(), request);
      baos.reset();
    }
  }

  /**
   * Splits raw name-value pair data into the separate elements and sets them into the request's parameter storage.
   * 
   * @param paramData The raw parameter data to parse.
   * @param request The request object to add the parsed name-value pair into.
   * @throws SambucaException
   */
  private void addGetParameter(byte[] paramData, SambucaHttpRequest request) throws SambucaException {
    String tmp;
    String[] tokens;

    tmp = new String(paramData);
    tokens = tmp.trim().split("=", 2);

    if (tokens != null && tokens.length == 2) {
      try {
        tokens[0] = URLDecoder.decode(tokens[0], "UTF-8");
        tokens[1] = URLDecoder.decode(tokens[1], "UTF-8");
      }
      catch (Exception e) {
        throw new SambucaException(e);
      }

      tokens = StringUtils.Trim(tokens);
      request.setParameter(tokens[0].trim().toUpperCase(), tokens[1]);
    }
  }

  /**
   * Parses the Method String out of the raw HTTP data.
   * 
   * @param headerData The raw HTTP Header data. 
   * @return The string representation of the HTTP Request command/method, such as GET or POST.
   */
  private String parseMethodString(byte[] headerData) {
    ByteArrayOutputStream baos;
    String methodStr;

    baos = new ByteArrayOutputStream();

    for (int i = 0; i < headerData.length; i++) {
      if (headerData[i] == SPACE_BYTE) {
        //Found space
        break;
      }
      else {
        baos.write(headerData[i]);
      }
    }

    methodStr = new String(baos.toByteArray());

    return methodStr;
  }

  /**
   * Translates the string representation of the HTTP command/method into
   * an internal Sambuca Integer constant used/stored by the SambucaHttpRequest object.
   * 
   * @param methodStr The string representation of the HTTP command/method
   * @return The translated internal Sambuca Integer constants for HTTP commands/methods.
   * See the METHOD_* constants in the SambucaHttpRequest class for the definitions of the constants.
   * METHOD_GET is for the HTTP Get Method and METHOD_POST is for the HTTP Post Method.
   */
  private int determineMethodCode(String methodStr) {
    int method = SambucaHttpRequest.METHOD_UNKNOWN;

    if ("GET".equalsIgnoreCase(methodStr)) {
      method = SambucaHttpRequest.METHOD_GET;
    }
    else if ("POST".equalsIgnoreCase(methodStr)) {
      method = SambucaHttpRequest.METHOD_POST;
    }

    return method;
  }

  /**
   * Creates a SambucaHttpResponse object out of the RogueLogic socket "connection" wrapper.
   * 
   * @param userSession The RogueLogic socket "connection" wrapper object to be used in the HTTP Response object.
   * @return The HTTP Response Object which wraps a particular client connection.
   */
  private SambucaHttpResponse createResponse(SocketSession userSession) {
    SambucaHttpResponse response;

    response = new SambucaHttpResponse();
    response.setSocketSession(userSession);

    return response;
  }

  /**
   * Quick helper method to send HTTP Error Responses which do not require User Defined ServiceHandler intervention.
   * 
   * @param response The HTTP Response to be used to send the Error Response to the client.
   * @throws RLNetException
   */
  private void sendError(SambucaHttpResponse response) throws RLNetException {
    response.sendHeaders();
    response.close();
  }

  /**
   * 
   * @param server Sets the reference to the SambucaHttpServer instance. Used for call backs to the server class itself.
   */
  public void setSambuca(SambucaHttpServer server) {
    this.server = server;
  }

  /**
   * Parses HTTP Post request data to obtain the requested URL and sets it to the Http Request object.
   * 
   * @param headerData The raw header data to be parsed.
   * @param request The request object to set the requested URL into.
   * @throws SambucaException
   */
  private void parsePostUrl(byte[] headerData, SambucaHttpRequest request) throws SambucaException {
    String url = null;
    ByteArrayOutputStream baos;

    baos = new ByteArrayOutputStream();

    for (int i = 5; i < headerData.length; i++) {
      if (headerData[i] == SPACE_BYTE) {
        //Found space
        break;
      }
      else {
        baos.write(headerData[i]);
      }
    }

    url = new String(baos.toByteArray());

    try {
      url = URLDecoder.decode(url, "UTF-8");
    }
    catch (Exception e) {
      throw new SambucaException(e);
    }

    request.setUrl(url.trim());
  }

  /**
   * Parses the Content-Length HTTP Header.
   * 
   * @param headerData The header data to be parsed.
   * @param request The http request object to set the content-length header into.
   * @throws SambucaException
   */
  private void parseContentLen(byte[] headerData, SambucaHttpRequest request) throws SambucaException {
    int contentLen = -1;
    String line;
    ByteArrayInputStream bais = null;
    InputStreamReader isr = null;
    BufferedReader br = null;

    try {
      bais = new ByteArrayInputStream(headerData);
      isr = new InputStreamReader(bais);
      br = new BufferedReader(isr);

      line = br.readLine();

      while (line != null) {
        line = line.trim().toUpperCase();

        if (line.startsWith("CONTENT-LENGTH:")) {
          contentLen = Integer.parseInt(line.substring(15).trim());
          break;
        }

        line = br.readLine();
      }

      request.setContentLen(contentLen);
    }
    catch (Exception e) {
      throw new SambucaException("An error occurred while attempting to parse content len!", e);
    }
    finally {
      if (br != null) {
        try {
          br.close();
        }
        catch (Exception e) {}
      }

      if (isr != null) {
        try {
          isr.close();
        }
        catch (Exception e) {}
      }

      if (bais != null) {
        try {
          bais.close();
        }
        catch (Exception e) {}
      }
    }
  }

  /**
   * Parses raw HTTP Post Request data into the HTTP Request object (header only).
   *  
   * @param request The HTTP Request to set the parsed header data into.
   * @param queue The Packet Queue to read HTTP data from.
   * @throws SambucaException
   */
  private void parsePostData(SambucaHttpRequest request, PacketQueue queue) throws SambucaException {
    byte[] data;

    if (request.getContentLen() > 0) {
      data = queue.dequeueHttpData(request.getContentLen());
      parseGetQueryString(data, 0, request);
    }
  }

  /**
   * A helper method to attempt to "repair" corrupted HTTP Header data.
   * This method is currently not used.
   * 
   * @param queue The queue of packets to be corrected.
   */
  private void correctCorruptHeader(PacketQueue queue) {
    ArrayList<byte[]> packetList;
    byte[] packet;
    String methodStr;
    ByteArrayOutputStream baos;

    packetList = new ArrayList<byte[]>();

    while (queue.hasCompleteHttpHeader()) {
      packet = queue.dequeueHttpHeader();

      methodStr = parseMethodString(packet);

      if (packetList.size() > 0 && ("GET".equals(methodStr) || "POST".equals(methodStr))) {
        queue.push(packet);
        break;
      }
      else {
        packetList.add(packet);
      }
    }

    if (packetList.size() > 0) {
      baos = new ByteArrayOutputStream();

      while (!packetList.isEmpty()) {
        packet = packetList.remove(0);
        baos.write(packet, 0, packet.length - 2); //-2 to remove invalid repeating CRLF's
      }

      baos.write((byte) 13);
      baos.write((byte) 10);

      queue.push(baos.toByteArray());
    }

  }

  /**
   * 
   * @param logger The instance of the Sambuca Logger Implementation to be used to log events.
   */
  public void setLogger(SambucaLogger logger) {
    this.logger = logger;
  }

  /**
   * Wrapper method used to log the "new connection" event.
   * @param userSession The RogueLogic socket "connection" wrapper object, representing the new connection that was just opened.
   */
  private void logNewConnection(SocketSession userSession) {
    LogMessage lMesg = new LogMessage();
    StringBuffer sb = new StringBuffer();

    sb.append("Connection from ");
    sb.append(userSession.getRemoteAddress());
    sb.append(":");
    sb.append(userSession.getRemotePort());

    lMesg.setMessage(sb.toString());
    lMesg.setCode("ConnectionOpened");

    if (logger != null) {
      logger.sambucaLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  /**
   * Wrapper method used to log HTTP Requests.
   * 
   * @param request The HTTP Request to be written to the log.
   */
  private void logRequest(SambucaHttpRequest request) {
    LogMessage lMesg = new LogMessage();
    StringBuffer sb = new StringBuffer();

    sb.append(request.getRemoteAddress());
    sb.append(":");
    sb.append(request.getRemotePort());

    sb.append(" - ");

    switch (request.getMethod()) {
      case SambucaHttpRequest.METHOD_GET:
        sb.append("GET ");
        break;
      case SambucaHttpRequest.METHOD_POST:
        sb.append("POST ");
        break;
    }

    sb.append(request.getUrl());

    lMesg.setMessage(sb.toString());
    lMesg.setCode("NewRequest");

    if (logger != null) {
      logger.sambucaLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  /**
   * A wrapper method used by the process method flow to call the ServiceHandler's implementation of the handler method.
   * 
   * @param request The complete HTTP Request object to be processed.
   * @param response The HTTP Response object used write response data back to the client.
   * @throws SambucaException
   */
  private void handle(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    logRequest(request);
    handler.handle(request, response);
  }

}
