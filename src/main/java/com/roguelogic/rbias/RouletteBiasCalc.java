/**
 * Created Apr 20, 2008
 */
package com.roguelogic.rbias;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RouletteBiasCalc {

  public static final String[] ROULETTE_WHEEL = { "0", "00", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24",
      "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36" };

  public static final int ROULETTE_NUM_SET_CNT = 38; //1 - 36 + 0 + 00

  private static final HashMap<String, Integer> RouletteWheelNumMap = new HashMap<String, Integer>();;

  private String calcSetName;
  private String inputFile;

  private String[] rawData;

  private int[] occurrences;
  private int[] numRanksByOccurs;

  private double[] percentages;
  private int[] numRanksByPcts;

  private double stdDevOfOccurs;

  private int hiOccurrences;
  private int loOccurrences;
  private double meanOfOccurs;

  private double unbiasAverage;

  private double chiSq;

  static {
    for (int i = 0; i < ROULETTE_WHEEL.length; i++) {
      RouletteWheelNumMap.put(ROULETTE_WHEEL[i], i);
    }
  }

  public RouletteBiasCalc() {
    occurrences = new int[ROULETTE_NUM_SET_CNT];
    numRanksByOccurs = new int[ROULETTE_NUM_SET_CNT];
    percentages = new double[ROULETTE_NUM_SET_CNT];
    numRanksByPcts = new int[ROULETTE_NUM_SET_CNT];
  }

  public String getInputFile() {
    return inputFile;
  }

  public void setInputFile(String inputFile) {
    this.inputFile = inputFile;
  }

  public String getCalcSetName() {
    return calcSetName;
  }

  public void setCalcSetName(String calcSessionName) {
    this.calcSetName = calcSessionName;
  }

  public synchronized void calculate() throws IOException {
    System.out.println("\nStarting Roulette Bias Calculator for Sample Set \"" + calcSetName + "\" at: " + StringUtils.GetTimeStamp());

    loadRawData();

    unbiasAverage = ((double) rawData.length) / ROULETTE_NUM_SET_CNT;

    calcOccurrences();
    calcNumRanksByOccurrences();

    calcStandardDeviationOfOccurs();

    calcPercentages();
    calcNumRanksByPercentages();

    calcChiSq();

    System.out.println("\nFinished Roulette Bias Calculations at: " + StringUtils.GetTimeStamp());
  }

  private void loadRawData() throws IOException {
    ArrayList<String> rdLst;
    FileInputStream fis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    String line;

    try {
      System.out.println("  >> Loading Raw Data from File: \"" + inputFile + "\" at: " + StringUtils.GetTimeStamp());

      fis = new FileInputStream(inputFile);
      isr = new InputStreamReader(fis);
      br = new BufferedReader(isr);

      rdLst = new ArrayList<String>();

      line = br.readLine();

      while (line != null) {
        line = line.trim();

        if (RouletteWheelNumMap.containsKey(line)) {
          rdLst.add(line);
        }

        line = br.readLine();
      }

      rawData = new String[rdLst.size()];
      rawData = rdLst.toArray(rawData);
      rdLst.clear();

      System.out.println("    >> Loaded " + rawData.length + " Roulette Spin Outcomes...");
    } //End try block
    finally {
      if (br != null) {
        try {
          br.close();
        }
        catch (Exception e) {}
      }

      if (isr != null) {
        try {
          isr.close();
        }
        catch (Exception e) {}
      }

      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
      }

    }
  }

  private void calcOccurrences() {
    int index;

    System.out.println("  >> Calc Occurrences at: " + StringUtils.GetTimeStamp());

    for (int i = 0; i < occurrences.length; i++) {
      occurrences[i] = 0;
    }

    for (String num : rawData) {
      index = RouletteWheelNumMap.get(num);
      occurrences[index]++;
    }

    if (occurrences.length > 0) {
      hiOccurrences = occurrences[0];
      loOccurrences = occurrences[0];
    }

    for (int num : occurrences) {
      if (num > hiOccurrences) {
        hiOccurrences = num;
      }

      if (num < loOccurrences) {
        loOccurrences = num;
      }
    }
  }

  private void calcNumRanksByOccurrences() {
    int tmp;
    int[] tmpArr = new int[ROULETTE_NUM_SET_CNT];

    System.out.println("  >> Calc Occurrences Rankings at: " + StringUtils.GetTimeStamp());

    for (int i = 0; i < tmpArr.length; i++) {
      tmpArr[i] = i;
    }

    //Sort Indexes
    for (int i = 0; i < occurrences.length; i++) {
      for (int j = i; j < occurrences.length; j++) {
        if (occurrences[tmpArr[j]] > occurrences[tmpArr[i]]) {
          tmp = tmpArr[i];
          tmpArr[i] = tmpArr[j];
          tmpArr[j] = tmp;
        }
      }
    }

    //Set rankings in order of indexes
    for (int i = 0; i < numRanksByOccurs.length; i++) {
      numRanksByOccurs[tmpArr[i]] = i + 1;
    }
  }

  private void calcPercentages() {
    int index;

    System.out.println("  >> Calc Percentages at: " + StringUtils.GetTimeStamp());

    for (int i = 0; i < percentages.length; i++) {
      percentages[i] = 0.0d;
    }

    for (String num : rawData) {
      index = RouletteWheelNumMap.get(num);
      percentages[index]++;
    }

    for (int i = 0; i < percentages.length; i++) {
      percentages[i] = percentages[i] / rawData.length;
    }
  }

  private void calcNumRanksByPercentages() {
    int tmp;
    int[] tmpArr = new int[ROULETTE_NUM_SET_CNT];

    System.out.println("  >> Calc Percentage Rankings at: " + StringUtils.GetTimeStamp());

    for (int i = 0; i < tmpArr.length; i++) {
      tmpArr[i] = i;
    }

    //Sort Indexes
    for (int i = 0; i < percentages.length; i++) {
      for (int j = i; j < percentages.length; j++) {
        if (percentages[tmpArr[j]] > percentages[tmpArr[i]]) {
          tmp = tmpArr[i];
          tmpArr[i] = tmpArr[j];
          tmpArr[j] = tmp;
        }
      }
    }

    //Set rankings in order of indexes
    for (int i = 0; i < numRanksByPcts.length; i++) {
      numRanksByPcts[tmpArr[i]] = i + 1;
    }
  }

  private void calcStandardDeviationOfOccurs() {
    double sqSumMinusMean = 0.0d, tmp;
    int sum = 0;

    System.out.println("  >> Calc Standard Deviation of Occurrences at: " + StringUtils.GetTimeStamp());

    for (int i = 0; i < occurrences.length; i++) {
      sum += occurrences[i];
    }

    meanOfOccurs = ((double) sum) / occurrences.length;

    for (int i = 0; i < occurrences.length; i++) {
      sqSumMinusMean += Math.pow((occurrences[i] - meanOfOccurs), 2);
    }

    tmp = sqSumMinusMean / occurrences.length;
    stdDevOfOccurs = Math.sqrt(tmp);
  }

  private void calcChiSq() {
    double tmp;

    chiSq = 0.0d;

    for (int i = 0; i < occurrences.length; i++) {
      tmp = Math.pow(occurrences[i] - unbiasAverage, 2);
      tmp /= unbiasAverage;

      chiSq += tmp;
    }
  }

  public boolean doesChiSqShowBias() {
    //Test taken from from research/documentation on the Internet
    //Source URL: http://www.indiangaming.com/istore/Jul07_Murphy.pdf
    return chiSq >= 55.0d;
  }

  public synchronized void printResults() {
    System.out.println(getResultsString());
  }

  public String getResultsString() {
    StringBuffer sb = new StringBuffer();

    sb.append("\nRoulette Bias Calculations for Set: \"");
    sb.append(calcSetName);
    sb.append("\" - \n\n");

    sb.append("Samples = ");
    sb.append(rawData.length);
    sb.append("\n");

    sb.append("Theorectical Unbias Average = ~");
    sb.append((int) unbiasAverage);
    sb.append(" (");
    sb.append(StringUtils.FormatDouble(unbiasAverage, 4));
    sb.append(")\n");

    sb.append("Low Occurrence = ");
    sb.append(loOccurrences);
    sb.append("\n");

    sb.append("High Occurrence = ");
    sb.append(hiOccurrences);
    sb.append("\n");

    sb.append("Mean Occurrence = ~");
    sb.append((int) meanOfOccurs);
    sb.append(" (");
    sb.append(StringUtils.FormatDouble(meanOfOccurs, 4));
    sb.append(")\n");

    sb.append("Standard Deviation of Occurrences = ");
    sb.append(StringUtils.FormatDouble(stdDevOfOccurs, 4));
    sb.append("\n");

    sb.append("Chi Squared = ");
    sb.append(StringUtils.FormatDouble(chiSq, 4));
    sb.append("\n");

    sb.append("Are Results Biased? ");
    sb.append(doesChiSqShowBias() ? "YES" : "NO");
    sb.append("\n");

    //Extra Line Break before summary report...
    sb.append("\n");

    sb.append(getOutcomeSummaryString());

    /*sb.append("\n-----------------\n\n");

     sb.append(getAveragesString());

     sb.append("\n-----------------\n\n");

     sb.append(getRankedAveragesString());*/

    return sb.toString();
  }

  private String getOutcomeSummaryString() {
    StringBuffer sb = new StringBuffer();
    String tmp;

    sb.append("Outcome Summary:\n");
    sb.append("---------------\n");
    sb.append("  NUMBER  OCCURS  #RANK  PERCENT  %RANK  DIST-MEAN  STD-DEV-REL\n");
    sb.append("  ------  ------  -----  -------  -----  ---------  -----------\n");

    for (int i = 0; i < percentages.length; i++) {
      sb.append("  ");

      //Wheel Number
      sb.append(ROULETTE_WHEEL[i]);

      for (int j = 1; j <= 8 - ROULETTE_WHEEL[i].length(); j++) {
        sb.append(" ");
      }

      //Occurrences
      sb.append(occurrences[i]);

      for (int j = 1; j <= 8 - String.valueOf(occurrences[i]).length(); j++) {
        sb.append(" ");
      }

      //Occurrences Rank
      sb.append(numRanksByOccurs[i]);

      for (int j = 1; j <= 7 - String.valueOf(numRanksByOccurs[i]).length(); j++) {
        sb.append(" ");
      }

      //Percentages
      tmp = StringUtils.FormatDouble(percentages[i] * 100, 3);
      sb.append(tmp);
      for (int j = 1; j <= 9 - tmp.length(); j++) {
        sb.append(" ");
      }

      //Percentages Rank
      sb.append(numRanksByPcts[i]);

      for (int j = 1; j <= 7 - String.valueOf(numRanksByPcts[i]).length(); j++) {
        sb.append(" ");
      }

      //Distance from Mean
      tmp = StringUtils.FormatDouble(occurrences[i] - meanOfOccurs, 3);
      sb.append(tmp);

      for (int j = 1; j <= 11 - tmp.length(); j++) {
        sb.append(" ");
      }

      //Distance Above / Below / Equal Standard Deviation
      tmp = (occurrences[i] - meanOfOccurs) > stdDevOfOccurs ? "ABOVE" : (occurrences[i] - meanOfOccurs) < stdDevOfOccurs ? "BELOW" : "EQUAL";
      sb.append(tmp);

      for (int j = 1; j <= 13 - tmp.length(); j++) {
        sb.append(" ");
      }

      sb.append("\n");
    }

    return sb.toString();
  }

  public static void main(String[] args) {
    int exitCd;
    RouletteBiasCalc calc;

    if (args.length != 2) {
      System.err.println("java " + RouletteBiasCalc.class.getName() + " [CALC_SET_NAME] [INPUT_FILE]");
      exitCd = 1;
    }
    else {
      try {
        calc = new RouletteBiasCalc();

        calc.setCalcSetName(args[0]);
        calc.setInputFile(args[1]);

        calc.calculate();

        calc.printResults();

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
