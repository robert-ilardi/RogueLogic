/*
 * Created on Apr 6, 2008
 */

/*
 Copyright 2008 Robert C. Ilardi

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

package com.roguelogic.sambuca.websrvcs.simple;

import com.roguelogic.sambuca.websrvcs.SambucaWebServiceException;
import com.roguelogic.util.Base64Codec;
import com.roguelogic.util.HttpServletCallData;
import com.roguelogic.util.SystemUtils;

/**
 * @author rilardi
 */

public class WSDataCodec {

  public WSDataCodec() {
  }

  public static String DecodeString(HttpServletCallData data) {
    return DecodeString(data.getPayloadData());
  }

  public static String EncodeString(String data) {
    return data;
  }

  public static int DecodeInteger(HttpServletCallData data) {
    return DecodeInteger(data.getPayloadData());
  }

  public static String EncodeInteger(int data) {
    return String.valueOf(data);
  }

  public static float DecodeFloat(HttpServletCallData data) {
    return DecodeFloat(data.getPayloadData());
  }

  public static String EncodeFloat(float data) {
    return String.valueOf(data);
  }

  public static double DecodeDouble(HttpServletCallData data) {
    return DecodeDouble(data.getPayloadData());
  }

  public static String EncodeDouble(double data) {
    return String.valueOf(data);
  }

  public static char DecodeChar(HttpServletCallData data) {
    return DecodeChar(data.getPayloadData());
  }

  public static String EncodeChar(char data) {
    return String.valueOf(data);
  }

  public static byte DecodeByte(HttpServletCallData data) {
    return DecodeByte(data.getPayloadData());
  }

  public static String EncodeByte(byte data) {
    return String.valueOf(SystemUtils.GetAsciiFromByte(data));
  }

  public static short DecodeShort(HttpServletCallData data) {
    return DecodeShort(data.getPayloadData());
  }

  public static String EncodeShort(short data) {
    return String.valueOf(data);
  }

  public static long DecodeLong(HttpServletCallData data) {
    return DecodeLong(data.getPayloadData());
  }

  public static String EncodeLong(long data) {
    return String.valueOf(data);
  }

  public static String EncodeBoolean(boolean data) {
    return data ? "TRUE" : "FALSE";
  }

  public static boolean DecodeBoolean(HttpServletCallData data) {
    return DecodeBoolean(data.getPayloadData());
  }

  public static String[] DecodeStringArr(HttpServletCallData data) {
    return DecodeStringArr(data.getPayloadData());
  }

  public static String EncodeStringArr(String[] arr) {
    StringBuffer buf = new StringBuffer();

    //Lengths Part
    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buf.append(",");
      }

