/*
 * Created on Mar 12, 2005
 */
package com.roguelogic.im.sflap;

/**
 * @author rilardi
 */

public class SFLAPSignonFrame extends SFLAPFrame {

  public static final int FLAP_VERSION_INDEX_1 = 9;
  public static final int FLAP_VERSION_INDEX_2 = 8;
  public static final int FLAP_VERSION_INDEX_3 = 7;
  public static final int FLAP_VERSION_INDEX_4 = 6;

  public static final int TLV_TAB_INDEX_1 = 11;
  public static final int TLV_TAB_INDEX_2 = 10;

  public static final int USERNAME_LEN_INDEX_1 = 12;
  public static final int USERNAME_LEN_INDEX_2 = 13;

  public static final int SIGNON_HEADER_LENGTH = 8;

  public static final int FLAP_VERSION = 1;
  public static final int TLV_TAG = 1;

  public SFLAPSignonFrame() {
    super(FLAP_SIGNON_FRAME);
    frameData[FRAME_TYPE_INDEX] = (byte) FLAP_SIGNON_FRAME;
    setFLAPVersion(FLAP_VERSION);
    setTLVTag(TLV_TAG);
  }

  public SFLAPSignonFrame(byte[] frameData) {
    super(frameData, FLAP_SIGNON_FRAME);
  }

  private void setFLAPVersion(int version) {
    frameData[FLAP_VERSION_INDEX_1] = (byte) (version & 0xff);
    version = version >> 8;
    frameData[FLAP_VERSION_INDEX_2] = (byte) (version & 0xff);
    version = version >> 8;
    frameData[FLAP_VERSION_INDEX_3] = (byte) (version & 0xff);
    version = version >> 8;
    frameData[FLAP_VERSION_INDEX_4] = (byte) (version & 0xff);
    version = version >> 8;
  }

  private void setTLVTag(int tag) {
    frameData[TLV_TAB_INDEX_1] = (byte) (tag & 0xff);
    tag = tag >> 8;
    frameData[TLV_TAB_INDEX_2] = (byte) (tag & 0xff);
  }

  public void setUsername(String username) {
    int baseOffset = FLAP_DATA_OFFSET + SIGNON_HEADER_LENGTH;
    char c;

    for (int i = 0; i < username.length(); i++) {
      c = username.charAt(i);
      if (c != ' ') {
        frameData[baseOffset + i] = (byte) c;
      }
    }

    setLength(SIGNON_HEADER_LENGTH + username.length());
    frameData[USERNAME_LEN_INDEX_1] = (byte) (username.length() / 256);
    frameData[USERNAME_LEN_INDEX_2] = (byte) (username.length() & 0xff);
  }

  public int getFLAPVersion() {
    return (((frameData[FLAP_VERSION_INDEX_4] & 0xff) * 16777216) + ((frameData[FLAP_VERSION_INDEX_3] & 0xff) * 65536)
        + ((frameData[FLAP_VERSION_INDEX_2] & 0xff) * 256) + (frameData[FLAP_VERSION_INDEX_1] & 0xff));
  }

}