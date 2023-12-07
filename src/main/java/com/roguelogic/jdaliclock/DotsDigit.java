/*
 Copyright 2009 Robert C. Ilardi

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

/**
 * Created Mar 25, 2009
 */
package com.roguelogic.jdaliclock;

import java.awt.Graphics;
import java.awt.Point;

/**
 * @author Robert C. Ilardi
 * 
 */
public class DotsDigit implements Digit {

  public static final int PNTS_ARR_SIZE = 20;

  public static final int MOVE_X_INCREMENT = 2;
  public static final int MOVE_Y_INCREMENT = 4;

  private int curDigit = 0;

  private int leftCornerX;
  private int leftCornerY;

  private Point[] points;

  public DotsDigit() {
    points = new Point[PNTS_ARR_SIZE];

    for (int i = 0; i < PNTS_ARR_SIZE; i++) {
      points[i] = new Point();
      points[i].x = -1;
      points[i].y = -1;
    }
  }

  private void morph0(Graphics g) {
    moveTowardsX(0, leftCornerX + 15);
    moveTowardsY(0, leftCornerY);

    moveTowardsX(1, leftCornerX + 5);
    moveTowardsY(1, leftCornerY + 10);

    moveTowardsX(2, leftCornerX);
    moveTowardsY(2, leftCornerY + 20);

    moveTowardsX(3, leftCornerX);
    moveTowardsY(3, leftCornerY + 30);

    moveTowardsX(4, leftCornerX);
    moveTowardsY(4, leftCornerY + 40);

    moveTowardsX(5, leftCornerX);
    moveTowardsY(5, leftCornerY + 50);

    moveTowardsX(6, leftCornerX + 5);
    moveTowardsY(6, leftCornerY + 60);

    moveTowardsX(7, leftCornerX + 15);
    moveTowardsY(7, leftCornerY + 70);

    moveTowardsX(8, leftCornerX + 25);
    moveTowardsY(8, leftCornerY);

    moveTowardsX(9, leftCornerX + 35);
    moveTowardsY(9, leftCornerY + 10);

    moveTowardsX(10, leftCornerX + 40);
    moveTowardsY(10, leftCornerY + 20);

    moveTowardsX(11, leftCornerX + 40);
    moveTowardsY(11, leftCornerY + 30);

    moveTowardsX(12, leftCornerX + 40);
    moveTowardsY(12, leftCornerY + 40);

    moveTowardsX(13, leftCornerX + 40);
    moveTowardsY(13, leftCornerY + 50);

    moveTowardsX(14, leftCornerX + 35);
    moveTowardsY(14, leftCornerY + 60);

    moveTowardsX(15, leftCornerX + 25);
    moveTowardsY(15, leftCornerY + 70);

    // Left Overs
    moveTowardsX(16, leftCornerX + 25);
    moveTowardsY(16, leftCornerY + 70);

    moveTowardsX(17, leftCornerX + 25);
    moveTowardsY(17, leftCornerY + 70);

    moveTowardsX(18, leftCornerX + 25);
    moveTowardsY(18, leftCornerY + 70);

    moveTowardsX(19, leftCornerX + 25);
    moveTowardsY(19, leftCornerY + 70);
  }

  private void morph1(Graphics g) {
    moveTowardsX(0, leftCornerX + 20);
    moveTowardsY(0, leftCornerY);

    moveTowardsX(1, leftCornerX + 20);
    moveTowardsY(1, leftCornerY + 10);

    moveTowardsX(2, leftCornerX + 20);
    moveTowardsY(2, leftCornerY + 20);

    moveTowardsX(3, leftCornerX + 20);
    moveTowardsY(3, leftCornerY + 30);

    moveTowardsX(4, leftCornerX + 20);
    moveTowardsY(4, leftCornerY + 40);

    moveTowardsX(5, leftCornerX + 20);
    moveTowardsY(5, leftCornerY + 50);

    moveTowardsX(6, leftCornerX + 20);
    moveTowardsY(6, leftCornerY + 60);

    moveTowardsX(7, leftCornerX + 20);
    moveTowardsY(7, leftCornerY + 70);

    moveTowardsX(8, leftCornerX + 20);
    moveTowardsY(8, leftCornerY);

    moveTowardsX(9, leftCornerX + 20);
    moveTowardsY(9, leftCornerY + 10);

    moveTowardsX(10, leftCornerX + 20);
    moveTowardsY(10, leftCornerY + 20);

    moveTowardsX(11, leftCornerX + 20);
    moveTowardsY(11, leftCornerY + 30);

    moveTowardsX(12, leftCornerX + 20);
    moveTowardsY(12, leftCornerY + 40);

    moveTowardsX(13, leftCornerX + 20);
    moveTowardsY(13, leftCornerY + 50);

    moveTowardsX(14, leftCornerX + 20);
    moveTowardsY(14, leftCornerY + 60);

    moveTowardsX(15, leftCornerX + 20);
    moveTowardsY(15, leftCornerY + 70);

    // Left Overs
    moveTowardsX(16, leftCornerX + 20);
    moveTowardsY(16, leftCornerY + 70);

    moveTowardsX(17, leftCornerX + 20);
    moveTowardsY(17, leftCornerY + 70);

    moveTowardsX(18, leftCornerX + 20);
    moveTowardsY(18, leftCornerY + 70);

    moveTowardsX(19, leftCornerX + 20);
    moveTowardsY(19, leftCornerY + 70);
  }

