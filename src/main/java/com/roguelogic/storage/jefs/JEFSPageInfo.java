/**
 * Created Jan 23, 2008
 */
package com.roguelogic.storage.jefs;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class JEFSPageInfo {
  private JEFSPageType pageType;
  private long startingAddress;
  private long nextPageChainAddress;
  private int usedUserSegment;

  public JEFSPageInfo() {}

  public long getStartingAddress() {
    return startingAddress;
  }

  public void _setStartingAddress(long startingAddress) {
    this.startingAddress = startingAddress;
  }

  public JEFSPageType getPageType() {
    return pageType;
  }

  public void _setPageType(JEFSPageType pageType) {
    this.pageType = pageType;
  }

  public long getNextPageChainAddress() {
    return nextPageChainAddress;
  }

  public void _setNextPageChainAddress(long nextPageChainAddress) {
    this.nextPageChainAddress = nextPageChainAddress;
  }

  public int getUsedUserSegment() {
    return usedUserSegment;
  }

  public void _setUsedUserSegment(int usedUserSegment) {
    this.usedUserSegment = usedUserSegment;
  }

  public byte[] getBytes() {
    byte[] data = null;
    StringBuffer header = new StringBuffer();

    header.append(pageType.getTypeIndicator());
    header.append(StringUtils.LPad(String.valueOf(startingAddress), '0', JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN));
    header.append(StringUtils.LPad(String.valueOf(nextPageChainAddress), '0', JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN));
    header.append(StringUtils.LPad(String.valueOf(usedUserSegment), '0', JEFSVolHeader.PAGE_SIZE_MAX_LEN));

    data = header.toString().getBytes();

    return data;
  }

  public static int GetMaxUserSegmentLength(JEFSVolHeader header) throws JEFSException {
    if (JEFSVolHeader.VERSION.equals(header.getVersion())) {
      return header.getPageSize() - GetUserSegmentOffset(header);
    }
    else {
      throw new JEFSException("Unsupported JEFS Version!");
    }
  }

  public static int GetUserSegmentOffset(JEFSVolHeader header) throws JEFSException {
    if (JEFSVolHeader.VERSION.equals(header.getVersion())) {
      return (JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN * 2) + JEFSVolHeader.PAGE_SIZE_MAX_LEN + 1;
    }
    else {
      throw new JEFSException("Unsupported JEFS Version!");
    }
  }

  public static int GetPageHeaderLen(JEFSVolHeader header) throws JEFSException {
    if (JEFSVolHeader.VERSION.equals(header.getVersion())) {
      return (JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN * 2) + JEFSVolHeader.PAGE_SIZE_MAX_LEN + 1;
    }
    else {
      throw new JEFSException("Unsupported JEFS Version!");
    }
  }

  public int getRemainingUserSegmentLen(JEFSVolHeader header) throws JEFSException {
    return GetMaxUserSegmentLength(header) - usedUserSegment;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[JEFSPageInfo - ");

    sb.append("PageType: ");
    sb.append(pageType.getTypeName());

    sb.append(", StartingAddress: ");
    sb.append(startingAddress);

    sb.append(", NextPageChainAddress: ");
    sb.append(nextPageChainAddress);

    sb.append(", UserUserSegment: ");
    sb.append(usedUserSegment);

    sb.append("]");

    return sb.toString();
  }

  public static JEFSPageInfo Decode(byte[] data, JEFSVolHeader header) throws JEFSException {
    JEFSPageInfo page = null;
    int usedUserSegLen;
    char ptInd;
    String tmp;
    long startingAddr, nxtPgChainAddr;

    if (!JEFSVolHeader.VERSION.equals(header.getVersion())) {
      throw new JEFSException("Unsupported JEFS Version!");
    }

    page = new JEFSPageInfo();

    //Decode Page Type Indicator
    ptInd = (char) data[0];

    switch (ptInd) {
      case 'F':
        page._setPageType(JEFSPageType.Free);
        break;
      case 'T':
        page._setPageType(JEFSPageType.Tracking);
        break;
      case 'D':
        page._setPageType(JEFSPageType.Data);
        break;
      default:
        throw new JEFSException("Invalid Page Type Indicator '" + ptInd + "'! Page Header appears to be corrupted!");
    }

    //Decode Starting Address
    tmp = new String(data, 1, JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN);
    startingAddr = Long.parseLong(tmp);
    page._setStartingAddress(startingAddr);

    //Decode Next Page Chain Address
    tmp = new String(data, 1 + JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN, JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN);
    nxtPgChainAddr = Long.parseLong(tmp);
    page._setNextPageChainAddress(nxtPgChainAddr);

    //Decode Used User Segment Length
    tmp = new String(data, 1 + (JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN * 2), JEFSVolHeader.PAGE_SIZE_MAX_LEN);
    usedUserSegLen = Integer.parseInt(tmp);
    page._setUsedUserSegment(usedUserSegLen);

    return page;
  }

}
