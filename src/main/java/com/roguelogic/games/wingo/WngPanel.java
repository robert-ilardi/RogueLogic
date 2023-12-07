/*
 * Created on Oct 2, 2007
 */
package com.roguelogic.games.wingo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JPanel;

/**
 * @author rilardi
 */
public class WngPanel extends JPanel {

  public static final int GRID_LETTER_BOX_WIDTH = 0;
  public static final int LETTERS_PER_WORD = 5;
  public static final int MAX_ALLOWED_GUESSES = 5;
  public static final int LETTER_X_OFFSET = -70;
  public static final int LETTER_Y_OFFSET = -30;

  public static final Color CORRECT_POSITION_COLOR = new Color(225, 140, 0); //Dark Orange
  public static final Color OUT_OF_POSITION_COLOR = new Color(250, 215, 0); //Gold

  private WingoApplet wngApplet;

  private String currentWord;
  private String lastWord;

  private ArrayList<Guess> guesses;
  private StringBuffer workingGuessBuf;

  private HashSet<String> sessionWords;

  public WngPanel(LayoutManager lm, boolean dBuf) {
    super(lm, dBuf);
    init();
  }

  public WngPanel(LayoutManager lm) {
    super(lm);
    init();
  }

  public WngPanel(boolean dBuf) {
    super(dBuf);
    init();
  }

  public WngPanel() {
    super();
    init();
  }

  private void init() {
    guesses = new ArrayList<Guess>();
    sessionWords = new HashSet<String>();
  }

  public void setWngApplet(WingoApplet wngApplet) {
    this.wngApplet = wngApplet;
  }

  public void newWord() {
    guesses.clear();
    sessionWords.clear();

    chooseWord();
    prepareWorkingGuessBuffer();

    repaint();
  }

  public String getCurrentWord() {
    return currentWord;
  }

  public String getLastWord() {
    return lastWord;
  }

  public void enterGuess(String word) {
    Guess guess = new Guess();

    word = word.trim().toUpperCase();

    if (word.trim().length() > LETTERS_PER_WORD) {
      word = word.substring(0, LETTERS_PER_WORD);
    }

    guess.setWord(word);

    if (currentWord.equalsIgnoreCase(word)) {
      guess.setState(Guess.STATE_CORRECT);
    }
    else if (checkDictionary(word)) {
      //Spelled Correctly
      guess.setState(Guess.STATE_INCORRECT_AND_VALID_WORD);
      updateGuessCharFlags(guess);
    }
    else {
      //Spelled Incorrectly
      guess.setState(Guess.STATE_INCORRECT_AND_INVALID_WORD);
    }

    guesses.add(guess);

    repaint();

    if (guess.getState() == Guess.STATE_CORRECT) {
      wngApplet.reportGuessCorrect(guesses.size());
    }
    else if (guess.getState() == Guess.STATE_INCORRECT_AND_INVALID_WORD) {
      wngApplet.reportGuessMisspelled();
    }
    else if (guesses.size() >= MAX_ALLOWED_GUESSES) {
      wngApplet.reportMaxGuessesExceeded();
    }
  }

  private void chooseWord() {
    HashSet<String> dict = wngApplet.getDictionary();
    Iterator<String> iter;
    Random stopPosRnd;
    int stopPos = 0, cnt;

    lastWord = currentWord;

    do {
      stopPosRnd = new Random();
      stopPos = stopPosRnd.nextInt(dict.size());

      iter = dict.iterator();
      cnt = 0;

      while (iter.hasNext()) {
        currentWord = iter.next();

        if (cnt == stopPos) {
          break;
        }

        cnt++;
      }
    } while (sessionWords.contains(currentWord));

    if (lastWord == null) {
      lastWord = currentWord;
    }

    sessionWords.add(currentWord);
  }

  private void updateGuessCharFlags(Guess g) {
    char gc, cwc;
    int[] cStates;
    boolean[] cwcMatched;

    cStates = new int[LETTERS_PER_WORD];
    g.setCharStates(cStates);

    cwcMatched = new boolean[LETTERS_PER_WORD];
    for (int i = 0; i < LETTERS_PER_WORD; i++) {
      cwcMatched[i] = false;
    }

    for (int i = 0; i < LETTERS_PER_WORD; i++) {
      gc = g.getWord().charAt(i);
      cwc = currentWord.charAt(i);

      if (gc == cwc) {
        cStates[i] = Guess.CHAR_STATE_CORRECT_POSITION;
        cwcMatched[i] = true;
      }
      else {
        cStates[i] = Guess.CHAR_STATE_NOT_CONTAINED; //Assumed...

        for (int j = 0; j < LETTERS_PER_WORD; j++) {
          cwc = currentWord.charAt(j);

          if (gc == cwc && !cwcMatched[j] && cwc != g.getWord().charAt(j)) {
            cStates[i] = Guess.CHAR_STATE_OUT_OF_POSITION;
            cwcMatched[j] = true;
            break;
          }
        }
      }
    }
  }

