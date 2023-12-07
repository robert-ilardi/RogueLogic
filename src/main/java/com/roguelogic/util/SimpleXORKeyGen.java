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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class SimpleXORKeyGen {

  public SimpleXORKeyGen() {}

  public static void main(String[] args) {
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    String outputFile;
    int keyLen, retCode;
    long startTime, endTime;
    double totalTime;

    if (args.length != 2) {
      System.err.println("Usage: java " + SimpleXORKeyGen.class.getName() + " [KEY_LENGTH] [OUTPUT_FILE]");
      System.exit(1);
    }
    else {
      startTime = System.currentTimeMillis();

      try {
        keyLen = Integer.parseInt(args[0]);
        outputFile = args[1];

        System.out.println("Generating Key File '" + outputFile + "' with " + keyLen + " Byte(s).");

        fos = new FileOutputStream(outputFile);
        bos = new BufferedOutputStream(fos);

        for (int i = 1; i <= keyLen; i++) {
          bos.write((int) (Math.random() * 256));
        }

        retCode = 0;
      }
      catch (Exception e) {
        retCode = 1;
        e.printStackTrace();
      }
      finally {
        if (bos != null) {
          try {
            bos.close();
          }
          catch (Exception e) {}
          bos = null;
        }

        if (fos != null) {
          try {
            fos.close();
          }
          catch (Exception e) {}
          fos = null;
        }
      }

      endTime = System.currentTimeMillis();
      totalTime = (endTime - startTime) / 1000.0d;

      System.out.println("Total Generation Time: " + totalTime + " second(s).");

      System.exit(retCode);
    }
  }

}
