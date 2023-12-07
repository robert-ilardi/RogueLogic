/*
 * Created on Oct 26, 2005
 */
package com.roguelogic.net.rltalk;

import com.roguelogic.util.IntUtils;

/**
 * @author rilardi
 */

public class PacketHeader {

  private byte[] version;
  private byte payloadType;
  private int command;
  private int multiplexerIndex;
  private int payloadLength;
  private boolean endOfMessage;
  private int statusCode;

  protected PacketHeader(byte[] data) throws RLTalkException {
    decodeHeader(data);
  }

  protected PacketHeader() {}

  private void decodeHeader(byte[] data) throws RLTalkException {
    if (data == null) {
      throw new RLTalkException("Packet Header cannot be decoded from NULL data!");
    }

    if (data.length < ProtocolConstants.PACKET_HEADER_LENGTH) {
      throw new RLTalkException("Packet Header cannot be decoded from data length less than " + ProtocolConstants.PACKET_HEADER_LENGTH);
    }

    DecodeHeader(this, data);
  }

  public static PacketHeader DecodeHeader(byte[] data) {
    return DecodeHeader(new PacketHeader(), data);
  }

  protected static PacketHeader DecodeHeader(PacketHeader pHeader, byte[] data) {
    byte[] temp;

    if (data != null && data.length >= ProtocolConstants.PACKET_HEADER_LENGTH) {
      pHeader.version = new byte[ProtocolConstants.PACKET_VERSION_SEGMENT_LENGTH];
      System.arraycopy(data, ProtocolConstants.VERSION_START_INDEX, pHeader.version, 0, ProtocolConstants.PACKET_VERSION_SEGMENT_LENGTH);

      pHeader.payloadType = data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX];

      temp = new byte[ProtocolConstants.PACKET_MULTIPLEXER_INDEX_LENGTH];
      System.arraycopy(data, ProtocolConstants.MULTIPLEXER_INDEX_START_INDEX, temp, 0, ProtocolConstants.PACKET_MULTIPLEXER_INDEX_LENGTH);
      pHeader.multiplexerIndex = IntUtils.BytesToInt(temp);

      temp = new byte[ProtocolConstants.STATUS_FIELD_LENGTH];
      System.arraycopy(data, ProtocolConstants.STATUS_FIELD_START_INDEX, temp, 0, ProtocolConstants.STATUS_FIELD_LENGTH);
      pHeader.statusCode = IntUtils.BytesToInt(temp);

      temp = new byte[ProtocolConstants.PACKET_COMMAND_SEGMENT_LENGTH];
      System.arraycopy(data, ProtocolConstants.COMMAND_START_INDEX, temp, 0, ProtocolConstants.PACKET_COMMAND_SEGMENT_LENGTH);
      pHeader.command = IntUtils.BytesToInt(temp);

      pHeader.payloadLength = _GetExpectedPayloadLength(data);

      pHeader.endOfMessage = (data[ProtocolConstants.END_OF_MESSAGE_INDICATOR_START_INDEX] == ProtocolConstants.PACKET_COMPLETES_MESSAGE);
    }
    else {
      pHeader = null;
    }

