/**
 * Created Dec 9, 2008
 */
package com.roguelogic.offsite;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class OffSitePublisher {

  public static final String APP_TITLE = "Off Site Publisher";

  public static final String PROP_ROOT_LOGGER = "RootLogger";

  private Properties ospProps;

  private OffsLogger logger;
  private BackupScheduler scheduler;

  public OffSitePublisher(Properties props) {
    this.ospProps = props;
  }

  public void boot() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
    System.out.println("Booting " + APP_TITLE + " at: " + StringUtils.GetTimeStamp());

    loadRootLogger();

    startSchedule();
  }

  private void startSchedule() throws IOException {
    scheduler = new BackupScheduler(this);
    scheduler.init();
    scheduler.start();
  }

  private void loadRootLogger() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    String rootLoggerClass = ospProps.getProperty(PROP_ROOT_LOGGER);

    System.out.println("Loading Root Logger: " + rootLoggerClass);

    logger = (OffsLogger) Class.forName(rootLoggerClass).newInstance();

    System.out.println();
  }

  public static void PrintWelcome() {
    System.out.print((new StringBuffer()).append("\n").append(Version.GetInfo()));
  }

  public void shutdown() {
    scheduler.shutdown();
  }

  public void waitWhileRunning() {
    scheduler.waitWhileRunning();
  }

  public void log(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.offsLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  public void log(Exception e) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setThrowable(e);

    if (logger != null) {
      logger.offsLogError(lMesg);
    }
    else {
      System.err.println(lMesg);
    }
  }

  public void logErr(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.offsLogError(lMesg);
    }
    else {
      System.err.println(lMesg);
    }
  }

  public String getProperty(String propName) {
    return ospProps.getProperty(propName);
  }

  public static void main(String[] args) {
    Properties props;
    int exitCd;
    FileInputStream fis = null;
    OffSitePublisher publisher;

    if (args.length != 1) {
      System.err.println("Usage: java " + OffSitePublisher.class.getName() + " [PUBLISHER_PROPERTIES_FILE]");
      exitCd = 1;
    }
    else {
      try {
        PrintWelcome();

        System.out.println("Using " + APP_TITLE + " Properties File: " + args[0]);

        fis = new FileInputStream(args[0]);
        props = new Properties();
        props.load(fis);
        fis.close();
        fis = null;

        publisher = new OffSitePublisher(props);
        publisher.boot();

        publisher.waitWhileRunning();

        exitCd = 0;
      } // End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
      finally {
        if (fis != null) {
          try {
            fis.close();
          }
          catch (Exception e) {}
        }
      }
    }

    System.exit(exitCd);
  }

}
