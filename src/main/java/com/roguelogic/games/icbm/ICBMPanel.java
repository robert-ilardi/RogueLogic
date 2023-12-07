/*
 * Created on Oct 2, 2007
 */
package com.roguelogic.games.icbm;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author rilardi
 */
public class ICBMPanel extends JPanel implements MouseListener, KeyListener {

  public static final int DEFAULT_MAX_SIMULTANEOUS_ICBMS = 1;
  public static final int DEFAULT_ICBM_FALL_RATE = 3;
  public static final int DEFAULT_ANTI_ICBM_RAISE_RATE = 20;

  public static final long INIT_ICBM_ID = 1000;
  public static final long INIT_ANTI_ICBM_ID = 1000;

  private ICBMApplet icbmApplet;

  private ImageIcon cityImg;
  private ImageIcon mLauncherImg;
  private ImageIcon missileImg;
  private ImageIcon rubbleImg;
  private ImageIcon mushroomCloudImg;
  private ImageIcon explosionImg;

  private Thread spThread;
  private boolean gameInProgress;
  private Object spLock;
  private boolean spRunning;

  private boolean paused;

  private boolean[] cityExistence;

  private long icbmId = INIT_ICBM_ID;
  private long antiIcbmId = INIT_ANTI_ICBM_ID;;

  private ArrayList<Icbm> activeIcbms;
  private ArrayList<AntiIcbmMissile> activeAntiIcbms;

  private ArrayList<Impact> nextCycleImpacts;

  private boolean allowMultipleMissilesPerCity = true;

  public static final boolean DEBUG = false;

  private Runnable screenPainter = new Runnable() {
    public void run() {
      synchronized (spLock) {
        spRunning = true;
        spLock.notifyAll();
      }

      while (gameInProgress) {
        //A cheap pause implementation
        //I was doing this at 1AM and too tired
        //to write it the right way with a mutext/wait/notify...
        if (paused) {
          try {
            Thread.sleep(1000);
          }
          catch (Exception e) {}

          continue;
        }

        removeImpactedICBMs();
        removeImpactedAntiICBMs();

        performCpuMove();

        moveICBMs();
        moveAntiICBMs();

        determineIcbmInterceptions();

        repaint();

        updateDestroyedCities();

        removeSkyExplosions();

        //Impacts after next cycle not before this cycle's repaint (for animation purposes)...
        determineICBMImpacts();
        determineAntiICBMImpacts();

        try {
          Thread.sleep(100);
        }
        catch (Exception e) {}
      }

      synchronized (spLock) {
        gameInProgress = false;
        spRunning = false;
        spLock.notifyAll();
      }

      nextCycleImpacts.clear();
      activeIcbms.clear();
      activeAntiIcbms.clear();
      repaint(); //Do final update...
    }
  };

  public ICBMPanel(LayoutManager lm, boolean dBuf) {
    super(lm, dBuf);
    init();
  }

  public ICBMPanel(LayoutManager lm) {
    super(lm);
    init();
  }

  public ICBMPanel(boolean dBuf) {
    super(dBuf);
    init();
  }

  public ICBMPanel() {
    super();
    init();
  }

  private void init() {
    addMouseListener(this);
    addKeyListener(this);

    setFocusable(true);

    spLock = new Object();
    gameInProgress = false;
    spRunning = false;

    activeIcbms = new ArrayList<Icbm>();
    activeAntiIcbms = new ArrayList<AntiIcbmMissile>();
    nextCycleImpacts = new ArrayList<Impact>();

    loadImages();
  }

