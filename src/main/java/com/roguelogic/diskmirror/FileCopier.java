/**
 * Created Feb 24, 2009
 */
package com.roguelogic.diskmirror;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class FileCopier {

	public static final int BUFFER_SIZE = 32768;

	public static final String PROP_WINDOWS_DRIVE_LETTER_MAPPING = "WindowsDriveLetterMapping";
	public static final String PROP_BACKUP_ROOT_DIR = "BackupRootDir";

	private DiskMirror dMirror;

	private HashMap<String, String> winDriveLetterMap;

	private String backupRootDir;

	public FileCopier(DiskMirror dMirror) {
		this.dMirror = dMirror;

		winDriveLetterMap = new HashMap<String, String>();
	}

	public void init() throws IOException {
		dMirror.log("Initializing File Copier...");

		readProperties();
	}

	private void readProperties() {
		String tmp;
		String[] tmpArr, tmpArr2;

		tmp = dMirror.getProperty(PROP_WINDOWS_DRIVE_LETTER_MAPPING);

		if (!StringUtils.IsNVL(tmp)) {
			tmpArr = tmp.split(";");
			tmpArr = StringUtils.Trim(tmpArr);

			for (int i = 0; i < tmpArr.length; i++) {
				tmpArr2 = tmpArr[i].split("=");
				winDriveLetterMap.put(tmpArr2[0].trim().toUpperCase(), tmpArr2[1].trim());
			}
		}

		backupRootDir = dMirror.getProperty(PROP_BACKUP_ROOT_DIR);
		backupRootDir = FilenameUtils.NormalizeToUnix(backupRootDir);
	}

	private String relativeizeLocalFile(String absPath) {
		String relPath, driveLetter, driveLetterMapDir;

		absPath = FilenameUtils.NormalizeToUnix(absPath.trim());

		if (absPath.length() >= 3 && ":/".equals(absPath.substring(1, 3))) {
			// Contains Windows Drive Letter
			driveLetter = absPath.substring(0, 1).toUpperCase();
			driveLetterMapDir = winDriveLetterMap.get(driveLetter);

			if (!StringUtils.IsNVL(driveLetterMapDir)) {
				relPath = absPath.substring(3);
				relPath = (new StringBuffer()).append("/").append(driveLetterMapDir).append("/").append(relPath).toString();
			}
			else {
				relPath = absPath.substring(2);
			}

			// dMirror.log("  >> Windows Drive Letter Detected. File Path Change: " + bakUpFilePath);
		}
		else {
			relPath = absPath;
		}

		return relPath;
	}

	private String getAbsoluteBackupFilePath(LocalFile lf) {
		String absPath = null;
		StringBuffer sb = new StringBuffer();
		String relPath;

		relPath = relativeizeLocalFile(lf.getName());

		sb.append(backupRootDir);

		relPath = relPath.trim();

		if (!backupRootDir.endsWith("/") && !relPath.startsWith("/")) {
			sb.append("/");
		}

		sb.append(relPath);

		absPath = sb.toString();

		return absPath;
	}

	private void ensureBackupFilePathExists(String backupFilePath) {
		File dir;
		String dirPath;

		dirPath = FilenameUtils.GetParentDirectory(backupFilePath);

		dir = new File(dirPath);
		dir.mkdirs();
	}

	private void synchronizeTimestamps(String sourcePath, String targetPath) {
		File src, trgt;

		src = new File(sourcePath);
		trgt = new File(targetPath);

		trgt.setLastModified(src.lastModified());
	}

	public boolean sourceEqualsTarget(LocalFile lf) {
		File src, trgt;
		boolean same = false;

		src = new File(lf.getName());
		trgt = new File(getAbsoluteBackupFilePath(lf));

		same = src.lastModified() == trgt.lastModified() && src.length() == trgt.length();

		return same;
	}

	public void removeFromTarget(LocalFile lf) {
		String targetPath;
		File trgt;

		targetPath = getAbsoluteBackupFilePath(lf);

		dMirror.log("  >> Removing Backup: " + targetPath);

		trgt = new File(targetPath);

		if (trgt.exists()) {
			if (trgt.isFile()) {
				trgt.delete();
			}
			else {
				SystemUtils.RecuriveDelete(trgt);
			}
		}
	}

	public void copyFile(LocalFile lf) throws IOException {
		String targetPath;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		try {
			targetPath = getAbsoluteBackupFilePath(lf);

			dMirror.log("  >> Copying To: " + targetPath);

			ensureBackupFilePathExists(targetPath);

			fis = new FileInputStream(lf.getName());
			bis = new BufferedInputStream(fis);

			fos = new FileOutputStream(targetPath);
			bos = new BufferedOutputStream(fos);

			SystemUtils.Copy(bis, bos);

			bos.close();
			bos = null;
			fos.close();
			fos = null;

			synchronizeTimestamps(lf.getName(), targetPath);
		} // End try block
		finally {
			if (bos != null) {
				try {
					bos.close();
				}
				catch (Exception e) {}
			}

			if (fos != null) {
				try {
					fos.close();
				}
				catch (Exception e) {}
			}

			if (bis != null) {
				try {
					bis.close();
				}
				catch (Exception e) {}
			}

			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {}
			}
		} // End finally block
	}

}
