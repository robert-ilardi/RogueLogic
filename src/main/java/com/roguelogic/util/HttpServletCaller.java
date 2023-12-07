/**
 * Created Sep 3, 2006
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

package com.roguelogic.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Robert C. Ilardi
 *
 */

public class HttpServletCaller {

  public static final String HTTP_POST_METHOD = "POST";
  public static final String SET_COOKIE_FIELD = "Set-Cookie";
  public static final String COOKIE_FIELD = "Cookie";

  private String servletUrl;

  private String[] rawCookies;
  private String cookieData = null;

  public HttpServletCaller(String servletUrl) {
    this.servletUrl = servletUrl;
  }

  public synchronized HttpServletCallData sendParameters(Properties parameters) throws IOException {
    URL url;
    URLConnection urlConn = null;
    HttpURLConnection httpUrlConn;
    OutputStream outs = null;
    PrintWriter writer = null;
    HttpServletCallData httpData = null;

    try {
      url = new URL(servletUrl);
      urlConn = url.openConnection();

      if (urlConn instanceof HttpURLConnection) {
        httpUrlConn = (HttpURLConnection) urlConn;

        httpData = new HttpServletCallData();

        //Send Back Session Id if we have one
        if (cookieData != null) {
          httpUrlConn.setRequestProperty(COOKIE_FIELD, cookieData);
        }

        //Send Parameters if any...
        if (parameters != null && parameters.size() > 0) {
          httpUrlConn.setRequestMethod(HTTP_POST_METHOD);
          urlConn.setDoOutput(true);

          //Write Parameters
          outs = httpUrlConn.getOutputStream();
          writer = new PrintWriter(outs);

          writeParameters(writer, parameters);

          writer.close();
          writer = null;

          outs.close();
          outs = null;
        } //End parameters null and size check

        //Read HTTP Data
        readHttpData(httpUrlConn, httpData);

        //Handle Cookies
        handleCookies(httpUrlConn, httpData);
      } //End urlConn instanceof check
    } //End try block
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        writer = null;
      }

      if (outs != null) {
        try {
          outs.close();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        outs = null;
      }
    } //End finally block

    return httpData;
  }

  private void writeParameters(PrintWriter writer, Properties parameters) throws IOException {
    Iterator iter;
    String name, value;
    StringBuffer paramStr;
    boolean keyOne;

    keyOne = true;
    paramStr = new StringBuffer();

    iter = parameters.keySet().iterator();
    while (iter.hasNext()) {
      name = (String) iter.next();
      value = parameters.getProperty(name);

      if (!keyOne) {
        paramStr.append("&");
      }
      else {
        keyOne = false;
      }

      paramStr.append(name);
      paramStr.append("=");
      paramStr.append(URLEncoder.encode(value, "UTF-8"));
    } //End while loop for iterator through parameter names

    writer.print(paramStr.toString());
    writer.flush();
  }

  private void readHttpData(HttpURLConnection httpUrlConn, HttpServletCallData httpData) throws IOException {
    ByteArrayOutputStream baos = null;
    String payloadData = null;
    InputStream ins = null;

    try {
      ins = httpUrlConn.getInputStream();

      baos = new ByteArrayOutputStream();
      SystemUtils.Copy(ins, baos);

      payloadData = new String(baos.toByteArray());

      httpData.setPayloadData(payloadData.toString());
    } //End try block
    finally {
      if (ins != null) {
        try {
          ins.close();
        }

        catch (Exception e) {
          e.printStackTrace();
        }
        ins = null;
      }

      if (baos != null) {
        try {
          baos.close();
        }

        catch (Exception e) {
          e.printStackTrace();
        }
        baos = null;
      }
    } //End finally block
  }

  private void handleCookies(HttpURLConnection httpUrlConn, HttpServletCallData httpData) {
    //Brain-Dead Cookie Handling (assumes single path)
    String[] tmpRawCookies;
    String tmpCookieData;

    tmpRawCookies = getCookies(httpUrlConn);

    if (tmpRawCookies != null && tmpRawCookies.length > 0) {
      rawCookies = tmpRawCookies;

      tmpCookieData = parseCookies(rawCookies);

      if (tmpCookieData != null && tmpCookieData.trim().length() > 0) {
        cookieData = tmpCookieData;
      }
    }

    httpData.setRawCookies(rawCookies);
    httpData.setCookieData(cookieData);
  }

  private String[] getCookies(HttpURLConnection httpUrlConn) {
    Map map;
    List list;
    String[] cookies = null;

    map = httpUrlConn.getHeaderFields();
    list = (List) map.get(SET_COOKIE_FIELD);

    if (list != null) {
      cookies = new String[list.size()];

      for (int i = 0; i < list.size(); i++) {
        cookies[i] = (String) list.get(i);
      }
    }

    return cookies;
  }

  private String parseCookies(String[] rawCookies) {
    StringBuffer cookieData = null;
    String[] tmpArr;

    if (rawCookies != null && rawCookies.length > 0) {
      cookieData = new StringBuffer();

      for (int i = 0; i < rawCookies.length; i++) {
        tmpArr = StringUtils.QuoteSplit(rawCookies[i], ';');
        if (tmpArr != null && tmpArr.length > 0) {
          tmpArr = StringUtils.Trim(tmpArr);
          if (i > 0) {
            cookieData.append("; ");
          }

          cookieData.append(tmpArr[0]);
        }
      }
    }

    return (cookieData != null ? cookieData.toString() : null);
  }

  public String getCookieData() {
    return cookieData;
  }

  public void setCookieData(String cookieData) {
    this.cookieData = cookieData;
  }

  public String[] getRawCookies() {
    return rawCookies;
  }

  public String getServletUrl() {
    return servletUrl;
  }

  public static void main(String[] args) {
    HttpServletCaller caller;
    Properties params = null;
    HttpServletCallData httpData;
    String hscCookie;
    int exitCode;

    if (args.length == 0 || (args.length > 0 && (args.length - 1) % 2 != 0)) {
      System.err.println("Usage: java <-DhscCookie=[COOKIE_DATA]> " + HttpServletCaller.class.getName() + " [URL] {<NAME(1)> <VALUE(1)>} {<NAME(2)> <VALUE(2)>} ... {<NAME(N)> <VALUE(N)>}");
      exitCode = 1;
    }
    else {
      try {
        caller = new HttpServletCaller(args[0]);

        if (args.length >= 3) {
          params = new Properties();

          for (int i = 1; i < args.length; i += 2) {
            params.setProperty(args[i], args[i + 1]);
          }
        }

        hscCookie = System.getProperty("hscCookie");
        if (hscCookie != null && hscCookie.trim().length() > 0) {
          caller.setCookieData(hscCookie.trim());
        }

        httpData = caller.sendParameters(params);
        httpData.print(System.out);

        exitCode = 0;
      } //End try block
      catch (Exception e) {
        exitCode = 1;
        e.printStackTrace();
      }
    }

    System.exit(exitCode);
  }

}
