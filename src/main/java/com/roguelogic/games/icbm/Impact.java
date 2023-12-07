/*
 * Created on Oct 23, 2007
 */
package com.roguelogic.games.icbm;

/**
 * @author rilardi
 */

public class Impact {

  public static final int IMPACT_TYPE_ICBM_CITY = 0;
  public static final int IMPACT_TYPE_ANTI_MISSILE = 1;

  private long missileId;
  private int cityIndex;

  private int x;
  private int y;

  private int type;

  private int cycleCnt = 0;

  public Impact() {}

  /**
   * @return Returns the type.
   */
  public int getType() {
    return type;
  }

  /**
   * @param type The type to set.
   */
  public void setType(int type) {
    this.type = type;
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
   * @return Returns the icbmId.
   */
  public long getMissileId() {
    return missileId;
  }

  /**
   * @param icbmId The icbmId to set.
   */
  public void setMissileId(long icbmId) {
    this.missileId = icbmId;
  }

  /**
   * @return Returns the cityIndex.
   */
  public int getCityIndex() {
    return cityIndex;
  }

  /**
   * @param cityIndex The cityIndex to set.
   */
  public void setCityIndex(int cityIndex) {
    this.cityIndex = cityIndex;
  }

  public void incrementCycleCnt() {
    cycleCnt++;
  }

  public int getCycleCnt() {
    return cycleCnt;
  }

}
