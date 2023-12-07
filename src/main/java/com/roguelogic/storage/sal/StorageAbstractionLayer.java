/*
 * Created on Aug 30, 2005
 */
package com.roguelogic.storage.sal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.roguelogic.util.Logger;

/**
 * @author rilardi
 */

public class StorageAbstractionLayer {

  protected HashMap<String, StorageDescriptor> storageSpaces;

  public static final int STORAGE_SIZE_UNLIMITED = -1;

  public static final int FILE_OBJECT_TYPE_ALL = 0;
  public static final int FILE_OBJECT_TYPE_FILE = 1;
  public static final int FILE_OBJECT_TYPE_DIRECTORY = 2;

  public StorageAbstractionLayer() {
    storageSpaces = new HashMap<String, StorageDescriptor>();
  }

  public void mount(String spaceName, StorageDevice device, String path, int mbSize, boolean readable, boolean writeable) throws IOException {
    StorageDescriptor descriptor;
    Logger logger = Logger.GetInstance();
    StringBuffer sb;

    if (spaceName != null && !storageSpaces.containsKey(spaceName.trim())) {
      descriptor = new StorageDescriptor();
      descriptor.setName(spaceName.trim());
      descriptor.setPath(path.trim());
      descriptor.setMbSize((mbSize < 0 ? STORAGE_SIZE_UNLIMITED : mbSize));
      descriptor.setReadable(readable);
      descriptor.setWriteable(writeable);
      descriptor.setDevice(device);

      storageSpaces.put(spaceName, descriptor);

      //Specific Device Mounting
      device.mount(descriptor);

      sb = new StringBuffer();
      sb.append("Mounting: ");
      sb.append(descriptor.toString());
      logger.debug(sb.toString());
    }
    else {
      logger.debug("Storage Space \"" + spaceName + "\" is already mounted!");
    }
  }

  public void unmount(String spaceName) throws IOException {
    StorageDescriptor descriptor;
    StorageDevice device;
    StringBuffer sb;
    Logger logger = Logger.GetInstance();

    descriptor = (StorageDescriptor) storageSpaces.remove(spaceName);

    if (descriptor != null) {

      device = descriptor.getDevice();

      sb = new StringBuffer();
      sb.append("Unmounting: ");
      sb.append(descriptor.toString());
      logger.debug(sb.toString());

      //Specific Device Unmounting
      if (device != null) {
        device.unmount(descriptor);
      }
    }
    else {
      logger.debug("Storage Space \"" + spaceName + "\" is NOT mounted!");
    }
  }

  public void unmountAll() throws IOException {
    ArrayList<StorageDescriptor> descriptors;
    StorageDescriptor descriptor;

    descriptors = new ArrayList<StorageDescriptor>(storageSpaces.values());
    while (!descriptors.isEmpty()) {
      descriptor = descriptors.remove(0);
      unmount(descriptor.getName());
    }
  }

  public InputStream openInputStream(String spaceName, String relativePath) throws IOException {
    InputStream ins = null;
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isReadable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Readable!");
    }

    device = descriptor.getDevice();

    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    ins = device.openInputStream(descriptor, relativePath);

    return ins;
  }

  public OutputStream openOutputStream(String spaceName, String relativePath) throws IOException {
    OutputStream outs = null;
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isWriteable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Writeable!");
    }

    device = descriptor.getDevice();

    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    outs = device.openOutputStream(descriptor, relativePath);

    return outs;
  }

  public String[] listFiles(String spaceName, String relativePath) throws IOException {
    String[] list = null;
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isReadable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Readable!");
    }

    device = descriptor.getDevice();

    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    list = device.listFiles(descriptor, relativePath);

    return list;
  }

  public void removeFile(String spaceName, String relativePath) throws IOException {
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isReadable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Readable!");
    }

    device = descriptor.getDevice();

    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    device.removeFile(descriptor, relativePath);
  }

  public String[] listDirectories(String spaceName, String relativePath) throws IOException {
    String[] list = null;
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isReadable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Readable!");
    }

    device = descriptor.getDevice();

    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    list = device.listDirectories(descriptor, relativePath);

    return list;
  }

  public void createDirectory(String spaceName, String relativePath) throws IOException {
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isWriteable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Writeable!");
    }

    if (relativePath != null) {
      relativePath = relativePath.trim();
    }

    if (relativePath == null || relativePath.length() == 0 || relativePath.equals("/") || relativePath.equals(".") || relativePath.equals("/.") || relativePath.equals("./")
        || relativePath.equals("/./")) {
      throw new IOException("The Relative Directory Name can NOT be NULL, Empty, '/', '\\', '.'!");
    }

    device = descriptor.getDevice();

    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    device.createDirectory(descriptor, relativePath);
  }

  public void removeDirectory(String spaceName, String relativePath) throws IOException {
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isWriteable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Writeable!");
    }

    if (relativePath != null) {
      relativePath = relativePath.trim();
    }

    if (relativePath == null || relativePath.length() == 0 || relativePath.equals("/") || relativePath.equals(".") || relativePath.equals("/.") || relativePath.equals("./")
        || relativePath.equals("/./")) {
      throw new IOException("The Relative Directory Name can NOT be NULL, Empty, '/', '\\', '.'!");
    }

    device = descriptor.getDevice();
    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    device.removeDirectory(descriptor, relativePath);
  }

  public void renameFile(String spaceName, String oldRelativePath, String newRelativePath) throws IOException {
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isReadable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Readable!");
    }

    device = descriptor.getDevice();
    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    device.renameFile(descriptor, oldRelativePath, newRelativePath);
  }

  public void renameDirectory(String spaceName, String oldRelativePath, String newRelativePath) throws IOException {
    StorageDescriptor descriptor;
    StorageDevice device;

    descriptor = (StorageDescriptor) storageSpaces.get(spaceName);

    //If no descriptor was found throw an Exception
    if (descriptor == null) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" NOT found!");
    }

    //If the storage space is not readable throw an Exception
    if (!descriptor.isReadable()) {
      throw new IOException("Storage Descriptor for Space Name \"" + spaceName + "\" is NOT Readable!");
    }

    device = descriptor.getDevice();
    //If the storage device was not found throw an Exception
    if (device == null) {
      throw new IOException("Storage Device for Space Name \"" + spaceName + "\" NOT Found!");
    }

    device.renameDirectory(descriptor, oldRelativePath, newRelativePath);
  }

}
