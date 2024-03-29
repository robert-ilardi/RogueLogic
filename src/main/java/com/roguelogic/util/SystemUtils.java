/*
 * Created on May 18, 2005
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author rilardi
 */

public class SystemUtils {

  public static final int OS_UNKNOWN = -1;
  public static final int OS_LINUX = 0;
  public static final String OS_LINUX_STR = "LINUX";
  public static final int OS_WINDOWS = 1;
  public static final String OS_WINDOWS_STR = "WINDOWS";

  public static final String SYSPROP_OS_NAME = "os.name";
  public static final String SYSPROP_OS_VERSION = "os.version";
  public static final String SYSPROP_JAVA_VERSION = "java.version";
  public static final String SYSPROP_JAVA_VENDOR = "java.vendor";

  public static double GetAvailableMemory() {
    Runtime rt = Runtime.getRuntime();
    double availableMem;
    long maxMem, freeMem, usedJvmMem;

    usedJvmMem = rt.totalMemory();
    maxMem = rt.maxMemory();
    freeMem = rt.freeMemory();
    availableMem = (maxMem - (usedJvmMem - freeMem)) / 1048576.0d;

    return availableMem;
  }

  public static double GetTotalMemory() {
    Runtime rt = Runtime.getRuntime();
    double maxMem = rt.maxMemory() / 1048576.0d;
    return maxMem;
  }

  public static void PrintMemoryUsage() {
    PrintMemoryUsage("Memory Available to JVM: ");
  }

  public static void PrintMemoryUsage(String memMesg) {
    StringBuffer sb = new StringBuffer();
    double availableMem;
    NumberFormat nf = NumberFormat.getInstance();
    nf.setGroupingUsed(true);
    nf.setMaximumFractionDigits(3);

    availableMem = GetAvailableMemory();

    sb.append(memMesg);

    sb.append(nf.format(availableMem));
    sb.append(" MB");

    System.out.println(sb.toString());
  }

  public static String GetWorkingDirectory() {
    return GetDirectory("user.dir");
  }

  public static String GetHomeDirectory() {
    return GetDirectory("user.home");
  }

  public static String GetDirectory(String systemProp) {
    File file;
    StringBuffer dir = new StringBuffer();

    file = new File(System.getProperty(systemProp));
    dir.append(file.getAbsolutePath());

    if (!dir.toString().endsWith(File.separator)) {
      dir.append(File.separator);
    }

    return dir.toString();
  }

  public static String GetHostname() {
    String hostname = null;

    try {
      InetAddress lh = InetAddress.getLocalHost();
      hostname = lh.getHostName();
    }
    catch (Exception e) {}

    return hostname;
  }

  public static int GetOperatingSystemType() {
    int osType;
    String osName = System.getProperty(SYSPROP_OS_NAME).toUpperCase();

    if (osName.indexOf(OS_LINUX_STR) >= 0) {
      osType = OS_LINUX;
    }
    else if (osName.indexOf(OS_WINDOWS_STR) >= 0) {
      osType = OS_WINDOWS;
    }
    else {
      osType = OS_UNKNOWN;
    }

    return osType;
  }

  public static void Sleep(int secs) {
    try {
      for (int i = 1; i <= secs; i++) {
        Thread.sleep(1000);
      }
    }
    catch (Exception e) {}
  }

