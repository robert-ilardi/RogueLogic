/**
 * Created Nov 2, 2007
 */

/*
 Copyright 2007 Robert C. Ilardi

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

package com.roguelogic.sambuca;

import java.util.ArrayList;
import java.util.Iterator;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;

/**
 * The "kernel" of the Sambuca HTTP Framework.
 * It is the class that is used to start a HTTP Server listening
 * on a specific port. Users must initialize this class correctly
 * to create an "embedded" java based HTTP Server.
 * 
 * @author Robert C. Ilardi
 *
 */

public class SambucaHttpServer implements SocketProcessorCustomizer, SocketSessionSweeper {

  private int port;
  private ServiceHandlerFactory handlerFactory;
  private SocketServer sockServer;

  private Object serverLock;
  private Object activeConnsLock;

  private ArrayList<SocketSession> activeConns;

  private Thread monitorThread;
  private boolean monitorRunning;
  private Object monitorLock;

  private SambucaLogger logger;

  private int lingerTimeOverride = -1;
  private boolean reuseAddress = false;

  public SambucaHttpServer() {
    serverLock = new Object();
    activeConnsLock = new Object();
    monitorLock = new Object();
    activeConns = new ArrayList<SocketSession>();
  }

  private Runnable monitor = new Runnable() {
    public void run() {
      synchronized (monitorLock) {
        if (monitorRunning) {
          return;
        }

        monitorRunning = true;
        monitorLock.notifyAll();
      }

      while (sockServer != null && sockServer.isListening()) {
        monitorConnections();

        try {
          Thread.sleep(1000);
        }
        catch (Exception e) {}
      }

      synchronized (monitorLock) {
        monitorRunning = false;
        monitorLock.notifyAll();
      }
    }
  };

  /**
   * 
   * @return The port that the server instance is listening for incoming connections on.
   */
  public int getPort() {
    return port;
  }

  /**
   * Sets the port for the server to use for listening on.
   *  
   * @param port The port for the server to listen for incoming connections on.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * 
   * @return A reference to the user defined ServiceHandlerFactory Implementation.
   */
  public ServiceHandlerFactory getHandlerFactory() {
    return handlerFactory;
  }

  /**
   * Sets the user defined ServiceHandlerFactory Implementation. See the ServiceHandlerFactory interface
   * for more information why a Factory is used instead of simply passing the class name of a ServiceHandler or
   * just a single instance of a ServiceHandler.
   *  
   * @param handlerFactory The ServiceHandlerFactory instance to be used to generate new ServiceHandler instances as needed by the server.
   */
  public void setHandlerFactory(ServiceHandlerFactory handlerFactory) {
    this.handlerFactory = handlerFactory;
  }

  /**
   * A helper method provided so that the calling thread
   * can block and wait while the server is listening. Useful for "main" methods.
   * 
   * @throws InterruptedException
   */
  public void waitWhileListening() throws InterruptedException {
    synchronized (serverLock) {
      while (sockServer.isListening()) {
        serverLock.wait();
      }
    }
  }

  /**
   * This method MUST be called to put the server into listening mode
   * so that the server can start receiving and accepting connections.
   * 
   * @throws RLNetException
   */
  public void listen() throws RLNetException {
    synchronized (serverLock) {
      sockServer = new SocketServer();

      sockServer.setSocketProcessorClass(SambucaHttpServerProcessor.class);
      sockServer.setSocketProcessorCustomizer(this);
      sockServer.setSocketSessionSweeper(this);

      sockServer.setLingerTimeOverride(lingerTimeOverride);
      sockServer.setReuseAddress(reuseAddress);

      sockServer.listen(port);
      print(Version.APP_TITLE + " Running on Port = " + sockServer.getPort());

      monitorThread = new Thread(monitor);
      monitorThread.start();

      serverLock.notifyAll();
    }
  }

  /**
   * This method will cause the server to stop listening
   * and shut all of it's internal threading mechanisms down.
   * It basically shuts down the server.
   * 
   * @throws InterruptedException
   */
  public void shutdown() throws InterruptedException {
    synchronized (serverLock) {
      if (sockServer != null) {
        sockServer.close();
      }

      synchronized (monitorLock) {
        while (monitorRunning) {
          monitorLock.wait();
        }
      }

      serverLock.notifyAll();
    }
  }

  /**
   * This method is the implementation of the same method SocketProcessorCustomizer interface
   * which allows the server to "customize" of initialize a newly created Socket Processor.
   * Socket Processors are "worker" implementations within the RogueLogic Socket Wrapper Framework
   * which are used to perform work on sockets which are read to be read from, since the framework
   * uses non-blocking IO via SocketChannels.
   */
  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    ServiceHandler handler;
    SambucaHttpServerProcessor shsProcessor;

