/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.hapair;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import com.roguelogic.clustering.ipc.IpcChainProcess;
import com.roguelogic.clustering.ipc.IpcEvent;
import com.roguelogic.clustering.ipc.IpcProcess;
import com.roguelogic.clustering.ipc.IpcProcessHost;
import com.roguelogic.net.RLNetException;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 * 
 *         This class allows a process to be paired with a failover process. It
 *         is configured via the following JVM system properties. This is so you
 *         can configure processes via the command line for easy Autosys
 *         Integration or other Unix Scheduling and Scripting.
 * 
 *         For a process to pair correct the following sequence of methods needs
 *         to be called in THIS order on an instance of HaPairConnector:
 * 
 *         0. Constructor Call: HaPairConnector(IpcProcessHost ipcHost) +
 *         Creates a HaPairConnector that will use an ALREADY initialized
 *         IpcProcessHost for communications...
 * 
 *         1. init() or init(Properties) + Initializes the HA Pair Connector
 * 
 *         2. waitForPeer() + Waits for HA Peer Process to become available. May
 *         time out based on configuration in call init(). If time's out, THIS
 *         process will be automatically started in Active Mode.
 * 
 *         3. determineGoFirst() + If waitForPeer() did NOT time out. This will
 *         exchange information with it's peer to determine who should be in
 *         active mode first! Does nothing if already in Active Mode.
 * 
 *         4. startMonitor() + If NOT in Active Mode, will start the Heart Beat
 *         Monitor Thread. (Actually the thread sends Heart Beat Requests to the
 *         Active Process.) This thread will determine if the Peer Process is
 *         down and change THIS process's mode from Hot-Standby to Active. Does
 *         nothing if already in Active Mode. (No reason to start monitoring
 *         yourself!)
 * 
 *         5. blockWhileNotActive() + Blocks until Active Mode is Achieved or
 *         The Montior Thread is shutdown. If you need to abort a method that is
 *         blocking on this call if the Monitor Thread was shutdown and active
 *         mode was not achieved, you should call isActive(). If !isActive()
 *         your method should abort.... Does nothing is already in Active Mode
 *         or stopMonitor() was called.
 * 
 * 
 *         JVM System Properties -
 * 
 *         nodeIndex -
 * 
 *         Syntax: -DhapNodeIndex=[INTEGER]
 * 
 *         Description: Used to set the process number. When two processes are
 *         started at the same time. The will communicate with each other and
 *         use the node index to to automatically determine which process should
 *         start in an active state and which process should start in a
 *         hot-standby state. A process that is started with a lower node index
 *         will start in active state, while a process that is started with a
 *         higher node index of a pair will be started in hot-standby.
 * 
 * 
 *         hapPeerName -
 * 
 *         Syntax: -DhapPeerName=[HA_PAIR_PEER_IPC_PROCESS_NAME]
 * 
 *         Description: This is used to set the associated HA Pair Peer Process
 *         of this process using the AMG IPC Framework's Process Naming Service.
 * 
 *         hapMonitorSleepSecs -
 * 
 *         Syntax: -DhapMonitorSleepSecs=[INTEGER]
 * 
 *         Description: Number of Seconds between each Heart Beat Request.
 * 
 * 
 *         hapInitPeerConnectTryCnt -
 * 
 *         Syntax: -DhapInitPeerConnectTryCnt=[INTEGER]
 * 
 *         Description: Sets the number of tries between peer connection
 *         attempts in the waitForPeer() method.
 * 
 * 
 *         hapInitPeerConnectTrySleepSecs -
 * 
 *         Syntax: -DhapInitPeerConnectTrySleepSecs=[INTEGER]
 * 
 *         Description: Sets the amount of time in seconds between each peer
 *         connection attempt in the waitForPeer() method.
 * 
 * 
 *         hapLastHeartBeatTTL -
 * 
 *         Syntax: -DhapLastHeartBeatTTL=[LONG]
 * 
 *         Description: Sets the amount of time in milliseconds that the Last
 *         Received Heart Beat is valid for. If the last received heart beat's
 *         time is older than it's TTL then the heart beat is invalid and the
 *         system assumes the Peer Process is DOWN! This will cause Active Mode
 *         to become TRUE and blockWhileNotActive to return...
 * 
 */

