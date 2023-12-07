/**
 * Created Oct 22, 2007
 */
package com.roguelogic.games.icbm;

/**
 * @author Robert C. Ilardi
 *
 */

public class Icbm {

  private long id;
  private int initX;
  private int curX;
  private int curY;
  private int cityCenterX;
  private int targetCityIndex = -1;

  public Icbm() {}

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

  public int getInitX() {
    return initX;
  }

  public void setInitX(int initX) {
    this.initX = initX;
  }

  public int getCityCenterX() {
    return cityCenterX;
  }

  public void setCityCenterX(int cityCenterX) {
    this.cityCenterX = cityCenterX;
  }

  public int getTargetCityIndex() {
    return targetCityIndex;
  }

  public void setTargetCityIndex(int targetCityIndex) {
    this.targetCityIndex = targetCityIndex;
  }

  /**
   * @return Returns the id.
   */
  public long getId() {
    return id;
  }

  /**
   * @param id The id to set.
   */
  public void setId(long id) {
    this.id = id;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[ICMB - ID: ");
    sb.append(id);

    sb.append(", TargetCityIndex: ");
    sb.append(targetCityIndex);

    sb.append(", CityCenterX: ");
    sb.append(cityCenterX);

    sb.append(", InitX: ");
    sb.append(initX);

    sb.append(", CurX: ");
    sb.append(curX);

    sb.append(", CurY: ");
    sb.append(curY);

    sb.append("]");

    return sb.toString();
  }

}
