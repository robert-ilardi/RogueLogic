/*
 * Created on Oct 22, 2007
 */
package com.roguelogic.games.icbm;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

/**
 * @author rilardi
 */

public class ICBMApplet extends JApplet {

  public static final String GAME_TITLE = "ICBMs 1.0";

  private static final String TOP_SCORE_UPDATE_PASSKEY = "RL/RI-ICBM23423!IsTheBest_GameEver78ThisIsTheWorstSecurityEver45245";

  public static final String SCORE_MANAGER_URL_PARAM = "ScoreManagerUrl";
  public static final String MAX_CITY_CNT_PARAM = "MaxCityCnt";

  public static final int DEFAULT_CITY_CNT = 3;

  public static final int ICBM_INTERCEPTION_BASE_SCORE = 50;

  private ICBMPanel icbmPanel;

  private int maxCityCnt;

  private int highScore;
  private int score;
  private int cityCnt;
  private int level;
  private int interceptedIcbms;

  private JLabel scoreLbl, cityCntLbl, levelLbl;
  private JButton newGameBtn, infoBtn, pauseBtn;

  private String scoreManagerUrl;

  private HashSet<Integer> destroyCitySet;

  public ICBMApplet() throws HeadlessException {
    super();
  }

  public void init() {
    destroyCitySet = new HashSet<Integer>();

    scoreManagerUrl = getParameter(SCORE_MANAGER_URL_PARAM);

    System.out.println("Score Manager URL: " + scoreManagerUrl);

    try {
      maxCityCnt = Integer.parseInt(getParameter(MAX_CITY_CNT_PARAM));
    }
    catch (Exception e) {
      System.err.println("Using Default City Cnt...");
      maxCityCnt = DEFAULT_CITY_CNT;
      //e.printStackTrace();
    }

    System.out.println("Drawing up to " + maxCityCnt + " cities");

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

    startNewGame();
  }

  public int getMaxCityCnt() {
    return maxCityCnt;
  }

  private void createGUI() {
    JPanel mainPanel, blankPanel, cntrlPanel;
    GridBagLayout frameGbl, mpGbl, cpGbl;
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

    pauseBtn = new JButton("Pause");
    cpGbl.setConstraints(pauseBtn, gbc);
    cntrlPanel.add(pauseBtn);

    pauseBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        doPause();
      }
    });

    cityCntLbl = new JLabel("Cities Remaining: 0");
    cpGbl.setConstraints(cityCntLbl, gbc);
    cntrlPanel.add(cityCntLbl);

    levelLbl = new JLabel("Level: 1");
    cpGbl.setConstraints(levelLbl, gbc);
    cntrlPanel.add(levelLbl);

    gbc.weightx = 1.0;
    scoreLbl = new JLabel("Score: 0 - High Score: 0");
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

    //Game Area Panel------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 2.0;
    gbc.weighty = 1.0;

    icbmPanel = new ICBMPanel();
    icbmPanel.setIcbmApplet(this);
    mpGbl.setConstraints(icbmPanel, gbc);
    mainPanel.add(icbmPanel);

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

    info.append(ICBMApplet.GAME_TITLE);
    info.append("\nCopyright (C) 2007 By: Robert C. Ilardi");
    info.append("\nVisit: http://www.roguelogic.com");

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(ICBMApplet.this, info.toString(), ICBMApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
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
      tmp = new StringBuffer().append(scoreManagerUrl).append("?action=list").toString();
      System.out.println("Opening: " + tmp);
      url = new URL(tmp);
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
      urlBuf.append(scoreManagerUrl);

      urlBuf.append("?action=update");
      urlBuf.append("&name=");
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

  public synchronized void startNewGame() {
    cityCnt = maxCityCnt;
    destroyCitySet.clear();

    level = 1;
    interceptedIcbms = 0;
    score = 0;

    updateCitiesRemaining();
    updateScore();
    updateLevel();

    icbmPanel.reset();
  }

  private void updateScore() {
    if (score > highScore) {
      highScore = score;
    }

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          scoreLbl.setText(new StringBuffer().append("Score: ").append(score).append("  -  High Score: ").append(highScore).toString());
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
          icbmPanel.stopProcessThread();

          try {
            if (score > getLowestTopScore()) {
              String name = JOptionPane.showInputDialog(ICBMApplet.this, "Game Over! All of your cities have been destroyed! - You have a new Top 10 Score! Enter Your Name: ", ICBMApplet.GAME_TITLE,
                  JOptionPane.INFORMATION_MESSAGE);

              if (name != null) {
                addTopScore(name.trim(), score);
              }
            }
            else {
              JOptionPane.showMessageDialog(ICBMApplet.this, "Game Over! All of your cities have been destroyed!", ICBMApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateCitiesRemaining() {
    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          cityCntLbl.setText(new StringBuffer().append("Cities Remaining: ").append(cityCnt).toString());
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public synchronized void reportDestroyedCity(int cityIndex) {
    if (!destroyCitySet.contains(cityIndex)) {
      destroyCitySet.add(cityIndex);

      cityCnt--;
      updateCitiesRemaining();

      if (cityCnt == 0) {
        gameOver();
      }
    }
  }

  public synchronized void reportIcbmIntercepted() {
    score += (ICBM_INTERCEPTION_BASE_SCORE * level);
    updateScore();

    interceptedIcbms++;

    if (interceptedIcbms % 10 == 0) {
      level++;
    }

    updateLevel();
  }

  private void updateLevel() {
    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          levelLbl.setText(new StringBuffer().append("Level: ").append(level).toString());
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int getLevel() {
    return level;
  }

  private void doPause() {
    icbmPanel.togglePause();
  }

}