public class HaPairConnector implements IpcChainProcess {

  public static final String PROP_NODE_INDEX = "hapNodeIndex";
  public static final String PROP_PEER_NAME = "hapPeerName";
  public static final String PROP_MONITOR_SLEEP_SECONDS = "hapMonitorSleepSecs";
  public static final String PROP_INIT_PEER_CONNECT_TRY_CNT = "hapInitPeerConnectTryCnt";
  public static final String PROP_INIT_PEER_CONNECT_TRY_SLEEP_SECONDS = "hapInitPeerConnectTrySleepSecs";
  public static final String PROP_LAST_HEART_BEAT_TTL = "hapLastHeartBeatTTL";

  private IpcProcessHost ipcHost;
  private IpcProcess previousProcess;

  private String peerName;
  private int nodeIndex;
  private int monSleepSecs;
  private int initPeerConnectTryCnt;
  private int initPeerConnectTrySleepSecs;
  private long lastHeartBeatTTL;

  private Thread hbMonThread;

  private Object hbmLock;
  private boolean runMonitor;
  private boolean monitoringHb;
  private boolean stoppedCalled;

  private boolean activeMode;
  private boolean receivedHeartBeatReply;
  private long lastHeartBeatReceived;
  private int peerNodeIndex;
  private int peerNodeMode;

  public HaPairConnector(IpcProcessHost ipcHost) {
    hbmLock = new Object();
    this.ipcHost = ipcHost;

    ipcHost.chainProcesses(this);
  }

  /**
   * Call to init using System Properties Internally calls
   * init(System.getProperties());
   * 
   */
  public void init() throws HaPairException {
    init(System.getProperties());
  }

  /**
   * Call to init using properties from argument...
   * 
   * @param props
   *          - Properties to init from
   */
  public void init(Properties props) throws HaPairException {
    String tmp;

    activeMode = false;

    peerName = props.getProperty(PROP_PEER_NAME);

    if (StringUtils.IsNVL(peerName)) {
      throw new HaPairException("hapPeerName can NOT be NULL or Empty!");
    }

    tmp = props.getProperty(PROP_NODE_INDEX);

    if (StringUtils.IsNVL(tmp)) {
      throw new HaPairException("hapNodeIndex can NOT be NULL or Empty!");
    }

    if (!StringUtils.isNumeric(tmp)) {
      throw new HaPairException("hapNodeIndex MUST be Numeric!");
    }

    nodeIndex = Integer.parseInt(tmp);

    tmp = props.getProperty(PROP_MONITOR_SLEEP_SECONDS);

    if (StringUtils.IsNVL(tmp)) {
      throw new HaPairException("hapMonitorSleepSecs can NOT be NULL or Empty!");
    }

    if (!StringUtils.isNumeric(tmp)) {
      throw new HaPairException("hapMonitorSleepSecs MUST be Numeric!");
    }

    monSleepSecs = Integer.parseInt(tmp);

    tmp = props.getProperty(PROP_INIT_PEER_CONNECT_TRY_CNT);

    if (StringUtils.IsNVL(tmp)) {
      throw new HaPairException("hapInitPeerConnectTryCnt can NOT be NULL or Empty!");
    }

    if (!StringUtils.isNumeric(tmp)) {
      throw new HaPairException("hapInitPeerConnectTryCnt MUST be Numeric!");
    }

    initPeerConnectTryCnt = Integer.parseInt(tmp);

    tmp = props.getProperty(PROP_INIT_PEER_CONNECT_TRY_SLEEP_SECONDS);

    if (StringUtils.IsNVL(tmp)) {
      throw new HaPairException("hapInitPeerConnectTrySleepSecs can NOT be NULL or Empty!");
    }

    if (!StringUtils.isNumeric(tmp)) {
      throw new HaPairException("hapInitPeerConnectTrySleepSecs MUST be Numeric!");
    }

    initPeerConnectTrySleepSecs = Integer.parseInt(tmp);

    tmp = props.getProperty(PROP_LAST_HEART_BEAT_TTL);

    if (StringUtils.IsNVL(tmp)) {
      throw new HaPairException("hapLastHeartBeatTTL can NOT be NULL or Empty!");
    }

    if (!StringUtils.isNumeric(tmp)) {
      throw new HaPairException("hapLastHeartBeatTTL MUST be Numeric!");
    }

    lastHeartBeatTTL = Long.parseLong(tmp);
  }

