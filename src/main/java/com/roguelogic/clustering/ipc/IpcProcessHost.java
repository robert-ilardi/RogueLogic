/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

import java.io.IOException;
import java.util.HashMap;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.util.InvalidValueException;
import com.roguelogic.util.StringUtils;
import com.roguelogic.workers.InvalidWorkerClassException;
import com.roguelogic.workers.WorkerException;
import com.roguelogic.workers.WorkerPool;
import com.roguelogic.workers.WorkerPoolException;

/**
 * @author Robert C. Ilardi
 * 
 */

public class IpcProcessHost implements SocketProcessorCustomizer {

  public static final int DEFAULT_INITIAL_WORKER_THREADS = 10;
  public static final int DEFAULT_MAX_WORKER_THREADS = 20;

  private String processName;
  private IpcProcess process;
  private IpcPeerRegistry peerRegistry;

  private SocketServer sockServer;
  private Object serverLock;
  private int port;

  private Object ipcAckMapLock;
  private HashMap<String, Integer> ipcAckMap;

  private WorkerPool parallelSendWorkers;

  private int initialParallelSendWorkersCnt;
  private int maxParallelSendWorkersCnt;

  public IpcProcessHost(String processName, String peerRegistryFile, IpcProcess process) throws IOException {
    this(processName, process);

    peerRegistry = IpcPeerRegistry.LoadRegistryFile(this, peerRegistryFile);

    assignPort();
  }

  public IpcProcessHost(String processName, IpcPeerRegistry peerRegistry, IpcProcess process) {
    this(processName, process);

    this.peerRegistry = peerRegistry;

    assignPort();
  }

  private IpcProcessHost(String processName, IpcProcess process) {
    this.processName = processName;
    this.process = process;

    serverLock = new Object();
    ipcAckMapLock = new Object();

    ipcAckMap = new HashMap<String, Integer>();

    initialParallelSendWorkersCnt = DEFAULT_INITIAL_WORKER_THREADS;
    maxParallelSendWorkersCnt = DEFAULT_MAX_WORKER_THREADS;
  }

  private void assignPort() {
    IpcPeer peer;

    peer = peerRegistry.getPeer(processName);
    port = peer.getPort();
  }

