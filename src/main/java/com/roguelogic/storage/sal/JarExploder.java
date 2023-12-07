/*
 * Created on Jul 31, 2006
 */

package com.roguelogic.storage.sal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


public class JarExploder {

  private static final int MAX_BUFFER_SIZE = 2048;

  private MountPoint mntPnt;

  public JarExploder(MountPoint mntPnt) {
    this.mntPnt = mntPnt;
  }

  public void explode(InputStream ins, String mpRelPath) throws IOException {
    JarEntry jEntry = null;
    JarInputStream jarIns = null;

    try {
      if (!mntPnt.dirExists(mpRelPath)) {
        mntPnt.createDirectory(mpRelPath);
      }

      jarIns = new JarInputStream(ins);

      jEntry = jarIns.getNextJarEntry();

      while (jEntry != null) {
        expandEntry(jEntry, jarIns, mpRelPath);

        jEntry = jarIns.getNextJarEntry();
      }
    } //End try block
    finally {
      if (jarIns != null) {
        try {
          jarIns.close();
        }
        catch (Exception e) {}
        jarIns = null;
      }
    }
  }

  private void expandEntry(JarEntry jEntry, JarInputStream jarIns, String mpRelPath) throws IOException {
    byte[] buf;
    int cnt;
    OutputStream outs = null;
    String fullPath;

    if (jEntry != null) {
      fullPath = SALUtils.CombineDirStrs(mpRelPath, jEntry.getName());

      if (jEntry.isDirectory() && !mntPnt.dirExists(fullPath)) {
        //Directory
        mntPnt.createDirectory(fullPath);
      }
      else {
        //File
        try {
          outs = mntPnt.openOutputStream(fullPath);

          buf = new byte[MAX_BUFFER_SIZE];

          cnt = jarIns.read(buf);
          while (cnt > 0) {

            cnt = jarIns.read(buf);
          } //End while cnt > 0
        } //End try block
        finally {
          if (outs != null) {
            try {
              outs.close();
            }
            catch (Exception e) {}
            outs.close();
          }
        } //End finally block
      } //End else block for files
    } //End null jEntry check
  }

}
