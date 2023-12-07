package com.roguelogic.storage.sal;

import static com.roguelogic.storage.sal.StorageAbstractionLayer.FILE_OBJECT_TYPE_ALL;
import static com.roguelogic.storage.sal.StorageAbstractionLayer.FILE_OBJECT_TYPE_DIRECTORY;
import static com.roguelogic.storage.sal.StorageAbstractionLayer.FILE_OBJECT_TYPE_FILE;

import java.io.File;
import java.io.IOException;

public class SALUtils {

  public static String GetFullPath(StorageDescriptor descriptor, String relativePath) throws IOException {
    StringBuffer fullPath = new StringBuffer();

    fullPath = new StringBuffer(descriptor.getPath());

    /*if (fullPath.charAt(fullPath.length() - 1) != '/') {
     fullPath.append("/");
     }*/

    if (relativePath != null && relativePath.trim().length() > 0) {
      relativePath = relativePath.trim();

      if (relativePath.indexOf("..") != -1) {
        throw new IOException("The Relative Path can NOT include '..'!");
      }

      if (relativePath.indexOf('\\') != -1) {
        throw new IOException("The Relative Path can NOT include '\\'!");
      }

      /*if (fullPath.charAt(fullPath.length() - 1) != '/' && relativePath.charAt(0) != '/') {
       fullPath.append("/");
       }*/

      if (fullPath.charAt(fullPath.length() - 1) == '/' && relativePath.charAt(0) == '/') {
        fullPath.deleteCharAt(fullPath.length() - 1);
      }

      fullPath.append(relativePath.trim());
    }

    return fullPath.toString();
  }

  public static String[] GetFileObjectList(String fullPath, int objectType) throws IOException {
    String[] list = null;
    File dir;
    File[] files;
    int objCnt;

    dir = new File(fullPath);
    if (!dir.isDirectory()) {
      throw new IOException("Path \"" + fullPath + "\" is NOT a Directory!");
    }

    files = dir.listFiles();
    if (files == null) {
      throw new IOException("Path \"" + fullPath + "\" could NOT be read!");
    }

    //Get Object Count
    if (objectType == FILE_OBJECT_TYPE_ALL) {
      objCnt = files.length;
    }
    else {
      objCnt = 0;
      for (int i = 0; i < files.length; i++) {
        if ((objectType == FILE_OBJECT_TYPE_FILE && files[i].isFile()) || (objectType == FILE_OBJECT_TYPE_DIRECTORY && files[i].isDirectory())) {
          objCnt++;
        }
      }
    }

    //Set Object Names
    list = new String[objCnt];
    objCnt = 0;
    for (int i = 0; i < files.length; i++) {
      if (objectType == FILE_OBJECT_TYPE_ALL || (objectType == FILE_OBJECT_TYPE_FILE && files[i].isFile())
          || (objectType == FILE_OBJECT_TYPE_DIRECTORY && files[i].isDirectory())) {
        list[objCnt++] = files[i].getName();
      }
    }

    return list;
  }

  public static String RemovePathSeparatorPrefix(String path) {
    if (path != null && (path.startsWith("/") || path.startsWith("\\"))) {
      return path.substring(1);
    }
    else {
      return path;
    }
  }

  public static String CombineDirStrs(String part1, String part2) {
    StringBuffer sb = new StringBuffer();

    if (part1 != null && part1.trim().length() > 0) {
      sb.append(part1.trim());
    }

    if (part2 != null && part2.trim().length() > 0) {
      if (sb.length() > 0 && !part1.trim().endsWith("/") && !part2.trim().startsWith("/")) {
        sb.append("/");
      }

      sb.append(part2.trim());
    }

    return sb.toString();
  }

}
