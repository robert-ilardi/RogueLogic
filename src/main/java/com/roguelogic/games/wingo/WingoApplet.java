/*
 * Created on Oct 18, 2007
 */
package com.roguelogic.games.wingo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author rilardi
 */

public class WingoApplet extends JApplet {

  public static final String GAME_TITLE = "Wingo 1.0";

  private static final String TOP_SCORE_UPDATE_PASSKEY = "RL/RI-BallBuster23423!IsTheBest_GameEver78ThisIsTheWorstSecurityEver45245";

  public static final String TOP_SCORE_UPDATE_URL_PARAM = "TopScoreUpdateUrl";
  public static final String GET_TOP_SCORE_URL_PARAM = "GetTopScoreUrl";
  public static final String DICTIONARY_URL_PARAM = "DictionaryUrl";
  public static final String PRACTICE_GAME_PARAM = "PracticeGame";
  public static final String TIME_LIMIT_PARAM = "TimeLimit";

  public static final int CORRECT_WORD_SCORE = 100;
  public static final int GUESS_BONUS = 10;
  public static final int INCORRECT_WORD_PENALTY = -25;
  public static final int NOT_GUESSED_PENALTY = -10;

  public static final long TEN_MINUTES = 600000; //Ten minutes in milliseconds

  private WngPanel wngPanel;

  private int highScore;
  private int score;
  private int round;
  private int correctGuesses;

  private JLabel scoreLbl, timerLbl, wordLbl, roundLbl;
  private JButton newGameBtn, infoBtn, submitBtn;
  private JTextField wordTxt;

  private String topScoreUpdateUrl;
  private String getTopScoreUrl;
  private String dictUrl;

  private HashSet<String> dictionary;
  private String statusMesg = null;

  private Thread timerThread;
  private boolean gameInProgress;
  private Object ttLock;
  private boolean ttRunning;

  private boolean practiceGame = false;
  private long timeLimit = TEN_MINUTES;

  private Runnable timerRunner = new Runnable() {
    public void run() {
      long startTime = 0, curTime = 0;

      synchronized (ttLock) {
        ttRunning = true;
        ttLock.notifyAll();
      }

      startTime = System.currentTimeMillis();

      while (gameInProgress) {
        curTime = System.currentTimeMillis();

        final String tlTxt = new StringBuffer().append("Time Remaining: ").append(calculateRemaining(startTime, curTime)).toString();

        try {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              timerLbl.setText(tlTxt);
            }
          });
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        try {
          Thread.sleep(1000);
        }
        catch (Exception e) {}
      }

      synchronized (ttLock) {
        gameInProgress = false;
        ttRunning = false;
        ttLock.notifyAll();
      }

