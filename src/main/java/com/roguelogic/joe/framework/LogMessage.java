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

package com.roguelogic.joe.framework;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.roguelogic.util.StringUtils;

/**
 * Log Message Value-Object Class
 * 
 * @author Robert C. Ilardi
 *
 */

public class LogMessage implements Serializable {

  private Date timestamp;
  private String message;
  private Throwable throwable;
  private String code;

  public LogMessage() {
    this.timestamp = new Date();
  }

  /**
   * 
   * @return Text of the Log message
   */
  public String getMessage() {
    return message;
  }

  /**
   * 
   * @param message Sets the Log Message Text.
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * 
   * @return The optional throwable object if one was set in the Log Message.
   * This is used if the log message is for an exception that occurred.
   */
  public Throwable getThrowable() {
    return throwable;
  }

  /**
   * 
   * @param throwable Sets a throwable such as an Exception into the Log Message.
   */
  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  /**
   * 
   * @return The Timestamp of the Log Message creation. (Automatically set via the default constructor but can be overwritten
   * via the setTimestamp(java.util.Date) method)
   */
  public Date getTimestamp() {
    return timestamp;
  }

  /**
   * 
   * @param timestamp Sets the timestamp of the Log Message. The default constructor automatically sets the timestamp as the
   * creation time of the Log Message instance.
   */
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * 
   * @return The Log Message "code". This code is normally reserved for a programmatic flag for custom Logger
   * implementations and is not normally displayed to the user.
   */
  public String getCode() {
    return code;
  }

  /**
   * 
   * @param code Sets a programmatic flag for custom Logger implementations. It can be any string, which
   * can be used by custom logger implementations for specific control over how a particular Log Message instance is handled.
   * See a custom logger implementation for details on how it uses this code flag
   */
  public void setCode(String code) {
    this.code = code;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    SimpleDateFormat sdf;

    if (timestamp != null) {
      sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

      sb.append("[");
      sb.append(sdf.format(timestamp));
      sb.append("] ");
    }

    if (message != null) {
      sb.append(message);
    }

    if (throwable != null) {
      if (message != null) {
        sb.append("\n");
      }

      sb.append(StringUtils.GetStackTraceString(throwable));
    }

    return sb.toString();
  }

}