    if (processor instanceof SambucaHttpServerProcessor) {
      shsProcessor = (SambucaHttpServerProcessor) processor;

      handler = handlerFactory.createHandler();
      shsProcessor.setHandler(handler);
      shsProcessor.setSambuca(this);
      shsProcessor.setLogger(logger);
    }
  }

  /**
   * A method to return the maximum Allowed Idled Connection Time. Currently this method
   * returns a static long, but it will be used in the future for when the Sambuca framework
   * allow users to customize security and other server settings.
   * 
   * @return The number of milliseconds that a connection is allowed to remain idled.
   */
  private long getAllowedIdleConnectionTime() {
    return 60000;
  }

  /**
   * This method is used by the monitor thread
   * to perform the monitoring logic on all active connections.
   *
   */
  private void monitorConnections() {
    Long touchTime;
    long curTime = System.currentTimeMillis();
    Iterator<SocketSession> iter;
    SocketSession conn;

    synchronized (activeConnsLock) {
      iter = activeConns.iterator();

      while (iter.hasNext()) {
        conn = iter.next();

        touchTime = (Long) conn.getUserItem(SambucaHttpServerProcessor.USOBJ_TOUCH_TIME);

        if (touchTime == null || (touchTime.longValue() + getAllowedIdleConnectionTime()) <= curTime) {
          try {
            conn.endSession();
            iter.remove();
          }
          catch (Exception e) {}
        }
      }
    }
  }

  /**
   * This method is used by the HTTP SocketProcessor implementation
   * to register next socket connections so that they can be monitored.
   * 
   * @param userSession The RogueLogic socket "connection" wrapper being registered.
   */
  public void registerConnection(SocketSession userSession) {
    synchronized (activeConnsLock) {
      activeConns.add(userSession);
    }
  }

  /**
   * 
   * @return A reference to the SambucaLogger being used by the server.
   */
  public SambucaLogger getLogger() {
    return logger;
  }

  /**
   * This method sets the instance of the Sambuca Logger implementation to be used to log events.
   *  
   * @param logger The instance of the SambucaLogger to be used by the server.
   */
  public void setLogger(SambucaLogger logger) {
    this.logger = logger;
  }

  /**
   * Helper method to log string messages as LogMessages written to the log by the SambucaLogger implementation.
   * Will use System.out to print the messages if no logger exists.
   * 
   * @param mesg The string message to be logged.
   */
  private void print(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.sambucaLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  /**
   * This method is the implementation of the same method in the SocketSessionSweeper interface.
   * It is used by the server to correctly close down a Client Connection. socketSessionSweeper
   * implementations are used by the RogueLogic Socket Wrapper Framework so that if the
   * Socket "connection" wrapper objects which may contain name-value pairs contain values
   * of complex objects which have special or specific methods to clean them up which MUST
   * be invoked manually (not by the garbage collector), there is a user definable hook
   * which can clean up these objects properly. This is the method hook for this cleanup process.
   */
  public void cleanup(SocketSession userSession) {
    logClosingConnection(userSession);
  }

  /**
   * A helper method to log the connection close event.
   * 
   * @param userSession The RogueLogic Socket "connection" wrapper that is being closed.
   */
  private void logClosingConnection(SocketSession userSession) {
    LogMessage lMesg = new LogMessage();
    StringBuffer sb = new StringBuffer();

    sb.append("Connection ");

    sb.append(userSession.getRemoteAddress());
    sb.append(":");
    sb.append(userSession.getRemotePort());

    sb.append(" Closed");

    lMesg.setMessage(sb.toString());
    lMesg.setCode("ConnectionClosed");

    if (logger != null) {
      logger.sambucaLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  /**
   * 
   * @return The number of seconds this instance of the server uses to override the Socket Lingering time.
   */
  public int getLingerTimeOverride() {
    return lingerTimeOverride;
  }

  /**
   * The default time for socket lingering should really be left alone, but if you really want to change it
   * use this method... The default is -1 which means use the default.
   * @param lingerTimeOverride - Number of seconds to override the default socket lingering time. 
   */
  public void setLingerTimeOverride(int lingerTimeOverride) {
    this.lingerTimeOverride = lingerTimeOverride;
  }

  /**
   * 
   * @return If the server will reuse the socket address.
   */
  public boolean isReuseAddress() {
    return reuseAddress;
  }

  /**
   * 
   * @param reuseAddress Set to true if you want the server to reuse a socket address. The default is false.
   */
  public void setReuseAddress(boolean reuseAddress) {
    this.reuseAddress = reuseAddress;
  }

}
