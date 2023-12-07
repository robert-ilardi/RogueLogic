/*
 Copyright 2008 Robert C. Ilardi

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
 * Created Aug 29, 2008
 */

package com.roguelogic.jdaliclock;

import java.awt.Graphics;

/**
 * @author rilardi
 * 
 */

public class LedDigit implements Digit {

	public static final int GV_INDX_LEFT_X = 0;
	public static final int GV_INDX_LEFT_Y = 1;
	public static final int GV_INDX_RIGHT_X = 2;
	public static final int GV_INDX_RIGHT_Y = 3;
	public static final int GV_INDX_TOP_X = 4;
	public static final int GV_INDX_TOP_Y = 5;
	public static final int GV_INDX_MIDDLE_X = 6;
	public static final int GV_INDX_MIDDLE_Y = 7;
	public static final int GV_INDX_BOTTOM_X = 8;
	public static final int GV_INDX_BOTTOM_Y = 9;
	public static final int GV_INDX_LEFT_WIDTH = 10;
	public static final int GV_INDX_LEFT_HEIGHT = 11;
	public static final int GV_INDX_RIGHT_WIDTH = 12;
	public static final int GV_INDX_RIGHT_HEIGHT = 13;
	public static final int GV_INDX_TOP_WIDTH = 14;
	public static final int GV_INDX_TOP_HEIGHT = 15;
	public static final int GV_INDX_MIDDLE_WIDTH = 16;
	public static final int GV_INDX_MIDDLE_HEIGHT = 17;
	public static final int GV_INDX_BOTTOM_WIDTH = 18;
	public static final int GV_INDX_BOTTOM_HEIGHT = 19;

	public static final int GV_ARR_SIZE = 20;

	public static final int MOVE_X_INCREMENT = 2;
	public static final int MOVE_Y_INCREMENT = 4;
	public static final int GROW_WIDTH_INCREMENT = 2;
	public static final int GROW_HEIGHT_INCREMENT = 3;

	private int curDigit = 0;

	private int leftCornerX;
	private int leftCornerY;

	private int[] glyphVals;

	public LedDigit() {
		glyphVals = new int[GV_ARR_SIZE];

		for (int i = 0; i < GV_ARR_SIZE; i++) {
			glyphVals[i] = -1;
		}
	}

	public void setCurrentDigit(int curDigit) {
		this.curDigit = curDigit;
	}

	public int getCurrentDigit() {
		return curDigit;
	}

	public int getLeftCornerX() {
		return leftCornerX;
	}

	public void setLeftCornerX(int leftCornerX) {
		this.leftCornerX = leftCornerX;
	}

	public int getLeftCornerY() {
		return leftCornerY;
	}

	public void setLeftCornerY(int leftCornerY) {
		this.leftCornerY = leftCornerY;
	}

	public void draw(Graphics g) {
		switch (curDigit) {
			case 0:
				// draw0(g);
				morph0(g);
				break;
			case 1:
				// draw1(g);
				morph1(g);
				break;
			case 2:
				// draw2(g);
				morph2(g);
				break;
			case 3:
				// draw3(g);
				morph3(g);
				break;
			case 4:
				// draw4(g);
				morph4(g);
				break;
			case 5:
				// draw5(g);
				morph5(g);
				break;
			case 6:
				// draw6(g);
				morph6(g);
				break;
			case 7:
				// draw7(g);
				morph7(g);
				break;
			case 8:
				// draw8(g);
				morph8(g);
				break;
			case 9:
				// draw9(g);
				morph9(g);
				break;
		}
	}

	private void moveTowardsX(int gvIndx, int x) {
		if (glyphVals[gvIndx] == -1) {
			glyphVals[gvIndx] = leftCornerX;
		}

		if (glyphVals[gvIndx] >= x - MOVE_X_INCREMENT && glyphVals[gvIndx] <= x + MOVE_X_INCREMENT) {
			glyphVals[gvIndx] = x;
		}
		else if (glyphVals[gvIndx] > x) {
			glyphVals[gvIndx] -= MOVE_X_INCREMENT;
		}
		else if (glyphVals[gvIndx] < x) {
			glyphVals[gvIndx] += MOVE_X_INCREMENT;
		}
	}