  private void loadImages() {
    byte[] imgData;
    MediaTracker mt;

    try {
      imgData = loadImageData("skyline.png");
      cityImg = new ImageIcon(imgData);

      imgData = loadImageData("mlauncher.png");
      mLauncherImg = new ImageIcon(imgData);

      imgData = loadImageData("missile.png");
      missileImg = new ImageIcon(imgData);

      imgData = loadImageData("rubble.png");
      rubbleImg = new ImageIcon(imgData);

      imgData = loadImageData("mushroom_cloud.png");
      mushroomCloudImg = new ImageIcon(imgData);

      imgData = loadImageData("explosion.png");
      explosionImg = new ImageIcon(imgData);

      mt = new MediaTracker(this);

      mt.addImage(cityImg.getImage(), 0);
      mt.addImage(mLauncherImg.getImage(), 1);
      mt.addImage(missileImg.getImage(), 2);
      mt.addImage(rubbleImg.getImage(), 3);
      mt.addImage(mushroomCloudImg.getImage(), 4);

      try {
        mt.waitForAll();
      }
      catch (Exception e) {
        e.printStackTrace();
      }

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setIcbmApplet(ICBMApplet icbmApplet) {
    this.icbmApplet = icbmApplet;
  }

  protected void paintComponent(Graphics g) {
    BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics imgGrphcs = img.getGraphics();

    //Paint Background
    imgGrphcs.setColor(new Color(0, 0, 80));
    imgGrphcs.fillRect(0, 0, getWidth(), getHeight());

    drawMissileLauncher(imgGrphcs);

    drawCities(imgGrphcs);

    drawImpacts(imgGrphcs);

    drawICBMs(imgGrphcs);

    drawAntiICBMs(imgGrphcs);

    //Paint Buffered Image
    g.drawImage(img, 0, 0, null);
  }

  private void drawMissileLauncher(Graphics g) {
    g.drawImage(mLauncherImg.getImage(), 0, getHeight() - mLauncherImg.getIconHeight(), null);
  }

  private void drawCities(Graphics g) {
    for (int i = 1; i <= icbmApplet.getMaxCityCnt(); i++) {
      if (cityExistence[i - 1]) {
        g.drawImage(cityImg.getImage(), (90 * i) + (25 * (i - 1)), getHeight() - cityImg.getIconHeight(), null);
      }
      else {
        g.drawImage(rubbleImg.getImage(), (90 * i) + (25 * (i - 1)), getHeight() - rubbleImg.getIconHeight(), null);
      }
    }
  }

  private byte[] loadImageData(String imgFile) throws IOException {
    InputStream ins = null;
    ByteArrayOutputStream baos = null;
    byte[] buf = null;
    int len;

    try {
      ins = this.getClass().getResourceAsStream(imgFile);
      buf = new byte[1024];
      baos = new ByteArrayOutputStream();

      len = ins.read(buf);

      while (len != -1) {
        baos.write(buf, 0, len);
        len = ins.read(buf);
      }

      buf = baos.toByteArray();
    }
    finally {
      if (ins != null) {
        try {
          ins.close();
        }
        catch (Exception e) {}
      }
    }

    return buf;
  }

  public synchronized void reset() {
    //Make Sure the sp thread is stopped
    synchronized (spLock) {
      gameInProgress = false;

      while (spRunning) {
        try {
          spLock.wait();
        }
        catch (Exception e) {
          //Opps! Once we start another sp thread, we might have more than one threading running if this happens...
          e.printStackTrace();
        }
      }
    }

    //Make sure everything is cleared;
    paused = false;

    icbmId = INIT_ICBM_ID;
    antiIcbmId = INIT_ANTI_ICBM_ID;

    activeIcbms.clear();
    activeAntiIcbms.clear();
    nextCycleImpacts.clear();

    if (cityExistence == null) {
      cityExistence = new boolean[icbmApplet.getMaxCityCnt()];
    }

    for (int i = 0; i < cityExistence.length; i++) {
      cityExistence[i] = true;
    }

    //Start Timer Thread and wait for it to start running...
    gameInProgress = true;
    spThread = new Thread(screenPainter);
    spThread.start();

    synchronized (spLock) {
      try {
        while (!spRunning) {
          spLock.wait();
        }
      }
      catch (Exception e) {
        //Opps! If this happens, don't know if the sp is running...
        e.printStackTrace();
      }
    }
  }

  public synchronized void stopProcessThread() {
    //Make Sure the sp thread is stopped
    synchronized (spLock) {
      gameInProgress = false;

      while (spRunning) {
        try {
          spLock.wait();
        }
        catch (Exception e) {
          //Opps! Once we start another sp thread, we might have more than one threading running if this happens...
          e.printStackTrace();
        }
      }
    }
  }

  private void performCpuMove() {
    while (true) {
      if (activeIcbms.size() < getMaxSimultaneousICBMs() && citiesExist() && (allowMultipleMissilesPerCity || targetAvailable())) {
        fireICBM();
      }
      else {
        break;
      }
    }
  }

  private boolean targetAvailable() {
    boolean availTarget = false;

    for (int i = 0; i < icbmApplet.getMaxCityCnt(); i++) {
      if (cityExistence[i] && !cityAlreadyTargeted(i)) {
        availTarget = true;
        break;
      }
    }

    return availTarget;
  }

  private boolean citiesExist() {
    boolean exist = false;

    for (int i = 0; i < cityExistence.length; i++) {
      if (cityExistence[i]) {
        exist = true;
        break;
      }
    }

    return exist;
  }

  private void fireICBM() {
    Icbm icbm;
    Random rnd;
    int skyPoint = 0;

    if (getHeight() <= 0 && getWidth() <= 0) {
      return;
    }

    //Launch new ICBM
    icbm = new Icbm();
    icbm.setId(icbmId++);

    //Plot Initial Sky Point
    rnd = new Random();

    while (skyPoint == 0) {
      skyPoint = rnd.nextInt(icbmApplet.getWidth());
    }

    icbm.setInitX(skyPoint);
    icbm.setCurX(skyPoint);
    icbm.setCurY(0);

    //Lock in on Target City
    determineTargetCityCenter(icbm);

    //Avoid a ZERO slope...
    if (icbm.getCityCenterX() == icbm.getInitX()) {
      icbm.setCityCenterX(icbm.getCityCenterX() + 1);
    }

    //Check if ICBM is "programmer" correctly
    //If so add it to the active missile list
    if (programCorrect(icbm)) {
      activeIcbms.add(icbm);
      System.out.println("ICBM Launched: " + icbm);
    }
    else {
      System.out.println("ICBM Bad Programming - Launch Aborted: " + icbm);
    }
  }

  private boolean programCorrect(Icbm icbm) {
    boolean pCorr = false;

    if (icbm != null) {
      pCorr = icbm.getCityCenterX() > 0 && icbm.getTargetCityIndex() >= 0 && icbm.getTargetCityIndex() < icbmApplet.getMaxCityCnt() && icbm.getInitX() > 0 && icbm.getInitX() < getWidth();
    }

    return pCorr;
  }

  private void determineTargetCityCenter(Icbm icbm) {
    int cityCenterX = 0, cIndex;
    Random nxtCity;

    /*for (int i = 0; i < icbmApplet.getMaxCityCnt(); i++) {
     if (cityExistence[i] && (allowMultipleMissilesPerCity || !cityAlreadyTargeted(i))) {
     icbm.setTargetCityIndex(i);

     cityCenterX = (90 * (i + 1)) + (25 * i) + (cityImg.getIconWidth() / 2);
     icbm.setCityCenterX(cityCenterX);

     break;
     }
     }*/

    nxtCity = new Random();

    while (citiesExist() && activeIcbms.size() <= getMaxSimultaneousICBMs()) {
      cIndex = nxtCity.nextInt(icbmApplet.getMaxCityCnt());

      if (cityExistence[cIndex] && (allowMultipleMissilesPerCity || !cityAlreadyTargeted(cIndex))) {
        icbm.setTargetCityIndex(cIndex);

        cityCenterX = (90 * (cIndex + 1)) + (25 * cIndex) + (cityImg.getIconWidth() / 2);
        icbm.setCityCenterX(cityCenterX);

        break;
      }
    }
  }

  private boolean cityAlreadyTargeted(int cityIndex) {
    boolean targeted = false;

    for (Icbm icbm : activeIcbms) {
      if (icbm.getTargetCityIndex() == cityIndex) {
        targeted = true;
        break;
      }
    }

    return targeted;
  }

  private void moveICBMs() {
    //y = m(x - a) + b ; (a, b) is a specific point on the graph
    int y, x, a, b, h;
    double m;

    h = getHeight();

    if (h <= 0) {
      return;
    }

    for (Icbm icbm : activeIcbms) {
      a = icbm.getCityCenterX();
      b = h;

      m = (double) h / (icbm.getCityCenterX() - icbm.getInitX());

      y = icbm.getCurY() + getIcbmFallRate();

      x = (int) (((y - b) + (m * a)) / m);

      icbm.setCurX(x);
      icbm.setCurY(y);

      if (DEBUG) {
        System.out.println(icbm);
      }
    }
  }

  private void drawICBMs(Graphics g) {
    for (Icbm icbm : activeIcbms) {
      g.setColor(Color.WHITE);
      g.drawLine(icbm.getInitX(), 0, icbm.getCurX(), icbm.getCurY());
    }
  }

  private void determineICBMImpacts() {
    int h = getHeight();
    Impact impact;

    if (h <= 0) {
      return;
    }

    for (Icbm icbm : activeIcbms) {
      if (icbm.getCurY() >= h) {
        System.out.println("City Impacted: " + icbm);
        impact = new Impact();

        impact.setMissileId(icbm.getId());
        impact.setType(Impact.IMPACT_TYPE_ICBM_CITY);
        impact.setX(icbm.getCurX());
        impact.setY(icbm.getCurY());
        impact.setCityIndex(icbm.getTargetCityIndex());

        nextCycleImpacts.add(impact);
      }
    }
  }

  private void removeImpactedICBMs() {
    Icbm icbm;

    for (Impact impact : nextCycleImpacts) {
      if (impact.getType() == Impact.IMPACT_TYPE_ICBM_CITY) {
        for (int i = 0; i < activeIcbms.size(); i++) {
          icbm = activeIcbms.get(i);

          if (icbm.getId() == impact.getMissileId()) {
            activeIcbms.remove(i);
            cityExistence[icbm.getTargetCityIndex()] = false; //City is now destroyed!
            icbm = null;
            break;
          }
        }
      }
    }
  }

  private void drawImpacts(Graphics g) {
    for (Impact impact : nextCycleImpacts) {
      switch (impact.getType()) {
        case Impact.IMPACT_TYPE_ICBM_CITY:
          g.drawImage(mushroomCloudImg.getImage(), (90 * (impact.getCityIndex() + 1)) + (25 * impact.getCityIndex()), getHeight() - mushroomCloudImg.getIconHeight(), null);
          break;
        case Impact.IMPACT_TYPE_ANTI_MISSILE:
          g.drawImage(explosionImg.getImage(), impact.getX() - (explosionImg.getIconWidth() / 2), impact.getY() - (explosionImg.getIconHeight() / 2), null);
          break;
      }
    }
  }

  private void updateDestroyedCities() {
    Impact impact;

    for (int i = 0; i < nextCycleImpacts.size(); i++) {
      impact = nextCycleImpacts.get(i);

      if (impact.getType() == Impact.IMPACT_TYPE_ICBM_CITY) {
        if (impact.getCycleCnt() >= 10) {
          nextCycleImpacts.remove(i);
          i = 0; //Reset

          icbmApplet.reportDestroyedCity(impact.getCityIndex());
        }
        else {
          impact.incrementCycleCnt();
        }
      }
    }
  }

  public void mouseClicked(MouseEvent me) {
    fireAntiIcbmMissile(me.getX(), me.getY());
  }

  public void mousePressed(MouseEvent me) {}

  public void mouseReleased(MouseEvent me) {}

  public void mouseEntered(MouseEvent me) {}

  public void mouseExited(MouseEvent me) {}

  private void fireAntiIcbmMissile(int x, int y) {
    AntiIcbmMissile anti;

    if (paused) {
      return;
    }

    if (y >= getHeight() - mLauncherImg.getIconHeight()) {
      return;
    }

    if (activeAntiIcbms.size() >= getMaxSimultaneousAntiICBMs()) {
      return;
    }

    if (x == (mLauncherImg.getIconWidth() / 2)) {
      x++;
    }

    anti = new AntiIcbmMissile();
    anti.setId(antiIcbmId++);
    anti.setTargetX(x);
    anti.setTargetY(y);
    anti.setCurX(mLauncherImg.getIconWidth() / 2);
    anti.setCurY(getHeight() - mLauncherImg.getIconHeight());

    activeAntiIcbms.add(anti);

    System.out.println("Anti-Missile Launched: " + anti);
  }

  private void drawAntiICBMs(Graphics g) {
    for (AntiIcbmMissile anti : activeAntiIcbms) {
      g.setColor(Color.RED);
      g.drawLine(mLauncherImg.getIconWidth() / 2, getHeight() - mLauncherImg.getIconHeight(), anti.getCurX(), anti.getCurY());
    }
  }

  private void moveAntiICBMs() {
    //y = m(x - a) + b ; (a, b) is a specific point on the graph
    int y, x, a, b, h;
    double m;

    h = getHeight();

    if (h <= 0) {
      return;
    }

    for (AntiIcbmMissile anti : activeAntiIcbms) {
      if (anti.getCurX() == anti.getTargetX() && anti.getCurY() == anti.getTargetY()) {
        continue;
      }

      a = anti.getTargetX();
      b = anti.getTargetY();

      m = (double) (anti.getTargetY() - (h - mLauncherImg.getIconHeight())) / (anti.getTargetX() - (mLauncherImg.getIconWidth() / 2));

      y = anti.getCurY() - getAntiIcbmRaiseRate();

      x = (int) (((y - b) + (m * a)) / m);

      if (anti.getTargetX() >= (mLauncherImg.getIconWidth() / 2)) {
        if (x >= anti.getTargetX()) {
          x = anti.getTargetX();
        }
      }
      else {
        if (x <= anti.getTargetX()) {
          x = anti.getTargetX();
        }
      }

      if (y <= anti.getTargetY()) {
        y = anti.getTargetY();
      }

      anti.setCurX(x);
      anti.setCurY(y);

      if (DEBUG) {
        System.out.println(anti);
      }
    }
  }

  private void determineAntiICBMImpacts() {
    int h = getHeight();
    Impact impact;

    if (h <= 0) {
      return;
    }

    for (AntiIcbmMissile anti : activeAntiIcbms) {
      if (anti.getCurX() == anti.getTargetX() && anti.getCurY() == anti.getTargetY()) {
        impact = new Impact();

        impact.setMissileId(anti.getId());
        impact.setType(Impact.IMPACT_TYPE_ANTI_MISSILE);
        impact.setX(anti.getTargetX());
        impact.setY(anti.getTargetY());

        nextCycleImpacts.add(impact);
      }
    }
  }

  private void removeImpactedAntiICBMs() {
    AntiIcbmMissile anti;

    /*for (Impact impact : nextCycleImpacts) {
     if (impact.getType() == Impact.IMPACT_TYPE_ANTI_MISSILE) {
     for (int i = 0; i < activeAntiIcbms.size(); i++) {
     anti = activeAntiIcbms.get(i);

     if (anti.getId() == impact.getMissileId()) {
     activeAntiIcbms.remove(i);
     anti = null;
     break;
     }
     }
     }
     }*/

    for (int i = 0; i < activeAntiIcbms.size(); i++) {
      anti = activeAntiIcbms.get(i);

      if (anti.getCurX() == anti.getTargetX() && anti.getCurY() == anti.getTargetY()) {
        activeAntiIcbms.remove(i);
        anti = null;
        break;
      }
    }
  }

  private void determineIcbmInterceptions() {
    Icbm icbm;

    for (Impact impact : nextCycleImpacts) {
      if (impact.getType() == Impact.IMPACT_TYPE_ANTI_MISSILE) {
        for (int i = 0; i < activeIcbms.size(); i++) {
          icbm = activeIcbms.get(i);

          if (icbm.getCurX() >= impact.getX() && icbm.getCurX() <= impact.getX() + explosionImg.getIconWidth() && icbm.getCurY() >= impact.getY()
              && icbm.getCurY() <= impact.getY() + explosionImg.getIconHeight()) {
            //ICBM Intercepted!
            System.out.println("ICBM Intercepted: " + icbm);
            activeIcbms.remove(i);
            icbm = null;
            i = 0;

            icbmApplet.reportIcbmIntercepted();
          }
        }
      }
    }
  }

  private void removeSkyExplosions() {
    Impact impact;

    for (int i = 0; i < nextCycleImpacts.size(); i++) {
      impact = nextCycleImpacts.get(i);

      if (impact.getType() == Impact.IMPACT_TYPE_ANTI_MISSILE) {
        if (impact.getCycleCnt() >= 8) {
          nextCycleImpacts.remove(i);
          i = 0; //Reset
        }
        else {
          impact.incrementCycleCnt();
        }
      }
    }
  }

  private int getIcbmFallRate() {
    //return DEFAULT_ICBM_FALL_RATE;
    return DEFAULT_ICBM_FALL_RATE + (icbmApplet.getLevel() / 50);
  }

  private int getAntiIcbmRaiseRate() {
    return DEFAULT_ANTI_ICBM_RAISE_RATE + (icbmApplet.getLevel() / 5);
  }

  private int getMaxSimultaneousICBMs() {
    int level = icbmApplet.getLevel();

    if (level == 1) {
      return 1;
    }
    else if (level >= 2 && level < 5) {
      return 2;
    }
    else if (level >= 5 && level < 10) {
      return 4;
    }
    else if (level >= 10 && level < 20) {
      return 5;
    }
    else if (level >= 20 && level < 50) {
      return 6;
    }
    else if (level >= 50 && level < 100) {
      return 8;
    }
    else if (level >= 100 && level < 200) {
      return 10 + ((level - 100) / 10);
    }
    else if (level >= 200 && level < 300) {
      return 20 + ((level - 200) / 10);
    }
    else if (level >= 300 && level < 400) {
      return 30 + ((level - 300) / 10);
    }
    else if (level >= 400 && level < 500) {
      return 40 + ((level - 400) / 10);
    }
    else if (level >= 500 && level < 600) {
      return 50 + ((level - 500) / 10);
    }
    else if (level >= 600 && level < 700) {
      return 60 + ((level - 600) / 10);
    }
    else if (level >= 700 && level < 800) {
      return 70 + ((level - 700) / 10);
    }
    else if (level >= 800 && level < 900) {
      return 80 + ((level - 800) / 10);
    }
    else if (level >= 900 && level < 1000) {
      return 90 + ((level - 900) / 10);
    }
    else {
      return 100;
    }
  }

  private int getMaxSimultaneousAntiICBMs() {
    return getMaxSimultaneousICBMs() * 2; //Make it a little easier for the humans! :) 
  }

  public synchronized void togglePause() {
    paused = !paused;
  }

  public void keyTyped(KeyEvent ke) {}

  public void keyPressed(KeyEvent ke) {}

  public void keyReleased(KeyEvent ke) {
    Point mPnt;

    if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
      mPnt = getMousePosition();
      fireAntiIcbmMissile((int) mPnt.getX(), (int) mPnt.getY());
    }
  }

}
