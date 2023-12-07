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

public class StringToBinary {

  public StringToBinary() {}

  public static void main(String[] args) {
    int exitCd, ascii;
    String bin;
    StringBuffer sb;
    byte[] bArr;
    byte b;
    boolean use8Bits;

    if (args.length != 2) {
      exitCd = 1;
      System.err.println("Usage: java " + StringToBinary.class.getName() + " [ASCII_STRING] [8_BITS:Y|N]");
    }
    else {
      try {
        use8Bits = "Y".equalsIgnoreCase(args[1]);

        sb = new StringBuffer();
        bArr = args[0].getBytes();

        for (int i = 0; i < bArr.length; i++) {
          b = bArr[i];

          ascii = SystemUtils.GetAsciiFromByte(b);
          bin = Integer.toBinaryString(ascii);

          if (use8Bits) {
            bin = StringUtils.LPad(bin, '0', 8);
          }
          else if (i > 0) {
            sb.append(" ");
          }

          sb.append(bin);
        }

        System.out.println(sb.toString());

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
