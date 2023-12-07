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

import java.io.PrintStream;

/**
 * @author Robert C. Ilardi
 *
 */

public class HttpServletCallData {

  private String payloadData;
  private String[] rawCookies;
  private String cookieData;

  public HttpServletCallData() {}

  public String getCookieData() {
    return cookieData;
  }

  public void setCookieData(String cookieData) {
    this.cookieData = cookieData;
  }

  public String getPayloadData() {
    return payloadData;
  }

  public void setPayloadData(String httpData) {
    this.payloadData = httpData;
  }

  public String[] getRawCookies() {
    return rawCookies;
  }

  public void setRawCookies(String[] rawCookies) {
    this.rawCookies = rawCookies;
  }

  public void print(PrintStream printer) {
    if (printer != null) {
      printer.println("\nHTTP Cookies Returned:\n");
      StringUtils.PrintArray(printer, "RawCookies", rawCookies);
      printer.println("\nReturn Cookie Data = " + cookieData);

      printer.println("\nHTTP Data Returned:\n\n");
      printer.println(payloadData);
    }
  }

}
