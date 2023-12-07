/*
 * Created on Oct 25, 2005
 */
package com.roguelogic.net.rltalk;

/**
 * @author rilardi
 */

public class Packet {

  private PacketHeader header;

  private byte[] payload;

  public Packet() {
    header = new PacketHeader();
  }

  public Packet(byte[] packetData) throws RLTalkException {
    int expectedLen;

    header = new PacketHeader(packetData);

    expectedLen = PacketHeader._GetExpectedPayloadLength(packetData);
    if (packetData.length - ProtocolConstants.PACKET_HEADER_LENGTH == expectedLen) {
      payload = new byte[expectedLen];
      System.arraycopy(packetData, ProtocolConstants.PAYLOAD_START_INDEX, payload, 0, expectedLen);
    }
    else {
      throw new RLTalkException("Actual Data Length not equal to Expected Data Length! (Actual = " + (packetData.length - ProtocolConstants.PACKET_HEADER_LENGTH) + " ; Expected = " + expectedLen
          + ")");
    }
  }

  public static boolean IsCompletePacket(byte[] data) {
    boolean completePacket = false;
    int expectedLen;

    if (PacketHeader.IsPacketHeaderComplete(data)) {
      expectedLen = PacketHeader._GetExpectedPayloadLength(data);
      completePacket = (expectedLen >= 0 && (data.length - ProtocolConstants.PACKET_HEADER_LENGTH) >= expectedLen);
    }

    return completePacket;
  }

  /**
   * @return Returns the command.
   */
  public int getCommand() {
    return header.getCommand();
  }

  /**
   * @param command The command to set.
   */
  public void setCommand(int command) {
    header.setCommand(command);
  }

  /**
   * @return Returns the endOfMessage.
   */
  public boolean isEndOfMessage() {
    return header.isEndOfMessage();
  }

  /**
   * @param endOfMessage The endOfMessage to set.
   */
  public void setEndOfMessage(boolean endOfMessage) {
    header.setEndOfMessage(endOfMessage);
  }

  /**
   * @return Returns the payload.
   */
  public byte[] getPayload() {
    return payload;
  }

  /**
   * @param payload The payload to set.
   */
  public void setPayload(byte[] payload) {
    this.payload = payload;
    header.setPayloadLength(payload != null ? payload.length : 0);
  }

  /**
   * @return Returns the multiplexerIndex.
   */
  public int getMultiplexerIndex() {
    return header.getMultiplexerIndex();
  }

  /**
   * @param multiplexerIndex The multiplexerIndex to set.
   */
  public void setMultiplexerIndex(int multiplexerIndex) {
    header.setMultiplexerIndex(multiplexerIndex);
  }

  /**
   * @return Returns the payloadType.
   */
  public byte getPayloadType() {
    return header.getPayloadType();
  }

  /**
   * @param payloadType The payloadType to set.
   */
  public void setPayloadType(byte payloadType) {
    header.setPayloadType(payloadType);
  }

  /**
   * @return Returns the version.
   */
  public byte[] getVersion() {
    return header.getVersion();
  }

  /**
   * @param version The version to set.
   */
  public void setVersion(byte[] version) {
    header.setVersion(version);
  }

  /**
   * @return Returns the statusCode.
   */
  public int getStatusCode() {
    return header.getStatusCode();
  }

  /**
   * @param statusCode The statusCode to set.
   */
  public void setStatusCode(int statusCode) {
    header.setStatusCode(statusCode);
  }

  public byte[] toByteArray() {
    byte[] bArr = null;

    bArr = new byte[ProtocolConstants.PACKET_HEADER_LENGTH + (payload != null ? payload.length : 0)];

    //Arrayize Header
    header.toByteArray(bArr);

    //Copy Payload
    if (payload != null) {
      System.arraycopy(payload, 0, bArr, ProtocolConstants.PAYLOAD_START_INDEX, payload.length);
    }

    return bArr;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("**Packet**\n");
    sb.append(header);
    sb.append("\nPayload -\n");
    if (payload != null) {
      sb.append(new String(payload));
    }

    return sb.toString();
  }

}
