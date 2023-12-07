package com.roguelogic.storage.sal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarReadOnlyStorageDevice implements StorageDevice {

  public JarReadOnlyStorageDevice() {}

  public void mount(StorageDescriptor descriptor) throws IOException {}

  public void unmount(StorageDescriptor descriptor) throws IOException {}

  private JarEntry findEntry(JarInputStream jarIns, String name) throws IOException {
    JarEntry jEntry = null;

    name = SALUtils.RemovePathSeparatorPrefix(name);

    jEntry = jarIns.getNextJarEntry();

    while (jEntry != null) {
      if (name.equals(jEntry.getName())) {
        break;
      }

      jEntry = jarIns.getNextJarEntry();
    }

    return jEntry;
  }

  public InputStream openInputStream(StorageDescriptor descriptor, String relativePath) throws IOException {
    FileInputStream ins = null;
    JarInputStream jarIns = null;
    JarEntry jEntry;

    ins = new FileInputStream(descriptor.getPath());

    try {
      jarIns = new JarInputStream(ins);

      jEntry = findEntry(jarIns, relativePath);

      if (jEntry == null) {
        throw new IOException("The file '" + relativePath + "' was NOT found in the JAR File System!");
      }
      else if (jEntry.isDirectory()) {
        throw new IOException("The path '" + relativePath + "' is a Directory and can NOT be opened for reading!");
      }
    }
    catch (IOException e) {
      try {
        jarIns.close();
      }
      catch (Exception e2) {}
      jarIns = null;

      try {
        ins.close();
      }
      catch (Exception e2) {}
      ins = null;

      throw e;
    }

    return jarIns;
  }

  public long fileSize(StorageDescriptor descriptor, String relativePath) throws IOException {
    FileInputStream ins = null;
    JarInputStream jarIns = null;
    JarEntry jEntry;
    long size = 0;

    try {
      ins = new FileInputStream(descriptor.getPath());
      jarIns = new JarInputStream(ins);

      jEntry = findEntry(jarIns, relativePath);

      if (jEntry != null) {
        size = jEntry.getSize();
      }
    }
    finally {
      try {
        jarIns.close();
      }
      catch (Exception e) {}
      jarIns = null;

      try {
        ins.close();
      }
      catch (Exception e) {}
      ins = null;
    }

    return size;
  }

  public String[] listFiles(StorageDescriptor descriptor, String relativePath) throws IOException {
    return null;
  }

  public String[] listDirectories(StorageDescriptor descriptor, String relativePath) throws IOException {
    return null;
  }

  //Unsupported Operations---------------------------------------------->

  public OutputStream openOutputStream(StorageDescriptor descriptor, String relativePath) throws IOException {
    throw new IOException("Unsupported JAR Operation (Read Only SAL Device)!");
  }

  public void removeFile(StorageDescriptor descriptor, String relativePath) throws IOException {
    throw new IOException("Unsupported JAR Operation (Read Only SAL Device)!");
  }

  public void createDirectory(StorageDescriptor descriptor, String relativePath) throws IOException {
    throw new IOException("Unsupported JAR Operation (Read Only SAL Device)!");
  }

  public void removeDirectory(StorageDescriptor descriptor, String relativePath) throws IOException {
    throw new IOException("Unsupported JAR Operation (Read Only SAL Device)!");
  }

  public void renameFile(StorageDescriptor descriptor, String oldRelativePath, String newRelativePath) throws IOException {
    throw new IOException("Unsupported JAR Operation (Read Only SAL Device)!");
  }

  public void renameDirectory(StorageDescriptor descriptor, String oldRelativePath, String newRelativePath) throws IOException {
    throw new IOException("Unsupported JAR Operation (Read Only SAL Device)!");
  }

  //Unsupported Operations---------------------------------------------->

}
