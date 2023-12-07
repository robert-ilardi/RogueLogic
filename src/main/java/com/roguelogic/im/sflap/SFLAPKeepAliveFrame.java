/*
 * Created on Mar 12, 2005
 */
package com.roguelogic.im.sflap;

/**
 * @author rilardi
 */

public class SFLAPKeepAliveFrame extends SFLAPFrame {

  public SFLAPKeepAliveFrame() {
    super(FLAP_KEEP_ALIVE_FRAME);
    frameData[FRAME_TYPE_INDEX] = (byte) FLAP_KEEP_ALIVE_FRAME;
  }

  public SFLAPKeepAliveFrame(byte[] frameData) {
    super(frameData, FLAP_KEEP_ALIVE_FRAME);
  }

}