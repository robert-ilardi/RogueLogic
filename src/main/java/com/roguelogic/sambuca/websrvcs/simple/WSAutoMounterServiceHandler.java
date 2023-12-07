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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.roguelogic.sambuca.LogMessage;
import com.roguelogic.sambuca.SambucaException;
import com.roguelogic.sambuca.SambucaHttpRequest;
import com.roguelogic.sambuca.SambucaHttpResponse;
import com.roguelogic.sambuca.SambucaLogger;
import com.roguelogic.sambuca.ServiceHandler;
import com.roguelogic.sambuca.Version;
import com.roguelogic.sambuca.websrvcs.SambucaWebServiceException;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * An implementation of the ServiceHandler that services HTTP requests for
 * simple non-soap based web services.
 * 
 * @author Robert C. Ilardi
 * 
 */

public class WSAutoMounterServiceHandler implements ServiceHandler {

  public static final String PARAM_ACTION = "ACTION";
  public static final String PARAM_SIGNATURE_KEY = "SIGNATURE-KEY";
  public static final String PARAM_PREFIX_WS_PARAM = "WSPARAM";
  public static final String PARAM_FACADE_NAME = "FACADE-NAME";

  public static final String ACTION_GET_WS_INVENTORY = "GET-WS-INVENTORY";
  public static final String ACTION_EXECUTE_WS_METHOD = "EXECUTE-WS-METHOD";

  private Class wsFacadeClass;
  private SambucaLogger logger;
  private FacadeInventory inventory;

  private HashMap<String, Method> methodMap;

  public WSAutoMounterServiceHandler() {
  }

  public Class getWsFacadeClass() {
    return wsFacadeClass;
  }

  public void setWsFacadeClass(Class wsFacadeClass) {
    this.wsFacadeClass = wsFacadeClass;
  }

  public HashMap<String, Method> getMethodMap() {
    return methodMap;
  }

  public void setMethodMap(HashMap<String, Method> methodMap) {
    this.methodMap = methodMap;
  }

  public SambucaLogger getLogger() {
    return logger;
  }

  public void setLogger(SambucaLogger logger) {
    this.logger = logger;
  }

  /**
   * @return Returns the inventory.
   */
  public FacadeInventory getInventory() {
    return inventory;
  }

  /**
   * @param inventory
   *          The inventory to set.
   */
  public void setInventory(FacadeInventory inventory) {
    this.inventory = inventory;
  }

  public void handle(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    String action;

    try {
      action = request.getParameter(PARAM_ACTION);

      if (ACTION_GET_WS_INVENTORY.equalsIgnoreCase(action)) {
        sendInventory(response);
      }
      else if (ACTION_EXECUTE_WS_METHOD.equalsIgnoreCase(action)) {
        executeWsMethod(request, response);
      }
      else {
        sendForbidden(request, response);
      }
    }
    catch (Exception e) {
      try {
        sendInternalError(request, response);
      }
      catch (Exception e2) {
        e2.printStackTrace();
      }

      throw new SambucaException("An error occurred while attempting to handle Web Service Request. System Message: " + e.getMessage(), e);
    }
    finally {
      if (request != null) {
        response.close();
      }
    }
  }

