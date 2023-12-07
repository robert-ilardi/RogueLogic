/*
 * Created on Oct 17, 2007
 */
package com.roguelogic.games.wingo;

import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * @author rilardi
 */

public class WordFinder {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println("Usage: java " + WordFinder.class.getName() + " [SRC_DICT_FILE] [WORD_LEN] [DEST_DICT_FILE]");
      System.exit(1);
    }
    else {
      TreeSet<String> destDict;
      RandomAccessFile raf = null;
      FileOutputStream fos = null;
      Iterator<String> iter;
      String line;
      int wordLen;

      try {
        wordLen = Integer.parseInt(args[1]);
        destDict = new TreeSet<String>();

        raf = new RandomAccessFile(args[0], "r");

        line = raf.readLine();

        while (line != null) {
          if (line.indexOf("'") == -1) {
            line = line.trim().toUpperCase();

            if (line.length() == wordLen && !destDict.contains(line)) {
              destDict.add(line);
              System.out.println("Adding: " + line);
            }
          }

          line = raf.readLine();
        }

        iter = destDict.iterator();
        fos = new FileOutputStream(args[2]);

        while (iter.hasNext()) {
          fos.write(new StringBuffer().append(iter.next().toUpperCase()).append("\n").toString().getBytes());
        }

        destDict.clear();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e) {}
        }

        if (raf != null) {
          try {
            raf.close();
          }
          catch (Exception e) {}
        }
      }
    }
  }

}
