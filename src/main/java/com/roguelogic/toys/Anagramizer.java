/**
 * Created Jun 7, 2007
 */
package com.roguelogic.toys;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Random;

/**
 * @author Robert C. Ilardi
 *
 */

public class Anagramizer {

  private String phrase;
  private int maxAnagrams;

  private boolean ignoreCase;

  private HashSet<String> anagrams;

  public Anagramizer() {}

  public String getPhrase() {
    return phrase;
  }

  public synchronized void setPhrase(String phrase) {
    this.phrase = phrase;
  }

  public int getMaxAnagrams() {
    return maxAnagrams;
  }

  public synchronized void setMaxAnagrams(int maxAnagrams) {
    this.maxAnagrams = maxAnagrams;
  }

  public synchronized HashSet<String> getAnagrams() {
    return anagrams;
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  private long factorial(int num) {
    long fNum = num;

    for (int i = num - 1; i > 1; i--) {
      fNum *= i;
    }

    return fNum;
  }

  public synchronized void calculate() {
    char[] characters;
    char c1, c2;
    int randomIndex, remainingLen, loopCnt;
    Random rGen;
    String anagram, tmp;
    long fNum, maxLoops;
    HashSet<String> permutations;
    StringBuffer permutation;

    if (anagrams == null) {
      anagrams = new HashSet<String>();
    }
    else {
      anagrams.clear();
    }

    //Calculate Max Loops
    fNum = factorial(phrase.length());
    if (maxAnagrams > fNum) {
      maxLoops = fNum;
    }
    else {
      maxLoops = maxAnagrams;
    }

    //Knuth Shuffle Swap
    rGen = new Random(System.currentTimeMillis());
    loopCnt = 1;
    permutations = new HashSet<String>();

    do {
      //Split phrase into list of characters
      if (ignoreCase) {
        characters = phrase.toUpperCase().toCharArray();
      }
      else {
        characters = phrase.toCharArray();
      }

      permutation = new StringBuffer();

      remainingLen = characters.length;

      for (int index = 0; index < characters.length; index++) {
        c1 = characters[index];

        randomIndex = index + rGen.nextInt(remainingLen);

        c2 = characters[randomIndex];

        //Swap
        characters[index] = c2;
        characters[randomIndex] = c1;

        if (index > 0) {
          permutation.append(",");
        }
        permutation.append(randomIndex);

        remainingLen--;
      }

      anagram = String.valueOf(characters);
      tmp = permutation.toString();

      if (!permutations.contains(tmp)) {
        permutations.add(tmp);

        if (!anagrams.contains(anagram)) {
          anagrams.add(anagram);
        }

        loopCnt++;
      }
    } while (loopCnt <= maxLoops);

    permutations.clear();
  }

  public synchronized void list(PrintStream out) {
    for (String anagram : anagrams) {
      out.println(anagram);
    }
  }

  public static void main(String[] args) {
    Anagramizer anagramizer;
    int exitCode;

    if (args.length != 3) {
      System.err.println("Usage: java " + Anagramizer.class.getName() + " [PHRASE] [MAX_ANAGRAMS] [IGNORE_CASE:Y|N]");
      exitCode = 1;
    }
    else {
      try {
        anagramizer = new Anagramizer();
        anagramizer.setPhrase(args[0]);
        anagramizer.setMaxAnagrams(Integer.parseInt(args[1]));
        anagramizer.setIgnoreCase("Y".equalsIgnoreCase(args[2].trim()));

        anagramizer.calculate();
        anagramizer.list(System.out);

        exitCode = 0;
      } //End try block
      catch (Exception e) {
        exitCode = 1;
        e.printStackTrace();
      }
    }

    System.out.flush();
    System.err.flush();
    System.exit(exitCode);
  }

}
