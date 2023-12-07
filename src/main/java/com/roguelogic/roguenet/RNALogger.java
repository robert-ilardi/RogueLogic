/**
 * Created Sep 24, 2006
 */
package com.roguelogic.roguenet;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNALogger {

  private static RNALogger LoggerInstance = null;
  private static RNALogger NullRootLogger = new RNALogger();

  private Logger rootLogger = null;

  private String rnaLoggerName;

  private static final String PROP_RNA_LOGGER_NAME = "RNALoggerName";

  private RNALogger() {}

  protected static synchronized void CreateLogger(Properties rnaProps) {
    if (LoggerInstance == null) {
      LoggerInstance = new RNALogger();
      LoggerInstance.rnaLoggerName = rnaProps.getProperty(PROP_RNA_LOGGER_NAME);
      LoggerInstance.rootLogger = Logger.getLogger(LoggerInstance.rnaLoggerName);
      LoggerInstance.rootLogger.setLevel(Level.ALL);
    }
  }

  public static RNALogger GetLogger() {
    return (LoggerInstance != null ? LoggerInstance : NullRootLogger);
  }

  public void info(String mesg) {
    if (rootLogger != null) {
      rootLogger.info(mesg);
    }
    else {
      System.out.print(mesg);
    }
  }

  public void warn(String mesg) {
    if (rootLogger != null) {
      rootLogger.warning(mesg);
    }
    else {
      System.err.print(mesg);
    }
  }

  public void error(String mesg) {
    if (rootLogger != null) {
      rootLogger.severe(mesg);
    }
    else {
      System.err.print(mesg);
    }
  }

  public void log(Throwable t) {
    if (rootLogger != null) {
      rootLogger.severe(StringUtils.GetStackTraceString(t));
    }
    else {
      t.printStackTrace();
    }
  }

  public void stdOutPrint(String s) {
    System.out.print(s);
  }

  public void stdOutPrintln(String s) {
    System.out.println(s);
  }

  public void stdErrPrint(String s) {
    System.err.print(s);
  }

  public void stdErrPrintln(String s) {
    System.err.println(s);
  }

}
