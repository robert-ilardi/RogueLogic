package com.roguelogic.storage.sal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StorageDevice {

  public void mount(StorageDescriptor descriptor) throws IOException;

  public void unmount(StorageDescriptor descriptor) throws IOException;

  public InputStream openInputStream(StorageDescriptor descriptor, String relativePath) throws IOException;

  public OutputStream openOutputStream(StorageDescriptor descriptor, String relativePath) throws IOException;

  public String[] listFiles(StorageDescriptor descriptor, String relativePath) throws IOException;

  public void removeFile(StorageDescriptor descriptor, String relativePath) throws IOException;

  public String[] listDirectories(StorageDescriptor descriptor, String relativePath) throws IOException;

  public void createDirectory(StorageDescriptor descriptor, String relativePath) throws IOException;

  public void removeDirectory(StorageDescriptor descriptor, String relativePath) throws IOException;

  public void renameFile(StorageDescriptor descriptor, String oldRelativePath, String newRelativePath) throws IOException;

  public void renameDirectory(StorageDescriptor descriptor, String oldRelativePath, String newRelativePath) throws IOException;

  public long fileSize(StorageDescriptor descriptor, String relativePath) throws IOException;

}