  private void prepareWorkingGuessBuffer() {
    workingGuessBuf = new StringBuffer();
    workingGuessBuf.append(currentWord.charAt(0)); //Give first letter...
  }

  private boolean checkDictionary(String word) {
    boolean spellingOk = false;
    HashSet<String> dict = wngApplet.getDictionary();

    spellingOk = dict.contains(word.toUpperCase());

    return spellingOk;
  }

  protected void paintComponent(Graphics g) {
    g.setColor(new Color(0, 0, 100));
    g.fillRect(0, 0, getWidth(), getHeight());

    paintGuesses(g);

    paintWorkingGuessBuffer(g);

    paintGrid(g);
  }

  private void paintGrid(Graphics g) {
    int widthBias, heightBias;

    g.setColor(Color.DARK_GRAY);

    widthBias = getWidth() / LETTERS_PER_WORD;
    heightBias = getHeight() / MAX_ALLOWED_GUESSES;

    //Draw Vertical Lines
    for (int i = 1; i < LETTERS_PER_WORD; i++) {
      for (int j = 0; j < 2; j++) {
        g.drawLine(((GRID_LETTER_BOX_WIDTH + widthBias) * i) + j, 0, ((GRID_LETTER_BOX_WIDTH + widthBias) * i) + j, getHeight());
      }
    }

    //Draw Horizontal Lines
    for (int i = 1; i < MAX_ALLOWED_GUESSES; i++) {
      for (int j = 0; j < 2; j++) {
        g.drawLine(0, ((GRID_LETTER_BOX_WIDTH + heightBias) * i) + j, getWidth(), ((GRID_LETTER_BOX_WIDTH + heightBias) * i) + j);
      }
    }
  }

  private void paintGuesses(Graphics g) {
    Guess guess;

    for (int i = 0; i < MAX_ALLOWED_GUESSES && i < guesses.size(); i++) {
      guess = guesses.get(i);
      paintGuess(g, guess, i + 1);
    } //End for i through guesses
  }

  private void paintGuess(Graphics g, Guess guess, int index) {
    int widthBias, heightBias;
    int[] cStates;

    widthBias = getWidth() / LETTERS_PER_WORD;
    heightBias = getHeight() / MAX_ALLOWED_GUESSES;

    //Fill Letter Backgrounds Colors
    cStates = guess.getCharStates();

    if (guess.getState() == Guess.STATE_CORRECT) {
      g.setColor(CORRECT_POSITION_COLOR);
      g.fillRect(0, heightBias * (index - 1), widthBias * LETTERS_PER_WORD, heightBias);
    }
    else if (cStates != null) {
      //Fill each box based on character states
      for (int i = 0; i < LETTERS_PER_WORD && i < guess.getWord().length(); i++) {
        switch (cStates[i]) {
          case Guess.CHAR_STATE_CORRECT_POSITION:
            //Orange
            g.setColor(CORRECT_POSITION_COLOR);
            g.fillRect(widthBias * i, heightBias * (index - 1), widthBias, heightBias);
            break;
          case Guess.CHAR_STATE_OUT_OF_POSITION:
            //Yellow
            g.setColor(OUT_OF_POSITION_COLOR);
            g.fillRect(widthBias * i, heightBias * (index - 1), widthBias, heightBias);
            break;
          case Guess.CHAR_STATE_NOT_CONTAINED:
            //"Clear"
            break;
        }
      }
    }

    //Draw Letters
    g.setFont(new Font("Times New Roman", Font.PLAIN, 48));
    g.setColor(Color.WHITE);

    for (int i = 0; i < LETTERS_PER_WORD && i < guess.getWord().length(); i++) {
      g.drawString(String.valueOf(guess.getWord().charAt(i)), LETTER_X_OFFSET + (widthBias * (i + 1)), LETTER_Y_OFFSET + (heightBias * index));
    }
  }

  private void paintWorkingGuessBuffer(Graphics g) {
    Guess workingGuess;

    if (guesses.size() > 0) {
      return;
    }

    workingGuess = new Guess();
    workingGuess.setWord(workingGuessBuf.toString());
    workingGuess.setState(Guess.STATE_NEW); //So we don't do anything special...

    paintGuess(g, workingGuess, guesses.size() + 1);
  }

}