      if ((curTime - startTime) >= timeLimit) {
        gameOver();
      }
    }
  };

  public String calculateRemaining(long startTime, long curTime) {
    StringBuffer sb = new StringBuffer();
    long remaining;
    int minutes, seconds;

    if ((curTime - startTime) >= timeLimit) {
      sb.append("0:00");
      gameInProgress = false;
    }
    else {

      remaining = timeLimit - (curTime - startTime);

      minutes = (int) (remaining / 60000);
      seconds = (int) ((remaining - (minutes * 60000)) / 1000);

      if (minutes == 0 && seconds <= 0) {
        sb.append("0:00");
        gameOver();
      }
      else {
        sb.append(minutes);

        sb.append(":");

        if (seconds < 10) {
          sb.append("0");
        }

        sb.append(seconds);
      }
    }

    return sb.toString();
  }

  public WingoApplet() throws HeadlessException {
    super();
  }

  public void init() {
    try {
      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          createGUI();
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    gameInProgress = false;
    ttLock = new Object();
    ttRunning = false;

    topScoreUpdateUrl = getParameter(TOP_SCORE_UPDATE_URL_PARAM);
    getTopScoreUrl = getParameter(GET_TOP_SCORE_URL_PARAM);
    dictUrl = getParameter(DICTIONARY_URL_PARAM);
    practiceGame = "TRUE".equalsIgnoreCase(getParameter(PRACTICE_GAME_PARAM));

    try {
      timeLimit = Long.parseLong(getParameter(TIME_LIMIT_PARAM));
    }
    catch (Exception e) {
      System.err.println("Invalid Time Limit Parameter! Using Default...");
      timeLimit = TEN_MINUTES;

      e.printStackTrace();
    }

    try {
      loadDictionary();

      try {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (practiceGame) {
              JOptionPane.showMessageDialog(WingoApplet.this, "Wingo untimed \"Practice Mode\" Enabled!", WingoApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }
            else {
              JOptionPane.showMessageDialog(WingoApplet.this, "The first game and it's timer will start as soon as you click ok.", WingoApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }

            startNewGame(); //Start First Game
          }
        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    catch (Exception e) {
      e.printStackTrace();

      try {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(WingoApplet.this, "An error occurred while attempting to load dictionary: " + dictUrl, WingoApplet.GAME_TITLE, JOptionPane.ERROR_MESSAGE);
          }
        });
      }
      catch (Exception e2) {
        e2.printStackTrace();
      }
    }
  }

  public void paint(Graphics g) {
    super.paint(g);

    if (statusMesg != null && statusMesg.trim().length() > 0) {
      g.setColor(Color.BLACK);
      g.fillRect(50, (getHeight() / 2) - 50, getWidth() - 100, 100);

      g.setColor(Color.WHITE);
      g.drawString(statusMesg.trim(), 60, getHeight() / 2);
    }
  }

  private void createGUI() {
    JPanel mainPanel, blankPanel, cntrlPanel, tePanel;
    GridBagLayout frameGbl, mpGbl, cpGbl, tepGbl;
    GridBagConstraints gbc;

    gbc = new GridBagConstraints();

    //Add the main panel
    mainPanel = new JPanel();
    mainPanel.setPreferredSize(getSize());
    mainPanel.setMaximumSize(getSize());
    mainPanel.setMinimumSize(getSize());
    mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;

    frameGbl = new GridBagLayout();
    frameGbl.setConstraints(mainPanel, gbc);
    setLayout(frameGbl);
    add(mainPanel);

    mpGbl = new GridBagLayout();
    mainPanel.setLayout(mpGbl);

    //Control Panel (Buttons and Labels)----------------------------------------->
    //Reset Constraints
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    cpGbl = new GridBagLayout();
    cntrlPanel = new JPanel();
    cntrlPanel.setLayout(cpGbl);
    cpGbl.setConstraints(cntrlPanel, gbc);
    mainPanel.add(cntrlPanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    newGameBtn = new JButton("New Game");
    cpGbl.setConstraints(newGameBtn, gbc);
    cntrlPanel.add(newGameBtn);

    newGameBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        doNewGameCmd();
      }
    });

    infoBtn = new JButton("Info");
    cpGbl.setConstraints(infoBtn, gbc);
    cntrlPanel.add(infoBtn);

    infoBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        doInfoCmd();
      }
    });

    timerLbl = new JLabel("Time Remaining: 10:00");
    cpGbl.setConstraints(timerLbl, gbc);
    cntrlPanel.add(timerLbl);

    gbc.weightx = 1.0;
    scoreLbl = new JLabel("Score: 0");
    cpGbl.setConstraints(scoreLbl, gbc);
    cntrlPanel.add(scoreLbl);

    //End Row - Control Panel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    cpGbl.setConstraints(blankPanel, gbc);
    cntrlPanel.add(blankPanel);

    //End Row - Main Panel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Text Entry Controls------------------------------------------------->
    //Reset Constraints
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    tepGbl = new GridBagLayout();
    tePanel = new JPanel();
    tePanel.setLayout(tepGbl);
    tepGbl.setConstraints(tePanel, gbc);
    mainPanel.add(tePanel);

    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 0.0;

    wordLbl = new JLabel("Enter Guess:");
    tepGbl.setConstraints(wordLbl, gbc);
    tePanel.add(wordLbl);

    wordTxt = new JTextField();
    wordTxt.setPreferredSize(new Dimension(300, 25));
    wordTxt.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
          doWordSubmit();
        }
      }

      public void keyTyped(KeyEvent ke) {}

      public void keyReleased(KeyEvent ke) {}
    });

    tepGbl.setConstraints(wordTxt, gbc);
    tePanel.add(wordTxt);

    submitBtn = new JButton("Submit");
    tepGbl.setConstraints(submitBtn, gbc);
    tePanel.add(submitBtn);

    submitBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        doWordSubmit();
      }
    });

    roundLbl = new JLabel("Round: 0");
    tepGbl.setConstraints(roundLbl, gbc);
    tePanel.add(roundLbl);

    //End Row - Text Entry Panel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    tepGbl.setConstraints(blankPanel, gbc);
    tePanel.add(blankPanel);

    //End Row - Main Panel
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 0.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Game Area Panel------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 2.0;
    gbc.weighty = 1.0;

    wngPanel = new WngPanel();
    wngPanel.setWngApplet(this);
    mpGbl.setConstraints(wngPanel, gbc);
    mainPanel.add(wngPanel);

    //End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    //Last Row (Main Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);
  }

  public void doNewGameCmd() {
    int retCode = JOptionPane.showConfirmDialog(this, "Are you sure you want to start a New Game?", GAME_TITLE, JOptionPane.YES_NO_OPTION);

    if (retCode == JOptionPane.YES_OPTION) {
      startNewGame();
    }
  }

  public void doInfoCmd() {
    final StringBuffer info = new StringBuffer();

    info.append(WingoApplet.GAME_TITLE);
    info.append("\nCopyright (C) 2007 By: Robert C. Ilardi");
    info.append("\nVisit: http://www.roguelogic.com");

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(WingoApplet.this, info.toString(), WingoApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void loadDictionary() throws IOException {
    URL url;
    HttpURLConnection conn = null;
    InputStream ins = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    String tmp;

    try {
      statusMesg = "Loading Dictionary: " + dictUrl;
      System.out.println("Loading Dictionary: " + dictUrl);
      repaint();

      url = new URL(dictUrl);
      conn = (HttpURLConnection) url.openConnection();

      conn.connect();

      ins = conn.getInputStream();
      isr = new InputStreamReader(ins);
      br = new BufferedReader(isr);

      dictionary = new HashSet<String>();

      tmp = br.readLine();

      while (tmp != null) {
        //System.out.println(tmp);
        dictionary.add(tmp.trim().toUpperCase());

        tmp = br.readLine();
      }

      statusMesg = null;
      repaint();
    }
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

      if (ins != null) {
        try {
          ins.close();
        }
        catch (Exception e) {}
      }

      if (conn != null) {
        try {
          conn.disconnect();
        }
        catch (Exception e) {}
      }
    }
  }

  public int getLowestTopScore() throws IOException {
    int lowestTopScore = 0;
    URL url;
    HttpURLConnection conn = null;
    InputStream ins = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    String tmp;
    String[] tmpArr;
    ArrayList<Integer> scores = new ArrayList<Integer>();

    try {
      System.out.println("Opening: " + getTopScoreUrl);
      url = new URL(getTopScoreUrl);
      conn = (HttpURLConnection) url.openConnection();

      conn.connect();

      ins = conn.getInputStream();
      isr = new InputStreamReader(ins);
      br = new BufferedReader(isr);

      tmp = br.readLine();

      while (tmp != null) {
        System.out.println(tmp);

        tmpArr = tmp.split("\\|");

        for (int i = 0; i < tmpArr.length; i++) {
          try {
            scores.add(Integer.valueOf(tmpArr[i]));
          }
          catch (NumberFormatException e) {}
        }

        tmp = br.readLine();
      }

      Collections.sort(scores);

      if (!scores.isEmpty()) {
        lowestTopScore = scores.get(0);
      }
    }
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

      if (ins != null) {
        try {
          ins.close();
        }
        catch (Exception e) {}
      }

      if (conn != null) {
        try {
          conn.disconnect();
        }
        catch (Exception e) {}
      }
    }

    return lowestTopScore;
  }

  public void addTopScore(String name, int score) throws IOException {
    URL url;
    StringBuffer urlBuf = new StringBuffer();
    HttpURLConnection conn = null;
    InputStream ins = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    String tmp;

    try {
      urlBuf.append(topScoreUpdateUrl);

      urlBuf.append("?name=");
      urlBuf.append(URLEncoder.encode(name, "UTF-8"));

      urlBuf.append("&score=");
      urlBuf.append(score);

      urlBuf.append("&update_passkey=");
      urlBuf.append(URLEncoder.encode(TOP_SCORE_UPDATE_PASSKEY, "UTF-8"));

      //System.out.println("Opening: " + urlBuf.toString());
      url = new URL(urlBuf.toString());
      conn = (HttpURLConnection) url.openConnection();

      conn.connect();

      ins = conn.getInputStream();
      isr = new InputStreamReader(ins);
      br = new BufferedReader(isr);

      tmp = br.readLine();

      while (tmp != null) {
        System.out.println(tmp);
        tmp = br.readLine();
      }
    }
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

      if (ins != null) {
        try {
          ins.close();
        }
        catch (Exception e) {}
      }

      if (conn != null) {
        try {
          conn.disconnect();
        }
        catch (Exception e) {}
      }
    }
  }

  public void doWordSubmit() {
    String guess = wordTxt.getText().trim();

    if (!gameInProgress) {
      try {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(WingoApplet.this, "Game Over! Click New Game to start a new game.", WingoApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
          }
        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    else if (guess.length() == WngPanel.LETTERS_PER_WORD) {
      wngPanel.enterGuess(guess);

      try {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            wordTxt.setText("");
          }
        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    else {
      try {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(WingoApplet.this, "Guess word MUST be EXACTLY 5 letters long!", WingoApplet.GAME_TITLE, JOptionPane.ERROR_MESSAGE);
          }
        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public HashSet<String> getDictionary() {
    return dictionary;
  }

  public void reportGuessCorrect(int guessCnt) {
    score += CORRECT_WORD_SCORE + ((WngPanel.MAX_ALLOWED_GUESSES - (guessCnt - 1)) * GUESS_BONUS);
    updateScore();

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(WingoApplet.this, "Correct! The word was: " + wngPanel.getLastWord(), WingoApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    correctGuesses++;
    round++;

    wngPanel.newWord();
  }

  public void reportGuessMisspelled() {
    score += INCORRECT_WORD_PENALTY;
    updateScore();

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(WingoApplet.this, "Incorrect Spelling! Or your word was NOT in our dictionary. The word was: " + wngPanel.getLastWord(), WingoApplet.GAME_TITLE,
              JOptionPane.ERROR_MESSAGE);
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    round++;

    wngPanel.newWord();
  }

  public void reportMaxGuessesExceeded() {
    score += NOT_GUESSED_PENALTY;
    updateScore();

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(WingoApplet.this, "Too many guesses! The word was: " + wngPanel.getLastWord(), WingoApplet.GAME_TITLE, JOptionPane.ERROR_MESSAGE);
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    round++;

    wngPanel.newWord();
  }

  public synchronized void startNewGame() {
    //Make Sure the timer thread is stopped
    if (!practiceGame) {
      synchronized (ttLock) {
        gameInProgress = false;

        while (ttRunning) {
          try {
            ttLock.wait();
          }
          catch (Exception e) {
            //Opps! Once we start another timer thread, we might have more than one threading running if this happens...
            e.printStackTrace();
          }
        }
      }
    }
    else {
      gameInProgress = true;
      System.out.println("Practice Game Mode Enabled...");

      try {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            timerLbl.setText("Practice");
          }
        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    round = 1;
    correctGuesses = 0;
    score = 0;
    updateScore();

    if (!practiceGame) {
      //Start Timer Thread and wait for it to start running...
      gameInProgress = true;
      timerThread = new Thread(timerRunner);
      timerThread.start();

      synchronized (ttLock) {
        try {
          while (!ttRunning) {
            ttLock.wait();
          }
        }
        catch (Exception e) {
          //Opps! If this happens, don't know if the timer is running...
          e.printStackTrace();
        }
      }
    }

    wngPanel.newWord();
  }

  private void updateScore() {
    if (score > highScore) {
      highScore = score;
    }

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          scoreLbl.setText(new StringBuffer().append("Score: ").append(score).append("  -  High Score: ").append(highScore).toString());
          roundLbl.setText(new StringBuffer().append("Round: ").append(round).toString());
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private synchronized void gameOver() {
    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          StringBuffer gameOverSb = new StringBuffer();

          gameOverSb.append("Game Over! Time has elapsed!\nYou have scored ");

          gameOverSb.append(score);

          if (score == 1) {
            gameOverSb.append(" point");
          }
          else {
            gameOverSb.append(" points");
          }

          gameOverSb.append("!\nYou Guessed ");

          gameOverSb.append(correctGuesses);

          if (correctGuesses == 1) {
            gameOverSb.append(" word");
          }
          else {
            gameOverSb.append(" words");
          }

          gameOverSb.append(" correctly out of ");

          gameOverSb.append(round);

          if (round == 1) {
            gameOverSb.append(" round!");
          }
          else {
            gameOverSb.append(" rounds!");
          }

          JOptionPane.showMessageDialog(WingoApplet.this, gameOverSb.toString(), WingoApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);

        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