  /*
   * Wraps the Thread.sleep(int milliseconds) static method
   * for quick and easy use...
   */
  public static void SleepTight(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    }
    catch (Exception e) {}
  }

  public static boolean IsClassDervivedFrom(Class childClass, Class superClass) {
    boolean derived = false;
    Class prevSuper;

    if (childClass != null && superClass != null) {

      //Make sure this class is dervived from the Worker Class 
      prevSuper = childClass;
      superClass = childClass.getSuperclass();

      while (!superClass.getName().equals("java.lang.Object")) {
        prevSuper = superClass;
        superClass = superClass.getSuperclass();
      }

      derived = (prevSuper.getName().equals(superClass.getName()));
    } //End null class parameters check

    return derived;
  }

  public static boolean DoesClassImplement(Class targetClass, Class interfaceClass) {
    boolean implemented = false;
    Class[] interfaces;

    if (targetClass != null && interfaceClass != null) {

      do {
        interfaces = targetClass.getInterfaces();

        for (Class intf : interfaces) {
          if (intf.getName().equals(interfaceClass.getName())) {
            implemented = true;
            break;
          }
        }

        targetClass = targetClass.getSuperclass();
      } while (!implemented && !targetClass.getName().equals("java.lang.Object"));
    } //End null class parameters check

    return implemented;
  }

  public static void PrintInputStream(InputStream ins) throws IOException {
    byte[] buf;
    int cnt;

    if (ins != null) {
      buf = new byte[1024];

      cnt = ins.read(buf);
      while (cnt > 0) {
        System.out.println(new String(buf, 0, cnt));
        cnt = ins.read(buf);
      }
    }
  }

  public static boolean EqualByteArrays(byte[] dcData, byte[] dcData2) {
    boolean same = false;

    if (dcData != null && dcData2 != null && dcData.length == dcData2.length) {
      same = true; //Assume True

      for (int i = 0; i < dcData.length; i++) {
        if (dcData[i] != dcData2[i]) {
          same = false;
          break;
        }
      }
    }

    return same;
  }

  public static void FillArray(byte[] arr, byte b) {
    if (arr != null) {
      for (int i = 0; i < arr.length; i++) {
        arr[i] = b;
      }
    }
  }

  public static Properties LoadPropertiesFile(String propFile) throws IOException {
    Properties props = null;
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(propFile);
      props = new Properties();
      props.load(fis);
    }
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
        fis = null;
      }
    }

    return props;
  }

  public static byte[] LoadDataFromClassLoader(String resourceClassPath) throws IOException {
    byte[] data = null, buf;
    ByteArrayOutputStream baos = null;
    InputStream ins = null;
    int cnt;

    try {
      ins = SystemUtils.class.getClassLoader().getResourceAsStream(resourceClassPath);

      if (ins == null) {
        throw new IOException("Could NOT Open Resource Stream for: " + resourceClassPath);
      }

      buf = new byte[1024];
      baos = new ByteArrayOutputStream();

      cnt = ins.read(buf);
      while (cnt != -1) {
        baos.write(buf, 0, cnt);
        cnt = ins.read(buf);
      }

      data = baos.toByteArray();
    }
    finally {
      if (ins != null) {
        try {
          ins.close();
        }
        catch (Exception e) {}
      }

      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }
    }

    return data;
  }

  public static String GetOperatingSystemName() {
    return System.getProperty(SYSPROP_OS_NAME);
  }

  public static String GetOperatingSystemVersion() {
    return System.getProperty(SYSPROP_OS_VERSION);
  }

  public static String GetJavaVersion() {
    return System.getProperty(SYSPROP_JAVA_VERSION);
  }

  public static String GetJavaVendor() {
    return System.getProperty(SYSPROP_JAVA_VENDOR);
  }

  public static void RedirectStdOut(OutputStream outs) throws IOException {
    System.setOut(new PrintStream(outs, true));
  }

  public static void RedirectStdErr(OutputStream outs) throws IOException {
    System.setErr(new PrintStream(outs, true));
  }

  public static void RedirectStdOut(String filePath) throws IOException {
    RedirectStdOut(new FileOutputStream(filePath));
  }

  public static void RedirectStdErr(String filePath) throws IOException {
    RedirectStdErr(new FileOutputStream(filePath));
  }

  public static int Sum(int[] arr) {
    int total = 0;

    if (arr != null) {
      for (int i = 0; i < arr.length; i++) {
        total += arr[i];
      }
    }

    return total;
  }

  public static void CopyFile(String src, String dest) throws IOException {
    FileInputStream fis = null;
    FileOutputStream fos = null;
    byte[] buf;
    int len;

    try {
      fis = new FileInputStream(src);
      fos = new FileOutputStream(dest);

      buf = new byte[2048];

      len = fis.read(buf);
      while (len != -1) {
        fos.write(buf, 0, len);
        len = fis.read(buf);
      }
    } //End try block
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
      }

      if (fos != null) {
        try {
          fos.close();
        }
        catch (Exception e) {}
      }
    }
  }

  public static byte[] GenerateRandomBytes(int maxLen) {
    int cnt;
    byte[] data;

    do {
      cnt = (int) (maxLen * Math.random());
    } while (cnt == 0);

    data = new byte[cnt];

    for (int i = 0; i < cnt; i++) {
      data[i] = (byte) (((int) (127 * Math.random())) * ((100 * Math.random() > 50 ? 1 : -1)));
    }

    return data;
  }

  public static boolean FileExists(String filePath) {
    File f;
    boolean exists = false;

    if (filePath != null) {
      f = new File(filePath);
      exists = f.exists();
    }

    return exists;
  }

  public static byte[] LoadDataFromFile(String filePath) throws IOException {
    byte[] data = null, buf;
    ByteArrayOutputStream baos = null;
    InputStream ins = null;
    int cnt;

    try {
      ins = new FileInputStream(filePath);

      buf = new byte[1024];
      baos = new ByteArrayOutputStream();

      cnt = ins.read(buf);
      while (cnt != -1) {
        baos.write(buf, 0, cnt);
        cnt = ins.read(buf);
      }

      data = baos.toByteArray();
    }
    finally {
      if (ins != null) {
        try {
          ins.close();
        }
        catch (Exception e) {}
      }

      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }
    }

    return data;
  }

  public static void StoreObject(Object target, String filePath) throws IOException {
    FileOutputStream fos = null;
    ObjectOutputStream oos = null;

    try {
      fos = new FileOutputStream(filePath);
      oos = new ObjectOutputStream(fos);
      oos.writeObject(target);
    }
    finally {
      if (oos != null) {
        try {
          oos.close();
        }
        catch (Exception e) {}
        oos = null;
      }

      if (fos != null) {
        try {
          fos.close();
        }
        catch (Exception e) {}
        fos = null;
      }
    }
  }

  public static Object LoadObject(String filePath) throws IOException, ClassNotFoundException {
    Object target = null;
    FileInputStream fis = null;
    ObjectInputStream ois = null;

    try {
      fis = new FileInputStream(filePath);
      ois = new ObjectInputStream(fis);
      target = ois.readObject();
    }
    finally {
      if (ois != null) {
        try {
          ois.close();
        }
        catch (Exception e) {}
        ois = null;
      }

      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
        fis = null;
      }
    }

    return target;
  }

  public static byte[] GetClasspathResourceBytes(String resourcePath) throws IOException {
    byte[] bArr = null, buf;
    ByteArrayOutputStream baos = null;
    InputStream ins = null;
    int len;

    try {
      buf = new byte[2048];

      ins = SystemUtils.class.getClassLoader().getResourceAsStream(resourcePath);
      baos = new ByteArrayOutputStream();

      len = ins.read(buf);

      while (len > 0) {
        baos.write(buf, 0, len);
        len = ins.read(buf);
      }

      bArr = baos.toByteArray();
    }
    finally {
      if (ins != null) {
        try {
          ins.close();
        }
        catch (Exception e) {}
      }

      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }
    }

    return bArr;
  }

  public static Object[] CombineArrays(Object[] arr1, Object[] arr2) {
    Object[] combinedArr = null;
    Class arrClass;
    int arr1Len, arr2Len, totalLen;

    if (arr1 != null || arr2 != null) {
      if (arr1 != null) {
        arrClass = arr1.getClass();
      }
      else {
        arrClass = arr2.getClass();
      }

      arr1Len = (arr1 != null ? arr1.length : 0);
      arr2Len = (arr2 != null ? arr2.length : 0);

      totalLen = arr1Len + arr2Len;

      combinedArr = (Object[]) Array.newInstance(arrClass.getComponentType(), totalLen);

      if (arr1Len > 0) {
        System.arraycopy(arr1, 0, combinedArr, 0, arr1Len);
      }

      if (arr2Len > 0) {
        System.arraycopy(arr2, 0, combinedArr, arr1Len, arr2Len);
      }
    }

    return combinedArr;
  }

  public static byte[] LoadDataFromStream(InputStream ins) throws IOException {
    byte[] data = null, buf;
    ByteArrayOutputStream baos = null;
    int cnt;

    try {
      buf = new byte[1024];
      baos = new ByteArrayOutputStream();

      cnt = ins.read(buf);
      while (cnt != -1) {
        baos.write(buf, 0, cnt);
        cnt = ins.read(buf);
      }

      data = baos.toByteArray();
    }
    finally {
      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }
    }

    return data;
  }

  public static int GetAsciiFromByte(byte b) {
    int ascii = b & 0xFF;

    return ascii;
  }

  public static byte GetByteFromAscii(int ascii) {
    byte b = (byte) (ascii & 0x000000FFL);

    return b;
  }

  public static void Copy(InputStream src, OutputStream dest) throws IOException {
    byte[] buf;
    int len;

    buf = new byte[2048];

    len = src.read(buf);
    while (len != -1) {
      dest.write(buf, 0, len);
      len = src.read(buf);
    }
  }

  public static byte[] GenerateRandomBytesFixedLen(int len) {
    byte[] data;

    data = new byte[len];

    for (int i = 0; i < len; i++) {
      data[i] = (byte) (((int) (127 * Math.random())) * ((100 * Math.random() > 50 ? 1 : -1)));
    }

    return data;
  }

  public static void RecuriveDelete(File f) {
    ArrayList<File> fileLst, dirLst;

    fileLst = new ArrayList<File>();
    TraverseDir(f, fileLst);

    dirLst = new ArrayList<File>();

    //Delete All Files
    for (File lf : fileLst) {
      if (lf.isFile()) {
        lf.delete();
      }
      else {
        dirLst.add(0, lf); //Add each dir we find to the front of the list to we delete in correct order!
      }
    }

    //Delete All Directories
    for (File d : dirLst) {
      d.delete();
    }
  }

  public static void TraverseDir(File root, ArrayList<File> fileLst) {
    File[] ls;

    fileLst.add(root);

    if (root.isFile()) {
      return;
    }

    ls = root.listFiles();

    if (ls == null) {
      return;
    }

    for (File f : ls) {
      if (f.isDirectory()) {
        TraverseDir(f, fileLst);
      }
      else if (f.isFile()) {
        fileLst.add(f);
      }
    }
  }

  public static byte[] ToByteArray(Serializable obj) throws IOException {
    ByteArrayOutputStream baos = null;
    ObjectOutputStream oos = null;
    byte[] bin = null;

    try {
      baos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(baos);
      oos.writeObject(obj);

      bin = baos.toByteArray();
    }
    finally {
      if (oos != null) {
        try {
          oos.close();
        }
        catch (Exception e) {}
        oos = null;
      }

      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }
    }

    return bin;
  }

  public static Object ToObject(byte[] objBin) throws IOException, ClassNotFoundException {
    Object obj = null;
    ByteArrayInputStream bais = null;
    ObjectInputStream ois = null;

    try {
      bais = new ByteArrayInputStream(objBin);
      ois = new ObjectInputStream(bais);
      obj = ois.readObject();
    }
    finally {
      if (ois != null) {
        try {
          ois.close();
        }
        catch (Exception e) {}
        ois = null;
      }

      if (bais != null) {
        try {
          bais.close();
        }
        catch (Exception e) {}
        bais = null;
      }
    }

    return obj;
  }

}
