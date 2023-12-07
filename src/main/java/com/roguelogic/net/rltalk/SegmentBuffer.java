/*
 * Created on Oct 26, 2005
 */
package com.roguelogic.net.rltalk;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * @author rilardi
 */

public class SegmentBuffer {

  private ArrayList<byte[]> buffer;

  public SegmentBuffer() {
    buffer = new ArrayList<byte[]>();
  }

  public synchronized void append(byte[] segment) {
    buffer.add(segment);
  }

  public synchronized boolean hasPacket() throws RLTalkException {
    byte[] segment;
    ByteArrayOutputStream multiSegBuf;
    boolean completePacket = false, wroteMSB = false;

    multiSegBuf = new ByteArrayOutputStream();

    for (int i = 0; i < buffer.size(); i++) {
      segment = buffer.get(i);

      if (Packet.IsCompletePacket(segment)) {
        completePacket = true;
        break;
      }
      else {
        multiSegBuf.write(segment, 0, segment.length);

        if (wroteMSB && Packet.IsCompletePacket(multiSegBuf.toByteArray())) {
          completePacket = true;
          break;
        }
        else {
          wroteMSB = true;
        }
      }
    }

    return completePacket;
  }

  public Packet nextPacket() throws RLTalkException {
    Packet p = null;
    byte[] segment, tmp;
    ByteArrayOutputStream multiSegBuf;
    boolean wroteMSB = false;
    int expectedLen, remainderLen;

    multiSegBuf = new ByteArrayOutputStream();

    while (!buffer.isEmpty()) {
      segment = buffer.remove(0);

      if (Packet.IsCompletePacket(segment)) {
        //Create new packet from complete segment
        tmp = segment;
        expectedLen = PacketHeader._GetExpectedPayloadLength(segment);
        segment = new byte[ProtocolConstants.PACKET_HEADER_LENGTH + expectedLen];
        System.arraycopy(tmp, 0, segment, 0, segment.length);
        p = new Packet(segment);

        //Push back remainder segment...
        remainderLen = tmp.length - segment.length;
        if (remainderLen > 0) {
          segment = new byte[remainderLen];
          System.arraycopy(tmp, ProtocolConstants.PACKET_HEADER_LENGTH + expectedLen, segment, 0, remainderLen);
          buffer.add(0, segment);
        }

        break;
      }
      else {
        multiSegBuf.write(segment, 0, segment.length);

        if (wroteMSB && Packet.IsCompletePacket(tmp = multiSegBuf.toByteArray())) {
          //Create new Packet from multiple segments
          expectedLen = PacketHeader._GetExpectedPayloadLength(tmp);
          segment = new byte[ProtocolConstants.PACKET_HEADER_LENGTH + expectedLen];
          System.arraycopy(tmp, 0, segment, 0, segment.length);
          p = new Packet(segment);

          //Push back remainder segment...
          remainderLen = tmp.length - segment.length;
          if (remainderLen > 0) {
            segment = new byte[remainderLen];
            System.arraycopy(tmp, ProtocolConstants.PACKET_HEADER_LENGTH + expectedLen, segment, 0, remainderLen);
            buffer.add(0, segment);
          }

          break;
        }
        else {
          wroteMSB = true;
        }
      }
    }

    return p;
  }

}
