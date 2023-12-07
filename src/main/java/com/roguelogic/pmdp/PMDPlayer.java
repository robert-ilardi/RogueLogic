/*
 * Created on Nov 21, 2007
 */
package com.roguelogic.pmdp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.bean.playerbean.MediaPlayer;
import javax.media.protocol.URLDataSource;

import com.roguelogic.pmd.protocol.pmd.PMDConnection;
import com.roguelogic.pmd.protocol.pmd.PMDHandlerFactory;

/**
 * @author rilardi
 */

public class PMDPlayer {

  public PMDPlayer() {}

  public static void main(String[] args) throws Exception {
    MediaPlayer mPlayer = new MediaPlayer();

    //URL.setURLStreamHandlerFactory(new PMDHandlerFactory());

    //URL u = new URL(null, "pmd://musicman|music1979@localhost:1979/C:/Empire/Fight of Your Life - The Phoenix and the Fall.mp3", new com.roguelogic.pmd.protocol.pmd.Handler());
    URL u = new URL(null, "pmd://musicman|music1979@localhost:1979/C:/Empire/test1.mp3", new com.roguelogic.pmd.protocol.pmd.Handler());
    //URL u = new URL("pmd://musicman|music1979@localhost:1979/C:/Empire/Fight of Your Life - The Phoenix and the Fall.mp3");
    //URL u = new URL("file:/C:/Empire/Fight of Your Life - The Phoenix and the Fall.mp3");
    //URLConnection conn = u.openConnection();
    //URL u = new URL("http://localhost:8080/test1.mp3");
    //URL u = new URL("http://ilardinet.com:42905/test1.mp3");

    //mPlayer.setMediaLocation("file:/C:/Empire/Fight of Your Life - The Phoenix and the Fall.mp3");
    //mPlayer.setMediaLocation("pmd://musicman|music1979@localhost:1979/C:/Empire/Fight of Your Life - The Phoenix and the Fall.mp3");

    //URLDataSource ds = new URLDataSource(u);

    /*PMDConnection conn = (PMDConnection) u.openConnection();
    conn.connect();
    System.out.println(conn.getContentLength());
    conn.disconnect();*/

    //ds.connect();
    //System.out.println(ds.getContentType());
    //mPlayer.setDataSource(ds);
    //mPlayer.setSource(ds);
    //MediaLocator locator = new MediaLocator(u);
    //mPlayer.setMediaLocator(ds.getLocator());
    //mPlayer.setMediaLocation("http://ilardinet.com:42905/test1.mp3");
    //mPlayer.start();
    /*MediaLocator locator = new MediaLocator(u);
    System.out.println(locator.getProtocol());
    System.out.println(locator.getRemainder());
    Player p = Manager.createPlayer(locator);
    p.realize();
    p.start();*/

    /*Player p = Manager.createRealizedPlayer(u);
    p.realize();
    p.start();*/

    PMDConnection conn = (PMDConnection) u.openConnection();
    conn.connect();

    InputStream ins = conn.getInputStream();

    byte[] buf = new byte[2048];
    int len;
    File tf = File.createTempFile("pmd", ".mp3");
    tf.deleteOnExit();
    System.out.println(tf.getPath());

    FileOutputStream fos = new FileOutputStream(tf.getPath());

    len = ins.read(buf);
    while (len != -1) {
      fos.write(buf, 0, len);
      len = ins.read(buf);
    }

    /*int b = ins.read();
    while (b != -1) {
      fos.write(b);
      b = ins.read();
    }*/

    ins.close();
    fos.close();

    conn.disconnect();

    System.out.println(tf.length());

    mPlayer.setMediaLocation("file:/" + tf.getPath());
    mPlayer.start();
  }
}
