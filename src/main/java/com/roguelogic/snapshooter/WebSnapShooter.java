/**
 * Created Jan 21, 2007
 */
package com.roguelogic.snapshooter;

import java.awt.Image;
import java.io.IOException;

import javax.media.CannotRealizeException;
import javax.media.NoPlayerException;

import com.roguelogic.util.RLException;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class WebSnapShooter {

  private int minutesPerCapture;
  private String outputDir;
  private String captureDeviceURL;

  private int imgCnt = 1;

  public WebSnapShooter() {}

  public String getCaptureDeviceURL() {
    return captureDeviceURL;
  }

  public void setCaptureDeviceURL(String captureDeviceURL) {
    this.captureDeviceURL = captureDeviceURL;
  }

  public int getMinutesPerCapture() {
    return minutesPerCapture;
  }

  public void setMinutesPerCapture(int minutesPerCapture) {
    this.minutesPerCapture = minutesPerCapture;
  }

  public String getOutputDir() {
    return outputDir;
  }

  public void setOutputDir(String outputDir) {
    this.outputDir = outputDir;
  }

  private void captureImage() throws NoPlayerException, CannotRealizeException, IOException {
    PicGrabber grabber = null;
    Image img;
    StringBuffer outputFile;

    try {
      grabber = new PicGrabber();
      grabber.init(captureDeviceURL);
      img = grabber.grabImage();

      outputFile = new StringBuffer();
      outputFile.append(outputDir);
      outputFile.append("/wssgrab_");
      outputFile.append(imgCnt);
      outputFile.append(".jpg");

      grabber.saveJPEG(img, outputFile.toString());

      imgCnt++;
    }
    finally {
      if (grabber != null) {
        grabber.close();
        grabber = null;
      }
    }
  }

  public void startCapturing() throws NoPlayerException, CannotRealizeException, IOException {
    System.out.print("\nStarting Web Snap Shooter Auto Capture Sequence: " + StringUtils.GetTimeStamp() + "\n");

    while (true) {
      System.out.print("\nCapturing Image at: " + StringUtils.GetTimeStamp() + "\n");

      captureImage();

      for (int i = 1; i <= minutesPerCapture; i++) {
        System.out.print("Waiting " + ((minutesPerCapture + 1) - i) + " minute(s)" + "\n");
        SystemUtils.Sleep(60);
      }
    }
  }

  public static void main(String[] args) {
    WebSnapShooter wss;
    String cdUrl, outputDir;
    int minutesPerCapture, retCode;

    if (args.length != 3) {
      System.err.println("Usage: java " + WebSnapShooter.class.getName() + " [CAPTURE_DEVICE_URL] [OUTPUT_DIRECTORY] [MINUTES_BETWEEN_CAPTURES]");
      retCode = 1;
    }
    else {
      try {
        cdUrl = args[0].trim();
        outputDir = args[1].trim();
        minutesPerCapture = Integer.parseInt(args[2].trim());

        if (minutesPerCapture < 1) {
          throw new RLException("Minutes Between Captures MUST be >= 1 minute!");
        }

        wss = new WebSnapShooter();
        wss.setCaptureDeviceURL(cdUrl);
        wss.setOutputDir(outputDir);
        wss.setMinutesPerCapture(minutesPerCapture);

        wss.startCapturing();

        retCode = 0;
      }
      catch (Exception e) {
        retCode = 1;
        e.printStackTrace();
      }
    }

    System.exit(retCode);
  }

}
