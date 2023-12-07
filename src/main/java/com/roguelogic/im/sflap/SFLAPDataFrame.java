/*
 * Created on Mar 12, 2005
 */
package com.roguelogic.im.sflap;

/**
 * @author rilardi
 */
public class SFLAPDataFrame extends SFLAPFrame {

  public SFLAPDataFrame() {
    super(FLAP_DATA_FRAME);
    frameData[FRAME_TYPE_INDEX] = (byte) FLAP_DATA_FRAME;
  }

  public SFLAPDataFrame(byte[] frameData) {
    super(frameData, FLAP_DATA_FRAME);
  }

  public void append(String mesg) {
    int dataLen = getLength();

    //Delete Previous NULL Terminator
    if (dataLen > 0) {
      dataLen--;
    }

    //Add String Message Data Payload
    for (int i = 0; i < mesg.length(); i++) {
      frameData[FLAP_DATA_OFFSET + dataLen + i] = (byte) mesg.charAt(i);
    }

    //Set NULL Terminator & Length
    dataLen += mesg.length() + 1;
    frameData[FLAP_DATA_OFFSET + dataLen + 1] = 0;
    setLength(dataLen);
  }

}