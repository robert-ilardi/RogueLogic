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
 * Created Apr 18, 2009
 */
package com.roguelogic.snakies;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * @author Robert C. Ilardi
 *
 */

public class SimpleSnakie implements Snakie {

  private int lastX1;
  private int lastY1;

  private int xDirection = 1;
  private int yDirection = 1;

  private int xNegPreference = 1;
  private int yNegPreference = 1;

  private Dimension screenSize;

  private ArrayList<Dot> points;

  public static final int MAX_DOTS = 30;

  private int fractIndex;

  public SimpleSnakie() {
    points = new ArrayList<Dot>();
  }

  /* (non-Javadoc)
   * @see com.roguelogic.fractuals.Fractual#setDrawingAreaSize(java.awt.Dimension)
   */
  @Override
  public void setDrawingAreaSize(Dimension d) {
    screenSize = d;

    lastX1 = d.width / 2;
    lastY1 = d.height / 2;
  }

  public void setFractualIndex(int index) {
    this.fractIndex = index;

    if ((index + 1) % 2 == 0) {
      xNegPreference = 1;
      yNegPreference = 1;
    }
    else {
      xNegPreference = -1;
      yNegPreference = -1;
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.fractuals.Fractual#drawFractual(java.awt.Graphics)
   */
  @Override
  public void drawFractual(Graphics g) {
    Dot d = new Dot();
    d.x = lastX1;
    d.y = lastY1;
    d.color = new Color((int) (10 + (Math.random() * 245)), (int) (10 + (Math.random() * 245)), (int) (10 + (Math.random() * 245)));
    points.add(d);

    if (points.size() > MAX_DOTS) {
      points.remove(0);
    }

    for (int i = 0; i < points.size(); i++) {
      d = points.get(i);
      g.setColor(d.color);

      if (i == points.size() - 1) {
        g.fillOval(d.x, d.y, 10, 10);
      }
      else {
        g.fillOval(d.x, d.y, 7, 7);
      }
    }

    if (lastX1 > screenSize.width - 10) {
      xDirection = -1;
      xNegPreference = -1;
    }
    else if (lastX1 < 10) {
      xDirection = 1;
      xNegPreference = 1;
    }
    else {
      xDirection = (Math.random() * 1000 >= 300 ? 1 * xNegPreference : -1 * xNegPreference);
    }

    if (lastY1 > screenSize.height - 10) {
      yDirection = -1;
      yNegPreference = -1;
    }
    else if (lastY1 < 10) {
      yDirection = 1;
      yNegPreference = 1;
    }
    else {
      yDirection = (Math.random() * 1000 >= 300 ? 1 * yNegPreference : -1 * yNegPreference);
    }

    lastX1 += (Math.random() * 10) * xDirection;

    lastY1 += (Math.random() * 10) * yDirection;
  }
}
