/*
 * Created on Dec 27, 2005
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

public class IntUtils {

  public static int IntLen(int i) {
    int len;
    int result;

    if (i >= 10) {
      len = 2;
      result = i / 10;
      while (result >= 10) {
        len++;
        result /= 10;
      }
    }
    else {
      len = 1;
    }

    return len;
  }

  public static byte[] IntToBytes(int i) {
    int digits, hiLimit, digit;
    byte[] bArr;

    digits = IntLen(i);
    bArr = new byte[digits];
    hiLimit = (int) Math.pow(10, digits - 1);

    for (int j = 0; j < digits; j++) {
      digit = i / hiLimit;

      bArr[j] = (byte) (48 + digit);

      i -= (digit * hiLimit);
      hiLimit /= 10;
    }

    return bArr;
  }

  public static byte[] AddZeroPadding(byte[] bArr, int fixedLen) {
    byte[] flArr;
    int cpStartIndex;

    if (bArr.length >= fixedLen) {
      flArr = bArr;
    }
    else {
      flArr = new byte[fixedLen];
      cpStartIndex = fixedLen - bArr.length;

      //Pre-pend Zeros for Padding as needed...
      for (int i = 0; i < cpStartIndex; i++) {
        flArr[i] = (byte) 48;
      }

      System.arraycopy(bArr, 0, flArr, cpStartIndex, bArr.length);
    }

    return flArr;
  }

  public static int BytesToInt(byte[] bArr) {
    return BytesToInt(bArr, 0);
  }

  public static int BytesToInt(byte[] bArr, int offset) {
    int cnt = 0, num = 0;

    for (int i = bArr.length - 1; i >= offset; i--) {
      num += (bArr[i] - 48) * ((int) Math.pow(10, cnt++));
    }

    return num;
  }

  public static void main(String[] args) {
    byte[] bArr;
    int num;

    System.out.println(BytesToInt(AddZeroPadding("".getBytes(), 4)));
    System.out.println(BytesToInt(AddZeroPadding("1".getBytes(), 4)));
    System.out.println(BytesToInt(AddZeroPadding("23".getBytes(), 4)));
    System.out.println(BytesToInt(AddZeroPadding("555".getBytes(), 4)));
    System.out.println(BytesToInt(AddZeroPadding("1234".getBytes(), 4)));
    System.out.println(BytesToInt(AddZeroPadding("12345".getBytes(), 4)));

    for (int i = 0; i <= 1000000000; i++) {
      bArr = IntToBytes(i);
      num = BytesToInt(bArr);
      //System.out.println(num);
      if (num != i) {
        System.out.println(num + " != " + i);
        break;
      }
      else if (num % 1000000 == 0) {
        System.out.println(num);
      }
    }
  }

}