  private void morph2(Graphics g) {
    moveTowardsX(0, leftCornerX);
    moveTowardsY(0, leftCornerY + 12);

    moveTowardsX(1, leftCornerX + 10);
    moveTowardsY(1, leftCornerY + 5);

    moveTowardsX(2, leftCornerX + 20);
    moveTowardsY(2, leftCornerY);

    moveTowardsX(3, leftCornerX + 30);
    moveTowardsY(3, leftCornerY);

    moveTowardsX(4, leftCornerX + 40);
    moveTowardsY(4, leftCornerY + 5);

    moveTowardsX(5, leftCornerX + 43);
    moveTowardsY(5, leftCornerY + 15);

    moveTowardsX(6, leftCornerX + 37);
    moveTowardsY(6, leftCornerY + 26);

    moveTowardsX(7, leftCornerX + 30);
    moveTowardsY(7, leftCornerY + 37);

    moveTowardsX(8, leftCornerX + 21);
    moveTowardsY(8, leftCornerY + 47);

    moveTowardsX(9, leftCornerX + 10);
    moveTowardsY(9, leftCornerY + 58);

    moveTowardsX(10, leftCornerX);
    moveTowardsY(10, leftCornerY + 70);

    moveTowardsX(11, leftCornerX + 10);
    moveTowardsY(11, leftCornerY + 70);

    moveTowardsX(12, leftCornerX + 20);
    moveTowardsY(12, leftCornerY + 70);

    moveTowardsX(13, leftCornerX + 30);
    moveTowardsY(13, leftCornerY + 70);

    moveTowardsX(14, leftCornerX + 40);
    moveTowardsY(14, leftCornerY + 70);

    // Left Overs
    moveTowardsX(15, leftCornerX + 40);
    moveTowardsY(15, leftCornerY + 70);

    moveTowardsX(16, leftCornerX + 40);
    moveTowardsY(16, leftCornerY + 70);

    moveTowardsX(17, leftCornerX + 40);
    moveTowardsY(17, leftCornerY + 70);

    moveTowardsX(18, leftCornerX + 40);
    moveTowardsY(18, leftCornerY + 70);

    moveTowardsX(19, leftCornerX + 40);
    moveTowardsY(19, leftCornerY + 70);
  }