	private void moveTowardsY(int gvIndx, int y) {
		if (glyphVals[gvIndx] == -1) {
			glyphVals[gvIndx] = leftCornerY;
		}

		if (glyphVals[gvIndx] >= y - MOVE_Y_INCREMENT && glyphVals[gvIndx] <= y + MOVE_Y_INCREMENT) {
			glyphVals[gvIndx] = y;
		}
		else if (glyphVals[gvIndx] > y) {
			glyphVals[gvIndx] -= MOVE_Y_INCREMENT;
		}
		else if (glyphVals[gvIndx] < y) {
			glyphVals[gvIndx] += MOVE_Y_INCREMENT;
		}
	}

	private void growTowardsWidth(int gvIndx, int width) {
		if (glyphVals[gvIndx] == -1) {
			glyphVals[gvIndx] = 0;
		}

		if (glyphVals[gvIndx] >= width - GROW_WIDTH_INCREMENT && glyphVals[gvIndx] <= width + GROW_WIDTH_INCREMENT) {
			glyphVals[gvIndx] = width;
		}
		if (glyphVals[gvIndx] > width) {
			glyphVals[gvIndx] -= GROW_WIDTH_INCREMENT;
		}
		else if (glyphVals[gvIndx] < width) {
			glyphVals[gvIndx] += GROW_WIDTH_INCREMENT;
		}
	}

	private void growTowardsHeight(int gvIndx, int height) {
		if (glyphVals[gvIndx] == -1) {
			glyphVals[gvIndx] = 0;
		}

		if (glyphVals[gvIndx] >= height - GROW_HEIGHT_INCREMENT && glyphVals[gvIndx] <= height + GROW_HEIGHT_INCREMENT) {
			glyphVals[gvIndx] = height;
		}
		if (glyphVals[gvIndx] > height) {
			glyphVals[gvIndx] -= GROW_HEIGHT_INCREMENT;
		}
		else if (glyphVals[gvIndx] < height) {
			glyphVals[gvIndx] += GROW_HEIGHT_INCREMENT;
		}
	}

	private void draw0(Graphics g) {
		g.fill3DRect(leftCornerX, leftCornerY + 5, 10, 70, true); // Left Vertical
		g.fill3DRect(leftCornerX + 5, leftCornerY, 35, 10, true); // Top
		g.fill3DRect(leftCornerX + 5, leftCornerY + 70, 35, 10, true); // Bottom
		g.fill3DRect(leftCornerX + 35, leftCornerY + 5, 10, 70, true); // Right Vertical
	}

