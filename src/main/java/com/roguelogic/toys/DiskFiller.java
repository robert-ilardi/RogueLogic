/**
 * Created May 16, 2010
 */
package com.roguelogic.toys;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

/**
 * @author Robert C. Ilardi
 *
 */

public class DiskFiller {

  public DiskFiller() {}

  public static void main(String[] args) {
    int exitCd;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    long maxKb, curKb;
    int percentComp;
    byte[] bArr;

    if (args.length != 2) {
      exitCd = 1;
      System.err.println("Usage: java " + DiskFiller.class.getName() + " [TARGET_FILE] [MAX_KB]");
    }
    else {
      curKb = 0;
      percentComp = 0;

      try {
        maxKb = Long.parseLong(args[1]);

        System.out.println("Writing to File: " + args[0]);
        System.out.println("...Target Max KB: " + maxKb);

        fos = new FileOutputStream(args[0]);
        bos = new BufferedOutputStream(fos);

        bArr = new byte[1024];
        for (int i = 0; i < bArr.length; i++) {
          bArr[i] = 48;
        }

        System.out.println("Writing: ");

        while (curKb < maxKb) {
          bos.write(bArr);
          curKb++;

          if ((100 * ((double) curKb / (double) maxKb)) > percentComp) {
            percentComp++;
            System.out.println(percentComp + "% completed");
          }
        }

        exitCd = 0;
      } // End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
      finally {
        if (bos != null) {
          try {
            bos.close();
          }
          catch (Exception e) {}
        }

        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e) {}
        }

        System.out.println("Wrote " + curKb + " KB...");
      }
    }

    System.exit(exitCd);
  }

}