  private void morph3(Graphics g) {
    moveTowardsX(0, leftCornerX);
    moveTowardsY(0, leftCornerY + 12);

    moveTowardsX(1, leftCornerX + 10);
    moveTowardsY(1, leftCornerY + 5);

    moveTowardsX(2, leftCornerX + 20);
    moveTowardsY(2, leftCornerY);

    moveTowardsX(3, leftCornerX + 30);
    moveTowardsY(3, leftCornerY);

    moveTowardsX(4, leftCornerX + 40);
    moveTowardsY(4, leftCornerY + 5);

    moveTowardsX(5, leftCornerX + 43);
    moveTowardsY(5, leftCornerY + 15);

    moveTowardsX(6, leftCornerX + 37);
    moveTowardsY(6, leftCornerY + 26);

    moveTowardsX(7, leftCornerX + 30);
    moveTowardsY(7, leftCornerY + 37);

    moveTowardsX(8, leftCornerX + 20);
    moveTowardsY(8, leftCornerY + 37);

    moveTowardsX(9, leftCornerX + 37);
    moveTowardsY(9, leftCornerY + 47);

    moveTowardsX(10, leftCornerX + 43);
    moveTowardsY(10, leftCornerY + 58);

    moveTowardsX(11, leftCornerX + 40);
    moveTowardsY(11, leftCornerY + 68);

    moveTowardsX(12, leftCornerX + 30);
    moveTowardsY(12, leftCornerY + 70);

    moveTowardsX(13, leftCornerX + 20);
    moveTowardsY(13, leftCornerY + 70);

    moveTowardsX(14, leftCornerX + 10);
    moveTowardsY(14, leftCornerY + 65);

    moveTowardsX(15, leftCornerX);
    moveTowardsY(15, leftCornerY + 58);

    // Left Overs
    moveTowardsX(16, leftCornerX);
    moveTowardsY(16, leftCornerY + 58);

    moveTowardsX(17, leftCornerX);
    moveTowardsY(17, leftCornerY + 58);

    moveTowardsX(18, leftCornerX);
    moveTowardsY(18, leftCornerY + 58);

    moveTowardsX(19, leftCornerX);
    moveTowardsY(19, leftCornerY + 58);
  }

  private void morph4(Graphics g) {
    moveTowardsX(0, leftCornerX);
    moveTowardsY(0, leftCornerY);

    moveTowardsX(1, leftCornerX);
    moveTowardsY(1, leftCornerY + 10);

    moveTowardsX(2, leftCornerX);
    moveTowardsY(2, leftCornerY + 20);

    moveTowardsX(3, leftCornerX);
    moveTowardsY(3, leftCornerY + 30);

    moveTowardsX(4, leftCornerX);
    moveTowardsY(4, leftCornerY + 40);

    moveTowardsX(5, leftCornerX + 10);
    moveTowardsY(5, leftCornerY + 40);

    moveTowardsX(6, leftCornerX + 20);
    moveTowardsY(6, leftCornerY + 40);

    moveTowardsX(7, leftCornerX + 30);
    moveTowardsY(7, leftCornerY + 40);

    moveTowardsX(8, leftCornerX + 40);
    moveTowardsY(8, leftCornerY + 40);

    moveTowardsX(9, leftCornerX + 40);
    moveTowardsY(9, leftCornerY + 30);

    moveTowardsX(10, leftCornerX + 40);
    moveTowardsY(10, leftCornerY + 20);

    moveTowardsX(11, leftCornerX + 40);
    moveTowardsY(11, leftCornerY + 10);

    moveTowardsX(12, leftCornerX + 40);
    moveTowardsY(12, leftCornerY);

    moveTowardsX(13, leftCornerX + 40);
    moveTowardsY(13, leftCornerY + 50);

    moveTowardsX(14, leftCornerX + 40);
    moveTowardsY(14, leftCornerY + 60);

    moveTowardsX(15, leftCornerX + 40);
    moveTowardsY(15, leftCornerY + 70);

    // Left Overs
    moveTowardsX(16, leftCornerX + 40);
    moveTowardsY(16, leftCornerY + 70);

    moveTowardsX(17, leftCornerX + 40);
    moveTowardsY(17, leftCornerY + 70);

    moveTowardsX(18, leftCornerX + 40);
    moveTowardsY(18, leftCornerY + 70);

    moveTowardsX(19, leftCornerX + 40);
    moveTowardsY(19, leftCornerY + 70);
  }