    return pHeader;
  }

  public static boolean IsPacketHeaderComplete(byte[] data) {
    return data != null && data.length >= ProtocolConstants.PACKET_HEADER_LENGTH && HasValidHeaderBytes(data);
  }

  private static boolean HasValidHeaderBytes(byte[] data) {
    boolean valid = true;

    //Check Version
    if (valid) {
      for (int i = ProtocolConstants.VERSION_START_INDEX; i < ProtocolConstants.VERSION_END_INDEX; i++) {
        if (data[i] != ProtocolConstants.PROTOCOL_VERSION[i - ProtocolConstants.VERSION_START_INDEX]) {
          valid = false;
          break;
        }
      }
    }

    //Check Payload Data Type
    if (valid) {
      valid = (data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_BINARY ||

      data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_SHORT ||

      data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_CHAR ||

      data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_INTEGER ||

      data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_FLOAT ||

      data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_LONG ||

      data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_DOUBLE ||

      data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_STRING ||

      data[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] == ProtocolConstants.PAYLOAD_DATA_TYPE_OBJECT);
    }

    //Check Multiplexer Index
    if (valid) {
      for (int i = ProtocolConstants.MULTIPLEXER_INDEX_START_INDEX; i < ProtocolConstants.MULTIPLEXER_INDEX_END_INDEX; i++) {
        if (data[i] < 48 || data[i] > 58) {
          valid = false;
          break;
        }
      }
    }

    //Check Status Code
    if (valid) {
      for (int i = ProtocolConstants.STATUS_FIELD_START_INDEX; i < ProtocolConstants.STATUS_FIELD_END_INDEX; i++) {
        if (data[i] < 48 || data[i] > 58) {
          valid = false;
          break;
        }
      }
    }

    //Check Command
    if (valid) {
      for (int i = ProtocolConstants.COMMAND_START_INDEX; i < ProtocolConstants.COMMAND_END_INDEX; i++) {
        if (data[i] < 48 || data[i] > 58) {
          valid = false;
          break;
        }
      }
    }

    //Check End of Message Indicator
    if (valid) {
      valid = (data[ProtocolConstants.END_OF_MESSAGE_INDICATOR_START_INDEX] == ProtocolConstants.PACKET_COMPLETES_MESSAGE ||

      data[ProtocolConstants.END_OF_MESSAGE_INDICATOR_START_INDEX] == ProtocolConstants.PACKET_MESSAGE_SEGMENT);
    }

    //Check Payload Length
    if (valid) {
      for (int i = ProtocolConstants.PAYLOAD_LENGTH_START_INDEX; i < ProtocolConstants.PAYLOAD_LENGTH_END_INDEX; i++) {
        if (data[i] < 48 || data[i] > 58) {
          valid = false;
          break;
        }
      }
    }

    return valid;
  }

  public static int GetExpectedPayloadLength(byte[] data) {
    int expectedLen = -1;

    if (IsPacketHeaderComplete(data)) {
      expectedLen = _GetExpectedPayloadLength(data);
    }

    return expectedLen;
  }

  protected static int _GetExpectedPayloadLength(byte[] data) {
    int expectedLen;
    byte[] temp;

    temp = new byte[ProtocolConstants.PACKET_PAYLOAD_SIZE_SEGMENT_LENGTH];
    System.arraycopy(data, ProtocolConstants.PAYLOAD_LENGTH_START_INDEX, temp, 0, ProtocolConstants.PACKET_PAYLOAD_SIZE_SEGMENT_LENGTH);
    expectedLen = IntUtils.BytesToInt(temp);

    return expectedLen;
  }

  /**
   * @return Returns the command.
   */
  public int getCommand() {
    return command;
  }

  /**
   * @return Returns the endOfMessage.
   */
  public boolean isEndOfMessage() {
    return endOfMessage;
  }

  /**
   * @return Returns the payloadType.
   */
  public byte getPayloadType() {
    return payloadType;
  }

  /**
   * @return Returns the version.
   */
  public byte[] getVersion() {
    return version;
  }

  /**
   * @param command The command to set.
   */
  public void setCommand(int command) {
    this.command = command;
  }

  /**
   * @param endOfMessage The endOfMessage to set.
   */
  public void setEndOfMessage(boolean endOfMessage) {
    this.endOfMessage = endOfMessage;
  }

  /**
   * @param payloadType The payloadType to set.
   */
  public void setPayloadType(byte payloadType) {
    this.payloadType = payloadType;
  }

  /**
   * @param version The version to set.
   */
  public void setVersion(byte[] version) {
    this.version = version;
  }

  /**
   * @return Returns the statusCode.
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * @param statusCode The statusCode to set.
   */
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public byte[] toByteArray() {
    byte[] bArr = null;

    bArr = new byte[ProtocolConstants.PACKET_HEADER_LENGTH];

    toByteArray(bArr);

    return bArr;
  }

  /**
   * @return Returns the payloadLength.
   */
  public int getPayloadLength() {
    return payloadLength;
  }

  /**
   * @param payloadLength The payloadLength to set.
   */
  public void setPayloadLength(int payloadLength) {
    this.payloadLength = payloadLength;
  }

  public byte[] toByteArray(byte[] bArr) {
    byte[] tmp;

    if (bArr != null) {
      System.arraycopy(version, 0, bArr, ProtocolConstants.VERSION_START_INDEX, ProtocolConstants.PACKET_VERSION_SEGMENT_LENGTH);

      bArr[ProtocolConstants.PAYLOAD_TYPE_INDICATOR_START_INDEX] = payloadType;

      tmp = IntUtils.IntToBytes(multiplexerIndex);
      tmp = IntUtils.AddZeroPadding(tmp, ProtocolConstants.PACKET_MULTIPLEXER_INDEX_LENGTH);
      System.arraycopy(tmp, 0, bArr, ProtocolConstants.MULTIPLEXER_INDEX_START_INDEX, ProtocolConstants.PACKET_MULTIPLEXER_INDEX_LENGTH);

      tmp = IntUtils.IntToBytes(statusCode);
      tmp = IntUtils.AddZeroPadding(tmp, ProtocolConstants.STATUS_FIELD_LENGTH);
      System.arraycopy(tmp, 0, bArr, ProtocolConstants.STATUS_FIELD_START_INDEX, ProtocolConstants.STATUS_FIELD_LENGTH);

      tmp = IntUtils.IntToBytes(command);
      tmp = IntUtils.AddZeroPadding(tmp, ProtocolConstants.PACKET_COMMAND_SEGMENT_LENGTH);
      System.arraycopy(tmp, 0, bArr, ProtocolConstants.COMMAND_START_INDEX, ProtocolConstants.PACKET_COMMAND_SEGMENT_LENGTH);

      tmp = IntUtils.IntToBytes(payloadLength);
      tmp = IntUtils.AddZeroPadding(tmp, ProtocolConstants.PACKET_PAYLOAD_SIZE_SEGMENT_LENGTH);
      System.arraycopy(tmp, 0, bArr, ProtocolConstants.PAYLOAD_LENGTH_START_INDEX, ProtocolConstants.PACKET_PAYLOAD_SIZE_SEGMENT_LENGTH);

      bArr[ProtocolConstants.END_OF_MESSAGE_INDICATOR_START_INDEX] = (endOfMessage ? ProtocolConstants.PACKET_COMPLETES_MESSAGE : ProtocolConstants.PACKET_MESSAGE_SEGMENT);
    }

    return bArr;
  }

  public int getMultiplexerIndex() {
    return multiplexerIndex;
  }

  public void setMultiplexerIndex(int multiplexerIndex) {
    this.multiplexerIndex = multiplexerIndex;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[PacketHeader - Version: ");
    sb.append(new String(version));
    sb.append(", PayloadType: ");
    sb.append(new String(new byte[] { payloadType }));
    sb.append(", Multiplexer Index: ");
    sb.append(multiplexerIndex);
    sb.append(", Command: ");
    sb.append(command);
    sb.append(", Status Code: ");
    sb.append(statusCode);
    sb.append(", PayloadLen: ");
    sb.append(payloadLength);
    sb.append(", EOM-Flag: ");
    sb.append(endOfMessage);
    sb.append("]");

    return sb.toString();
  }

}
