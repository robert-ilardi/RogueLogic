/**
 * Created Feb 24, 2009
 */
package com.roguelogic.diskmirror;

import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class LocalFile {

	private String name;
	private boolean includeSubDirs;
	private boolean dirForced;
	private boolean updateAlways;

	public LocalFile() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isIncludeSubDirs() {
		return includeSubDirs;
	}

	public void setIncludeSubDirs(boolean includeSubDirs) {
		this.includeSubDirs = includeSubDirs;
	}

	public boolean isDirForced() {
		return dirForced;
	}

	public void setDirForced(boolean dirForced) {
		this.dirForced = dirForced;
	}

	public boolean isUpdateAlways() {
		return updateAlways;
	}

	public void setUpdateAlways(boolean updateAlways) {
		this.updateAlways = updateAlways;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[LocalFile - Name: ");
		sb.append(name);

		sb.append(" ; IncludeSubDirs: ");
		sb.append((includeSubDirs ? "YES" : "NO"));

		sb.append(" ; DirForced: ");
		sb.append((dirForced ? "YES" : "NO"));

		sb.append(" ; UpdateAlways: ");
		sb.append((updateAlways ? "YES" : "NO"));

		return sb.toString();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		LocalFile other;
		boolean same = false;

		if (obj instanceof LocalFile) {
			other = (LocalFile) obj;
			same = name.equals(other.name);
		}

		return same;
	}

	public static LocalFile ParseLocalFileConfig(String configLine) {
		String[] tokens;
		LocalFile lf = null;

		if (configLine != null) {
			tokens = configLine.trim().split("\\|");

			tokens = StringUtils.Trim(tokens);

			lf = new LocalFile();

			lf.setName(FilenameUtils.NormalizeToUnix(tokens[0]));

			ProcessAndSetConfigOptions(lf, tokens);
		}

		return lf;
	}

	private static void ProcessAndSetConfigOptions(LocalFile lf, String[] tokens) {
		for (int i = 1; i < tokens.length; i++) {
			if ("S".equalsIgnoreCase(tokens[i])) {
				lf.setIncludeSubDirs(true);
			}
			else if ("D".equalsIgnoreCase(tokens[i])) {
				lf.setDirForced(true);
			}
			else if ("U".equalsIgnoreCase(tokens[i])) {
				lf.setUpdateAlways(true);
			}
		}
	}
}
