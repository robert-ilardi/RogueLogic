package com.roguelogic.containercore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.roguelogic.storage.sal.MountPoint;

/**
 * @author Robert C. Ilardi
 *
 */

public class ContainerClassLoader extends ClassLoader {

  protected static final int MAX_BUF = 1024;

  protected HashMap<String, Class<?>> classCache;

  protected MountPoint mountPoint;

  //protected ClassLoader parentCL;

  protected static final boolean DEBUG = true;

  public ContainerClassLoader(MountPoint mountPoint) {
    super();
    this.mountPoint = mountPoint;
    init();
  }

  public ContainerClassLoader(ClassLoader parentCL, MountPoint mountPoint) {
    super(parentCL);
    //this.parentCL = parentCL;
    this.mountPoint = mountPoint;
    init();
  }

  protected void init() {
    classCache = new HashMap<String, Class<?>>();
  }

  private byte[] loadClassData(String name) throws IOException {
    byte[] classData = null;
    InputStream ins = null;
    ByteArrayOutputStream baos = null;
    byte[] buf;
    int cnt;
    StringBuffer physicalName;

    try {
      if (DEBUG) {
        System.out.println("Loading Binary of Class: " + name);
      }

      physicalName = new StringBuffer();
      physicalName.append(convertPackagePrefixToDir(name));

      if (physicalName == null || physicalName.length() == 0) {
        throw new IOException("Could NOT Open Non-Named File!");
      }

      physicalName.append(".class");

      ins = mountPoint.openInputStream(physicalName.toString());

      buf = new byte[MAX_BUF];
      baos = new ByteArrayOutputStream();

      cnt = ins.read(buf);
      while (cnt > 0) {
        baos.write(buf, 0, cnt);
        cnt = ins.read(buf);
      }

      classData = baos.toByteArray();
    } //End Try Block
    finally {
      if (ins != null) {
        try {
          ins.close();
        }
        catch (Exception e) {}
        ins = null;
      }

      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }

      buf = null;
    }

    return classData;
  }

  //public synchronized Class<?> findClass(String name) throws ClassNotFoundException {
  public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    byte[] classData;
    Class<?> clazz = null;

    if (DEBUG) {
      System.out.println("Loading Class: " + name);
    }

    //Try to get class from Cache
    clazz = classCache.get(name);

    if (clazz != null) {
      if (DEBUG) {
        System.out.println("Retreived Class from Cache: " + name);
      }

      return clazz;
    }

    //Attempt to load the class from the System Class Loader
    try {
      clazz = super.findSystemClass(name);

      if (clazz != null) {
        if (DEBUG) {
          System.out.println("Loaded Class from System Class Loader: " + name);
        }

        return clazz;
      }
    }
    catch (ClassNotFoundException e) {}

    //Attempt to load the class from the Parent Class Loader
    /*if (parentCL != null && parentCL != this && parentCL != getSystemClassLoader()) {
     try {
     clazz = parentCL.loadClass(name);

     if (clazz != null) {
     if (DEBUG) {
     System.out.println("Loaded Class from Parent Class Loader: " + name);
     }

     return clazz;
     }
     }
     catch (ClassNotFoundException e) {}
     }*/

    //Load the Binary data of the Class from the Storage Abstraction Layer
    try {
      classData = loadClassData(name);
    }
    catch (IOException e) {
      throw new ClassCastException("Could NOT Read Class '" + name + "' from the Storage Abstraction Layer... IOEX: " + e.getMessage());
    }

    if (classData != null) {
      clazz = (Class<?>) defineClass(name, classData, 0, classData.length);

      //Store class in the cache
      if (clazz != null) {
        classCache.put(name, clazz);

        if (resolve) {
          resolveClass(clazz);
        }

        if (DEBUG) {
          System.out.println("Loaded Class from Storage Abstraction Layer: " + name);
        }

        return clazz;
      }
    }

    throw new ClassNotFoundException("Class '" + name + "' NOT Founded!");
  }

  public InputStream getResourceAsStream(String name) {
    InputStream ins = null;

    try {
      if (DEBUG) {
        System.out.println("Opening Resource: " + name);
      }

      ins = mountPoint.openInputStream(name);
    }
    catch (Exception e) {
      if (DEBUG) {
        e.printStackTrace();
      }

      ins = null;
    }

    return ins;
  }

  private String convertPackagePrefixToDir(String pName) {
    String dName = null;

    if (pName != null) {
      dName = pName.replaceAll("\\.", "/");
    }

    return dName;
  }

}
