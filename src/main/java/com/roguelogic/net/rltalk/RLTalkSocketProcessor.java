package com.roguelogic.net.rltalk;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketSession;

public abstract class RLTalkSocketProcessor implements SocketProcessor {

  //User Session Object Keys
  //----------------------------------------------------------------------->

  public static final String USOBJ_SEGMENT_BUFFER = "_RLTalk_UserSessionObj_SegmentBuffer";
  public static final String USOBJ_PACKET_QUEUE = "_RLTalk_UserSessionObj_PacketQueue";

  //----------------------------------------------------------------------->

  protected SocketSession userSession;

  public RLTalkSocketProcessor() {}

  public void clearSession() {}

  public void destroyProcessor() {}

  public void process(SocketSession userSession, byte[] data) throws RLNetException {
    SegmentBuffer segBuf;
    PacketQueue packetQueue;
    Packet packet;
    CommandDataPair cmDatPair;
    //long startTime, endTime, totalTime;
    //double totalSeconds;

    //startTime = System.currentTimeMillis();

    try {
      pushSession(userSession); //keep the session for later use by other methods

      if (!userSession.wasHandshook()) {
        //Perform Optional Server Initiated Handshake
        userSession.setHandshookStatus(true);
        _rlTalkHandshake();
      }
      else {
        //Normal Read Processing

        //Obtain Core Protocol Implementation Objects from User Session
        segBuf = (SegmentBuffer) userSession.getUserItem(USOBJ_SEGMENT_BUFFER);
        if (segBuf == null) {
          segBuf = new SegmentBuffer();
          userSession.putUserItem(USOBJ_SEGMENT_BUFFER, segBuf);
        }

        packetQueue = (PacketQueue) userSession.getUserItem(USOBJ_PACKET_QUEUE);
        if (packetQueue == null) {
          packetQueue = new PacketQueue();
          userSession.putUserItem(USOBJ_PACKET_QUEUE, packetQueue);
        }

        //Add "NEW" data to Segment Buffer
        if (data != null && data.length > 0) {
          segBuf.append(data);
        }

        //Read Available Packets from Segment Buffer and queue them in the Packet Queue
        while (segBuf.hasPacket()) {
          packet = segBuf.nextPacket();
          packetQueue.enqueue(packet);
        }

        //Process ALL Completed Packets!
        while (packetQueue.hasPair()) {
          cmDatPair = packetQueue.dequeue();
          _rlTalkHandle(cmDatPair);
        }

      } //End else Block

    }//End Try Block
    catch (Exception e) {
      System.err.println("RL-Talk Error!");
      System.err.println("Begin Raw Data Dump: ");
      if (data != null) {
        System.err.println(new String(data));
      }

      if (e instanceof RLTalkException) {
        throw ((RLTalkException) e);
      }
      else {
        throw new RLTalkException(e);
      }
    } //End Catch Block

    /*endTime = System.currentTimeMillis();
     totalTime = endTime - startTime;
     totalSeconds = totalTime / 1000.0d;

     System.out.println("Total RLTalk Processing Time: " + totalSeconds + (" sec(s)."));*/
  }

  public void pushSession(SocketSession session) {
    this.userSession = session;
  }

  protected void _rlTalkSend(CommandDataPair cmDatPair) throws RLNetException {
    Packet[] packets;

    //Synchronized to make sure a complete message
    //from a single thread will be sent before
    //the next message can be sent!
    synchronized (userSession.getSendLock()) {
      packets = PacketFactory.GetPackets(cmDatPair); //Convert CDP to Packets 

      //Send Packets across the network
      for (int i = 0; i < packets.length; i++) {
        userSession.send(packets[i].toByteArray());
      }
    }
  }

  protected abstract void _rlTalkHandshake() throws RLNetException;

  protected abstract void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException;

}
