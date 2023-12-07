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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;

import javax.swing.JApplet;
import javax.swing.JPanel;

/**
 * @author rilardi
 * 
 */
public class JDaliClockApplet extends JApplet {

  public static final String PARAM_24_HOUR_MODE = "Mode24Hours";
  public static final String PARAM_DIGIT_CLASS = "DigitClass";

  private JDaliClockPanel jdcPanel;

  /**
   * @throws HeadlessException
   */
  public JDaliClockApplet() throws HeadlessException {
    setSize(500, 150);
    createGUI();
  }

  public void startClock() {
    try {
      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          try {
            jdcPanel.startClock();
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void createGUI() {
    JPanel mainPanel, blankPanel;
    GridBagLayout frameGbl, mpGbl;
    GridBagConstraints gbc;

    gbc = new GridBagConstraints();

    // Add the main panel
    mainPanel = new JPanel();
    mainPanel.setPreferredSize(getSize());
    mainPanel.setMaximumSize(getSize());
    mainPanel.setMinimumSize(getSize());
    mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.setAlignmentY(Component.TOP_ALIGNMENT);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;

    frameGbl = new GridBagLayout();
    frameGbl.setConstraints(mainPanel, gbc);
    setLayout(frameGbl);
    add(mainPanel);

    mpGbl = new GridBagLayout();
    mainPanel.setLayout(mpGbl);

    // JDaliClockPanel------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.weightx = 2.0;
    gbc.weighty = 1.0;

    jdcPanel = new JDaliClockPanel();
    mpGbl.setConstraints(jdcPanel, gbc);
    mainPanel.add(jdcPanel);

    // End Row
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);

    // Last Row (Main Panel)------------------------------------------------------->
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    blankPanel = new JPanel();
    blankPanel.setPreferredSize(new Dimension(1, 1));
    mpGbl.setConstraints(blankPanel, gbc);
    mainPanel.add(blankPanel);
  }

  public void set24HourMode(boolean mode24Hour) {
    jdcPanel.set24HourMode(mode24Hour);
  }

  public void setDigitOverrideClass(String digitOverrideClass) {
    jdcPanel.setDigitOverrideClass(digitOverrideClass);
  }

  public void init() {
    setOptions();
    startClock();
  }

  private void setOptions() {
    set24HourMode("TRUE".equalsIgnoreCase(getParameter(PARAM_24_HOUR_MODE)));
    setDigitOverrideClass(getParameter(PARAM_DIGIT_CLASS));
  }

}
