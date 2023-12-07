/**
 * Created Apr 20, 2008
 */
package com.roguelogic.rbias;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * @author Robert C. Ilardi
 *
 */

public class RouletteNumberGenerator {

  public static final String[] ROULETTE_WHEEL = { "0", "00", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24",
      "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36" };

  private String outputFile;
  private int iterations;

  public RouletteNumberGenerator() {}

  public String getOutputFile() {
    return outputFile;
  }

  public void setOutputFile(String outputFile) {
    this.outputFile = outputFile;
  }

  public int getIterations() {
    return iterations;
  }

  public void setIterations(int iterations) {
    this.iterations = iterations;
  }

  public synchronized void generate() throws IOException {
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    String num;
    int index;
    Random rnd;

    try {
      fos = new FileOutputStream(outputFile);
      bos = new BufferedOutputStream(fos);

      rnd = new Random();

      for (int i = 1; i <= iterations; i++) {
        index = rnd.nextInt(ROULETTE_WHEEL.length);
        num = ROULETTE_WHEEL[index];

        bos.write(num.getBytes());
        bos.write("\n".getBytes());
      }
    } //End try block
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
    }
  }

  public static void main(String[] args) {
    int exitCd;
    RouletteNumberGenerator gen;

    if (args.length != 2) {
      System.err.println("java " + RouletteNumberGenerator.class.getName() + " [OUTPUT_FILE] [ITERATIONS]");
      exitCd = 1;
    }
    else {
      try {
        gen = new RouletteNumberGenerator();

        gen.setOutputFile(args[0]);
        gen.setIterations(Integer.parseInt(args[1]));

        gen.generate();

        exitCd = 0;
      } //End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
    }

    System.exit(exitCd);
  }

}