	private void morph0(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY + 5);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 35);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY + 5);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX + 5);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX + 5);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX + 5);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 70);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 10);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 70);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 70);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 35);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 0);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 0);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 35);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle (Disappears to top)
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom
		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
	}

	private void draw1(Graphics g) {
		g.fill3DRect(leftCornerX, leftCornerY, 20, 10, true); // Top
		g.fill3DRect(leftCornerX + 15, leftCornerY, 10, 70, true); // Middle Vertical (Assume Left)
		g.fill3DRect(leftCornerX, leftCornerY + 70, 40, 10, true); // Bottom
	}

	private void morph1(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX + 15);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 20);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY + 70);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 70);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 6);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 70);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 5);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 70);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 20);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 0);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 0);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 40);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical (Half Middle)
		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical (Half Middle)
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Midde (Disappears to bottom)
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom
	}

	private void draw2(Graphics g) {
		g.fill3DRect(leftCornerX + 30, leftCornerY, 10, 35, true); // Right Vertical
		g.fill3DRect(leftCornerX, leftCornerY, 40, 10, true); // Top
		g.fill3DRect(leftCornerX, leftCornerY + 35, 40, 10, true); // Middle
		g.fill3DRect(leftCornerX, leftCornerY + 70, 40, 10, true); // Bottom
		g.fill3DRect(leftCornerX, leftCornerY + 35, 10, 35, true); // Left Vertical
	}

	private void morph2(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 70);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 10);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 35);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 35);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 40);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 40);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 10);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 40);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom
		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical
	}

	private void draw3(Graphics g) {
		g.fill3DRect(leftCornerX + 30, leftCornerY, 10, 70, true); // Right Vertical
		g.fill3DRect(leftCornerX, leftCornerY, 40, 10, true); // Top
		g.fill3DRect(leftCornerX, leftCornerY + 35, 40, 10, true); // Middle
		g.fill3DRect(leftCornerX, leftCornerY + 70, 40, 10, true); // Bottom
	}

	private void morph3(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 70);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 0);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 0);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 70);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 40);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 40);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 10);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 40);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical (Disappears to right)
		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom
	}

	private void draw4(Graphics g) {
		g.fill3DRect(leftCornerX + 30, leftCornerY, 10, 80, true); // Right Vertical
		g.fill3DRect(leftCornerX, leftCornerY + 35, 40, 10, true); // Middle
		g.fill3DRect(leftCornerX, leftCornerY, 10, 35, true); // Left Vertical
	}

	private void morph4(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 35);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 10);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 35);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 80);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 0);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 0);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 40);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 10);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 0);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical
		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top (Disappears to middle)
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom (Disappears to middle)
	}

	private void draw5(Graphics g) {
		g.fill3DRect(leftCornerX, leftCornerY, 10, 35, true); // Left Vertical
		g.fill3DRect(leftCornerX + 30, leftCornerY + 35, 10, 35, true); // Right Vertical
		g.fill3DRect(leftCornerX, leftCornerY, 35, 10, true); // Top
		g.fill3DRect(leftCornerX, leftCornerY + 35, 40, 10, true); // Middle
		g.fill3DRect(leftCornerX, leftCornerY + 70, 40, 10, true); // Bottom
	}

	private void morph5(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 70);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 10);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 35);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 35);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 35);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 40);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 10);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 40);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical
		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom
	}

	private void draw6(Graphics g) {
		g.fill3DRect(leftCornerX, leftCornerY, 10, 80, true); // Left Vertical
		g.fill3DRect(leftCornerX + 30, leftCornerY + 35, 10, 35, true); // Right Vertical
		g.fill3DRect(leftCornerX, leftCornerY, 35, 10, true); // Top
		g.fill3DRect(leftCornerX, leftCornerY + 35, 30, 10, true); // Middle
		g.fill3DRect(leftCornerX, leftCornerY + 70, 40, 10, true); // Bottom
	}

	private void morph6(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 70);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 10);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 80);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 35);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 35);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 30);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 10);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 40);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical
		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom
	}

	private void draw7(Graphics g) {
		g.fill3DRect(leftCornerX + 30, leftCornerY, 10, 80, true); // Right Vertical
		g.fill3DRect(leftCornerX, leftCornerY, 30, 10, true); // Top
	}

	private void morph7(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 0);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 0);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 80);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 30);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 0);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 0);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 0);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 0);

		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical (Disappears to right)
		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle (Disappears to Top)
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom (Disappears to Top)
	}

	private void draw8(Graphics g) {
		g.fill3DRect(leftCornerX, leftCornerY, 10, 80, true); // Left Vertical
		g.fill3DRect(leftCornerX + 30, leftCornerY, 10, 80, true); // Right Vertical
		g.fill3DRect(leftCornerX, leftCornerY, 30, 10, true); // Top
		g.fill3DRect(leftCornerX, leftCornerY + 35, 40, 10, true); // Middle
		g.fill3DRect(leftCornerX, leftCornerY + 70, 30, 10, true); // Bottom
	}

	private void morph8(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 70);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 10);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 80);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 80);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 30);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 40);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 10);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 30);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical
		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom
	}

	private void draw9(Graphics g) {
		g.fill3DRect(leftCornerX + 30, leftCornerY, 10, 80, true); // Right Vertical
		g.fill3DRect(leftCornerX, leftCornerY, 10, 35, true); // Left Vertical
		g.fill3DRect(leftCornerX, leftCornerY, 30, 10, true); // Top
		g.fill3DRect(leftCornerX, leftCornerY + 35, 40, 10, true); // Middle
	}

	private void morph9(Graphics g) {
		moveTowardsX(GV_INDX_LEFT_X, leftCornerX);
		moveTowardsY(GV_INDX_LEFT_Y, leftCornerY);

		moveTowardsX(GV_INDX_RIGHT_X, leftCornerX + 30);
		moveTowardsY(GV_INDX_RIGHT_Y, leftCornerY);

		moveTowardsX(GV_INDX_TOP_X, leftCornerX);
		moveTowardsY(GV_INDX_TOP_Y, leftCornerY);

		moveTowardsX(GV_INDX_MIDDLE_X, leftCornerX);
		moveTowardsY(GV_INDX_MIDDLE_Y, leftCornerY + 35);

		moveTowardsX(GV_INDX_BOTTOM_X, leftCornerX);
		moveTowardsY(GV_INDX_BOTTOM_Y, leftCornerY + 35);

		growTowardsWidth(GV_INDX_LEFT_WIDTH, 10);
		growTowardsHeight(GV_INDX_LEFT_HEIGHT, 35);

		growTowardsWidth(GV_INDX_RIGHT_WIDTH, 10);
		growTowardsHeight(GV_INDX_RIGHT_HEIGHT, 80);

		growTowardsWidth(GV_INDX_TOP_WIDTH, 30);
		growTowardsHeight(GV_INDX_TOP_HEIGHT, 10);

		growTowardsWidth(GV_INDX_MIDDLE_WIDTH, 30);
		growTowardsHeight(GV_INDX_MIDDLE_HEIGHT, 10);

		growTowardsWidth(GV_INDX_BOTTOM_WIDTH, 30);
		growTowardsHeight(GV_INDX_BOTTOM_HEIGHT, 10);

		g.fill3DRect(glyphVals[GV_INDX_RIGHT_X], glyphVals[GV_INDX_RIGHT_Y], glyphVals[GV_INDX_RIGHT_WIDTH], glyphVals[GV_INDX_RIGHT_HEIGHT], true); // Right Vertical
		g.fill3DRect(glyphVals[GV_INDX_LEFT_X], glyphVals[GV_INDX_LEFT_Y], glyphVals[GV_INDX_LEFT_WIDTH], glyphVals[GV_INDX_LEFT_HEIGHT], true); // Left Vertical
		g.fill3DRect(glyphVals[GV_INDX_TOP_X], glyphVals[GV_INDX_TOP_Y], glyphVals[GV_INDX_TOP_WIDTH], glyphVals[GV_INDX_TOP_HEIGHT], true); // Top
		g.fill3DRect(glyphVals[GV_INDX_MIDDLE_X], glyphVals[GV_INDX_MIDDLE_Y], glyphVals[GV_INDX_MIDDLE_WIDTH], glyphVals[GV_INDX_MIDDLE_HEIGHT], true); // Middle
		g.fill3DRect(glyphVals[GV_INDX_BOTTOM_X], glyphVals[GV_INDX_BOTTOM_Y], glyphVals[GV_INDX_BOTTOM_WIDTH], glyphVals[GV_INDX_BOTTOM_HEIGHT], true); // Bottom (Disappears to middle)
	}

	public void drawColon(Graphics g) {
		g.fillOval(leftCornerX + 15, leftCornerY + 20, 10, 10);
		g.fillOval(leftCornerX + 15, leftCornerY + 50, 10, 10);
	}

}
