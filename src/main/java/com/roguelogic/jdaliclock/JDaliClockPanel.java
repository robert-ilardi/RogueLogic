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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import com.roguelogic.util.StringUtils;

/**
 * @author rilardi
 * 
 */
public class JDaliClockPanel extends JPanel implements MouseInputListener {

	public static final int MAX_CLOCK_DIGITS = 6;
	public static final int MAX_CLOCK_COLONS = 2;

	public static final int SP_SLEEP = 50;

	private Digit[] digits;
	private Digit[] colons;

	private Thread spThread;
	private Object spLock;
	private boolean clockTicking;
	private boolean spRunning;

	private boolean mode24Hour = false;

	private String digitOverrideClass;

	private static final SimpleDateFormat Sdf24HourMode = new SimpleDateFormat("HHmmss");
	private static final SimpleDateFormat Sdf12HourMode = new SimpleDateFormat("hhmmss");

	public JDaliClockPanel() {
		addMouseListener(this);
		initScreenPainter();
	}

	private void initScreenPainter() {
		spLock = new Object();
		spRunning = false;
		clockTicking = false;
	}

	private Runnable screenPainter = new Runnable() {
		public void run() {
			synchronized (spLock) {
				spRunning = true;
				clockTicking = true;
				spLock.notifyAll();
			}

			while (clockTicking) {
				repaint();

				try {
					Thread.sleep(SP_SLEEP);
				}
				catch (Exception e) {}
			}

			synchronized (spLock) {
				clockTicking = false;
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
		imgGrphcs.setColor(new Color(0, 0, 80));
		imgGrphcs.fillRect(0, 0, getWidth(), getHeight());

		// Calls to Draw Digits
		drawDigits(imgGrphcs);

		// Paint Buffered Image
		g.drawImage(img, 0, 0, null);
	}

	private void initDigits() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		int colonSpace = 0;
		int colonCnt = 0;

		digits = new Digit[MAX_CLOCK_DIGITS];
		colons = new Digit[MAX_CLOCK_COLONS];

		for (int i = 0; i < MAX_CLOCK_DIGITS; i++) {
			digits[i] = createDigit();

			digits[i].setCurrentDigit(getCurrentClockDigitAt(i));

			if (i > 0 && i % 2 == 0) {
				// Position Colon and add 50 to colon space

				colons[colonCnt] = createDigit();
				colons[colonCnt].setLeftCornerX((i == 0 ? 10 : 10 * (i + 1)) + (i * 50) + colonSpace);
				colons[colonCnt].setLeftCornerY(10);

				colonSpace += 50;
				colonCnt++;
			}

			digits[i].setLeftCornerX((i == 0 ? 10 : 10 * (i + 1)) + (i * 50) + colonSpace);
			digits[i].setLeftCornerY(10);
		}
	}

	private int getCurrentClockDigitAt(int index) {
		SimpleDateFormat sdf;
		String tStr;
		int digit = 0;

		sdf = (mode24Hour ? Sdf24HourMode : Sdf12HourMode);

		tStr = sdf.format(new Date());
		digit = Integer.parseInt(tStr.substring(index, index + 1));

		return digit;
	}

	private void drawDigits(Graphics g) {
		if (digits == null) {
			return;
		}

		g.setColor(Color.WHITE);

		/*
		 * for (int i = 0; i < MAX_CLOCK_DIGITS; i++) { digits[i].setCurrentDigit(getCurrentClockDigitAt(i)); digits[i].draw(g); }
		 */

		digits[0].setCurrentDigit(getCurrentClockDigitAt(0));

		if (mode24Hour || digits[0].getCurrentDigit() > 0) {
			digits[0].draw(g);
		}

		digits[1].setCurrentDigit(getCurrentClockDigitAt(1));
		digits[1].draw(g);

		colons[0].drawColon(g);

		digits[2].setCurrentDigit(getCurrentClockDigitAt(2));
		digits[2].draw(g);

		digits[3].setCurrentDigit(getCurrentClockDigitAt(3));
		digits[3].draw(g);

		colons[1].drawColon(g);

		digits[4].setCurrentDigit(getCurrentClockDigitAt(4));
		digits[4].draw(g);

		digits[5].setCurrentDigit(getCurrentClockDigitAt(5));
		digits[5].draw(g);
	}

	public void startClock() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// Make Sure the sp thread is stopped
		stopClock();

		// Lazy Init Digits if need...
		if (digits == null) {
			initDigits();
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

	public void stopClock() {
		// Make Sure the sp thread is stopped
		synchronized (spLock) {
			clockTicking = false;

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

	public void set24HourMode(boolean mode24Hour) {
		this.mode24Hour = mode24Hour;
	}

	public void setDigitOverrideClass(String digitOverrideClass) {
		this.digitOverrideClass = digitOverrideClass;
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
					JOptionPane.showMessageDialog(JDaliClockPanel.this, (new StringBuffer()).append(Version.GetInfo()).append("Using Digit Class: ").append(
							(StringUtils.IsNVL(digitOverrideClass) ? LedDigit.class.getName() : digitOverrideClass)).toString(), Version.APP_TITLE, JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Digit createDigit() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Digit d;

		if (StringUtils.IsNVL(digitOverrideClass)) {
			d = new LedDigit();
		}
		else {
			d = (Digit) Class.forName(digitOverrideClass).newInstance();
		}

		return d;
	}

}
