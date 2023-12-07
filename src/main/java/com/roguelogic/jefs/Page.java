/**
 * Created Sep 9, 2012
 */
package com.roguelogic.jefs;

import java.io.Serializable;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class Page implements Serializable {

  protected static final int PAGE_HEADER_LEN = 21;

  private static final int PAGE_STATUS_FLAG_POS = 0;
  private static final byte ACTIVE_FLAG = (byte) 65;
  private static final byte DELETED_FLAG = (byte) 68;

  private static final int PAGE_NEXT_PAGE_POS_POS = 1;
  private static final int PAGE_NEXT_PAGE_POS_LEN = 12;

  private static final int PAGE_DATA_SEG_LEN_POS = 13;
  private static final int PAGE_DATA_SEG_LEN_LEN = 8;

  private static final int DATA_SEG_START_POS = 21;

  private int dataSegLen;
  private int pageSize;

  private long startPos;
  private boolean active;

  private long nextPagePos;

  private byte[] dataSegment;

  public Page() {}

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public int getDataSegLen() {
    return dataSegLen;
  }

  public void setDataSegLen(int dataSegLen) {
    this.dataSegLen = dataSegLen;
  }

  public long getStartPos() {
    return startPos;
  }

  public void setStartPos(long startPos) {
    this.startPos = startPos;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public byte[] getDataSegment() {
    return dataSegment;
  }

  public void setDataSegment(byte[] dataSegment) {
    this.dataSegment = dataSegment;
  }

  public long getNextPagePos() {
    return nextPagePos;
  }

  public void setNextPagePos(long nextPagePos) {
    this.nextPagePos = nextPagePos;
  }

  public void parse(byte[] rawPage) {
    String tmp;

    if (rawPage[0] == ACTIVE_FLAG) {
      active = true;
    }

    tmp = new String(rawPage, PAGE_NEXT_PAGE_POS_POS, PAGE_NEXT_PAGE_POS_LEN);
    nextPagePos = Long.parseLong(tmp);

    tmp = new String(rawPage, PAGE_DATA_SEG_LEN_POS, PAGE_DATA_SEG_LEN_LEN);
    dataSegLen = Integer.parseInt(tmp);

    dataSegment = new byte[dataSegLen];
    System.arraycopy(rawPage, DATA_SEG_START_POS, dataSegment, 0, dataSegLen);
  }

  public byte[] toRawPage() {
    byte[] rawPage;
    String tmp;

    rawPage = new byte[pageSize];

    if (active) {
      rawPage[0] = ACTIVE_FLAG;
    }
    else {
      rawPage[0] = DELETED_FLAG;
    }

    tmp = String.valueOf(nextPagePos);
    tmp = StringUtils.LPad(tmp, '0', PAGE_NEXT_PAGE_POS_LEN);
    System.arraycopy(tmp.getBytes(), 0, rawPage, PAGE_NEXT_PAGE_POS_POS, PAGE_NEXT_PAGE_POS_LEN);

    tmp = String.valueOf(dataSegLen);
    tmp = StringUtils.LPad(tmp, '0', PAGE_DATA_SEG_LEN_LEN);
    System.arraycopy(tmp.getBytes(), 0, rawPage, PAGE_DATA_SEG_LEN_POS, PAGE_DATA_SEG_LEN_LEN);

    System.arraycopy(dataSegment, 0, rawPage, DATA_SEG_START_POS, dataSegLen);

    return rawPage;
  }

}
