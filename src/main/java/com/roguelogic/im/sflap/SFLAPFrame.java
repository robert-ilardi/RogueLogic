/*
 * Created on Mar 12, 2005
 */
package com.roguelogic.im.sflap;

/**
 * @author rilardi
 */

public abstract class SFLAPFrame {

  public static final String FLAP_GREETING = "FLAPON\r\n\r\n";

  //Custom SFLAP/FLAP Frame Types
  public static final int FLAP_INPUT_FRAME = -1;
  public static final int FLAP_UNKNOWN_FRAME = -2;

  //Real SFLAP/FLAP Frame Types
  public static final int FLAP_SIGNON_FRAME = 1;
  public static final int FLAP_DATA_FRAME = 2;
  public static final int FLAP_ERROR_FRAME = 3;
  public static final int FLAP_SIGNOFF_FRAME = 4;
  public static final int FLAP_KEEP_ALIVE_FRAME = 5;

  public static final int MAX_FRAME_SIZE = 8192;
  public static final int FLAP_DATA_OFFSET = 6;

  public static final int FRAME_TYPE_INDEX = 1;
  public static final int FRAME_LENGTH_INDEX_1 = 4;
  public static final int FRAME_LENGTH_INDEX_2 = 5;
  public static final int FRAME_SEQUENCE_INDEX_1 = 2;
  public static final int FRAME_SEQUENCE_INDEX_2 = 3;

  protected int frameType;
  protected byte[] frameData;

  public SFLAPFrame(int frameType) {
    this.frameType = frameType;
    frameData = new byte[MAX_FRAME_SIZE];
    initHeader();
  }

  public SFLAPFrame(byte[] frame, int frameType) {
    this.frameType = frameType;
    this.frameData = frame;
  }

  protected void initHeader() {
    frameData[0] = (byte) '*';
    frameData[1] = 0;
    frameData[2] = 0;
    frameData[3] = 0;
    frameData[4] = 0;
    frameData[5] = 0;
  }

  public void setSequence(int sequence) {
    frameData[FRAME_SEQUENCE_INDEX_1] = (byte) ((sequence / 256) & 0xff);
    frameData[FRAME_SEQUENCE_INDEX_2] = (byte) (sequence & 0xff);
  }

  public int getSequence() {
    return ((frameData[FRAME_SEQUENCE_INDEX_1] & 0xff) * 256) + (frameData[FRAME_SEQUENCE_INDEX_2] & 0xff);
  }

  public int getLength() {
    return GetLength(frameData);
  }

  public static int GetLength(byte[] buffer) {
    return ((buffer[FRAME_LENGTH_INDEX_1] & 0xff) * 256) + (buffer[FRAME_LENGTH_INDEX_2] & 0xff);
  }

  public void setLength(int length) {
    frameData[FRAME_LENGTH_INDEX_1] = (byte) (length / 256);
    frameData[FRAME_LENGTH_INDEX_2] = (byte) (length & 0xff);
  }

  public int getFrameType() {
    return frameType;
  }

  public int getFrameTypeFromBuffer() {
    return (int) frameData[FRAME_TYPE_INDEX];
  }

  public byte[] getFrameData() {
    return frameData;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    String temp;
    int byteCode;

    for (int i = 0; i < getLength() + 6; i++) {
      byteCode = frameData[i] & 0xff;
      temp = Integer.toHexString(byteCode);
      if (temp.length() < 2) {
        sb.append("0");
      }
      sb.append(temp);
      sb.append(" ");
    }

    return sb.toString();
  }

}