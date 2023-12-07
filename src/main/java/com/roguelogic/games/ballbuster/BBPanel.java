/*
 * Created on Oct 2, 2007
 */
package com.roguelogic.games.ballbuster;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author rilardi
 */
public class BBPanel extends JPanel implements MouseListener {

  private Ball[][] balls;
  private BallBusterApplet bbApplet;

  private boolean bustedSomeBalls;
  private int score;
  private int bustedBallCnt;

  public static final int BALL_DIAMETER = 25;
  public static final int STARTING_ROW = 0;

  public BBPanel(LayoutManager lm, boolean dBuf) {
    super(lm, dBuf);
    init();
  }

  public BBPanel(LayoutManager lm) {
    super(lm);
    init();
  }

  public BBPanel(boolean dBuf) {
    super(dBuf);
    init();
  }

  public BBPanel() {
    super();
    init();
  }

  private void init() {
    addMouseListener(this);
  }

  public void setBBApplet(BallBusterApplet bbApplet) {
    this.bbApplet = bbApplet;
  }

  public void newGame() {
    int rows, cols, id, color, x, y;
    Random rnd;

    bustedSomeBalls = false;
    score = 0;
    bustedBallCnt = 0;
    bbApplet.updateScore(score);

    rows = (bbApplet.getWidth() / BALL_DIAMETER) - 2;
    cols = bbApplet.getHeight() / BALL_DIAMETER - 1;

    balls = new Ball[rows][cols];
    rnd = new Random();
    id = 0;
    y = BALL_DIAMETER * STARTING_ROW;

    for (int row = 0; row < balls.length; row++) {
      x = 0;

      for (int col = 0; col < balls[row].length; col++) {
        balls[row][col] = new Ball();
        balls[row][col].setId(id);
        balls[row][col].setRow(row);
        balls[row][col].setCol(col);

        color = rnd.nextInt(Ball.MAX_COLOR) + 1;
        balls[row][col].setColor(color);

        balls[row][col].setX(x);
        balls[row][col].setY(y);

        //System.out.println(balls[row][col].toString());

        x += BALL_DIAMETER;
        id++;
      }

      y += BALL_DIAMETER;
    }

    repaint();
  }

  protected void paintComponent(Graphics g) {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());