  private synchronized void createParallelSendWorkerPool() throws WorkerPoolException, InvalidWorkerClassException, InvalidValueException {
    if (parallelSendWorkers == null) {
      parallelSendWorkers = new WorkerPool("ParallelSendWorkers");
      parallelSendWorkers.registerWorkerClass(IpcParallelSendWorker.class);
      parallelSendWorkers.setShrinkable(true);
      parallelSendWorkers.setUseThreads(true);
      parallelSendWorkers.setInitialSize(initialParallelSendWorkersCnt);
      parallelSendWorkers.setMaxSize(maxParallelSendWorkersCnt);
      parallelSendWorkers.createPool();
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    IpcProcessHostSockProcessor aiphsp = (IpcProcessHostSockProcessor) processor;
    aiphsp.setProcessHost(this);
  }

  private void listen() throws RLNetException {
    synchronized (serverLock) {
      sockServer = new SocketServer();

      sockServer.setSocketProcessorClass(IpcProcessHostSockProcessor.class);
      sockServer.setSocketProcessorCustomizer(this);

      sockServer.listen(port);
    }
  }

  private void shutdownServer() {
    synchronized (serverLock) {
      if (sockServer != null) {
        sockServer.close();
      }
    }
  }

  private void shutdownParalleSendWorkers() throws WorkerPoolException {
    if (parallelSendWorkers != null) {
      parallelSendWorkers.destroyPool();
      parallelSendWorkers = null;
    }
  }

  /**
   * Call this method to join an IPC conversation with other processes. This
   * MUST be called once before any send methods are called.
   * 
   * @throws RLNetException
   */
  public void join() throws RLNetException {
    System.out.println("IpcProcessHost> Joining IPC Conversation...");

    synchronized (serverLock) {
      listen();

      serverLock.notifyAll();
    }
  }

  /**
   * Call this method to leave (disconnect from) an IPC conversation.
   * 
   * @throws InterruptedException
   */
  public void leave() throws InterruptedException, WorkerPoolException {
    System.out.println("IpcProcessHost> Leaving IPC Conversation...");

    synchronized (serverLock) {
      shutdownParalleSendWorkers();
      peerRegistry.closeAll();
      shutdownServer();

      serverLock.notifyAll();
    }

  }

  /**
   * Sends an IPC Event to a target process. Automatically sets the
   * originatingProcessName and the eventId in the AmgIpcEvent argument.
   * 
   * @param event
   * @return Returns TRUE if event's requiresIpcAck is FALSE OR if
   *         requiresIpcAck is TRUE AND the return status from the target
   *         process is also TRUE.
   * @throws RLNetException
   * @throws IOException
   * @throws InterruptedException
   */
  public boolean send(IpcEvent event) throws RLNetException, IOException, InterruptedException {
    IpcPeer peer;
    Integer status;
    boolean ackNack;
    String eventId;

    event.setOriginatingProcessName(processName);

    eventId = getNextEventId();
    event.setEventId(eventId);

    peer = peerRegistry.getPeer(event.getTargetProcessName());
    peer.sendTo(event);

    if (event.isRequiresIpcAck()) {
      synchronized (ipcAckMapLock) {
        // ipcAckMap.remove(eventId);

        while (!ipcAckMap.containsKey(eventId)) {
          ipcAckMapLock.wait();
        }

        status = ipcAckMap.remove(eventId);
        ackNack = (status != null && status.intValue() == IpcConstants.IPC_ACK);
      } // End synchronized block
    }
    else {
      // No IPC level acknowledgement required, so always return true!
      ackNack = true;
    }

    return ackNack;
  }

  public boolean handleIncomingEvent(IpcEvent event) {
    return process.handleAmgIpcEvent(event);
  }

  public void processIpcAck(CommandDataPair cmDatPair) {
    String eventId;
    Integer status;

    synchronized (ipcAckMapLock) {
      status = new Integer(cmDatPair.getStatusCode());
      eventId = cmDatPair.getString();

      ipcAckMap.put(eventId, status);

      ipcAckMapLock.notifyAll();
    }
  }

  private String getNextEventId() {
    return new StringBuffer().append(processName).append(".").append(StringUtils.GenerateTimeUniqueIdNoDelay()).toString();
  }

  public int getInitialParallelSendWorkersCnt() {
    return initialParallelSendWorkersCnt;
  }

  public void setInitialParallelSendWorkersCnt(int initialParallelSendWorkersCnt) {
    this.initialParallelSendWorkersCnt = initialParallelSendWorkersCnt;
  }

  public int getMaxParallelSendWorkersCnt() {
    return maxParallelSendWorkersCnt;
  }

  public void setMaxParallelSendWorkersCnt(int maxParallelSendWorkersCnt) {
    this.maxParallelSendWorkersCnt = maxParallelSendWorkersCnt;
  }

  public String parallelSend(final IpcEvent event) throws WorkerException, WorkerPoolException, InvalidWorkerClassException, InvalidValueException {
    IpcPeer peer;
    String eventId;
    IpcParallelSendWorkerParam wParam;

    createParallelSendWorkerPool();

    event.setOriginatingProcessName(processName);
    eventId = getNextEventId();
    event.setEventId(eventId);

    peer = peerRegistry.getPeer(event.getTargetProcessName());

    wParam = new IpcParallelSendWorkerParam();
    wParam.setPeer(peer);
    wParam.setEvent(event);

    parallelSendWorkers.performWork(wParam);

    return eventId;
  }

  public boolean isProcessAvailable(String processName) {
    IpcPeer peer;

    peer = peerRegistry.getPeer(processName);

    return peer.isAvailable();
  }

  public void chainProcesses(IpcChainProcess chainProcess) {
    chainProcess.setPreviousProcessLink(process);
    process = chainProcess;
  }

  public String getProcessName() {
    return processName;
  }

  public void processLostConnection(String eventId) {
    synchronized (ipcAckMapLock) {
      ipcAckMap.put(eventId, IpcConstants.IPC_NACK);

      ipcAckMapLock.notifyAll();
    }
  }

}
