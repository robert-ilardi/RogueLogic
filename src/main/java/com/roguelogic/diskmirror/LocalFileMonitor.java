/**
 * Created Feb 24, 2009
 */
package com.roguelogic.diskmirror;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.roguelogic.util.FilenameUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class LocalFileMonitor {

	public static final String PROP_INCLUDE_FILE = "IncludeFile";
	public static final String PROP_EXCLUDE_FILE = "ExcludeFile";
	public static final String PROP_REMOVAL_FILE = "RemovalFile";

	private String includeFile;
	private String excludeFile;
	private String removalFile;

	private DiskMirror dMirror;

	private HashMap<String, LocalFile> includeList;
	private HashMap<String, LocalFile> excludeList;
	private HashMap<String, LocalFile> removalList;

	public LocalFileMonitor(DiskMirror dMirror) {
		this.dMirror = dMirror;
	}

	public void init() throws IOException {
		dMirror.log("Initializing Local File Monitor:");

		readProperties();
		loadIncludeList();
		loadExcludeList();
		loadRemovalList();
	}

	private void readProperties() {
		includeFile = dMirror.getProperty(PROP_INCLUDE_FILE);
		excludeFile = dMirror.getProperty(PROP_EXCLUDE_FILE);
		removalFile = dMirror.getProperty(PROP_REMOVAL_FILE);
	}

	private void loadLocalFileList(String listFile, HashMap<String, LocalFile> listMap) throws IOException {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String line;
		LocalFile lf;
		File f;

		try {
			f = new File(listFile);

			if (!f.exists()) {
				dMirror.log("      >> List File Does NOT Exist! Skipping...");
			}
			else {
				fis = new FileInputStream(listFile);
				isr = new InputStreamReader(fis);
				br = new BufferedReader(isr);

				line = br.readLine();
				while (line != null) {

					lf = LocalFile.ParseLocalFileConfig(line);

					if (lf != null) {
						listMap.put(lf.getName(), lf);
					}

					line = br.readLine();
				}
			} // End else block
		} // End try block
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

			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {}
			}
		}
	}

	private void loadIncludeList() throws IOException {
		dMirror.log("  >> Loading Include List");

		dMirror.log("    >> Using File: " + includeFile);

		includeList = new HashMap<String, LocalFile>();

		loadLocalFileList(includeFile, includeList);

		dMirror.log("      >> Include List Size = " + includeList.size());
	}

	private void loadExcludeList() throws IOException {
		dMirror.log("  >> Loading Exclude List");

		dMirror.log("    >> Using File: " + excludeFile);

		excludeList = new HashMap<String, LocalFile>();

		loadLocalFileList(excludeFile, excludeList);

		dMirror.log("      >> Exclude List Size = " + excludeList.size());
	}

	private void loadRemovalList() throws IOException {
		dMirror.log("  >> Loading Removal List");

		dMirror.log("    >> Using File: " + removalFile);

		removalList = new HashMap<String, LocalFile>();

		loadLocalFileList(removalFile, removalList);

		dMirror.log("      >> Removal List Size = " + removalList.size());
	}

	public ArrayList<LocalFile> getLocalFiles() throws IOException {
		ArrayList<LocalFile> fileLst = new ArrayList<LocalFile>();
		File root;

		for (LocalFile rootLf : includeList.values()) {
			root = new File(rootLf.getName());

			if (!inExcludeList(root)) {
				if (root.exists() && root.isDirectory()) {
					// Dir
					traverseDir(rootLf, root, fileLst);
				}
				else if (root.exists() && root.isFile()) {
					// File
					fileLst.add(rootLf);
				}
			}
		}

		return fileLst;
	}

	private void traverseDir(LocalFile rootLf, File root, ArrayList<LocalFile> fileLst) throws IOException {
		File[] ls;
		LocalFile lf;

		ls = root.listFiles();

		if (ls == null) {
			return;
		}

		for (File f : ls) {
			lf = new LocalFile();
			lf.setName(f.getAbsolutePath());
			lf.setIncludeSubDirs(rootLf.isIncludeSubDirs());
			lf.setUpdateAlways(rootLf.isUpdateAlways());

			if (!inExcludeList(f)) {
				if (f.isDirectory() && rootLf.isIncludeSubDirs()) {
					traverseDir(lf, f, fileLst);
				}
				else if (f.isFile()) {
					fileLst.add(lf);
				}
			}
		}
	}

	private boolean inExcludeList(File f) {
		boolean excluded = false;
		String normalizedPath;

		normalizedPath = FilenameUtils.NormalizeToUnix(f.getAbsolutePath());

		excluded = excludeList.containsKey(normalizedPath);

		return excluded;
	}

	public ArrayList<LocalFile> getRemovalList() throws IOException {
		ArrayList<LocalFile> fileLst = new ArrayList<LocalFile>();

		fileLst.addAll(removalList.values());

		return fileLst;
	}

}
