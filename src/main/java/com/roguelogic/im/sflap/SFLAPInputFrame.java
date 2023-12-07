/*
 * Created on Mar 13, 2005
 */
package com.roguelogic.im.sflap;

/**
 * @author rilardi
 */

public class SFLAPInputFrame extends SFLAPFrame {

  private int frameLen;

  public SFLAPInputFrame() {
    super(FLAP_INPUT_FRAME);
    frameLen = 0;
  }

  public boolean isCompleteFrame() {
    return (frameLen > 5) && ((frameLen - 6) >= getLength());
  }

  public void append(byte[] buffer, int len) {
    for (int i = 0; i < len; i++) {
      frameData[frameLen + i] = buffer[i];
    }
    frameLen += len;
  }

  public void append(byte b) {
    frameData[frameLen++] = b;
  }

  public SFLAPFrame createDervivedFrame() throws SFLAPFrameException {
    SFLAPFrame dervivedFrame = null;

    if (((char) frameData[0]) != '*') {
      throw new SFLAPFrameException("SFLAP Frame does not start with '*' as required but SFLAP/TOC protocol!");
    }

    switch ((int) frameData[1]) {
      case FLAP_SIGNON_FRAME:
        dervivedFrame = new SFLAPSignonFrame(frameData);
        break;
      case FLAP_DATA_FRAME:
        dervivedFrame = new SFLAPDataFrame(frameData);
        break;
      case FLAP_ERROR_FRAME:
        dervivedFrame = new SFLAPErrorFrame(frameData);
        break;
      case FLAP_SIGNOFF_FRAME:
        dervivedFrame = new SFLAPSignoffFrame(frameData);
        break;
      case FLAP_KEEP_ALIVE_FRAME:
        dervivedFrame = new SFLAPKeepAliveFrame(frameData);
        break;
      default:
        throw new SFLAPFrameException("SFLAP Frame Type NOT Supported!: " + ((int) frameData[1]));
    }

    return dervivedFrame;
  }

  public int getFrameLen() {
    return frameLen - 6;
  }

}