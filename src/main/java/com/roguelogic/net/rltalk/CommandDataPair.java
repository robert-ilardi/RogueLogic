/*
 * Created on Oct 26, 2005
 */
package com.roguelogic.net.rltalk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author rilardi
 */

public class CommandDataPair {

  private int command;

  private byte[] data;

  private byte dataType;

  private int multiplexerIndex;

  private int statusCode;

  public CommandDataPair() {
    command = 0;
    data = new byte[0];
    dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_BINARY;
    multiplexerIndex = 0;
    statusCode = 0;
  }

  /**
   * @return Returns the command.
   */
  public int getCommand() {
    return command;
  }

  /**
   * @param command The command to set.
   */
  public void setCommand(int command) throws RLTalkException {
    if (command > ProtocolConstants.PROTOCOL_COMMAND_CODE_LIMIT) {
      this.command = command;
    }
    else {
      throw new RLTalkException("Command Code = " + command + " is below the User Command Code Space! System Code Range: 0 to "
          + ProtocolConstants.PROTOCOL_COMMAND_CODE_LIMIT);
    }
  }

  /**
   * @return Returns the data.
   */
  public byte[] getData() {
    return data;
  }

  public void setTypelessData(byte[] data) {
    this.data = data;
  }

  /**
   * @param data The data to set.
   */
  public void setData(byte[] data) {
    this.data = data;
    this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_BINARY;
  }

  public void setData(short data) {
    this.data = String.valueOf(data).getBytes();
    this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_SHORT;
  }

  public void setData(char data) {
    this.data = String.valueOf(data).getBytes();
    this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_CHAR;
  }

  public void setData(int data) {
    this.data = String.valueOf(data).getBytes();
    this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_INTEGER;
  }

  public void setData(float data) {
    this.data = String.valueOf(data).getBytes();
    this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_FLOAT;
  }

  public void setData(long data) {
    this.data = String.valueOf(data).getBytes();
    this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_LONG;
  }

  public void setData(double data) {
    this.data = String.valueOf(data).getBytes();
    this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_DOUBLE;
  }

  public void setData(String data) {
    this.data = data.getBytes();
    this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_STRING;
  }

  public void setData(Serializable data) throws IOException {
    ObjectOutputStream oos = null;
    ByteArrayOutputStream baos = null;

    try {
      baos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(baos);
      oos.writeObject(data);
      oos.close();
      oos = null;

      this.data = baos.toByteArray();
      this.dataType = ProtocolConstants.PAYLOAD_DATA_TYPE_OBJECT;
    }
    finally {
      if (oos != null) {
        try {
          oos.close();
        }
        catch (Exception e) {}
        oos = null;
      }

      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }
    }
  }

  protected void setSystemLevelCommand(int command) {
    this.command = command;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[CommandDataPair - Command: ");
    sb.append(command);
    sb.append(", DataLen: ");
    sb.append((data != null ? data.length : 0));
    sb.append(", DataType: ");
    sb.append(new String(new byte[] { dataType }));
    sb.append(", MultiplexerIndex: ");
    sb.append(multiplexerIndex);
    sb.append(", StatusCode: ");
    sb.append(statusCode);
    sb.append("]");

    return sb.toString();
  }

  /**
   * @return Returns the dataType.
   */
  public byte getDataType() {
    return dataType;
  }

  public int getMultiplexerIndex() {
    return multiplexerIndex;
  }

  public void setMultiplexerIndex(int multiplexerIndex) throws RLTalkException {
    if (multiplexerIndex >= 0 && multiplexerIndex <= ProtocolConstants.PROTOCOL_MULTIPLEXER_INDEX_LIMIT) {
      this.multiplexerIndex = multiplexerIndex;
    }
    else {
      throw new RLTalkException("Multiplexer Index must be between 0 and 99");
    }
  }

  public void setData(byte[] data, byte dataType) {
    this.data = data;
    this.dataType = dataType;
  }

  public int dataLen() {
    return (data != null ? data.length : -1);
  }

  public short getShort() {
    return Short.parseShort(new String(data));
  }

  public char getChar() {
    return (new String(data)).charAt(0);
  }

  public int getInt() {
    return Integer.parseInt(new String(data));
  }

  public float getFloat() {
    return Float.parseFloat(new String(data));
  }

  public long getLong() {
    return Long.parseLong(new String(data));
  }

  public double getDouble() {
    return Double.parseDouble(new String(data));
  }

  public String getString() {
    return new String(data);
  }

  public Object getObject() throws IOException, ClassNotFoundException {
    Object obj = null;
    ObjectInputStream ios = null;
    ByteArrayInputStream bais = null;

    if (!isDataNull()) {
      try {
        bais = new ByteArrayInputStream(data);
        ios = new ObjectInputStream(bais);
        obj = ios.readObject();
      }
      finally {
        if (ios != null) {
          try {
            ios.close();
          }
          catch (Exception e) {}
          ios = null;
        }

        if (bais != null) {
          try {
            bais.close();
          }
          catch (Exception e) {}
          bais = null;
        }
      }
    }

    return obj;
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
  public void setStatusCode(int statusCode) throws RLTalkException {
    if (statusCode >= 0 && statusCode <= ProtocolConstants.STATUS_CODE_LIMIT) {
      this.statusCode = statusCode;
    }
    else {
      throw new RLTalkException("Status Code must be between 0 and 9");
    }
  }

  public boolean equals(Object obj) {
    boolean isEq = false;
    CommandDataPair other;

    if (obj instanceof CommandDataPair) {
      other = (CommandDataPair) obj;

      isEq = (this.command == other.command && this.dataType == other.dataType && this.multiplexerIndex == other.multiplexerIndex && areArraysEqual(
          this.data, other.data));
    }

    return isEq;
  }

  private boolean areArraysEqual(byte[] arr1, byte[] arr2) {
    boolean arrEq = false;

    if (arr1 == null && arr2 == null) {
      arrEq = true;
    }
    else if (arr1 != null && arr2 != null && arr1.length == arr2.length) {
      arrEq = true;

      for (int i = 0; i < arr1.length; i++) {
        if (arr1[i] != arr2[i]) {
          arrEq = false;
          break;
        }
      }
    }

    return arrEq;
  }

  public boolean isDataNull() {
    return data == null || data.length == 0;
  }

}
