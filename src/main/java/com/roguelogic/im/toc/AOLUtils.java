/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.toc;

/**
 * @author rilardi
 */

public class AOLUtils {

  public static final String ROAST_KEY = "Tic/Toc";
  public static final int ROAST_LENGTH = 7;

  public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  public static final char[] SPECIAL_CHARS = { '\"', '(', ')', '$', '\\', '{', '}', '[', ']' };

  public static String EncodeText(String plainText) {
    StringBuffer encodedText = new StringBuffer();
    char c;

    encodedText.append("\"");
    for (int i = 0; i < plainText.length(); i++) {
      c = plainText.charAt(i);
      for (int j = 0; j < SPECIAL_CHARS.length; j++) {
        if (c == SPECIAL_CHARS[j]) {
          encodedText.append('\\');
          break;
        }
      }
      encodedText.append(c);
    }
    encodedText.append("\"");

    return encodedText.toString();
  }

  public static String FormatUsername(String username) {
    StringBuffer formattedUsername = new StringBuffer();
    char c;

    username = username.toLowerCase();

    for (int i = 0; i < username.length(); i++) {
      c = username.charAt(i);
      if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')) {
        formattedUsername.append(c);
      }
    }

    return formattedUsername.toString();
  }

  public static String Roast(String password) {
    StringBuffer rPasswd = new StringBuffer();
    int roast;

    rPasswd.append("0x");

    for (int i = 0; i < password.length(); i++) {
      roast = password.charAt(i) ^ ROAST_KEY.charAt(i % ROAST_LENGTH);
      rPasswd.append(HEX_CHARS[(roast >> 4) & 0x0f]);
      rPasswd.append(HEX_CHARS[roast & 0x0f]);
    }

    return rPasswd.toString();
  }

}