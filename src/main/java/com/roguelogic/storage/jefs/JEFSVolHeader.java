/**
 * Created Jan 3, 2008
 */
package com.roguelogic.storage.jefs;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class JEFSVolHeader {

  public static final String FILE_FORMAT_SIGNATURE = "$RL-VRTL-FS-VLM$";

  public static final String VERSION_PARAMETER = "Version";
  public static final String VERSION = "1.0";
  public static final int VERSION_MAX_LEN = 10;

  public static final String VOLUME_NAME_PARAMETER = "VolumeName";
  public static final int VOLUME_NAME_MAX_LEN = 64;

  public static final String NEXT_DIR_INDEX_PARAMETER = "NextDirIndex";
  public static final int NEXT_DIR_INDEX_MAX_LEN = 10;
  public static final int INIT_DIR_INDEX = 1;

  public static final String NEXT_FILE_INDEX_PARAMETER = "NextFileIndex";
  public static final int NEXT_FILE_INDEX_MAX_LEN = 10;
  public static final int INIT_FILE_INDEX = 1;

  public static final String PAGE_SIZE_PARAMETER = "PageSize";
  public static final int PAGE_SIZE_MAX_LEN = 7;
  public static final int DEFAULT_PAGE_SIZE = 4092;

  public static final String FIRST_TRACKING_PAGE_ADDRESS_PARAMETER = "FirstTrackingPageAddress";
  public static final int FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN = 15;

  public static final String FIRST_FREE_PAGE_ADDRESS_PARAMETER = "FirstFreePageAddress";
  public static final int FIRST_FREE_PAGE_ADDRESS_MAX_LEN = 15;

  public static final String LAST_FREE_PAGE_ADDRESS_PARAMETER = "LastFreePageAddress";
  public static final int LAST_FREE_PAGE_ADDRESS_MAX_LEN = 15;

  public static final String NEXT_VOLUME_LINK_PARAMETER = "NextVolumeLink";
  public static final int NEXT_VOLUME_LINK_LEN = 256;

  public static final int HEADER_LENGTH = FILE_FORMAT_SIGNATURE.length() + 1 + VERSION_PARAMETER.length() + VERSION_MAX_LEN + 2 + VOLUME_NAME_PARAMETER.length() + VOLUME_NAME_MAX_LEN + 2
      + NEXT_DIR_INDEX_PARAMETER.length() + NEXT_DIR_INDEX_MAX_LEN + 2 + NEXT_FILE_INDEX_PARAMETER.length() + NEXT_FILE_INDEX_MAX_LEN + 2 + PAGE_SIZE_PARAMETER.length() + PAGE_SIZE_MAX_LEN + 2
      + FIRST_TRACKING_PAGE_ADDRESS_PARAMETER.length() + FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN + 2 + FIRST_FREE_PAGE_ADDRESS_PARAMETER.length() + FIRST_FREE_PAGE_ADDRESS_MAX_LEN + 2
      + LAST_FREE_PAGE_ADDRESS_PARAMETER.length() + LAST_FREE_PAGE_ADDRESS_MAX_LEN + 2 + NEXT_VOLUME_LINK_PARAMETER.length() + NEXT_VOLUME_LINK_LEN + 2;

  private String name;
  private String version = VERSION;
  private int dirIndex = INIT_DIR_INDEX;
  private int fileIndex = INIT_FILE_INDEX;
  private int pageSize = DEFAULT_PAGE_SIZE;
  private long firstTrackingPageAddress;
  private long firstFreePageAddress;
  private long lastFreePageAddress;
  private String nextVolLink;

  public JEFSVolHeader() {}

  public static JEFSVolHeader ReadHeader(RandomAccessFile raf) throws IOException, JEFSException {
    JEFSVolHeader header = null;
    String tmp;
    String[] tokens;
    boolean found;

    raf.seek(0);

    if (!CheckFileSignature(raf)) {
      throw new JEFSException("Invalid Volume File Format Signature!");
    }

    header = new JEFSVolHeader();

    //Read Version
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.VERSION_PARAMETER.equals(tokens[0].trim()) && JEFSVolHeader.VERSION.equals(tokens[1].trim());

      if (!found) {
        throw new JEFSException("Volume File Format Version NOT Supported or Volume is Corrupt!");
      }

      header._setVersion(tokens[1].trim());
    }

    //Read Volume Name
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.VOLUME_NAME_PARAMETER.equals(tokens[0].trim()) && !StringUtils.IsNVL(tokens[1]);

      if (!found) {
        throw new JEFSException("Volume Name NOT Found. Volume appears to be Corrupt!");
      }

      header.setName(tokens[1].trim());
    }

    //Read Next Dir Index
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.NEXT_DIR_INDEX_PARAMETER.equals(tokens[0].trim()) && !StringUtils.IsNVL(tokens[1]);

      if (!found) {
        throw new JEFSException("Volume Next Directory Index NOT Found. Volume appears to be Corrupt!");
      }

      try {
        header._setDirIndex(Integer.parseInt(tokens[1].trim()));
      }
      catch (Exception e) {
        throw new JEFSException("Volume Next Directory Index is Invalid. Volume appears to be Corrupt!");
      }
    }

    //Read Next File Index
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.NEXT_FILE_INDEX_PARAMETER.equals(tokens[0].trim()) && !StringUtils.IsNVL(tokens[1]);

      if (!found) {
        throw new JEFSException("Volume Next File Index NOT Found. Volume appears to be Corrupt!");
      }

      try {
        header._setFileIndex(Integer.parseInt(tokens[1].trim()));
      }
      catch (Exception e) {
        throw new JEFSException("Volume Next File Index is Invalid. Volume appears to be Corrupt!");
      }
    }

    //Read Page Size Parameter
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.PAGE_SIZE_PARAMETER.equals(tokens[0].trim()) && !StringUtils.IsNVL(tokens[1]);

      if (!found) {
        throw new JEFSException("Volume Page Size Parameter NOT Found. Volume appears to be Corrupt!");
      }

      try {
        header._setPageSize(Integer.parseInt(tokens[1].trim()));
      }
      catch (Exception e) {
        throw new JEFSException("Volume Page Size Parameter is Invalid. Volume appears to be Corrupt!");
      }
    }

    //Read First Tracking Page Address Parameter
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.FIRST_TRACKING_PAGE_ADDRESS_PARAMETER.equals(tokens[0].trim()) && !StringUtils.IsNVL(tokens[1]);

      if (!found) {
        throw new JEFSException("Volume First Tracking Page Address Parameter NOT Found. Volume appears to be Corrupt!");
      }

      try {
        header._setFirstTrackingPageAddress(Integer.parseInt(tokens[1].trim()));
      }
      catch (Exception e) {
        throw new JEFSException("Volume First Tracking Page Address Parameter is Invalid. Volume appears to be Corrupt!");
      }
    }

    //Read First Free Page Address Parameter
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.FIRST_FREE_PAGE_ADDRESS_PARAMETER.equals(tokens[0].trim()) && !StringUtils.IsNVL(tokens[1]);

      if (!found) {
        throw new JEFSException("Volume First Free Page Address Parameter NOT Found. Volume appears to be Corrupt!");
      }

      try {
        header._setFirstFreePageAddress(Integer.parseInt(tokens[1].trim()));
      }
      catch (Exception e) {
        throw new JEFSException("Volume First Free Page Address Parameter is Invalid. Volume appears to be Corrupt!");
      }
    }

    //Read Last Free Page Address Parameter
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.LAST_FREE_PAGE_ADDRESS_PARAMETER.equals(tokens[0].trim()) && !StringUtils.IsNVL(tokens[1]);

      if (!found) {
        throw new JEFSException("Volume Last Free Page Address Parameter NOT Found. Volume appears to be Corrupt!");
      }

      try {
        header._setLastFreePageAddress(Integer.parseInt(tokens[1].trim()));
      }
      catch (Exception e) {
        throw new JEFSException("Volume Last Free Page Address Parameter is Invalid. Volume appears to be Corrupt!");
      }
    }

    //Read Next Volume Link
    tmp = raf.readLine();

    if (!StringUtils.IsNVL(tmp)) {
      tokens = tmp.trim().split("=", 2);

      found = tokens != null && tokens.length == 2 && JEFSVolHeader.NEXT_VOLUME_LINK_PARAMETER.equals(tokens[0].trim());

      if (!found) {
        throw new JEFSException("Next Volume Link NOT Found. Volume appears to be Corrupt!");
      }

      if (!StringUtils.IsNVL(tokens[1])) {
        header._setNextVolLink(tokens[1].trim());
      }
      else {
        header._setNextVolLink(null);
      }
    }

    return header;
  }

  private static boolean CheckFileSignature(RandomAccessFile raf) throws IOException {
    boolean foundSig = false;
    String tmp;

    tmp = raf.readLine();

    foundSig = (tmp != null && JEFSVolHeader.FILE_FORMAT_SIGNATURE.equals(tmp.trim()));

    return foundSig;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) throws JEFSException {
    if (name != null && name.length() > VOLUME_NAME_MAX_LEN) {
      throw new JEFSException("Volume Name must not be longer than " + VOLUME_NAME_MAX_LEN + " characters!");
    }

    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void _setVersion(String version) {
    this.version = version;
  }

  public int getDirIndex() {
    return dirIndex;
  }

  public void _setDirIndex(int dirIndex) {
    this.dirIndex = dirIndex;
  }

  public int getFileIndex() {
    return fileIndex;
  }

  public void _setFileIndex(int fileIndex) {
    this.fileIndex = fileIndex;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void _setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public String getNextVolLink() {
    return nextVolLink;
  }

  public void _setNextVolLink(String nextVolLink) {
    this.nextVolLink = nextVolLink;
  }

  public long getFirstTrackingPageAddress() {
    return firstTrackingPageAddress;
  }

  public void _setFirstTrackingPageAddress(long firstTrackingPageAddress) {
    this.firstTrackingPageAddress = firstTrackingPageAddress;
  }

  public long getFirstFreePageAddress() {
    return firstFreePageAddress;
  }

  public void _setFirstFreePageAddress(long firstFreePageAddress) {
    this.firstFreePageAddress = firstFreePageAddress;
  }

  public long getLastFreePageAddress() {
    return lastFreePageAddress;
  }

  public void _setLastFreePageAddress(long lastFreePageAddress) {
    this.lastFreePageAddress = lastFreePageAddress;
  }

  public byte[] getBytes() {
    byte[] data = null;
    StringBuffer header = new StringBuffer();

    header.append(FILE_FORMAT_SIGNATURE);
    header.append("\n");

    header.append(VERSION_PARAMETER);
    header.append("=");
    header.append(StringUtils.RPad(version, ' ', VERSION_MAX_LEN));
    header.append("\n");

    header.append(VOLUME_NAME_PARAMETER);
    header.append("=");
    header.append(StringUtils.RPad(name, ' ', VOLUME_NAME_MAX_LEN));
    header.append("\n");

    header.append(NEXT_DIR_INDEX_PARAMETER);
    header.append("=");
    header.append(StringUtils.LPad(String.valueOf(dirIndex), '0', NEXT_DIR_INDEX_MAX_LEN));
    header.append("\n");

    header.append(NEXT_FILE_INDEX_PARAMETER);
    header.append("=");
    header.append(StringUtils.LPad(String.valueOf(fileIndex), '0', NEXT_FILE_INDEX_MAX_LEN));
    header.append("\n");

    header.append(PAGE_SIZE_PARAMETER);
    header.append("=");
    header.append(StringUtils.LPad(String.valueOf(pageSize), '0', PAGE_SIZE_MAX_LEN));
    header.append("\n");

    header.append(FIRST_TRACKING_PAGE_ADDRESS_PARAMETER);
    header.append("=");
    header.append(StringUtils.LPad(String.valueOf(firstTrackingPageAddress), '0', FIRST_TRACKING_PAGE_ADDRESS_MAX_LEN));
    header.append("\n");

    header.append(FIRST_FREE_PAGE_ADDRESS_PARAMETER);
    header.append("=");
    header.append(StringUtils.LPad(String.valueOf(firstFreePageAddress), '0', FIRST_FREE_PAGE_ADDRESS_MAX_LEN));
    header.append("\n");

    header.append(LAST_FREE_PAGE_ADDRESS_PARAMETER);
    header.append("=");
    header.append(StringUtils.LPad(String.valueOf(lastFreePageAddress), '0', LAST_FREE_PAGE_ADDRESS_MAX_LEN));
    header.append("\n");

    header.append(NEXT_VOLUME_LINK_PARAMETER);
    header.append("=");
    header.append(StringUtils.RPad(nextVolLink, ' ', NEXT_VOLUME_LINK_LEN));
    header.append("\n");

    data = header.toString().getBytes();

    return data;
  }

  public String toString() {
    StringBuffer header = new StringBuffer();

    header.append("JEFS Volume Header -\n");

    header.append(VERSION_PARAMETER);
    header.append("=");
    header.append(version);
    header.append("\n");

    header.append(VOLUME_NAME_PARAMETER);
    header.append("=");
    header.append(name);
    header.append("\n");

    header.append(NEXT_DIR_INDEX_PARAMETER);
    header.append("=");
    header.append(dirIndex);
    header.append("\n");

    header.append(NEXT_FILE_INDEX_PARAMETER);
    header.append("=");
    header.append(fileIndex);
    header.append("\n");

    header.append(PAGE_SIZE_PARAMETER);
    header.append("=");
    header.append(pageSize);
    header.append("\n");

    header.append(FIRST_TRACKING_PAGE_ADDRESS_PARAMETER);
    header.append("=");
    header.append(firstTrackingPageAddress);
    header.append("\n");

    header.append(FIRST_FREE_PAGE_ADDRESS_PARAMETER);
    header.append("=");
    header.append(firstFreePageAddress);
    header.append("\n");

    header.append(LAST_FREE_PAGE_ADDRESS_PARAMETER);
    header.append("=");
    header.append(lastFreePageAddress);
    header.append("\n");

    header.append(NEXT_VOLUME_LINK_PARAMETER);
    header.append("=");
    header.append(nextVolLink);
    header.append("\n");

    return header.toString();
  }

  public static int GetHeaderLen(String version) throws JEFSException {
    if (VERSION.equals(version)) {
      return HEADER_LENGTH;
    }
    else {
      throw new JEFSException("Unsupported JEFS Version!");
    }
  }

}