  private void morph5(Graphics g) {
    moveTowardsX(0, leftCornerX);
    moveTowardsY(0, leftCornerY);

    moveTowardsX(1, leftCornerX);
    moveTowardsY(1, leftCornerY + 10);

    moveTowardsX(2, leftCornerX);
    moveTowardsY(2, leftCornerY + 20);

    moveTowardsX(3, leftCornerX);
    moveTowardsY(3, leftCornerY + 30);

    moveTowardsX(4, leftCornerX);
    moveTowardsY(4, leftCornerY + 40);

    moveTowardsX(5, leftCornerX + 10);
    moveTowardsY(5, leftCornerY + 40);

    moveTowardsX(6, leftCornerX + 20);
    moveTowardsY(6, leftCornerY + 40);

    moveTowardsX(7, leftCornerX + 30);
    moveTowardsY(7, leftCornerY + 40);

    moveTowardsX(8, leftCornerX + 40);
    moveTowardsY(8, leftCornerY + 40);

    moveTowardsX(9, leftCornerX + 10);
    moveTowardsY(9, leftCornerY);

    moveTowardsX(10, leftCornerX + 20);
    moveTowardsY(10, leftCornerY);

    moveTowardsX(11, leftCornerX + 30);
    moveTowardsY(11, leftCornerY);

    moveTowardsX(12, leftCornerX + 40);
    moveTowardsY(12, leftCornerY);

    moveTowardsX(13, leftCornerX + 43);
    moveTowardsY(13, leftCornerY + 50);

    moveTowardsX(14, leftCornerX + 43);
    moveTowardsY(14, leftCornerY + 60);

    moveTowardsX(15, leftCornerX + 40);
    moveTowardsY(15, leftCornerY + 70);

    moveTowardsX(16, leftCornerX + 30);
    moveTowardsY(16, leftCornerY + 70);

    moveTowardsX(17, leftCornerX + 20);
    moveTowardsY(17, leftCornerY + 70);

    moveTowardsX(18, leftCornerX + 10);
    moveTowardsY(18, leftCornerY + 65);

    moveTowardsX(19, leftCornerX);
    moveTowardsY(19, leftCornerY + 58);
  }

  private void morph6(Graphics g) {
    moveTowardsX(0, leftCornerX + 25);
    moveTowardsY(0, leftCornerY);

    moveTowardsX(1, leftCornerX + 15);
    moveTowardsY(1, leftCornerY);

    moveTowardsX(2, leftCornerX + 5);
    moveTowardsY(2, leftCornerY + 10);

    moveTowardsX(3, leftCornerX);
    moveTowardsY(3, leftCornerY + 20);

    moveTowardsX(4, leftCornerX);
    moveTowardsY(4, leftCornerY + 30);

    moveTowardsX(5, leftCornerX);
    moveTowardsY(5, leftCornerY + 40);

    moveTowardsX(6, leftCornerX);
    moveTowardsY(6, leftCornerY + 50);

    moveTowardsX(7, leftCornerX + 5);
    moveTowardsY(7, leftCornerY + 60);

    moveTowardsX(8, leftCornerX + 15);
    moveTowardsY(8, leftCornerY + 70);

    moveTowardsX(9, leftCornerX + 25);
    moveTowardsY(9, leftCornerY + 70);

    moveTowardsX(10, leftCornerX + 35);
    moveTowardsY(10, leftCornerY + 60);

    moveTowardsX(11, leftCornerX + 40);
    moveTowardsY(11, leftCornerY + 50);

    moveTowardsX(12, leftCornerX + 35);
    moveTowardsY(12, leftCornerY + 40);

    moveTowardsX(13, leftCornerX + 28);
    moveTowardsY(13, leftCornerY + 30);

    moveTowardsX(14, leftCornerX + 18);
    moveTowardsY(14, leftCornerY + 30);

    moveTowardsX(15, leftCornerX + 8);
    moveTowardsY(15, leftCornerY + 40);

    // Left Overs
    moveTowardsX(16, leftCornerX + 8);
    moveTowardsY(16, leftCornerY + 40);

    moveTowardsX(17, leftCornerX + 8);
    moveTowardsY(17, leftCornerY + 40);

    moveTowardsX(18, leftCornerX + 8);
    moveTowardsY(18, leftCornerY + 40);

    moveTowardsX(19, leftCornerX + 8);
    moveTowardsY(19, leftCornerY + 40);
  }

