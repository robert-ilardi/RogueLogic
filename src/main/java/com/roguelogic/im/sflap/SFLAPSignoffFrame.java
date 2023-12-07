/*
 * Created on Mar 12, 2005
 */
package com.roguelogic.im.sflap;

/**
 * @author rilardi
 */

public class SFLAPSignoffFrame extends SFLAPFrame {

  public SFLAPSignoffFrame() {
    super(FLAP_SIGNOFF_FRAME);
    frameData[FRAME_TYPE_INDEX] = (byte) FLAP_SIGNOFF_FRAME;
  }

  public SFLAPSignoffFrame(byte[] frameData) {
    super(frameData, FLAP_SIGNOFF_FRAME);
  }

}