  public void setPreviousProcessLink(IpcProcess previousProcess) {
    this.previousProcess = previousProcess;
  }

  public boolean handleAmgIpcEvent(IpcEvent event) {
    Serializable payload;
    boolean retCd;

    try {
      payload = event.getData();

      if (payload instanceof HaHeartBeat) {
        handleHeartBeat((HaHeartBeat) payload);
        retCd = true;
      }
      else if (previousProcess != null) {
        retCd = previousProcess.handleAmgIpcEvent(event);
      }
      else {
        retCd = true;
      }
    } // End try block
    catch (Exception e) {
      retCd = false;
      e.printStackTrace();
    }

    return retCd;
  }

  private void handleHeartBeat(HaHeartBeat beat) throws RLNetException, IOException, InterruptedException {
    switch (beat.getType()) {
      case HaHeartBeat.HB_TYPE_REQUEST:
        sendReply(beat);
        break;
      case HaHeartBeat.HB_TYPE_REPLY:
        processReply(beat);
        break;
    }
  }

  private void sendReply(HaHeartBeat reqBeat) throws RLNetException, IOException, InterruptedException {
    IpcEvent event;
    HaHeartBeat beat;

    event = new IpcEvent();
    event.setTargetProcessName(reqBeat.getSendingNodeName());

    beat = new HaHeartBeat();
    event.setData(beat);

    beat.setType(HaHeartBeat.HB_TYPE_REPLY);
    beat.setSendingNodeIndex(nodeIndex);
    beat.setSendingNodeMode((activeMode ? HaHeartBeat.NODE_MODE_ACTIVE : HaHeartBeat.NODE_MODE_HOT_STANDBY));
    beat.setSendingNodeName(ipcHost.getProcessName());

    beat.setEchoMesg(reqBeat.getEchoMesg());
    beat.setSystemTime(System.currentTimeMillis());

    ipcHost.send(event);
  }

  public void reset() {
    stoppedCalled = false;
    activeMode = false;
  }

  public void startMonitor() throws InterruptedException {
    if (activeMode || stoppedCalled) {
      return;
    }

    synchronized (hbmLock) {
      if (!monitoringHb) {
        runMonitor = true;

        hbMonThread = new Thread(hbMonRunner);
        hbMonThread.start();

        while (!monitoringHb) {
          hbmLock.wait();
        }
      }
    }
  }

  public void stopMonitor() throws InterruptedException {
    synchronized (hbmLock) {
      System.out.println("HaPairConnector> Stopping Heart Beat Monitor...");

      stoppedCalled = true;

      if (monitoringHb) {
        runMonitor = false;

        while (monitoringHb) {
          hbmLock.wait();
        }
      }
    }
  }

  public void waitForPeer() {
    System.out.println("HaPairConnector> Waiting a while for High Availablity Peer Process to become available...");

    activeMode = true;

    for (int i = 1; i <= initPeerConnectTryCnt; i++) {
      System.out.println("HaPairConnector> Attempting to Connect to High Availablity Peer Process (Try " + i + " of " + initPeerConnectTryCnt + ")...");

      if (ipcHost.isProcessAvailable(peerName)) {
        activeMode = false;
        break;
      }
      else if (stoppedCalled) {
        activeMode = false;
        return;
      }
      else {
        SystemUtils.Sleep(initPeerConnectTrySleepSecs);
      }
    }

    if (activeMode) {
      System.out.println("HaPairConnector> Could NOT connect to Peer HA Process after maximum configured attempts. Process will automatically enter Active Mode!");
    }
    else {
      System.out.println("HaPairConnector> Connected to Peer HA Process! Will determine Active/Hot-Standby based on blockWhileNotActive()...");
    }
  }

