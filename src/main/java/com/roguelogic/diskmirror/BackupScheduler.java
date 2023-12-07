/**
 * Created Feb 24, 2009
 */
package com.roguelogic.diskmirror;

import java.io.IOException;
import java.util.ArrayList;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.TimeBasedScheduler;

/**
 * @author Robert C. Ilardi
 * 
 */

public class BackupScheduler implements Runnable {

	public static final String PROP_BACKUP_SCHEDULE = "BackupSchedule";
	public static final String PROP_TRIGGER_BACKUP_ON_BOOT = "TriggerBackupOnBoot";
	public static final String PROP_TRY_CNT = "ProcessRetryCnt";
	public static final String PROP_CONSECUTIVE_FAILURE_ALLOWANCE = "ConsecutiveFailureAllowance";

	private DiskMirror dMirror;
	private LocalFileMonitor lfMon;
	private FileCopier fCopier;

	private TimeBasedScheduler scheduler;

	private String[] backupTimes;
	private boolean triggerBackupOnBoot;
	private int processRetryCnt;
	private int consecutiveFailureAllowance;

	public BackupScheduler(DiskMirror dMirror) {
		this.dMirror = dMirror;
	}

	public void init() throws IOException {
		dMirror.log("Initializing Backup Scheduler:");

		readProperties();

		initFileCopier();

		initLocalFileMonitor();

		initTimeBasedScheduler();

		dMirror.log("Backup Scheduler Init Complete!");
	}

	private void readProperties() {
		String tmp;

		tmp = dMirror.getProperty(PROP_BACKUP_SCHEDULE);
		backupTimes = tmp.trim().split(";");
		backupTimes = StringUtils.Trim(backupTimes);

		tmp = dMirror.getProperty(PROP_TRIGGER_BACKUP_ON_BOOT);
		triggerBackupOnBoot = "TRUE".equalsIgnoreCase(tmp);

		tmp = dMirror.getProperty(PROP_TRY_CNT);
		processRetryCnt = Integer.parseInt(tmp);

		tmp = dMirror.getProperty(PROP_CONSECUTIVE_FAILURE_ALLOWANCE);
		consecutiveFailureAllowance = Integer.parseInt(tmp);
	}

	private void initTimeBasedScheduler() {
		scheduler = new TimeBasedScheduler();
		scheduler.setTask(this);
		scheduler.setSchedule(backupTimes);
		scheduler.setTriggerTaskOnStart(triggerBackupOnBoot);

		dMirror.log("Scheduled Backup Execution Times: " + StringUtils.ArrayToDelimitedString(backupTimes, ", ", false));

		dMirror.log("Triggering Backup On Boot? " + (triggerBackupOnBoot ? "YES" : "NO"));
	}

	private void initLocalFileMonitor() throws IOException {
		lfMon = new LocalFileMonitor(dMirror);
		lfMon.init();
	}

	private void initFileCopier() throws IOException {
		fCopier = new FileCopier(dMirror);
		fCopier.init();
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
			dMirror.log(e);
		}
	}

	private void executeBackup() throws IOException, DiskMirrorException {
		long startTime, endTime, totalTime;

		dMirror.log("------------------------------------\n\nExecuting Scheduled Backup at: " + StringUtils.GetTimeStamp() + "\n");

		startTime = System.currentTimeMillis();

		processRemoves();

		processCopies();

		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;

		dMirror.log(">>>> Total Backup Execution Time: " + StringUtils.CompactHumanReadableTime(totalTime));

		dMirror.log("------------------------------------\n\nCompleted Scheduled Backup at: " + StringUtils.GetTimeStamp());
	}

	private void processRemoves() throws IOException {
		ArrayList<LocalFile> localFiles;
		long startTime, endTime, totalTime;
		int totalFiles;
		LocalFile lf;

		dMirror.log(">>>> Starting Removal Processing...");

		startTime = System.currentTimeMillis();

		localFiles = lfMon.getRemovalList();

		if (localFiles == null) {
			dMirror.log(">>>> No Files to be removed! Aborting Removal Processing...");
			return;
		}

		totalFiles = localFiles.size();

		for (int i = 0; i < totalFiles; i++) {
			lf = localFiles.get(i);

			for (int j = 1; j <= processRetryCnt; j++) {
				try {
					dMirror.log("Processing File (" + (i + 1) + " of " + totalFiles + "): " + lf.getName());
					fCopier.removeFromTarget(lf);
					break; // End retry loop
				} // End try block
				catch (Exception e) {
					dMirror.log(e);

					if (j < processRetryCnt) {
						dMirror.log("  >> Retrying (try " + (j + 1) + " of " + processRetryCnt + ") Processing File: " + lf);
					}
					else {
						dMirror.log("  >> Exceeded Maximum Removal Tries of " + processRetryCnt + " while Processing File: " + lf);
					}
				}
			} // end for j through processRetryCnt
		} // end for through localFiles

		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;

		dMirror.log(">>>> Removal Processing Time: " + StringUtils.CompactHumanReadableTime(totalTime));
	}

	private void processCopies() throws IOException, DiskMirrorException {
		ArrayList<LocalFile> localFiles;
		LocalFile lf;
		int totalFiles, totalFilesCopied, fails;
		long startTime, endTime, totalTime;

		dMirror.log(">>>> Starting Copy Processing...");

		startTime = System.currentTimeMillis();

		totalFilesCopied = 0;

		localFiles = lfMon.getLocalFiles();

		if (localFiles == null) {
			return;
		}

		fails = 0;
		totalFiles = localFiles.size();

		for (int i = 0; i < totalFiles; i++) {
			lf = localFiles.get(i);

			for (int j = 1; j <= processRetryCnt; j++) {
				try {
					if (lf.isUpdateAlways() || !fCopier.sourceEqualsTarget(lf)) {
						dMirror.log("Processing File (" + (i + 1) + " of " + totalFiles + "): " + lf.getName());
						fCopier.copyFile(lf);
						totalFilesCopied++;
					}
					else {
						dMirror.log("Skipping Unchanged File (" + (i + 1) + " of " + totalFiles + "): " + lf.getName());
					}

					fails = 0;
					break; // End retry loop
				} // End try block
				catch (Exception e) {
					dMirror.log(e);

					if (j < processRetryCnt) {
						dMirror.log("  >> Retrying (try " + (j + 1) + " of " + processRetryCnt + ") Processing File: " + lf);
					}
					else {
						dMirror.log("  >> Exceeded Maximum Copy Tries of " + processRetryCnt + " while Processing File: " + lf);
						fails++;
					}
				}
			} // end for j through processRetryCnt

			if (fails >= consecutiveFailureAllowance) {
				throw new DiskMirrorException("Too Many Consecutive Failures (" + fails + " fails of a maximum allowance of " + consecutiveFailureAllowance + ")");
			}
		} // end for i through totalFiles

		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;

		dMirror.log(">>>> Copied " + totalFilesCopied + (totalFiles == 1 ? " File" : " Files") + " in " + StringUtils.CompactHumanReadableTime(totalTime));
	}

}
