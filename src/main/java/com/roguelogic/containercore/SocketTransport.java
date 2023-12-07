package com.roguelogic.containercore;

import java.util.Properties;

import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSessionSweeper;

public abstract class SocketTransport implements Transport {

  public static final String PROPS_PORT = "Port";
  public static final String PROPS_INIT_WORKERS = "InitWorkers";
  public static final String PROPS_MAX_WORKERS = "MaxWorkers";

  private SocketServer server;

  protected CTMediator ctMediator;

  private Properties props;
  private int port;

  public SocketTransport() {
    port = -1;
    server = new SocketServer();
  }

  public void transportStart() throws TransportLayerException {
    if (port <= 0) {
      throw new TransportLayerException("Can NOT Start Socket Transport Layer! Port MUST Be Specified. (Port MUST be greater than 0)...");
    }

    try {
      server.listen(port);
      System.out.println("Socket Transport Layer Server Listening on Port = " + server.getPort());
    }
    catch (Exception e) {
      throw new TransportLayerException("An error occurred while attempting to Start Socket Transport Layer! Server Port = " + port, e);
    }
  }

  public void transportStop() throws TransportLayerException {
    server.close();
  }

  public void setTransportProperties(Properties props) {
    this.props = props;
  }

  public void initTransport() throws TransportLayerException {
    int port, initWorkerCnt, maxWorkerCnt;
    String tmp;

    tmp = props.getProperty(PROPS_PORT); //Required
    if (tmp != null) {
      port = Integer.parseInt(tmp);
      setPort(port);
    }
    else {
      throw new TransportLayerException("SocketTransport> " + PROPS_PORT + "Parameter MUST be Specified!");
    }

    tmp = props.getProperty(PROPS_INIT_WORKERS); //Required
    if (tmp != null) {
      initWorkerCnt = Integer.parseInt(tmp);
      setInitialWorkers(initWorkerCnt);
    }
    else {
      throw new TransportLayerException("SocketTransport> " + PROPS_INIT_WORKERS + " Parameter MUST be Specified!");
    }

    tmp = props.getProperty(PROPS_MAX_WORKERS); //Optional
    if (tmp != null) {
      maxWorkerCnt = Integer.parseInt(tmp);
      setMaxWorkers(maxWorkerCnt);
    }
  }

  public void setCTMediator(CTMediator ctMediator) {
    this.ctMediator = ctMediator;
  }

  public void setInitialWorkers(int initWorkersCnt) {
    server.setInitialWorkersCnt(initWorkersCnt);
  }

  public void setMaxWorkers(int maxWorkersCnt) {
    server.setMaxWorkersCnt(maxWorkersCnt);
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setSocketProcessorClass(Class sockProcessorClass) {
    server.setSocketProcessorClass(sockProcessorClass);
  }

  public void setSocketSessionSweeper(SocketSessionSweeper sweeper) {
    server.setSocketSessionSweeper(sweeper);
  }

  public void setSocketProcessorCustomizer(SocketProcessorCustomizer customizer) {
    server.setSocketProcessorCustomizer(customizer);
  }

}
