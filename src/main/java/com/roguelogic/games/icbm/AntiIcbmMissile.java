/**
 * Created Oct 23, 2007
 */
package com.roguelogic.games.icbm;

/**
 * @author Robert C. Ilardi
 *
 */

public class AntiIcbmMissile {

  private long id;

  private int targetX;
  private int targetY;

  private int curX;
  private int curY;

  public AntiIcbmMissile() {}

  public int getCurX() {
    return curX;
  }

  public void setCurX(int curX) {
    this.curX = curX;
  }

  public int getCurY() {
    return curY;
  }

  public void setCurY(int curY) {
    this.curY = curY;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getTargetX() {
    return targetX;
  }

  public void setTargetX(int targetX) {
    this.targetX = targetX;
  }

  public int getTargetY() {
    return targetY;
  }

  public void setTargetY(int targetY) {
    this.targetY = targetY;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[AntiIcbmMissile - ID: ");
    sb.append(id);

    sb.append(", TargetX: ");
    sb.append(targetX);

    sb.append(", TargetY: ");
    sb.append(targetY);

    sb.append(", CurX: ");
    sb.append(curX);

    sb.append(", CurY: ");
    sb.append(curY);

    sb.append("]");

    return sb.toString();
  }

}
