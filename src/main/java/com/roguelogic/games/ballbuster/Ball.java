/*
 * Created on Oct 2, 2007
 */
package com.roguelogic.games.ballbuster;

/**
 * @author rilardi
 */

public class Ball {

  public static final int BUSTED_BALL = 0;
  public static final int RED_BALL = 1;
  public static final int BLUE_BALL = 2;
  public static final int GREEN_BALL = 3;
  public static final int ORANGE_BALL = 4;
  public static final int YELLOW_BALL = 5;

  public static final int MAX_COLOR = 5;

  private int id;
  private int row;
  private int col;

  private boolean busted;
  private int color;

  private int x;
  private int y;

  private boolean selected;

  private boolean checkedLeft;
  private boolean checkedRight;
  private boolean checkedUp;
  private boolean checkedDown;

  public Ball() {}

  /**
   * @return Returns the busted.
   */
  public boolean isBusted() {
    return busted;
  }

  /**
   * @param busted The busted to set.
   */
  public void setBusted(boolean busted) {
    this.busted = busted;
  }

  /**
   * @return Returns the color.
   */
  public int getColor() {
    return color;
  }

  /**
   * @param color The color to set.
   */
  public void setColor(int color) {
    this.color = color;
  }

  /**
   * @return Returns the x.
   */
  public int getX() {
    return x;
  }

  /**
   * @param x The x to set.
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * @return Returns the y.
   */
  public int getY() {
    return y;
  }

  /**
   * @param y The y to set.
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * @return Returns the id.
   */
  public int getId() {
    return id;
  }

  /**
   * @param id The id to set.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return Returns the selected.
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * @param selected The selected to set.
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  /**
   * @return Returns the col.
   */
  public int getCol() {
    return col;
  }

  /**
   * @param col The col to set.
   */
  public void setCol(int col) {
    this.col = col;
  }

  /**
   * @return Returns the row.
   */
  public int getRow() {
    return row;
  }

  /**
   * @param row The row to set.
   */
  public void setRow(int row) {
    this.row = row;
  }

  /**
   * @return Returns the checkedDown.
   */
  public boolean isCheckedDown() {
    return checkedDown;
  }

  /**
   * @param checkedDown The checkedDown to set.
   */
  public void setCheckedDown(boolean checkedDown) {
    this.checkedDown = checkedDown;
  }

  /**
   * @return Returns the checkedLeft.
   */
  public boolean isCheckedLeft() {
    return checkedLeft;
  }

  /**
   * @param checkedLeft The checkedLeft to set.
   */
  public void setCheckedLeft(boolean checkedLeft) {
    this.checkedLeft = checkedLeft;
  }

  /**
   * @return Returns the checkedRight.
   */
  public boolean isCheckedRight() {
    return checkedRight;
  }

  /**
   * @param checkedRight The checkedRight to set.
   */
  public void setCheckedRight(boolean checkedRight) {
    this.checkedRight = checkedRight;
  }

  /**
   * @return Returns the checkedUp.
   */
  public boolean isCheckedUp() {
    return checkedUp;
  }

  /**
   * @param checkedUp The checkedUp to set.
   */
  public void setCheckedUp(boolean checkedUp) {
    this.checkedUp = checkedUp;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[BALL Id=");
    sb.append(id);

    sb.append("; Color=");
    sb.append(color);

    sb.append("; X=");
    sb.append(x);

    sb.append("; Y=");
    sb.append(y);

    sb.append("; Selected=");
    sb.append(selected);

    sb.append("; Row=");
    sb.append(row);

    sb.append("; Col=");
    sb.append(col);

    sb.append("; Busted=");
    sb.append(busted);

    sb.append("]");

    return sb.toString();
  }

}
