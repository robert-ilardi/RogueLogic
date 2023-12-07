/**
 * Created Apr 6, 2007
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

import java.io.File;

/**
 * @author Robert C. Ilardi
 * 
 */

public class FilenameUtils {

	public static String GetDeepestDirectory(String path) {
		String dir = null;
		int lastIndex;

		if (path != null && path.trim().length() > 0) {
			path = path.trim();

			if (path.endsWith("/") && path.length() > 1) {
				path = path.substring(0, path.length() - 1);
			}

			dir = path;

			if (path.length() > 1) {
				lastIndex = path.lastIndexOf("/");

				if (lastIndex >= 0) {
					dir = path.substring(lastIndex + 1);
				}
			}
		}

		return dir;
	}

	public static String GetParentDirectory(String path) {
		String parentDir = "/";
		int lastIndex;

		if (path != null && path.trim().length() > 0) {
			path = path.trim();

			if (path.endsWith("/") && path.length() > 1) {
				path = path.substring(0, path.length() - 1);
			}

			if (path.length() > 1) {
				lastIndex = path.lastIndexOf("/");

				if (lastIndex > 0) {
					parentDir = path.substring(0, lastIndex);
				}
			}
		}

		return parentDir;
	}

	public static String GetFilename(String path) {
		String file = null;
		int lastIndex;

		if (path != null && path.trim().length() > 0) {
			path = path.trim();

			if (!path.endsWith("/")) {
				file = path;
				lastIndex = path.lastIndexOf("/");

				if (lastIndex >= 0) {
					file = path.substring(lastIndex + 1, path.length());
				}
			}
		}

		return file;
	}

	public static String[] SplitDirAndFile(String path) {
		String[] arr = new String[2];
		int lastIndex;

		if (path != null && path.trim().length() > 0) {
			path = path.trim();

			if (!path.endsWith("/")) {
				lastIndex = path.lastIndexOf("/");

				if (lastIndex >= 0) {
					arr[0] = path.substring(0, lastIndex);
					arr[1] = path.substring(lastIndex + 1, path.length());
				}
				else {
					arr[0] = "/"; // Assume Root Directory
					arr[1] = path;
				}
			}
			else if (path.length() > 1) {
				arr[0] = path.substring(0, path.length() - 1);
			}
			else {
				arr[0] = path;
			}
		}

		return arr;
	}

	public static String GetFileExt(String filename) {
		String ext = null;

		int i = filename.lastIndexOf('.');

		if (i > 0 && i < filename.length() - 1) {
			ext = filename.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	public static boolean IsValidPath(String path) {
		File f;

		f = new File(path);

		return f.exists();
	}

	public static boolean IsValidFile(String filePath) {
		File f;

		f = new File(filePath);

		return f.exists() && f.isFile();
	}

	public static boolean IsValidDirectory(String dirPath) {
		File f;

		f = new File(dirPath);

		return f.exists() && f.isDirectory();
	}

	public static String NormalizeToUnix(String path) {
		String normalizedPath = null;

		if (!StringUtils.IsNVL(path)) {
			normalizedPath = path.trim().replaceAll("\\\\", "/");

			if (normalizedPath.endsWith("/") && normalizedPath.length() > 1) {
				normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1).trim();
			}
		}

		return normalizedPath;
	}

}