    paintBalls(g);
    paintOutlineSelectedAdjacentBalls(g);
  }

  private void paintBalls(Graphics g) {
    if (balls == null) {
      return;
    }

    for (int row = 0; row < balls.length; row++) {
      for (int col = 0; col < balls[row].length; col++) {
        switch (balls[row][col].getColor()) {
          case Ball.RED_BALL:
            g.setColor(Color.RED);
            break;
          case Ball.BLUE_BALL:
            g.setColor(Color.BLUE);
            break;
          case Ball.GREEN_BALL:
            g.setColor(Color.GREEN);
            break;
          case Ball.ORANGE_BALL:
            g.setColor(Color.PINK);
            break;
          case Ball.YELLOW_BALL:
            g.setColor(Color.YELLOW);
            break;
          default:
            g.setColor(Color.WHITE);
        }

        g.fillOval(balls[row][col].getX(), balls[row][col].getY(), BALL_DIAMETER, BALL_DIAMETER);
      }
    }

    if (bustedSomeBalls) {
      bustedSomeBalls = false;
      bbApplet.updateScore(score);
      playBustEffect();
      bustedBallCnt = 0;
      checkEndGame();
    }
  }

  private void paintOutlineSelectedAdjacentBalls(Graphics g) {
    for (int row = 0; row < balls.length; row++) {
      for (int col = 0; col < balls[row].length; col++) {
        if (!balls[row][col].isBusted() && balls[row][col].isSelected()) {
          g.setColor(Color.BLACK);

          for (int i = 0; i < 5; i++) {
            g.drawOval(balls[row][col].getX(), balls[row][col].getY() + i, BALL_DIAMETER, BALL_DIAMETER - 1);
          }
        }
      }
    }
  }

  private Ball determineClickedBall(MouseEvent me) {
    Ball b = null;
    int row = 0, col = 0;

    for (int i = 0; i <= balls.length; i++) {
      if (me.getY() >= (i * BALL_DIAMETER) && me.getY() < ((i + 1) * BALL_DIAMETER)) {
        row = i - STARTING_ROW;
        break;
      }
    }

    for (int i = 0; i < balls[0].length; i++) {
      if (me.getX() >= (i * BALL_DIAMETER) && me.getX() < ((i + 1) * BALL_DIAMETER)) {
        col = i;
        break;
      }
    }

    if (row >= 0 && col >= 0) {
      //System.out.println(row + ", " + col);
      b = balls[row][col];
    }

    return b;
  }

  public synchronized void mouseClicked(MouseEvent me) {
    Ball b;

    b = determineClickedBall(me);
    //System.out.println(b);

    if (b != null) {
      if (b.isBusted()) {
        return;
      }
      else if (b.isSelected()) {
        //Clicked Selected Ball
        bustBalls();
        moveUpBustedBalls();
        shiftEmptyColumnsLeft();
        resetAllSelections();
      }
      else {
        //Clicked non-selected Ball
        resetAllSelections();

        markSelectedAdjacentBalls(b);
      }
    }

    repaint();
  }

  public void mousePressed(MouseEvent me) {}

  public void mouseReleased(MouseEvent me) {}

  public void mouseEntered(MouseEvent me) {}

  public void mouseExited(MouseEvent me) {}

  private void resetAllSelections() {
    for (int row = 0; row < balls.length; row++) {
      for (int col = 0; col < balls[row].length; col++) {
        balls[row][col].setSelected(false);
        balls[row][col].setCheckedLeft(false);
        balls[row][col].setCheckedRight(false);
        balls[row][col].setCheckedUp(false);
        balls[row][col].setCheckedDown(false);
      }
    }
  }

  private void markSelectedAdjacentBalls(Ball selectedBall) {
    checkLeftBall(selectedBall);

    checkRightBall(selectedBall);

    checkUpBall(selectedBall);

    checkDownBall(selectedBall);
  }

  private void checkLeftBall(Ball selectedBall) {
    Ball b;

    if (selectedBall.isCheckedLeft()) {
      return;
    }
    else {
      selectedBall.setCheckedLeft(true);
    }

    if (selectedBall.getCol() > 0) {
      b = balls[selectedBall.getRow()][selectedBall.getCol() - 1];

      if (selectedBall.getColor() == b.getColor()) {
        selectedBall.setSelected(true);
        b.setSelected(true);

        markSelectedAdjacentBalls(b);
      }
    }
  }

  private void checkRightBall(Ball selectedBall) {
    Ball b;

    if (selectedBall.isCheckedRight()) {
      return;
    }
    else {
      selectedBall.setCheckedRight(true);
    }

    if (selectedBall.getCol() + 1 < balls[0].length) {
      b = balls[selectedBall.getRow()][selectedBall.getCol() + 1];

      if (selectedBall.getColor() == b.getColor()) {
        selectedBall.setSelected(true);
        b.setSelected(true);

        markSelectedAdjacentBalls(b);
      }
    }
  }

  private void checkUpBall(Ball selectedBall) {
    Ball b;

    if (selectedBall.isCheckedUp()) {
      return;
    }
    else {
      selectedBall.setCheckedUp(true);
    }

    if (selectedBall.getRow() > 0) {
      b = balls[selectedBall.getRow() - 1][selectedBall.getCol()];

      if (selectedBall.getColor() == b.getColor()) {
        selectedBall.setSelected(true);
        b.setSelected(true);

        markSelectedAdjacentBalls(b);
      }
    }
  }

  private void checkDownBall(Ball selectedBall) {
    Ball b;

    if (selectedBall.isCheckedDown()) {
      return;
    }
    else {
      selectedBall.setCheckedDown(true);
    }

    if (selectedBall.getRow() + 1 < balls.length) {
      b = balls[selectedBall.getRow() + 1][selectedBall.getCol()];

      if (selectedBall.getColor() == b.getColor()) {
        selectedBall.setSelected(true);
        b.setSelected(true);

        markSelectedAdjacentBalls(b);
      }
    }
  }

  private void bustBalls() {
    for (int row = 0; row < balls.length; row++) {
      for (int col = 0; col < balls[row].length; col++) {
        if (balls[row][col].isSelected()) {
          bustedSomeBalls = true;

          balls[row][col].setBusted(true);
          balls[row][col].setColor(Ball.BUSTED_BALL);

          bustedBallCnt++;
        }
      }
    }

    //score += bustedBallCnt + ((bustedBallCnt / 2) - 1);
    if (bustedBallCnt > 4) {
      score += bustedBallCnt + (bustedBallCnt * bustedBallCnt);
    }
    else if (bustedBallCnt == 4) {
      score += 8;
    }
    else {
      score += bustedBallCnt;
    }
  }

  private void moveUpBustedBalls() {
    boolean foundUnbusted, foundBusted;
    int row;

    for (int col = 0; col < balls[0].length; col++) {
      do {
        //Skip top busted balls
        foundUnbusted = false;
        for (row = 0; row < balls.length; row++) {
          if (!balls[row][col].isBusted()) {
            foundUnbusted = true;
            break;
          }
        }

        //Find first busted ball and move it up one
        foundBusted = false;

        if (foundUnbusted) {
          for (++row; row < balls.length; row++) {
            if (balls[row][col].isBusted()) {
              foundBusted = true;
              swapBallDetails(balls[row - 1][col], balls[row][col]); //Move Busted Ball Up One
              break;
            }
          }
        } //End if foundUnbusted check
      } while (foundBusted);
    } //End for col loop through columns
  }

  private void swapBallDetails(Ball b1, Ball b2) {
    int color, id;
    //int x, y, row, col;
    boolean busted;

    color = b1.getColor();
    //x = b1.getX();
    //y = b1.getY();
    id = b1.getId();
    //row = b1.getRow();
    //col = b1.getCol();
    busted = b1.isBusted();

    b1.setColor(b2.getColor());
    //b1.setX(b2.getX());
    //b1.setY(b2.getY());
    b1.setId(b2.getId());
    //b1.setRow(b2.getRow());
    //b1.setCol(b2.getCol());
    b1.setBusted(b2.isBusted());

    b2.setColor(color);
    //b2.setX(x);
    //b2.setY(y);
    b2.setId(id);
    //b2.setRow(row);
    //b2.setCol(col);
    b2.setBusted(busted);
  }

  private void checkEndGame() {
    boolean hasAdjacentMatches = false;

    for (int row = 0; row < balls.length; row++) {
      for (int col = 0; col < balls[row].length; col++) {
        if (!balls[row][col].isBusted()) {
          markSelectedAdjacentBalls(balls[row][col]);

          if (balls[row][col].isSelected()) {
            hasAdjacentMatches = true;
            break;
          }
        }
      }
    }

    if (!hasAdjacentMatches) {
      doEndGame();
    }
    else {
      resetAllSelections();
    }
  }

  private void doEndGame() {
    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          bbApplet.playEndGameClip();

          try {
            if (score > bbApplet.getLowestTopScore()) {
              String name = JOptionPane.showInputDialog(BBPanel.this, "Game Board Complete! - You have a new Top 10 Score! Enter Your Name: ", BallBusterApplet.GAME_TITLE,
                  JOptionPane.INFORMATION_MESSAGE);

              if (name != null) {
                bbApplet.addTopScore(name.trim(), score);
              }
            }
            else {
              JOptionPane.showMessageDialog(BBPanel.this, "Game Board Completed!", BallBusterApplet.GAME_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }
          }
          catch (Exception e) {
            e.printStackTrace();
          }

          newGame();
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void playBustEffect() {
    if (bustedBallCnt < 10) {
      bbApplet.playBustClip();
    }
    else {
      bbApplet.playBreakGlassClip();
    }
  }

  private void shiftEmptyColumnsLeft() {
    boolean foundNonEmpty, foundEmpty;
    int col;

    do {
      //Find first Non Empty Column
      foundNonEmpty = false;
      for (col = 0; col < balls[0].length; col++) {
        for (int row = 0; row < balls.length; row++) {
          if (!balls[row][col].isBusted()) {
            foundNonEmpty = true;
            break;
          }
        }

        if (foundNonEmpty) {
          break;
        }
      }

      //Find first empty column and move it left
      foundEmpty = false;

      if (foundNonEmpty) {
        for (++col; col < balls[0].length; col++) {
          foundEmpty = true;

          for (int row = 0; row < balls.length; row++) {
            if (!balls[row][col].isBusted()) {
              foundEmpty = false;
              break;
            }
          }

          if (foundEmpty) {
            swapBallColumns(col - 1, col);
            break;
          }
        } //End for col loop through columns
      } //End if foundNonEmpty check
    } while (foundEmpty);
  }

  private void swapBallColumns(int col1, int col2) {
    //System.out.println("Swapping Column " + col1 + " with Column " + col2);
    for (int row = 0; row < balls.length; row++) {
      swapBallDetails(balls[row][col1], balls[row][col2]);
    }
  }

}
