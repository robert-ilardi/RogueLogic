package com.roguelogic.storage.sal;

import static com.roguelogic.storage.sal.StorageAbstractionLayer.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PhysicalStorageDevice implements StorageDevice {

  public PhysicalStorageDevice() {}

  public void mount(StorageDescriptor descriptor) throws IOException {}

  public void unmount(StorageDescriptor descriptor) throws IOException {}

  public InputStream openInputStream(StorageDescriptor descriptor, String relativePath) throws IOException {
    String fullPath;
    InputStream ins;

    fullPath = SALUtils.GetFullPath(descriptor, relativePath);
    ins = new FileInputStream(fullPath);

    return ins;
  }

  public OutputStream openOutputStream(StorageDescriptor descriptor, String relativePath) throws IOException {
    String fullPath;
    OutputStream outs;

    fullPath = SALUtils.GetFullPath(descriptor, relativePath);
    outs = new FileOutputStream(fullPath);

    return outs;
  }

  public String[] listFiles(StorageDescriptor descriptor, String relativePath) throws IOException {
    String fullPath;
    String[] list;

    fullPath = SALUtils.GetFullPath(descriptor, relativePath);
    list = SALUtils.GetFileObjectList(fullPath, FILE_OBJECT_TYPE_FILE);

    return list;
  }

  public void removeFile(StorageDescriptor descriptor, String relativePath) throws IOException {
    File f;
    String fullPath;

    fullPath = SALUtils.GetFullPath(descriptor, relativePath);
    f = new File(fullPath);

    if (!f.isFile()) {
      throw new IOException("The path \"" + fullPath + "\" does NOT refer to a file!");
    }

    if (!f.delete()) {
      throw new IOException("The File \"" + fullPath + "\" could NOT be deleted!");
    }
  }

  public String[] listDirectories(StorageDescriptor descriptor, String relativePath) throws IOException {
    String fullPath;
    String[] list;

    fullPath = SALUtils.GetFullPath(descriptor, relativePath);
    list = SALUtils.GetFileObjectList(fullPath, FILE_OBJECT_TYPE_DIRECTORY);

    return list;
  }

  public void createDirectory(StorageDescriptor descriptor, String relativePath) throws IOException {
    String fullPath;
    File dir;

    fullPath = SALUtils.GetFullPath(descriptor, relativePath);
    dir = new File(fullPath);
    if (!dir.mkdirs()) {
      throw new IOException("The Directory \"" + fullPath + "\" could NOT be created!");
    }
  }

  public void removeDirectory(StorageDescriptor descriptor, String relativePath) throws IOException {
    String fullPath;
    File dir, baseDir;

    fullPath = SALUtils.GetFullPath(descriptor, relativePath);
    dir = new File(fullPath);
    baseDir = new File(descriptor.getPath());

    if (!dir.isDirectory()) {
      throw new IOException("The path \"" + fullPath + "\" does NOT refer to a directory!");
    }

    if (dir.equals(baseDir)) {
      throw new IOException("The ROOT Storage Space Directory can NOT be deleted!");
    }

    if (!dir.delete()) {
      throw new IOException("The Directory \"" + fullPath + "\" could NOT be deleted!");
    }
  }

  public void renameFile(StorageDescriptor descriptor, String oldRelativePath, String newRelativePath) throws IOException {
    File oldFile, newFile;
    String oldFullPath, newFullPath;

    oldFullPath = SALUtils.GetFullPath(descriptor, oldRelativePath);
    newFullPath = SALUtils.GetFullPath(descriptor, newRelativePath);

    oldFile = new File(oldFullPath);
    if (!oldFile.isFile()) {
      throw new IOException("The path \"" + oldFullPath + "\" does NOT refer to a file!");
    }

    newFile = new File(newFullPath);
    if (newFile.exists()) {
      throw new IOException("The path \"" + newFullPath + "\" already exists!");
    }

    if (newFile.isDirectory()) {
      throw new IOException("The path \"" + newFullPath + "\" does NOT refer to a file!");
    }

    if (!oldFile.renameTo(newFile)) {
      throw new IOException("The File \"" + oldFullPath + "\" could NOT be renamed!");
    }
  }

  public void renameDirectory(StorageDescriptor descriptor, String oldRelativePath, String newRelativePath) throws IOException {
    File oldDir, newDir;
    String oldFullPath, newFullPath;

    oldFullPath = SALUtils.GetFullPath(descriptor, oldRelativePath);
    newFullPath = SALUtils.GetFullPath(descriptor, newRelativePath);

    oldDir = new File(oldFullPath);
    if (!oldDir.isDirectory()) {
      throw new IOException("The path \"" + oldFullPath + "\" does NOT refer to a directory!");
    }

    newDir = new File(newFullPath);
    if (newDir.exists()) {
      throw new IOException("The path \"" + newFullPath + "\" already exists!");
    }

    if (newDir.isFile()) {
      throw new IOException("The path \"" + newFullPath + "\" does NOT refer to a directory!");
    }

    if (!oldDir.renameTo(newDir)) {
      throw new IOException("The Directory \"" + oldFullPath + "\" could NOT be renamed!");
    }
  }

  public long fileSize(StorageDescriptor descriptor, String relativePath) throws IOException {
    long len;
    String fullPath;
    File f;

    fullPath = SALUtils.GetFullPath(descriptor, relativePath);
    f = new File(fullPath);
    len = f.length();

    return len;
  }

}
