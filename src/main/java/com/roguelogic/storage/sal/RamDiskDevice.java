/*
 * Created on Aug 30, 2005
 */
package com.roguelogic.storage.sal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * @author rilardi
 */

public class RamDiskDevice implements StorageDevice {

  private HashMap<String, HashMap<String, Object>> directories;

  public RamDiskDevice() {
    directories = new HashMap<String, HashMap<String, Object>>();
  }

  public void mount(StorageDescriptor descriptor) throws IOException {
    createDirectory(descriptor, null);
  }

  public void unmount(StorageDescriptor descriptor) throws IOException {
    String[] list;

    //Remove All Virtual Directories under this descriptor.
    list = listDirectories(descriptor, null);
    for (int i = 0; i < list.length; i++) {
      if (list[i].startsWith(descriptor.getPath())) {
        directories.remove(list[i]);
      }
    }
  }

  public InputStream openInputStream(StorageDescriptor descriptor, String relativePath) throws IOException {
    ByteArrayInputStream bais = null;
    BinHolder file;
    byte[] bin;
    String filePath;

    filePath = SALUtils.GetFullPath(descriptor, relativePath);

    if (filePath != null) {
      file = getFile(filePath);
      if (file != null) {
        bin = file.getBin();
        if (bin != null) {
          bais = new ByteArrayInputStream(bin);
        }
      }
    }

    return bais;
  }

  public OutputStream openOutputStream(StorageDescriptor descriptor, String relativePath) throws IOException {
    RamDiskOutputStream rdos = null;
    String filename, filePath;
    HashMap<String, Object> dir;
    BinHolder file;

    filePath = SALUtils.GetFullPath(descriptor, relativePath);

    if (filePath != null) {
      dir = getDirectoryOfFile(filePath);
      filename = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());

      file = new BinHolder();
      dir.put(filename, file);
      rdos = new RamDiskOutputStream();
      rdos.setFile(file);
    }

