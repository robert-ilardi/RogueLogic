/*
 * Created on Nov 21, 2005
 */
package com.roguelogic.net.rltalk;

/**
 * @author rilardi
 */

public class PacketFactory {

  public PacketFactory() {}

  public static Packet[] GetPackets(CommandDataPair cdPair) {
    Packet[] packets = null;
    int packetCnt, nextSliceIndex, sliceSize;
    byte[] payload, slice;

    if (cdPair != null) {
      //Caluate Required Packets needed to send Command Data Pair...
      payload = cdPair.getData();

      if (payload != null && payload.length > 0) {
        if (payload.length <= ProtocolConstants.PACKET_PAYLOAD_MAX_SIZE) {
          //Fits in one packet...
          packetCnt = 1;
        }
        else {
          //Needs multiple packets...
          //Determine the number...
          packetCnt = (int) Math.ceil((double) payload.length / (double) ProtocolConstants.PACKET_PAYLOAD_MAX_SIZE);
        }
      } //End null payload check
      else {
        packetCnt = 1;
      }

      packets = new Packet[packetCnt];

      //Setup Base Packet...
      packets[0] = new Packet();
      packets[0].setVersion(ProtocolConstants.PROTOCOL_VERSION);
      packets[0].setCommand(cdPair.getCommand());
      packets[0].setPayloadType(cdPair.getDataType());
      packets[0].setMultiplexerIndex(cdPair.getMultiplexerIndex());
      packets[0].setStatusCode(cdPair.getStatusCode());
      packets[0].setEndOfMessage(packetCnt == 1 ? true : false);

      if (payload != null) {

        //Setup Supplemental Packets
        for (int i = 1; i < packets.length; i++) {
          packets[i] = new Packet();
          packets[i].setVersion(ProtocolConstants.PROTOCOL_VERSION);
          packets[i].setCommand(cdPair.getCommand());
          packets[i].setPayloadType(cdPair.getDataType());
          packets[i].setMultiplexerIndex(cdPair.getMultiplexerIndex());
          packets[i].setStatusCode(cdPair.getStatusCode());

          if (i == packets.length - 1) {
            //End of Message
            packets[i].setEndOfMessage(true);
          }
          else {
            packets[i].setEndOfMessage(false);
          }
        } //End for i loop through supplemental packets

        //Divide up payload amoung packets
        nextSliceIndex = 0;
        for (int i = 0; i < packets.length; i++) {
          sliceSize = (i != (packets.length - 1) ? ProtocolConstants.PACKET_PAYLOAD_MAX_SIZE : payload.length - (i * ProtocolConstants.PACKET_PAYLOAD_MAX_SIZE));
          slice = new byte[sliceSize];
          System.arraycopy(payload, nextSliceIndex, slice, 0, sliceSize);
          packets[i].setPayload(slice);
          nextSliceIndex += sliceSize;
        }

      } //End null payload check

    } //End NULL cdPair check

    return packets;
  }

}
