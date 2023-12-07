/*
 * Created on Oct 26, 2005
 */
package com.roguelogic.net.rltalk;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * @author rilardi
 */

public class PacketQueue {

  private ArrayList<Packet> queue;

  public PacketQueue() {
    queue = new ArrayList<Packet>();
  }

  public synchronized void enqueue(Packet packet) {
    queue.add(packet);
  }

  public synchronized boolean hasPair() {
    boolean completePair = false;
    Packet packet;

    for (int i = 0; i < queue.size(); i++) {
      packet = queue.get(i);
      if (packet.isEndOfMessage()) {
        completePair = true;
      }
    }

    return completePair;
  }

  public synchronized CommandDataPair dequeue() throws RLTalkException {
    CommandDataPair cdPair = null;
    Packet p, first, next = null;
    ArrayList<Packet> mesgQueue = null;
    ByteArrayOutputStream baos;
    byte[] bArr;

    //Read All Packets for a Single Message
    while (!queue.isEmpty()) {
      next = queue.remove(0);
      if (next.isEndOfMessage()) {
        break;
      }
      else {
        if (mesgQueue == null) {
          mesgQueue = new ArrayList<Packet>();
        }

        mesgQueue.add(next);
        next = null;
      }
    }

    if (mesgQueue != null || next != null) {
      cdPair = new CommandDataPair();

      if (mesgQueue != null) {
        //Multiple Packets for a single CDP
        baos = new ByteArrayOutputStream();

        //Setup CDP from Base Packet
        first = mesgQueue.remove(0);

        cdPair.setSystemLevelCommand(first.getCommand()); //System Level so All Commands Work...
        cdPair.setMultiplexerIndex(first.getMultiplexerIndex());
        cdPair.setStatusCode(first.getStatusCode());

        bArr = first.getPayload();
        baos.write(bArr, 0, bArr.length);

        //Append Additional Data
        while (!mesgQueue.isEmpty()) {
          p = mesgQueue.remove(0);
          bArr = p.getPayload();
          baos.write(bArr, 0, bArr.length);
        }

        //Append Last Packet's Data
        bArr = next.getPayload();
        baos.write(bArr, 0, bArr.length);

        //Set Data in CDP
        bArr = baos.toByteArray();
        cdPair.setData(bArr, first.getPayloadType());

        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;

        mesgQueue = null;

        p = null;
        first = null;
        next = null;
      } //End null message queue check
      else {
        //One Packet for a single CDP
        cdPair.setSystemLevelCommand(next.getCommand()); //System Level so All Commands Work...
        cdPair.setMultiplexerIndex(next.getMultiplexerIndex());
        cdPair.setStatusCode(next.getStatusCode());

        bArr = next.getPayload();
        cdPair.setData(bArr, next.getPayloadType());

        next = null;
      }
    } //End null mesgQueue and next check

    return cdPair;
  }

}
