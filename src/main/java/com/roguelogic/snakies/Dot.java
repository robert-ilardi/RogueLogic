/**
 * Created Apr 18, 2009
 */
package com.roguelogic.snakies;

import java.awt.Color;
import java.awt.Point;

/**
 * @author Robert C. Ilardi
 *
 */
public class Dot extends Point {

  public Color color;

  public Dot() {}

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

}
