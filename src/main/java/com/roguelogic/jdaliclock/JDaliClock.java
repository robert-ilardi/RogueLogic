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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author rilardi
 * 
 */
public class JDaliClock extends JFrame implements WindowListener {

	public static final int DEFAULT_WIDTH = 500;
	public static final int DEFAULT_HEIGHT = 150;

	private JDaliClockPanel jdcPanel;

	private int startX;
	private int startY;

	/**
	 * @throws HeadlessException
	 */
	public JDaliClock() throws HeadlessException {
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(Version.APP_TITLE);
		addWindowListener(this);

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

	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent arg0) {}

	public void windowClosing(WindowEvent arg0) {
		exitClock();
	}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}

	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}

	private void exitClock() {
		System.exit(0);
	}

	public void setOptions(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String opt = args[i].trim().toUpperCase();

			if ("-24HOUR".equals(opt)) {
				set24HourMode(true);
			}
			else if (opt.startsWith("-X")) {
				setXPosition(args[i]);
			}
			else if (opt.startsWith("-Y")) {
				setYPosition(args[i]);
			}
			else if (opt.startsWith("-DIGITCLASS=")) {
				setDigitOverrideClass(args[i]);
			}
		}

		configureClock();
	}

	private void configureClock() {
		setLocation(startX, startY);
	}

	public void set24HourMode(boolean mode24Hour) {
		jdcPanel.set24HourMode(mode24Hour);
	}

	public void setXPosition(String xStr) {
		startX = Integer.parseInt(xStr.substring(2));
	}

	public void setYPosition(String yStr) {
		startY = Integer.parseInt(yStr.substring(2));
	}

	public void setDigitOverrideClass(String digitOverrideClass) {
		digitOverrideClass = digitOverrideClass.substring(12);
		jdcPanel.setDigitOverrideClass(digitOverrideClass);
	}

	public static void main(String[] args) throws Exception {
		JDaliClock jdc;

		jdc = new JDaliClock();
		jdc.setOptions(args);

		jdc.setVisible(true);

		jdc.startClock();
	}

}
