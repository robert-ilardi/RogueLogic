/**
 * Created Jan 21, 2007
 */
package com.roguelogic.snapshooter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import com.roguelogic.util.SystemUtils;
import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author Robert C. Ilardi
 *
 */

public class PicGrabber {

  public static final String WEBCAM_CAPTURE_DEVICE_URL = "vfw://0";
  public static final int INIT_SLEEP_TIME = 10;

  private Player player = null;

  public PicGrabber() {}

  public void init(String captureDeviceURL) throws NoPlayerException, CannotRealizeException, IOException {
    MediaLocator ml;

    System.out.println("Creating Player for Capture Device: " + captureDeviceURL);
    ml = new MediaLocator(captureDeviceURL);

    player = Manager.createRealizedPlayer(ml);
    player.start();

    SystemUtils.Sleep(INIT_SLEEP_TIME);
  }

  public void close() {
    System.out.println("Deallocating Capture Device Player...");

    player.close();
    player = null;
  }

  public Image grabImage() throws IOException {
    Image img;
    FrameGrabbingControl fgc;
    Buffer buf;
    BufferToImage btoi;
    TrackControl trkCtrl;
    VideoFormat format;

    System.out.println("Grabbing Image...");

    trkCtrl = (TrackControl) player.getControl("javax.media.control.TrackControl");
    format = (VideoFormat) trkCtrl.getFormat();

    System.out.println("Native Camera Format: " + format);

    fgc = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
    buf = fgc.grabFrame();

    btoi = new BufferToImage((VideoFormat) buf.getFormat());
    img = btoi.createImage(buf);

    return img;
  }

  public void saveJPEG(Image img, String outputFile) throws ImageFormatException, FileNotFoundException, IOException {
    BufferedImage bufImg;
    Graphics2D gd2;
    FileOutputStream fos = null;

    try {
      System.out.println("Saving JPEG...");

      System.out.println("Scaling Native Capture: 2X");
      img = img.getScaledInstance(img.getWidth(null) * 2, img.getHeight(null) * 2, Image.SCALE_SMOOTH);

      bufImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

      gd2 = bufImg.createGraphics();
      gd2.drawImage(img, null, null);

      System.out.println("Writing JPEG File: " + outputFile);
      fos = new FileOutputStream(outputFile);

      JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
      JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufImg);
      param.setQuality(1.0f, false);
      encoder.setJPEGEncodeParam(param);

      encoder.encode(bufImg);
    } //End try block
    finally {
      if (fos != null) {
        try {
          fos.close();
        }
        catch (Exception e) {}
      }
    }
  }

}