  private void sendForbidden(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    StringBuffer sb = new StringBuffer();
    String html;
    byte[] bArr;

    sb.append("<HTML><HEAD><TITLE>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_FORBIDDEN);
    sb.append("</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\" TEXT=\"#000000\"><H1>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_FORBIDDEN);
    sb.append("</H1><PRE>");
    sb.append(StringUtils.SimpleHTMLTextEncoder(request.getUrl()));
    sb.append("</PRE>");

    sb.append(getServerFooterHtml(request));

    sb.append("</BODY></HTML>");

    html = sb.toString();
    bArr = html.getBytes();

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_FORBIDDEN);
    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_HTML_TEXT);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private void sendNotFound(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    StringBuffer sb = new StringBuffer();
    String html;
    byte[] bArr;

    sb.append("<HTML><HEAD><TITLE>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_NOT_FOUND);
    sb.append("</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\" TEXT=\"#000000\"><H1>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_NOT_FOUND);
    sb.append("</H1><PRE>");
    sb.append(StringUtils.SimpleHTMLTextEncoder(request.getUrl()));
    sb.append("</PRE>");

    sb.append(getServerFooterHtml(request));

    sb.append("</BODY></HTML>");

    html = sb.toString();
    bArr = html.getBytes();

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_NOT_FOUND);
    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_HTML_TEXT);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private String getServerFooterHtml(SambucaHttpRequest request) {
    StringBuffer html = new StringBuffer();

    html.append("<BR><HR><P><B>");
    html.append(Version.APP_TITLE_SHORT);
    html.append(" </B><I>Version: ");
    html.append(Version.VERSION);
    html.append(" - listening on ");
    html.append(request.getServerAddress());
    html.append(" (");
    html.append(SystemUtils.GetOperatingSystemName());
    html.append(") on port ");
    html.append(request.getServerPort());
    html.append("</I></P>");

    return html.toString();
  }

  private void sendInternalError(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    StringBuffer sb = new StringBuffer();
    String html;
    byte[] bArr;

    sb.append("<HTML><HEAD><TITLE>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_INTERNAL_SERVER_ERROR);
    sb.append("</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\" TEXT=\"#000000\"><H1>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_INTERNAL_SERVER_ERROR);
    sb.append("</H1><PRE>");
    sb.append(StringUtils.SimpleHTMLTextEncoder(request.getUrl()));
    sb.append("</PRE>");

    sb.append(getServerFooterHtml(request));

    sb.append("</BODY></HTML>");

    html = sb.toString();
    bArr = html.getBytes();

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_FORBIDDEN);
    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_HTML_TEXT);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private void sendInventory(SambucaHttpResponse response) throws SambucaException {
    String xml;
    byte[] bArr;

    xml = inventory.getXMLDescriptor();
    bArr = xml.getBytes();

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_OK);
    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_APPLICATION_XML);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private void executeWsMethod(SambucaHttpRequest request, SambucaHttpResponse response) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      SambucaException {
    String facadeName, signatureKey, retData = null;
    Method method;
    Object facadeInst, retVal;
    Object[] params;
    byte[] bArr;

    //System.out.println(request);

    facadeName = request.getParameter(PARAM_FACADE_NAME);
    signatureKey = request.getParameter(PARAM_SIGNATURE_KEY);

    if (!wsFacadeClass.getName().equals(facadeName)) {
      throw new SambucaWebServiceException("Web Service Facade: '" + facadeName + "' NOT Mounted on this server!");
    }

    method = methodMap.get(signatureKey);

    if (method == null) {
      throw new SambucaWebServiceException("Method: '" + signatureKey + "' on Web Service Facade: '" + facadeName + "' NOT Found!");
    }

    params = decodeParameters(method, request);

    facadeInst = wsFacadeClass.newInstance();

    print("Invoking WS Method: " + signatureKey);

    retVal = method.invoke(facadeInst, params);

    if (retVal != null) {
      retData = WSDataCodec.Encode(retVal);
    }

    bArr = (retData != null ? retData.getBytes() : new byte[0]);

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_OK);
    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private Object[] decodeParameters(Method method, SambucaHttpRequest request) throws SambucaWebServiceException {
    Class[] pTypes;
    Object[] params;
    String tmp;

    pTypes = method.getParameterTypes();
    params = new Object[pTypes.length];

    for (int i = 0; i < params.length; i++) {
      tmp = request.getParameter((new StringBuffer()).append(PARAM_PREFIX_WS_PARAM).append(i + 1).toString());
      params[i] = decodeParameter(pTypes[i], tmp);
    } //End for i loop through params

    return params;
  }

  private Object decodeParameter(Class type, String data) throws SambucaWebServiceException {
    Object param = null;

    if (type.isArray()) {
      type = type.getComponentType();

      if ("java.lang.String".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeStringArr(data);
      }
      else if ("int".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeIntegerArr(data);
      }
      else if ("float".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeFloatArr(data);
      }
      else if ("double".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeDoubleArr(data);
      }
      else if ("char".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeCharArr(data);
      }
      else if ("byte".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeByteArr(data);
      }
      else if ("short".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeShortArr(data);
      }
      else if ("long".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeLongArr(data);
      }
      else if ("boolean".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeBooleanArr(data);
      }
      else {
        throw new SambucaWebServiceException("Unsupported Data Type for Parameter Decoding! Array Data Type = " + type.getName());
      }
    } //End if type.isArray block
    else {
      if ("java.lang.String".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeString(data);
      }
      else if ("int".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeInteger(data);
      }
      else if ("float".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeFloat(data);
      }
      else if ("double".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeDouble(data);
      }
      else if ("char".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeChar(data);
      }
      else if ("byte".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeByte(data);
      }
      else if ("short".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeShort(data);
      }
      else if ("long".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeLong(data);
      }
      else if ("boolean".equalsIgnoreCase(type.getName())) {
        param = WSDataCodec.DecodeBoolean(data);
      }
      else {
        throw new SambucaWebServiceException("Unsupported Data Type for Parameter Decoding! Scalar Data Type = " + type.getName());
      }
    } //End else block

    return param;
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

}