  public void blockWhileNotActive() throws InterruptedException {
    if (stoppedCalled) {
      return;
    }

    System.out.println("HaPairConnector> Waiting to become the Active Process...");

    synchronized (hbmLock) {
      while (!stoppedCalled && !activeMode) {
        hbmLock.wait();
      }
    }

    if (activeMode) {
      System.out.println("HaPairConnector> Active Mode Achieved!");
    }
  }

  public boolean isActiveMode() {
    return activeMode;
  }

  public void determineGoFirst() throws RLNetException, IOException, InterruptedException {
    System.out.println("HaPairConnector> Determining Active Mode Go First...");

    if (activeMode || stoppedCalled) {
      return;
    }

    synchronized (hbmLock) {
      sendHeartBeatRequest();

      while (!receivedHeartBeatReply && !stoppedCalled) {
        hbmLock.wait();
      }
    }

    activeMode = peerNodeMode != HaHeartBeat.NODE_MODE_ACTIVE && peerNodeIndex > nodeIndex;
  }

  private void sendHeartBeatRequest() throws RLNetException, IOException, InterruptedException {
    IpcEvent event;
    HaHeartBeat beat;

    event = new IpcEvent();
    event.setTargetProcessName(peerName);

    beat = new HaHeartBeat();
    event.setData(beat);

    beat.setType(HaHeartBeat.HB_TYPE_REQUEST);
    beat.setSendingNodeIndex(nodeIndex);
    beat.setSendingNodeMode((activeMode ? HaHeartBeat.NODE_MODE_ACTIVE : HaHeartBeat.NODE_MODE_HOT_STANDBY));
    beat.setSendingNodeName(ipcHost.getProcessName());

    receivedHeartBeatReply = false;
    ipcHost.send(event);

    // A quick hack to give enough time for the reply to happen. because we
    // don't want this method to block!
    SystemUtils.SleepTight(500);
  }

  private void processReply(HaHeartBeat beat) {
    synchronized (hbmLock) {
      receivedHeartBeatReply = true;

      lastHeartBeatReceived = System.currentTimeMillis();
      peerNodeIndex = beat.getSendingNodeIndex();
      peerNodeMode = beat.getSendingNodeMode();

      hbmLock.notifyAll();
    }
  }

  private boolean peerProcessDown() throws RLNetException, IOException, InterruptedException {
    boolean down;

    // Let's make sure...
    synchronized (hbmLock) {
      if (ipcHost.isProcessAvailable(peerName)) {
        sendHeartBeatRequest();

        while (!receivedHeartBeatReply) {
          hbmLock.wait((monSleepSecs * 1000L));
        }
      }

      down = (!stoppedCalled && (System.currentTimeMillis() > (lastHeartBeatReceived + lastHeartBeatTTL)));
    }

    return down;
  }

  private Runnable hbMonRunner = new Runnable() {
    public void run() {
      try {
        synchronized (hbmLock) {
          monitoringHb = true;
          hbmLock.notifyAll();
        }

        lastHeartBeatReceived = System.currentTimeMillis();

        while (runMonitor) {
          try {
            if (peerProcessDown()) {
              activeMode = true;
              break; // We don't need to monitor anymore. THIS Process is now
              // active!
            }
            else {
              SystemUtils.Sleep(monSleepSecs);
            }
          } // end try block
          catch (Exception e) {
            System.err.println("HaPairConnector> Error in HA Monitor Thread. Will Log and Continue...");
            e.printStackTrace();
          }
        } // End while(runMonitor)
      }// End try block
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        synchronized (hbmLock) {
          monitoringHb = false;
          hbmLock.notifyAll();
        }
      }
    }
  };

}