    return rdos;
  }

  public String[] listFiles(StorageDescriptor descriptor, String relativePath) throws IOException {
    HashMap dir;
    Set filenames;
    Iterator iter;
    String[] fileList = null;
    int cnt;
    String directoryPath;

    directoryPath = SALUtils.GetFullPath(descriptor, relativePath);

    if (directoryPath != null) {
      dir = getDirectory(directoryPath);

      if (dir != null) {
        cnt = 0;
        fileList = new String[dir.size()];
        filenames = dir.keySet();
        iter = filenames.iterator();

        while (iter.hasNext()) {
          fileList[cnt++] = (String) iter.next();
        }
      }
    }

    return fileList;
  }

  public void removeFile(StorageDescriptor descriptor, String relativePath) throws IOException {
    HashMap dir;
    String filename, filePath;

    filePath = SALUtils.GetFullPath(descriptor, relativePath);

    if (filePath != null) {
      dir = getDirectoryOfFile(filePath);
      filename = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());

      if (dir.containsKey(filename)) {
        dir.remove(filename);
      }
      else {
        throw new IOException("File \"" + filePath + "\" does NOT exist!");
      }
    }
  }

  public String[] listDirectories(StorageDescriptor descriptor, String relativePath) throws IOException {
    Set dirSet;
    Iterator iter;
    String[] dirList;
    int cnt;

    cnt = 0;
    dirList = new String[directories.size()];
    dirSet = directories.keySet();
    iter = dirSet.iterator();

    while (iter.hasNext()) {
      dirList[cnt++] = (String) iter.next();
    }

    return dirList;
  }

  public void createDirectory(StorageDescriptor descriptor, String relativePath) throws IOException {
    HashMap<String, Object> dir;
    String path;

    path = SALUtils.GetFullPath(descriptor, relativePath);

    if (path != null) {
      if (!path.trim().endsWith("/")) {
        StringBuffer sb = new StringBuffer(path);
        sb.append("/");
        path = sb.toString();
        sb = null;
      }
      if (!directories.containsKey(path)) {
        dir = new HashMap<String, Object>();
        directories.put(path, dir);
      }
      else {
        throw new IOException("Virtual Directory \"" + path + "\" already exists!");
      }
    }
  }

  public void removeDirectory(StorageDescriptor descriptor, String relativePath) throws IOException {
    HashMap dir;
    String path;

    path = SALUtils.GetFullPath(descriptor, relativePath);

    if (path != null) {
      if (!path.trim().endsWith("/")) {
        StringBuffer sb = new StringBuffer(path);
        sb.append("/");
        path = sb.toString();
        sb = null;
      }
      dir = directories.remove(path);
      if (dir == null) {
        throw new IOException("Virtual Directory \"" + path + "\" does NOT exist!");
      }
    }
  }

  public void renameFile(StorageDescriptor descriptor, String oldRelativePath, String newRelativePath) throws IOException {
    HashMap<String, Object> oldDir, newDir;
    String oldFilename, newFilename;
    String oldFilePath, newFilePath;
    Object file;

    oldFilePath = SALUtils.GetFullPath(descriptor, oldRelativePath);
    newFilePath = SALUtils.GetFullPath(descriptor, newRelativePath);

    if (oldFilePath != null && newFilePath != null) {
      oldDir = getDirectoryOfFile(oldFilePath);
      oldFilename = oldFilePath.substring(oldFilePath.lastIndexOf("/") + 1, oldFilePath.length());

      if (oldDir.containsKey(oldFilename)) {
        newDir = getDirectoryOfFile(newFilePath);
        newFilename = newFilePath.substring(newFilePath.lastIndexOf("/") + 1, newFilePath.length());
        if (newDir.containsKey(newFilename)) {
          throw new IOException("File \"" + newFilePath + "\" already exists!");
        }
        else {
          file = oldDir.remove(oldFilename);
          newDir.put(newFilename, file);
        }
      }
      else {
        throw new IOException("File \"" + oldFilePath + "\" does NOT exist!");
      }
    }
  }

  public void renameDirectory(StorageDescriptor descriptor, String oldRelativePath, String newRelativePath) throws IOException {
    HashMap<String, Object> dir;
    String oldDirPath, newDirPath;

    oldDirPath = SALUtils.GetFullPath(descriptor, oldRelativePath);
    newDirPath = SALUtils.GetFullPath(descriptor, newRelativePath);

    if (oldDirPath != null && newDirPath != null) {
      if (!oldDirPath.trim().endsWith("/")) {
        StringBuffer sb = new StringBuffer(oldDirPath);
        sb.append("/");
        oldDirPath = sb.toString();
        sb = null;
      }

      if (!newDirPath.trim().endsWith("/")) {
        StringBuffer sb = new StringBuffer(newDirPath);
        sb.append("/");
        newDirPath = sb.toString();
        sb = null;
      }

      if (directories.containsKey(newDirPath)) {
        throw new IOException("Virtual Directory \"" + newDirPath + "\" already exists!");
      }
      else {
        dir = directories.remove(oldDirPath);
        if (dir == null) {
          throw new IOException("Virtual Directory \"" + oldDirPath + "\" does NOT exist!");
        }
        directories.put(newDirPath, dir);
      }
    }
  }

  public long fileSize(StorageDescriptor descriptor, String relativePath) throws IOException {
    BinHolder file;
    byte[] bin;
    long size = 0;
    String filePath;

    filePath = SALUtils.GetFullPath(descriptor, relativePath);

    if (filePath != null) {
      file = getFile(filePath);
      if (file != null) {
        bin = file.getBin();
        size = (bin == null ? 0 : bin.length);
      }
    }

    return size;
  }

  public void format() {
    directories.clear();
  }

  private HashMap<String, Object> getDirectoryOfFile(String filePath) throws IOException {
    HashMap<String, Object> dir = null;
    String dirPath;

    if (filePath != null) {
      dirPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
      dir = directories.get(dirPath);

      if (dir == null) {
        throw new IOException("Virtual Directory \"" + dirPath + "\" does NOT exist!");
      }
    }

    return dir;
  }

  private HashMap getDirectory(String dirPath) throws IOException {
    HashMap dir = null;

    if (dirPath != null) {
      if (!dirPath.trim().endsWith("/")) {
        StringBuffer sb = new StringBuffer(dirPath);
        sb.append("/");
        dirPath = sb.toString();
        sb = null;
      }
      dir = directories.get(dirPath);

      if (dir == null) {
        throw new IOException("Virtual Directory \"" + dirPath + "\" does NOT exist!");
      }
    }

    return dir;
  }

  private BinHolder getFile(String filePath) throws IOException {
    String filename;
    HashMap dir;
    BinHolder file = null;

    if (filePath != null) {
      dir = getDirectoryOfFile(filePath);
      filename = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
      file = (BinHolder) dir.get(filename);
    }

    return file;
  }

}
