/**
 * Created Jan 8, 2008
 */
package com.roguelogic.storage.jefs;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class JEFSFileEntry {

  public static final int MAX_NAME_LEN = 100; //87; //141;

  private String name;
  private int dirIndex;
  private long startingAddress;
  private JEFSFileType fileType;
  private long length;
  private int parentDirIndex;

  public JEFSFileEntry() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getDirIndex() {
    return dirIndex;
  }

  public void _setDirIndex(int dirIndex) {
    this.dirIndex = dirIndex;
  }

  public long getStartingAddress() {
    return startingAddress;
  }

  public void _setStartingAddress(long startingAddress) {
    this.startingAddress = startingAddress;
  }

  public JEFSFileType getFileType() {
    return fileType;
  }

  public void setFileType(JEFSFileType fileType) {
    this.fileType = fileType;
  }

  public long getLength() {
    return length;
  }

  public void _setLength(long length) {
    this.length = length;
  }

  public int getParentDirIndex() {
    return parentDirIndex;
  }

  public void _setParentDirIndex(int parentDirIndex) {
    this.parentDirIndex = parentDirIndex;
  }

  public byte[] getBytes() {
    byte[] data = null;
    StringBuffer header = new StringBuffer();

    header.append(fileType.getTypeIndicator());
    header.append(StringUtils.RPad(name, ' ', MAX_NAME_LEN));
    header.append(StringUtils.LPad(String.valueOf(dirIndex), '0', JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN));
    header.append(StringUtils.LPad(String.valueOf(parentDirIndex), '0', JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN));
    header.append(StringUtils.LPad(String.valueOf(startingAddress), '0', JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN));
    header.append(StringUtils.LPad(String.valueOf(length), '0', JEFSVolHeader.FIRST_FREE_PAGE_ADDRESS_MAX_LEN));

    data = header.toString().getBytes();

    return data;
  }

  public static int GetFileEntryDataLen(JEFSVolHeader header) throws JEFSException {
    if (JEFSVolHeader.VERSION.equals(header.getVersion())) {
      return MAX_NAME_LEN + JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN + JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN + JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN
          + JEFSVolHeader.FIRST_FREE_PAGE_ADDRESS_MAX_LEN + 1;
    }
    else {
      throw new JEFSException("Unsupported JEFS Version!");
    }
  }

  public static JEFSFileEntry Decode(byte[] data, JEFSVolHeader header) throws JEFSException {
    JEFSFileEntry entry;
    int tmpI;
    char ftInd;
    String tmp;
    long tmpL;

    if (!JEFSVolHeader.VERSION.equals(header.getVersion())) {
      throw new JEFSException("Unsupported JEFS Version!");
    }

    entry = new JEFSFileEntry();

    //Decode File Type Indicator
    ftInd = (char) data[0];

    switch (ftInd) {
      case 'D':
        entry.setFileType(JEFSFileType.Directory);
        break;
      case 'F':
        entry.setFileType(JEFSFileType.File);
        break;
      case 'X':
        entry.setFileType(JEFSFileType.Deletion);
        break;
      default:
        throw new JEFSException("Invalid File Type Indicator '" + ftInd + "'! File Entry appears to be corrupted!");
    }

    //Decode Name
    tmp = new String(data, 1, JEFSFileEntry.MAX_NAME_LEN);
    entry.setName(tmp.trim());

    //Decode Dir Index
    tmp = new String(data, 1 + JEFSFileEntry.MAX_NAME_LEN, JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN);
    tmpI = Integer.parseInt(tmp);
    entry._setDirIndex(tmpI);

    //Decode Parent Dir Index
    tmp = new String(data, 1 + JEFSFileEntry.MAX_NAME_LEN + JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN, JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN);
    tmpI = Integer.parseInt(tmp);
    entry._setParentDirIndex(tmpI);

    //Decode Starting Address
    tmp = new String(data, 1 + JEFSFileEntry.MAX_NAME_LEN + (JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN * 2), JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN);
    tmpL = Long.parseLong(tmp);
    entry._setStartingAddress(tmpL);

    //Decode Length
    tmp = new String(data, 1 + JEFSFileEntry.MAX_NAME_LEN + (JEFSVolHeader.NEXT_DIR_INDEX_MAX_LEN * 2) + JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN,
        JEFSVolHeader.FIRST_FREE_PAGE_ADDRESS_MAX_LEN);
    tmpL = Long.parseLong(tmp);
    entry._setLength(tmpL);

    return entry;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[JEFSFileEntry - ");

    sb.append("FileType: ");
    sb.append(fileType.getTypeName());

    sb.append(", Name: ");
    sb.append((name != null ? name.trim() : null));

    sb.append(", Length: ");
    sb.append(length);

    sb.append(", StartingAddress: ");
    sb.append(startingAddress);

    sb.append(", DirIndex: ");
    sb.append(dirIndex);

    sb.append(", ParentDirIndex: ");
    sb.append(parentDirIndex);

    sb.append("]");

    return sb.toString();
  }

}
