/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.toc;

/**
 * @author Administrator
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

import com.roguelogic.im.BuddyStatus;
import com.roguelogic.im.IMException;
import com.roguelogic.im.InstantMessage;
import com.roguelogic.im.sflap.SFLAPDataFrame;
import com.roguelogic.im.sflap.SFLAPFrame;
import com.roguelogic.im.sflap.SFLAPFrameException;
import com.roguelogic.im.sflap.SFLAPInputFrame;
import com.roguelogic.im.sflap.SFLAPKeepAliveFrame;
import com.roguelogic.im.sflap.SFLAPSignonFrame;
import com.roguelogic.util.ByteBuffer;
import com.roguelogic.util.StringUtils;

public class TOCConnection {

  public static final int MIN_SEND_POINTS = 5;
  public static final int MAX_SEND_POINTS = 10;
  public static final int SEND_POINT_RECOVERY_INTERVAL = 2200;
  public static final int KEEP_ALIVE_INTERVAL = 60000;
  public static final int MESG_PROCESSOR_SLEEP_INTERVAL = 100;
  public static final int MIN_POINT_WAIT_INTERVAL = 5; //In Seconds

  public static final int MAX_BUFFER = 2048;

  private String host;
  private int port;

  private Socket socket;
  private OutputStream outs;
  private InputStream ins;

  private Thread readerThread;
  private Thread processorThread;

  private boolean loginComplete;
  private boolean connected;
  private long lastKeepAlive;
  private int sendPoints;
  private long lastImSent;

  private int clientSequence;
  private int serverSequence;

  private boolean loggedIn;
  private String username;

  private ByteBuffer messageBuffer;
  private ArrayList messageQueue;

  private ArrayList tocObservers;

  private Runnable sockReader = new Runnable() {
    public void run() {
      SFLAPInputFrame inputFrame;

      try {
        inputFrame = readSFLAPFrame();
        while (connected && inputFrame != null) {
          //System.out.println(new String(inputFrame.getFrameData(), 0, inputFrame.getFrameLen()));
          synchronized (messageQueue) {
            messageQueue.add(inputFrame);
          }
          inputFrame = readSFLAPFrame();
        }
      }
      catch (Exception e) {
        if (e instanceof SocketException) {
          if (connected) {
            e.printStackTrace();
          }
        }
        else {
          e.printStackTrace();
        }
        disconnect();
      }
    }
  };

  private Runnable mesgProcessor = new Runnable() {
    public void run() {
      SFLAPInputFrame inputFrame;

      try {
        while (connected) {
          inputFrame = null;
          synchronized (messageQueue) {
            if (!messageQueue.isEmpty()) {
              inputFrame = (SFLAPInputFrame) messageQueue.remove(0);
            }
          }
          process(inputFrame);
          Thread.sleep(MESG_PROCESSOR_SLEEP_INTERVAL);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        disconnect();
      }
    }
  };

  public TOCConnection(String host, int port) {
    this.host = host;
    this.port = port;

    username = null;
    loggedIn = false;
    connected = false;
    clientSequence = 0;
    serverSequence = 0;
    sendPoints = MAX_SEND_POINTS;

    messageBuffer = new ByteBuffer();
    messageQueue = new ArrayList();

    tocObservers = new ArrayList();
  }

  public void connect() throws IOException, SFLAPFrameException, IMException {
    SFLAPInputFrame inputFrame;
    SFLAPSignonFrame signonFrame;

    messageBuffer.clear();
    messageQueue.clear();
    inputFrame = new SFLAPInputFrame();

    //Open Socket Connection
    socket = new Socket(host, port);
    ins = socket.getInputStream();
    outs = socket.getOutputStream();

    //Send FLAP Handshake "Greeting"
    outs.write(SFLAPFrame.FLAP_GREETING.getBytes());
    outs.flush();

    //Read Respose FLAP Frame
    inputFrame = readSFLAPFrame();
    if (inputFrame.getFrameTypeFromBuffer() == SFLAPFrame.FLAP_SIGNON_FRAME) {
      signonFrame = (SFLAPSignonFrame) inputFrame.createDervivedFrame();

      //Get Initial Sequence Numbers
      clientSequence = signonFrame.getSequence();
      serverSequence = clientSequence;

      connected = true;
    }
    else {
      throw new IMException("Did not receive Signon FLAP Frame as excepted by Connect proceedure!");
    }
  }

  public void login(String authHost, int authPort, String username, String password) throws IOException, IMException {
    SFLAPInputFrame inputFrame;
    SFLAPSignonFrame signonFrame;
    TOCSignon signonTOC;
    String normalizedUsername;
    TOCAddBuddy tocAdd;
    TOCInitDone tocInit;
    TOCSetInfo tocInfo;

    normalizedUsername = AOLUtils.FormatUsername(username);
    this.username = username;

    //Send Signon Frame
    signonFrame = new SFLAPSignonFrame();
    signonFrame.setSequence(nextSequence());
    signonFrame.setUsername(normalizedUsername);

    outs.write(signonFrame.getFrameData(), 0, signonFrame.getLength() + 6);
    outs.flush();

    //Send Signon TOC Message
    signonTOC = new TOCSignon(authHost, authPort, username, password);
    send(signonTOC);

    //Wait for SIGN_ON from TOC Server
    inputFrame = readSFLAPFrame();

    if (isSignonResponse(inputFrame)) {
      //Add self to buddy list
      tocAdd = new TOCAddBuddy();
      tocAdd.addBuddy(normalizedUsername);
      send(tocAdd);

      //Send Init Complete!
      tocInit = new TOCInitDone();
      send(tocInit);

      //Set User Info
      tocInfo = new TOCSetInfo();
      send(tocInfo);

      //Start Reader Thread
      readerThread = new Thread(sockReader);
      readerThread.start();

      //Start Processor Thread
      processorThread = new Thread(mesgProcessor);
      processorThread.start();

      lastKeepAlive = System.currentTimeMillis();
      lastImSent = System.currentTimeMillis();
      loggedIn = true;

      System.out.println("Logged in at: " + (new Date()));
    }
    else {
      //System.out.println("Frame(" + inputFrame.getFrameTypeFromBuffer() + "): " + new String(inputFrame.getFrameData(), 0, inputFrame.getLength() + 6));
      disconnect();
      throw new IMException(getErrorMessage(inputFrame));
    }
  }

  public synchronized void disconnect() {

    connected = false;
    loggedIn = false;

    try {
      if (ins != null) {
        ins.close();
        ins = null;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    try {
      if (outs != null) {
        outs.close();
        outs = null;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    try {
      if (socket != null) {
        socket.close();
        socket = null;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Disconnected at: " + (new Date()));

  }

  public void addBuddy(String buddyName) throws IOException {
    TOCAddBuddy tocAdd;
    String normalizedUsername;

    if (buddyName != null && buddyName.trim().length() > 0) {
      normalizedUsername = AOLUtils.FormatUsername(buddyName.trim());

      tocAdd = new TOCAddBuddy();
      tocAdd.addBuddy(normalizedUsername);
      send(tocAdd);
    }
  }

  public void removeBuddy(String buddyName) throws IOException {
    TOCRemoveBuddy tocRemove;
    String normalizedUsername;

    if (buddyName != null && buddyName.trim().length() > 0) {
      normalizedUsername = AOLUtils.FormatUsername(buddyName.trim());

      tocRemove = new TOCRemoveBuddy();
      tocRemove.removeBuddy(normalizedUsername);
      send(tocRemove);
    }
  }

  public boolean isCompleteMessage(byte[] buffer) {
    return (buffer.length > 5) && ((buffer.length - 6) >= SFLAPFrame.GetLength(buffer));
  }

  private SFLAPInputFrame readSFLAPFrame() throws IOException {
    SFLAPInputFrame inputFrame = null;
    byte[] buffer, mesg;
    int len = 0, mesgLen;

    //Check if message is already in the queue
    mesg = messageBuffer.getBytes();
    if (isCompleteMessage(mesg)) {
      mesgLen = SFLAPFrame.GetLength(mesg) + 6;
      inputFrame = new SFLAPInputFrame();
      inputFrame.append(messageBuffer.remove(mesgLen), mesgLen);
    }
    else {
      //Read until complete message is in the queue!
      buffer = new byte[MAX_BUFFER];

      while (len >= 0) {
        len = ins.read(buffer);
        if (len >= 0) {
          messageBuffer.add(buffer, len);
          mesg = messageBuffer.getBytes();

          if (isCompleteMessage(mesg)) {
            mesgLen = SFLAPFrame.GetLength(mesg) + 6;
            inputFrame = new SFLAPInputFrame();
            inputFrame.append(messageBuffer.remove(mesgLen), mesgLen);
            break;
          }
        }
      } //End while (len >= 0)
    }

    return inputFrame;
  }

  private void process(SFLAPInputFrame inputFrame) throws SFLAPFrameException, IOException {
    SFLAPFrame frame;
    String data;

    if (inputFrame != null) {
      frame = inputFrame.createDervivedFrame();
      //System.out.println("Frame(" + frame.getFrameTypeFromBuffer() + "): " + new String(frame.getFrameData(), 0, frame.getLength() + 6));
      switch (frame.getFrameTypeFromBuffer()) {
        case SFLAPFrame.FLAP_DATA_FRAME:
          data = new String(frame.getFrameData(), 6, frame.getLength());

          //Command Switch Block
          if (data.startsWith(TOCConstants.COMMAND_IM_IN)) {
            //IM Received
            processIncomingIM(data);
          }
          else if (data.startsWith(TOCConstants.COMMAND_UPDATE_BUDDY_STATUS)) {
            //Buddy Status Update
            processUpdateBuddyStatus(data);
          }
          else {
            System.out.println("Data(Type = " + frame.getFrameTypeFromBuffer() + " Expected Len = " + frame.getLength() + " Actual Len = " + data.length() + "): " + data);
          }

          break;
        case SFLAPFrame.FLAP_SIGNOFF_FRAME:
          notifyObserversSignoff();
        case SFLAPFrame.FLAP_KEEP_ALIVE_FRAME:
          //sendKeepAlive();
          break;
      }
    } //End NULL Input Frame Check

  }

  private int nextSequence() {
    int sequence = clientSequence++;

    if (clientSequence > 65535) {
      clientSequence = 0;
    }

    return sequence;
  }

  public void sendKeepAlive() throws IOException {
    SFLAPKeepAliveFrame frame = new SFLAPKeepAliveFrame();
    frame.setSequence(nextSequence());
    frame.setLength(0);
    //System.out.println("Sending Keep Alive: " + new String(frame.getFrameData(), 0, frame.getLength() + 6));
    outs.write(frame.getFrameData());
    outs.flush();
    lastKeepAlive = System.currentTimeMillis();
  }

  public void send(TOCMessage mesg) throws IOException {
    SFLAPDataFrame frame = new SFLAPDataFrame();
    frame.setSequence(nextSequence());
    frame.append(mesg.getTOCMessage());
    //System.out.println("Sending TOC: " + new String(frame.getFrameData(), 0, frame.getLength() + 6));
    outs.write(frame.getFrameData(), 0, frame.getLength() + 6);
    outs.flush();
  }

  private void processIncomingIM(String imData) {
    String[] tokens;
    InstantMessage im;

    if (imData != null && imData.trim().length() > 0) {
      tokens = StringUtils.Tokenize(imData, ':');
      if (tokens != null && tokens.length >= 4) {
        im = new InstantMessage(tokens[TOCConstants.IM_SENDER_INDEX], username, tokens[TOCConstants.IM_MESSAGE_INDEX], tokens[TOCConstants.IM_AUTO_INDEX].equalsIgnoreCase("T"), new Date());
        notifyObservers(im);
      }
    }
  }

  private void processUpdateBuddyStatus(String imData) {
    String[] tokens;
    BuddyStatus bs;

    if (imData != null && imData.trim().length() > 0) {
      tokens = StringUtils.Tokenize(imData, ':');
      if (tokens != null && tokens.length >= 7) {
        bs = new BuddyStatus(tokens[TOCConstants.BUDDY_STATUS_USERNAME_INDEX], "T".equalsIgnoreCase(tokens[TOCConstants.BUDDY_STATUS_IS_ONLINE_INDEX]), Integer
            .parseInt(tokens[TOCConstants.BUDDY_STATUS_EVIL_AMOUNT_INDEX]), Long.parseLong(tokens[TOCConstants.BUDDY_STATUS_SIGNON_TIME_INDEX]), Long
            .parseLong(tokens[TOCConstants.BUDDY_STATUS_IDLE_MINUTES_INDEX]), BuddyStatus.GetUserClass(tokens[TOCConstants.BUDDY_STATUS_USER_CLASS_INDEX]));
        notifyObservers(bs);
      }
    }
  }

  public void addObserver(TOCObserver observer) {
    synchronized (tocObservers) {
      tocObservers.add(observer);
    }
  }

  public void removeObserver(TOCObserver observer) {
    synchronized (tocObservers) {
      tocObservers.remove(observer);
    }
  }

  private void notifyObservers(InstantMessage im) {
    TOCObserver observer;

    synchronized (tocObservers) {
      for (int i = 0; i < tocObservers.size(); i++) {
        observer = (TOCObserver) tocObservers.get(i);
        observer.imReceived(im);
      }
    }
  }

  private void notifyObservers(BuddyStatus bs) {
    TOCObserver observer;

    synchronized (tocObservers) {
      for (int i = 0; i < tocObservers.size(); i++) {
        observer = (TOCObserver) tocObservers.get(i);
        observer.imStatusUpdate(bs);
      }
    }
  }

  private void notifyObserversSignoff() {
    TOCObserver observer;

    synchronized (tocObservers) {
      for (int i = 0; i < tocObservers.size(); i++) {
        observer = (TOCObserver) tocObservers.get(i);
        observer.imPunted();
      }
    }
  }

  public synchronized void sendIM(String recipient, String mesg, boolean auto) throws IOException {
    long millisSinceLastSend;
    TOCSendIM tocIm;

    //Recover Send Points
    if (sendPoints < MAX_SEND_POINTS) {
      millisSinceLastSend = System.currentTimeMillis() - lastImSent;
      sendPoints += millisSinceLastSend / SEND_POINT_RECOVERY_INTERVAL;
      if (sendPoints > MAX_SEND_POINTS) {
        sendPoints = MAX_SEND_POINTS;
      }
    }

    //If we are dangerously low on send points, force a wait!
    try {
      while (sendPoints < MIN_SEND_POINTS) {
        for (int i = 1; i < MIN_POINT_WAIT_INTERVAL; i++) {
          Thread.sleep(1000);
        }
        sendPoints++;
      }
    }
    catch (InterruptedException ie) {}

    //Send Instant Message!
    tocIm = new TOCSendIM(recipient, mesg, auto);
    send(tocIm);
    sendPoints--;
    lastImSent = System.currentTimeMillis();
  }

  private String getErrorMessage(SFLAPFrame frame) {
    String data;
    String message = "An unknown error has occurred!";
    String[] tokens;

    if (frame != null) {
      data = new String(frame.getFrameData(), 6, frame.getLength());
      if (data.startsWith(TOCConstants.COMMAND_ERROR)) {
        tokens = StringUtils.Tokenize(data, ':');
        message = getErrorMessage(tokens);
      }
    }

    return message;
  }

  private String getErrorMessage(String[] tokens) {
    String message;
    String errorCode;

    if (tokens != null && tokens.length >= 2) {
      errorCode = tokens[1].trim();
      if (errorCode.equals("901")) {
        message = (tokens.length >= 3 ? tokens[2].trim() : "[UNKNOWN]") + " not currently available";
      }
      else if (errorCode.equals("902")) {
        message = "Warning of " + (tokens.length >= 3 ? tokens[2].trim() : "[UNKNOWN]") + " not currently available";
      }
      else if (errorCode.equals("903")) {
        message = "A message has been dropped, you are exceeding the server speed limit";
      }
      else if (errorCode.equals("911")) {
        message = "Error validating input";
      }
      else if (errorCode.equals("912")) {
        message = "Invalid account";
      }
      else if (errorCode.equals("913")) {
        message = "Error encountered while processing request";
      }
      else if (errorCode.equals("914")) {
        message = "Service unavailable";
      }
      else if (errorCode.equals("950")) {
        message = "Chat in " + (tokens.length >= 3 ? tokens[2].trim() : "[UNKNOWN]") + " is unavailable";
      }
      else if (errorCode.equals("960")) {
        message = "You are sending message too fast to " + (tokens.length >= 3 ? tokens[2].trim() : "[UNKNOWN]");
      }
      else if (errorCode.equals("961")) {
        message = "You missed an im from " + (tokens.length >= 3 ? tokens[2].trim() : "[UNKNOWN]") + " because it was too long";
      }
      else if (errorCode.equals("962")) {
        message = "You missed an im from " + (tokens.length >= 3 ? tokens[2].trim() : "[UNKNOWN]") + " because it was sent too fast";
      }
      else if (errorCode.equals("980")) {
        message = "Invalid username or password";
      }
      else if (errorCode.equals("981")) {
        message = "The service is temporarily unavailable";
      }
      else if (errorCode.equals("982")) {
        message = "Your warning level is currently too high to sign on";
      }
      else if (errorCode.equals("983")) {
        message = "You have been connecting and disconnecting too frequently. Wait 10 minutes and try again. If you continue to try, you will need to wait even longer.";
      }
      else if (errorCode.equals("989")) {
        message = "An unknown signon error has occurred " + (tokens.length >= 3 ? tokens[2].trim() : "[UNKNOWN]");
      }
      else {
        message = "An unknown error has occurred! Error Code = " + tokens[1];
      }
    }
    else {
      message = "An unknown error has occurred! No Error Code Found!";
    }

    /*
     Messages Not Implemented Yet:
     970   - Failure
     971   - Too many matches
     972   - Need more qualifiers
     973   - Dir service temporarily unavailable
     974   - Email lookup restricted
     975   - Keyword Ignored
     976   - No Keywords
     977   - Language not supported
     978   - Country not supported
     979   - Failure unknown $1
     */

    return message;
  }

  private boolean isSignonResponse(SFLAPFrame frame) {
    boolean retVal = false;
    String data;

    if (frame != null) {
      data = new String(frame.getFrameData(), 6, frame.getLength());
      retVal = data.startsWith(TOCConstants.COMMAND_SIGNON);
    }

    return retVal;

  }

  /**
   * @return Returns the connected.
   */
  public boolean isConnected() {
    return connected;
  }

}
