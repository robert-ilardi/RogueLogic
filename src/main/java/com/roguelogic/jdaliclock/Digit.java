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

/**
 * @author Robert C. Ilardi
 * 
 */
public interface Digit {

  public void setCurrentDigit(int curDigit);

  public void draw(Graphics g);

  public void drawColon(Graphics g);

  public int getCurrentDigit();

  public void setLeftCornerX(int x);

  public void setLeftCornerY(int y);

}
