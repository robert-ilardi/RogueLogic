/**
 * Created Dec 10, 2008
 */
package com.roguelogic.offsite;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.roguelogic.net.RLNetException;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.TimeBasedScheduler;

/**
 * @author Robert C. Ilardi
 * 
 */

public class BackupScheduler implements Runnable {

  public static final String PROP_BACKUP_SCHEDULE = "BackupSchedule";
  public static final String PROP_TRIGGER_BACKUP_ON_BOOT = "TriggerBackupOnBoot";
  public static final String PROP_SEND_TRY_CNT = "SendProcessRetryCnt";
  public static final String PROP_CONSECUTIVE_SEND_FAILURE_ALLOWANCE = "ConsecutiveSendFailureAllowance";

  private OffSitePublisher publisher;
  private LocalFileMonitor lfMon;
  private FileTransmitter transmitter;

  private TimeBasedScheduler scheduler;

  private String[] backupTimes;
  private boolean triggerBackupOnBoot;
  private int sendRetryCnt;
  private int consecutiveSendFailureAllowance;

  public BackupScheduler(OffSitePublisher publisher) {
    this.publisher = publisher;
  }

  public void init() throws IOException {
    publisher.log("Initializing Backup Scheduler:");

    readProperties();

    initFileTransmitter();

    initLocalFileMonitor();

    initTimeBasedScheduler();

    publisher.log("Backup Scheduler Init Complete!");
  }

  private void readProperties() {
    String tmp;

    tmp = publisher.getProperty(PROP_BACKUP_SCHEDULE);
    backupTimes = tmp.trim().split(";");
    backupTimes = StringUtils.Trim(backupTimes);

    tmp = publisher.getProperty(PROP_TRIGGER_BACKUP_ON_BOOT);
    triggerBackupOnBoot = "TRUE".equalsIgnoreCase(tmp);

    tmp = publisher.getProperty(PROP_SEND_TRY_CNT);
    sendRetryCnt = Integer.parseInt(tmp);

    tmp = publisher.getProperty(PROP_CONSECUTIVE_SEND_FAILURE_ALLOWANCE);
    consecutiveSendFailureAllowance = Integer.parseInt(tmp);
  }

  private void initTimeBasedScheduler() {
    scheduler = new TimeBasedScheduler();
    scheduler.setTask(this);
    scheduler.setSchedule(backupTimes);
    scheduler.setTriggerTaskOnStart(triggerBackupOnBoot);

    publisher.log("Scheduled Backup Execution Times: " + StringUtils.ArrayToDelimitedString(backupTimes, ", ", false));

    publisher.log("Triggering Backup On Boot? " + (triggerBackupOnBoot ? "YES" : "NO"));
  }

  private void initLocalFileMonitor() throws IOException {
    lfMon = new LocalFileMonitor(publisher);
    lfMon.init();
  }

  private void initFileTransmitter() throws IOException {
    transmitter = new FileTransmitter(publisher);
    transmitter.init();
  }

  public void start() {
    scheduler.start();
  }

  public void shutdown() {
    scheduler.shutdown();
  }

  public void waitWhileRunning() {
    scheduler.waitWhileRunning();
  }

  public void run() {
    try {
      executeBackup();
    } // End try block
    catch (Exception e) {
      publisher.log(e);
    }
  }

  private void executeBackup() throws RLNetException, OffSiteException, InterruptedException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
      NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    long startTime, endTime, totalTime;

    try {
      publisher.log("------------------------------------\n\nExecuting Scheduled Backup at: " + StringUtils.GetTimeStamp() + "\n");

      startTime = System.currentTimeMillis();

      transmitter.startSession();

      processRemoves();

      processSends();

      endTime = System.currentTimeMillis();
      totalTime = endTime - startTime;

      publisher.log(">>>> Total Backup Execution Time: " + StringUtils.CompactHumanReadableTime(totalTime));

      publisher.log("------------------------------------\n\nCompleted Scheduled Backup at: " + StringUtils.GetTimeStamp());
    } // End try block
    finally {
      transmitter.endSession();
    }
  }

  private void processRemoves() throws RLNetException, OffSiteException, InterruptedException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
      NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    ArrayList<LocalFile> localFiles;
    long startTime, endTime, totalTime;

    publisher.log(">>>> Starting Removal Processing...");

    startTime = System.currentTimeMillis();

    localFiles = lfMon.getRemovalList();

    if (localFiles == null) {
      publisher.log(">>>> No Files to be removed! Aborting Removal Processing...");
      return;
    }

    for (LocalFile lf : localFiles) {
      for (int j = 1; j <= sendRetryCnt; j++) {
        try {
          transmitter.removeFromRemote(lf);
          break; // End retry loop
        } // End try block
        catch (Exception e) {
          publisher.log(e);

          if (j < sendRetryCnt) {
            transmitter.endSession();
            publisher.log("Retrying (try " + j + " of " + sendRetryCnt + ") Processing File: " + lf);
          }
        }
      } // end for j through sendRetryCnt
    } // end for through localFiles

    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;

    publisher.log(">>>> Removal Processing Time: " + StringUtils.CompactHumanReadableTime(totalTime));
  }

  private void processSends() throws RLNetException, OffSiteException, InterruptedException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
      NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    ArrayList<LocalFile> localFiles;
    LocalFile lf;
    int totalFiles, totalFilesSent, fails;
    long startTime, endTime, totalTime;

    publisher.log(">>>> Starting Sends Processing...");

    startTime = System.currentTimeMillis();

    totalFilesSent = 0;

    localFiles = lfMon.getLocalFiles();

    if (localFiles == null) {
      return;
    }

    fails = 0;
    totalFiles = localFiles.size();

    for (int i = 0; i < totalFiles; i++) {
      lf = localFiles.get(i);

      for (int j = 1; j <= sendRetryCnt; j++) {
        try {
          if (lf.isUpdateAlways() || !transmitter.localEqualsRemote(lf.getName())) {
            publisher.log("Sending File (" + (i + 1) + " of " + totalFiles + "): " + lf.getName());
            transmitter.transmitFile(lf);
            totalFilesSent++;
          } else {
            publisher.log("Skipping Unchanged File (" + (i + 1) + " of " + totalFiles + "): " + lf.getName());
          }

          fails = 0;
          break; // End retry loop
        } // End try block
        catch (Exception e) {
          publisher.log(e);

          if (j < sendRetryCnt) {
            transmitter.endSession();
            publisher.log("Retrying (try " + j + " of " + sendRetryCnt + ") Processing File: " + lf);
          } else {
            fails++;
          }
        }
      } // end for j through sendRetryCnt

      if (fails >= consecutiveSendFailureAllowance) {
        throw new OffSiteException("Too Many Consecutive Send Failures (" + fails + " fails of a maximum allowance of " + consecutiveSendFailureAllowance + ")");
      }
    } // end for i through totalFiles

    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;

    publisher.log(">>>> Sent " + totalFilesSent + (totalFiles == 1 ? " File" : " Files") + " in " + StringUtils.CompactHumanReadableTime(totalTime));
  }

}