      buf.append(arr[i].length());
    }

    buf.append("|");

    //Data Part
    for (int i = 0; i < arr.length; i++) {
      buf.append(arr[i]);
    }

    return buf.toString();
  }

  public static int[] DecodeIntegerArr(HttpServletCallData data) {
    return DecodeIntegerArr(data.getPayloadData());
  }

  public static String EncodeIntegerArr(int[] arr) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buf.append("|");
      }

      buf.append(arr[i]);
    }

    return buf.toString();
  }

  public static float[] DecodeFloatArr(HttpServletCallData data) {
    return DecodeFloatArr(data.getPayloadData());
  }

  public static String EncodeFloatArr(float[] arr) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buf.append("|");
      }

      buf.append(arr[i]);
    }

    return buf.toString();
  }

  public static double[] DecodeDoubleArr(HttpServletCallData data) {
    return DecodeDoubleArr(data.getPayloadData());
  }

  public static String EncodeDoubleArr(double[] arr) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buf.append("|");
      }

      buf.append(arr[i]);
    }

    return buf.toString();
  }

  public static char[] DecodeCharArr(HttpServletCallData data) {
    return DecodeCharArr(data.getPayloadData());
  }

  public static String EncodeCharArr(char[] arr) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < arr.length; i++) {
      buf.append(arr[i]);
    }

    return buf.toString();
  }

  public static byte[] DecodeByteArr(HttpServletCallData data) {
    return Base64Codec.Decode(data.getPayloadData());
  }

  public static String EncodeByteArr(byte[] arr) {
    return Base64Codec.Encode(arr);
  }

  public static short[] DecodeShortArr(HttpServletCallData data) {
    return DecodeShortArr(data.getPayloadData());
  }

  public static String EncodeShortArr(short[] arr) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buf.append("|");
      }

      buf.append(arr[i]);
    }

    return buf.toString();
  }

  public static String EncodeBooleanArr(boolean[] arr) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buf.append("|");
      }

      buf.append(arr[i] ? "TRUE" : "FALSE");
    }

    return buf.toString();
  }

  public static long[] DecodeLongArr(HttpServletCallData data) {
    return DecodeLongArr(data.getPayloadData());
  }

  public static String EncodeLongArr(long[] arr) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buf.append("|");
      }

      buf.append(arr[i]);
    }

    return buf.toString();
  }

  public static String DecodeString(String data) {
    return data;
  }

  public static int DecodeInteger(String data) {
    return Integer.parseInt(data.trim());
  }

  public static float DecodeFloat(String data) {
    return Float.parseFloat(data.trim());
  }

  public static double DecodeDouble(String data) {
    return Double.parseDouble(data.trim());
  }

  public static char DecodeChar(String data) {
    return data.length() > 0 ? data.charAt(0) : ' ';
  }

  public static byte DecodeByte(String data) {
    return SystemUtils.GetByteFromAscii(Integer.parseInt(data.trim()));
  }

  public static short DecodeShort(String data) {
    return Short.parseShort(data.trim());
  }

  public static long DecodeLong(String data) {
    return Long.parseLong(data.trim());
  }

  public static boolean DecodeBoolean(String data) {
    return data != null && "TRUE".equalsIgnoreCase(data.trim());
  }

  public static String[] DecodeStringArr(String data) {
    String[] tmpArr, lengthsStr, arr;
    int[] lengths;
    int curIndex = 0;

    tmpArr = data.split("\\|", 2);
    lengthsStr = tmpArr[0].split(",");

    lengths = new int[lengthsStr.length];

    for (int i = 0; i < lengthsStr.length; i++) {
      lengths[i] = Integer.parseInt(lengthsStr[i].trim());
    }

    arr = new String[lengths.length];

    for (int i = 0; i < lengths.length; i++) {
      arr[i] = tmpArr[1].substring(curIndex, curIndex + lengths[i]);

      curIndex += lengths[i];
    }

    return arr;
  }

  public static int[] DecodeIntegerArr(String data) {
    String[] tmpArr;
    int[] arr;

    tmpArr = data.trim().split("\\|");
    arr = new int[tmpArr.length];

    for (int i = 0; i < tmpArr.length; i++) {
      arr[i] = Integer.parseInt(tmpArr[i].trim());
    }

    return arr;
  }

  public static float[] DecodeFloatArr(String data) {
    String[] tmpArr;
    float[] arr;

    tmpArr = data.trim().split("\\|");
    arr = new float[tmpArr.length];

    for (int i = 0; i < tmpArr.length; i++) {
      arr[i] = Float.parseFloat(tmpArr[i].trim());
    }

    return arr;
  }

  public static double[] DecodeDoubleArr(String data) {
    String[] tmpArr;
    double[] arr;

    tmpArr = data.trim().split("\\|");
    arr = new double[tmpArr.length];

    for (int i = 0; i < tmpArr.length; i++) {
      arr[i] = Double.parseDouble(tmpArr[i].trim());
    }

    return arr;
  }

  public static char[] DecodeCharArr(String data) {
    String tmp;
    char[] arr;

    tmp = data;
    arr = new char[tmp.length()];

    for (int i = 0; i < tmp.length(); i++) {
      arr[i] = tmp.charAt(i);
    }

    return arr;
  }

  public static byte[] DecodeByteArr(String data) {
    return Base64Codec.Decode(data);
  }

  public static short[] DecodeShortArr(String data) {
    String[] tmpArr;
    short[] arr;

    tmpArr = data.trim().split("\\|");
    arr = new short[tmpArr.length];

    for (int i = 0; i < tmpArr.length; i++) {
      arr[i] = Short.parseShort(tmpArr[i].trim());
    }

    return arr;
  }

  public static long[] DecodeLongArr(String data) {
    String[] tmpArr;
    long[] arr;

    tmpArr = data.trim().split("\\|");
    arr = new long[tmpArr.length];

    for (int i = 0; i < tmpArr.length; i++) {
      arr[i] = Long.parseLong(tmpArr[i].trim());
    }

    return arr;
  }

  public static boolean[] DecodeBooleanArr(HttpServletCallData data) {
    return DecodeBooleanArr(data.getPayloadData());
  }

  public static boolean[] DecodeBooleanArr(String data) {
    String[] tmpArr;
    boolean[] arr;

    tmpArr = data.trim().split("\\|");
    arr = new boolean[tmpArr.length];

    for (int i = 0; i < tmpArr.length; i++) {
      arr[i] = (tmpArr[i] != null && "TRUE".equalsIgnoreCase(tmpArr[i].trim()));
    }

    return arr;
  }

  public static String Encode(Object val) throws SambucaWebServiceException {
    String data = null;

    if (val instanceof String[]) {
      data = EncodeStringArr((String[]) val);
    }
    else if (val instanceof String) {
      data = EncodeString((String) val);
    }
    else if (val instanceof int[]) {
      data = EncodeIntegerArr((int[]) val);
    }
    else if (val instanceof Integer) {
      data = EncodeInteger((Integer) val);
    }
    else if (val instanceof float[]) {
      data = EncodeFloatArr((float[]) val);
    }
    else if (val instanceof Float) {
      data = EncodeFloat((Float) val);
    }
    else if (val instanceof double[]) {
      data = EncodeDoubleArr((double[]) val);
    }
    else if (val instanceof Double) {
      data = EncodeDouble((Double) val);
    }
    else if (val instanceof char[]) {
      data = EncodeCharArr((char[]) val);
    }
    else if (val instanceof Character) {
      data = EncodeChar((Character) val);
    }
    else if (val instanceof byte[]) {
      data = EncodeByteArr((byte[]) val);
    }
    else if (val instanceof Byte) {
      data = EncodeByte((Byte) val);
    }
    else if (val instanceof short[]) {
      data = EncodeShortArr((short[]) val);
    }
    else if (val instanceof Short) {
      data = EncodeShort((Short) val);
    }
    else if (val instanceof short[]) {
      data = EncodeShortArr((short[]) val);
    }
    else if (val instanceof Short) {
      data = EncodeShort((Short) val);
    }
    else if (val instanceof long[]) {
      data = EncodeLongArr((long[]) val);
    }
    else if (val instanceof Long) {
      data = EncodeLong((Long) val);
    }
    else if (val instanceof boolean[]) {
      data = EncodeBooleanArr((boolean[]) val);
    }
    else if (val instanceof Boolean) {
      data = EncodeBoolean((Boolean) val);
    }
    else {
      throw new SambucaWebServiceException("Data Type '"
          + val.getClass().getName() + "' NOT support for Encoding!");
    }

    return data;
  }

}