  private void morph7(Graphics g) {
    moveTowardsX(0, leftCornerX);
    moveTowardsY(0, leftCornerY);

    moveTowardsX(1, leftCornerX + 10);
    moveTowardsY(1, leftCornerY);

    moveTowardsX(2, leftCornerX + 20);
    moveTowardsY(2, leftCornerY);

    moveTowardsX(3, leftCornerX + 30);
    moveTowardsY(3, leftCornerY);

    moveTowardsX(4, leftCornerX + 40);
    moveTowardsY(4, leftCornerY);

    moveTowardsX(5, leftCornerX + 35);
    moveTowardsY(5, leftCornerY + 10);

    moveTowardsX(6, leftCornerX + 30);
    moveTowardsY(6, leftCornerY + 20);

    moveTowardsX(7, leftCornerX + 25);
    moveTowardsY(7, leftCornerY + 30);

    moveTowardsX(8, leftCornerX + 20);
    moveTowardsY(8, leftCornerY + 40);

    moveTowardsX(9, leftCornerX + 15);
    moveTowardsY(9, leftCornerY + 50);

    moveTowardsX(10, leftCornerX + 10);
    moveTowardsY(10, leftCornerY + 60);

    moveTowardsX(11, leftCornerX + 5);
    moveTowardsY(11, leftCornerY + 70);

    // Left Overs
    moveTowardsX(12, leftCornerX + 5);
    moveTowardsY(12, leftCornerY + 70);

    moveTowardsX(13, leftCornerX + 5);
    moveTowardsY(13, leftCornerY + 70);

    moveTowardsX(14, leftCornerX + 5);
    moveTowardsY(14, leftCornerY + 70);

    moveTowardsX(15, leftCornerX + 5);
    moveTowardsY(15, leftCornerY + 70);

    moveTowardsX(16, leftCornerX + 5);
    moveTowardsY(16, leftCornerY + 70);

    moveTowardsX(17, leftCornerX + 5);
    moveTowardsY(17, leftCornerY + 70);

    moveTowardsX(18, leftCornerX + 5);
    moveTowardsY(18, leftCornerY + 70);

    moveTowardsX(19, leftCornerX + 5);
    moveTowardsY(19, leftCornerY + 70);
  }

  private void morph8(Graphics g) {
    moveTowardsX(0, leftCornerX + 15);
    moveTowardsY(0, leftCornerY);

    moveTowardsX(1, leftCornerX + 5);
    moveTowardsY(1, leftCornerY + 10);

    moveTowardsX(2, leftCornerX);
    moveTowardsY(2, leftCornerY + 20);

    moveTowardsX(3, leftCornerX + 7);
    moveTowardsY(3, leftCornerY + 30);

    moveTowardsX(4, leftCornerX + 7);
    moveTowardsY(4, leftCornerY + 40);

    moveTowardsX(5, leftCornerX);
    moveTowardsY(5, leftCornerY + 50);

    moveTowardsX(6, leftCornerX + 5);
    moveTowardsY(6, leftCornerY + 60);

    moveTowardsX(7, leftCornerX + 15);
    moveTowardsY(7, leftCornerY + 70);

    moveTowardsX(8, leftCornerX + 25);
    moveTowardsY(8, leftCornerY);

    moveTowardsX(9, leftCornerX + 35);
    moveTowardsY(9, leftCornerY + 10);

    moveTowardsX(10, leftCornerX + 40);
    moveTowardsY(10, leftCornerY + 20);

    moveTowardsX(11, leftCornerX + 33);
    moveTowardsY(11, leftCornerY + 30);

    moveTowardsX(12, leftCornerX + 33);
    moveTowardsY(12, leftCornerY + 40);

    moveTowardsX(13, leftCornerX + 40);
    moveTowardsY(13, leftCornerY + 50);

    moveTowardsX(14, leftCornerX + 35);
    moveTowardsY(14, leftCornerY + 60);

    moveTowardsX(15, leftCornerX + 25);
    moveTowardsY(15, leftCornerY + 70);

    moveTowardsX(16, leftCornerX + 20);
    moveTowardsY(16, leftCornerY + 35);

    // Left Overs
    moveTowardsX(17, leftCornerX + 20);
    moveTowardsY(17, leftCornerY + 35);

    moveTowardsX(18, leftCornerX + 20);
    moveTowardsY(18, leftCornerY + 35);

    moveTowardsX(19, leftCornerX + 20);
    moveTowardsY(19, leftCornerY + 35);
  }

