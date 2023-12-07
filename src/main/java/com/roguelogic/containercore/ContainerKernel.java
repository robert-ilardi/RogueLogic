/**
 * Created 4/15/2006 
 */
package com.roguelogic.containercore;

import java.util.ArrayList;
import java.util.Date;

import com.roguelogic.ipcp2p.IPCException;
import com.roguelogic.ipcp2p.IPCMessage;
import com.roguelogic.ipcp2p.IPCP2PService;
import com.roguelogic.ipcp2p.IPCPeer;
import com.roguelogic.util.OutOfUniqueIdsException;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class ContainerKernel {

  private Container container;
  private ArrayList<Transport> transports;
  private CTMediator ctMediator;
  private IPCP2PService clusteringService;
  private ContainerSessionsManager sessionsManager;

  private Date lastBootTs;
  private Date uptimeTs;

  public ContainerKernel() {
    transports = new ArrayList<Transport>();
    sessionsManager = new ContainerSessionsManager(this);
  }

  public void setContainer(Container container) {
    this.container = container;
  }

  public void addTransport(Transport transport) {
    transports.add(transport);
  }

  public void setClusterOptions(int port, String connectionKey) {
    //If the options are set, the service will be started on boot...
    clusteringService = new IPCP2PService();

    clusteringService.setPort(port);
    clusteringService.setConnectionKey(connectionKey);
  }

  public void addClusterPeer(IPCPeer peer) {
    clusteringService.addPeer(peer);
  }

  public void boot() throws ContainerKernelException, ContainerException, TransportLayerException, IPCException {
    lastBootTs = new Date();
    System.out.println("Container Kernel Boot at: " + lastBootTs);

    if (container == null) {
      throw new ContainerKernelException("Container Kernel can NOT Boot with NULL Container object!");
    }

    if (transports.size() == 0) {
      throw new ContainerKernelException("Container Kernel needs at least ONE Transport Installed for Boot!");
    }

    //Create Container-Transports Mediator
    System.out.println("Setting Up Container-Transport Layer Mediation...");
    ctMediator = new CTMediator(container, transports);
    container.setCTMediator(ctMediator);
    for (Transport transport : transports) {
      transport.setCTMediator(ctMediator);
    }

    //Start Container
    System.out.println("Starting Container...");
    container.setContainerKernel(this);
    container.containerStart();

    //Start Clustering Service
    if (clusteringService != null) {
      System.out.println("Clustering Enabled - Joining P2P Environment...");
      clusteringService.addDataObserver(sessionsManager);
      clusteringService.start();
    }

    //Bring Up Transports
    System.out.println("Starting Transport Layer...");
    for (Transport transport : transports) {
      transport.transportStart();
    }

    uptimeTs = new Date();
    System.out.println("Container Kernel in Running Mode at: " + uptimeTs + "\n");
  }

  public void shutdown() throws ContainerException, TransportLayerException {
    System.out.println("Container Kernel Shutdown at: " + StringUtils.GetTimeStamp());

    if (clusteringService != null) {
      System.out.println("Stopping Clustering Service - Disconnecting from P2P Environment...");
      clusteringService.stop();
    }

    if (container != null) {
      System.out.println("Stopping Container...");

      container.containerStop();
    }

    if (transports.size() > 0) {
      System.out.println("Stopping Transport Layer...");

      for (Transport transport : transports) {
        transport.transportStop();
      }
    }

    System.out.println("Removing All Container Sessions...");
    sessionsManager.removeAll();
  }

  public void clusterBroadcast(IPCMessage mesg) throws IPCException {
    if (clusteringService != null) {
      clusteringService.broadcast(mesg);
    }
  }

  public ContainerSession createSession() throws OutOfUniqueIdsException {
    return sessionsManager.createSession();
  }

  public ContainerSession getSession(String sessionId) {
    return sessionsManager.getSession(sessionId);
  }

  public void terminateSession(String sessionId) {
    sessionsManager.remove(sessionId);
  }

  public boolean reestablishSession(String sessionId) {
    return sessionsManager.reestablishSession(sessionId);
  }

}
