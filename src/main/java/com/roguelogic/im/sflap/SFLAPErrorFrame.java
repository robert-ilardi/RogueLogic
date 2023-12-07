/*
 * Created on Mar 12, 2005
 */
package com.roguelogic.im.sflap;

/**
 * @author rilardi
 */

public class SFLAPErrorFrame extends SFLAPFrame {

  public SFLAPErrorFrame() {
    super(FLAP_ERROR_FRAME);
    frameData[FRAME_TYPE_INDEX] = (byte) FLAP_ERROR_FRAME;
  }

  public SFLAPErrorFrame(byte[] frameData) {
    super(frameData, FLAP_ERROR_FRAME);
  }

}