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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 * @author rilardi
 * 
 */
public class SnakiesPanel extends JPanel implements MouseInputListener {

  public static final int SP_SLEEP = 100;

  private Thread spThread;
  private Object spLock;
  private boolean drawFractuals;
  private boolean spRunning;

  private ArrayList<Snakie> fractuals;

  public static final int DEFAULT_NUM_SNAKES = 4;

  public SnakiesPanel() {
    addMouseListener(this);
    initScreenPainter();
  }

  private void initScreenPainter() {
    spLock = new Object();
    spRunning = false;
    drawFractuals = false;
  }

  private Runnable screenPainter = new Runnable() {
    public void run() {
      synchronized (spLock) {
        spRunning = true;
        drawFractuals = true;
        spLock.notifyAll();
      }

      while (drawFractuals) {
        repaint();

        try {
          Thread.sleep(SP_SLEEP);
        }
        catch (Exception e) {}
      }

      synchronized (spLock) {
        drawFractuals = false;
        spRunning = false;
        spLock.notifyAll();
      }

      repaint(); // Do final update...
    }
  };

  protected void paintComponent(Graphics g) {
    BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics imgGrphcs = img.getGraphics();

    // Paint Background
    imgGrphcs.setColor(new Color(0, 0, 0));
    imgGrphcs.fillRect(0, 0, getWidth(), getHeight());

    // Calls to Draw Digits
    drawFractual(imgGrphcs);

    // Paint Buffered Image
    g.drawImage(img, 0, 0, null);
  }

  public void startFractuals() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    // Make Sure the sp thread is stopped
    stopFractuals();

    if (fractuals == null) {
      loadFractuals();
    }

    // Start Timer Thread and wait for it to start running...
    spThread = new Thread(screenPainter);
    spThread.start();

    synchronized (spLock) {
      try {
        while (!spRunning) {
          spLock.wait();
        }
      }
      catch (Exception e) {
        // Opps! If this happens, don't know if the sp is running...
        e.printStackTrace();
      }
    }
  }

  public void stopFractuals() {
    // Make Sure the sp thread is stopped
    synchronized (spLock) {
      drawFractuals = false;

      while (spRunning) {
        try {
          spLock.wait();
        }
        catch (Exception e) {
          // Opps! Once we start another sp thread, we might have more than one threading running if this happens...
          e.printStackTrace();
        }
      }
    }
  }

  public void mouseClicked(MouseEvent me) {
    if (me.getClickCount() >= 2) {
      showAbout();
    }
  }

  public void mousePressed(MouseEvent me) {}

  public void mouseReleased(MouseEvent me) {}

  public void mouseEntered(MouseEvent me) {}

  public void mouseExited(MouseEvent me) {}

  public void mouseDragged(MouseEvent me) {}

  public void mouseMoved(MouseEvent me) {}

  public void showAbout() {
    try {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(SnakiesPanel.this, Version.GetInfo(), Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE);
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void loadFractuals() {
    Dimension d;
    SimpleSnakie sf;

    fractuals = new ArrayList<Snakie>();

    d = new Dimension(getWidth(), getHeight());

    for (int i = 0; i < DEFAULT_NUM_SNAKES; i++) {
      sf = new SimpleSnakie();

      sf.setFractualIndex(i);
      sf.setDrawingAreaSize(d);

      fractuals.add(sf);
    }
  }

  private void drawFractual(Graphics g) {
    if (fractuals == null || fractuals.isEmpty()) {
      return;
    }

    for (Snakie f : fractuals) {
      f.drawFractual(g);
    }
  }

}
