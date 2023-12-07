package com.roguelogic.storage.sal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MountPoint {

  private StorageAbstractionLayer sal;
  private String spaceName;

  public MountPoint(StorageAbstractionLayer sal, String spaceName) {
    this.sal = sal;
    this.spaceName = spaceName;
  }

  public void mount(StorageDevice device, String path, int mbSize, boolean readable, boolean writeable) throws IOException {
    sal.mount(spaceName, device, path, mbSize, readable, writeable);
  }

  public void unmount() throws IOException {
    sal.unmount(spaceName);
  }

  public InputStream openInputStream(String relativePath) throws IOException {
    return sal.openInputStream(spaceName, relativePath);
  }

  public OutputStream openOutputStream(String relativePath) throws IOException {
    return sal.openOutputStream(spaceName, relativePath);
  }

  public String[] listFiles(String relativePath) throws IOException {
    return sal.listFiles(spaceName, relativePath);
  }

  public void removeFile(String relativePath) throws IOException {
    sal.removeFile(spaceName, relativePath);
  }

  public String[] listDirectories(String relativePath) throws IOException {
    return sal.listDirectories(spaceName, relativePath);
  }

  public void createDirectory(String relativePath) throws IOException {
    sal.createDirectory(spaceName, relativePath);
  }

  public void removeDirectory(String relativePath) throws IOException {
    sal.removeDirectory(spaceName, relativePath);
  }

  public void renameFile(String oldRelativePath, String newRelativePath) throws IOException {
    sal.renameFile(spaceName, oldRelativePath, newRelativePath);
  }

  public void renameDirectory(String oldRelativePath, String newRelativePath) throws IOException {
    sal.renameDirectory(spaceName, oldRelativePath, newRelativePath);
  }

  public boolean dirExists(String relativePath) throws IOException {
    boolean exists = false;
    String[] dirList;

    relativePath = SALUtils.RemovePathSeparatorPrefix(relativePath);
    dirList = listDirectories(null);

    for (int i = 0; i < dirList.length; i++) {
      exists = (relativePath.equals(dirList[i]));

      if (exists) {
        break;
      }
    }

    return exists;
  }

}
