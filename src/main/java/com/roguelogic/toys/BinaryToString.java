/**
 * Created Apr 20, 2009
 */
package com.roguelogic.toys;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class BinaryToString {

  public BinaryToString() {}

  public static void main(String[] args) {
    int exitCd, ascii;
    String[] bin;
    String bs;
    byte[] bArr;

    if (args.length == 0) {
      exitCd = 1;
      System.err.println("Usage: java " + BinaryToString.class.getName() + " [ASCII_STRING]");
    }
    else {
      try {
        if (args[0].indexOf(" ") >= 0) {
          //Space delimited binary encoded ASCII
          bin = args[0].split(" ");
          bin = StringUtils.Trim(bin);
          bin = StringUtils.RemoveEmpties(bin);
          bArr = new byte[bin.length];
        }
        else {
          //One long binary string.
          //Assume 8 bits to a byte...
          bin = StringUtils.SplitAtLen(args[0], 8);
          bArr = new byte[bin.length];
        }

        for (int i = 0; i < bin.length; i++) {
          bs = bin[i];
          ascii = Integer.parseInt(bs, 2);

          bArr[i] = SystemUtils.GetByteFromAscii(ascii);
        }

        System.out.println(new String(bArr));

        exitCd = 0;
      } // End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
    }

    System.exit(exitCd);
  }

}
