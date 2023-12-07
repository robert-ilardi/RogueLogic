/*
 * Created on Oct 2, 2007
 */
package com.roguelogic.games.ballbuster;

import java.applet.AudioClip;
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

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author rilardi
 */

public class BallBusterApplet extends JApplet {

  public static final String GAME_TITLE = "Ball Buster 1.0";

  private static final String TOP_SCORE_UPDATE_PASSKEY = "RL/RI-BallBuster23423!IsTheBest_GameEver78ThisIsTheWorstSecurityEver45245";

  public static final String TOP_SCORE_UPDATE_URL_PARAM = "TopScoreUpdateUrl";
  public static final String GET_TOP_SCORE_URL_PARAM = "GetTopScoreUrl";

  private BBPanel bbPanel;
  private JButton newGameBtn, infoBtn;
  private JLabel scoreLbl;

  private int highScore;

  private AudioClip bustClip = null;
  private AudioClip breakGlassClip = null;
  private AudioClip endGameClip = null;

  private String topScoreUpdateUrl;
  private String getTopScoreUrl;

  public BallBusterApplet() throws HeadlessException {
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

    topScoreUpdateUrl = getParameter(TOP_SCORE_UPDATE_URL_PARAM);
    getTopScoreUrl = getParameter(GET_TOP_SCORE_URL_PARAM);

    bustClip = getAudioClip(getCodeBase(), "bust1.au");
    breakGlassClip = getAudioClip(getCodeBase(), "break_glass.au");
    endGameClip = getAudioClip(getCodeBase(), "end_game1.au");

    bbPanel.newGame(); //Start first game
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

    //Reset Constraints
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    //Control Panel (Buttons and Labels)    
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

    //Game Area Panel------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(5, 5, 5, 2);
    gbc.weightx = 2.0;
    gbc.weighty = 1.0;

    bbPanel = new BBPanel();
    bbPanel.setBBApplet(this);
    mpGbl.setConstraints(bbPanel, gbc);
    mainPanel.add(bbPanel);

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
      bbPanel.newGame();
    }
  }

  public void updateScore(int score) {
    final int fScore = score;

    if (score > highScore) {
      highScore = score;
    }

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          scoreLbl.setText(new StringBuffer().append("Score: ").append(fScore).append("  -  High Score: ").append(highScore).toString());
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void doInfoCmd() {
    final StringBuffer info = new StringBuffer();

    info.append(BallBusterApplet.GAME_TITLE);
    info.append("\nCopyright (C) 2007 By: Robert C. Ilardi");
    info.append("\nVisit: http://www.roguelogic.com");

    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(BallBusterApplet.this, info.toString(), BallBusterApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public synchronized void playBustClip() {
    if (bustClip != null) {
      bustClip.play();
    }
  }

  public void playBreakGlassClip() {
    if (breakGlassClip != null) {
      breakGlassClip.play();
    }
  }

  public void playEndGameClip() {
    if (endGameClip != null) {
      endGameClip.play();
    }
  }

  public int getHighestTopScore() {
    int topScore = 0;

    return topScore;
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

}