  private void morph9(Graphics g) {
    moveTowardsX(0, leftCornerX + 15);
    moveTowardsY(0, leftCornerY);

    moveTowardsX(1, leftCornerX + 5);
    moveTowardsY(1, leftCornerY + 10);

    moveTowardsX(2, leftCornerX);
    moveTowardsY(2, leftCornerY + 20);

    moveTowardsX(3, leftCornerX + 7);
    moveTowardsY(3, leftCornerY + 30);

    moveTowardsX(4, leftCornerX + 7);
    moveTowardsY(4, leftCornerY + 30);

    moveTowardsX(5, leftCornerX + 7);
    moveTowardsY(5, leftCornerY + 30);

    moveTowardsX(6, leftCornerX + 7);
    moveTowardsY(6, leftCornerY + 30);

    moveTowardsX(7, leftCornerX + 15);
    moveTowardsY(7, leftCornerY + 70);

    moveTowardsX(8, leftCornerX + 25);
    moveTowardsY(8, leftCornerY);

    moveTowardsX(9, leftCornerX + 35);
    moveTowardsY(9, leftCornerY + 10);

    moveTowardsX(10, leftCornerX + 40);
    moveTowardsY(10, leftCornerY + 20);

    moveTowardsX(11, leftCornerX + 33);
    moveTowardsY(11, leftCornerY + 30);

    moveTowardsX(12, leftCornerX + 40);
    moveTowardsY(12, leftCornerY + 30);

    moveTowardsX(13, leftCornerX + 40);
    moveTowardsY(13, leftCornerY + 40);

    moveTowardsX(14, leftCornerX + 40);
    moveTowardsY(14, leftCornerY + 50);

    moveTowardsX(15, leftCornerX + 35);
    moveTowardsY(15, leftCornerY + 60);

    moveTowardsX(16, leftCornerX + 25);
    moveTowardsY(16, leftCornerY + 70);

    moveTowardsX(17, leftCornerX + 20);
    moveTowardsY(17, leftCornerY + 35);

    // Left Overs
    moveTowardsX(18, leftCornerX + 20);
    moveTowardsY(18, leftCornerY + 35);

    moveTowardsX(19, leftCornerX + 20);
    moveTowardsY(19, leftCornerY + 35);
  }

  private void moveTowardsX(int pntIndx, int x) {
    if (points[pntIndx].x == -1) {
      points[pntIndx].x = leftCornerX;
    }

    if (points[pntIndx].x >= x - MOVE_X_INCREMENT && points[pntIndx].x <= x + MOVE_X_INCREMENT) {
      points[pntIndx].x = x;
    }
    else if (points[pntIndx].x > x) {
      points[pntIndx].x -= MOVE_X_INCREMENT;
    }
    else if (points[pntIndx].x < x) {
      points[pntIndx].x += MOVE_X_INCREMENT;
    }
  }

  private void moveTowardsY(int pntIndx, int y) {
    if (points[pntIndx].y == -1) {
      points[pntIndx].y = leftCornerY;
    }

    if (points[pntIndx].y >= y - MOVE_Y_INCREMENT && points[pntIndx].y <= y + MOVE_Y_INCREMENT) {
      points[pntIndx].y = y;
    }
    else if (points[pntIndx].y > y) {
      points[pntIndx].y -= MOVE_Y_INCREMENT;
    }
    else if (points[pntIndx].y < y) {
      points[pntIndx].y += MOVE_Y_INCREMENT;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.jdaliclock.Digit#draw(java.awt.Graphics)
   */
  @Override
  public void draw(Graphics g) {
    switch (curDigit) {
      case 0:
        morph0(g);
        break;
      case 1:
        morph1(g);
        break;
      case 2:
        morph2(g);
        break;
      case 3:
        morph3(g);
        break;
      case 4:
        morph4(g);
        break;
      case 5:
        morph5(g);
        break;
      case 6:
        morph6(g);
        break;
      case 7:
        morph7(g);
        break;
      case 8:
        morph8(g);
        break;
      case 9:
        morph9(g);
        break;
    }

    for (int i = 0; i < PNTS_ARR_SIZE; i++) {
      if (points[i].x != -1 && points[i].y != -1) {
        g.fillOval(points[i].x, points[i].y, 10, 10);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.jdaliclock.Digit#drawColon(java.awt.Graphics)
   */
  @Override
  public void drawColon(Graphics g) {
    g.fillOval(leftCornerX + 15, leftCornerY + 20, 10, 10);
    g.fillOval(leftCornerX + 15, leftCornerY + 50, 10, 10);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.jdaliclock.Digit#getCurrentDigit()
   */
  @Override
  public int getCurrentDigit() {
    return curDigit;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.jdaliclock.Digit#setCurrentDigit(int)
   */
  @Override
  public void setCurrentDigit(int curDigit) {
    this.curDigit = curDigit;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.jdaliclock.Digit#setLeftCornerX(int)
   */
  @Override
  public void setLeftCornerX(int x) {
    this.leftCornerX = x;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.jdaliclock.Digit#setLeftCornerY(int)
   */
  @Override
  public void setLeftCornerY(int y) {
    this.leftCornerY = y;
  }

}
