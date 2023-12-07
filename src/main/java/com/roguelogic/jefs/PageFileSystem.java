/**
 * Created Sep 9, 2012
 */
package com.roguelogic.jefs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class PageFileSystem {

  private static final int HEADER_LEN = 4096;
  private static final long FIRST_DATA_PAGE_POS = 4096;

  private static final int DEFAULT_PAGE_SIZE = 4096;

  private static final byte[] MAGIC_STRING = "PGFS>".getBytes();
  private static final int MAGIC_STRING_START_POS = 0;
  private static final int MAGIC_STRING_LEN = 5;

  private static final String VERSION_STRING_1DOT0 = "V1.0";
  private static final int VERSION_STRING_START_POS = 5;
  private static final int VERSION_STRING_LEN = 8;

  private static final int FIRST_DATA_PAGE_START_POS_START_POS = 13;
  private static final int FIRST_DATA_PAGE_START_POS_LEN = 12;

  private static final int FREE_PAGE_START_POS_START_POS = 25;
  private static final int FREE_PAGE_START_POS_LEN = 12;

  private String physicalVolumeFilePath;

  private RandomAccessFile raf;

  private long firstDataPageStartPos;
  private long freePageStartPos;

  private int pageSize;

  private boolean alwaysCreateNewVolume;

  public PageFileSystem() {
    pageSize = DEFAULT_PAGE_SIZE;
  }

  public synchronized void mount() throws JefsException, IOException {
    File f;

    if (raf != null) {
      return;
    }

    if (StringUtils.IsNVL(physicalVolumeFilePath)) {
      throw new JefsException(
          "A Physical Volume File Path MUST first be set by using mount(String path) or mount(String path, Properties conf) before this method can be used to remount a File System!");
    }

    f = new File(physicalVolumeFilePath);

    if (alwaysCreateNewVolume) {
      //Override - Always create a new Volume
      if (f.exists()) {
        f.delete();
      }

      _createVolume();
    }
    else if (f.exists()) {
      //Load existing Volume
      _loadVolume();
    }
    else {
      //Create new Volume
      _createVolume();
    }

  }

  public synchronized void mount(String path) throws JefsException, IOException {
    this.physicalVolumeFilePath = path;
    mount();
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    if (pageSize > DEFAULT_PAGE_SIZE) {
      this.pageSize = pageSize;
    }
  }

  public boolean isAlwaysCreateNewVolume() {
    return alwaysCreateNewVolume;
  }

  public void setAlwaysCreateNewVolume(boolean alwaysCreateNewVolume) {
    this.alwaysCreateNewVolume = alwaysCreateNewVolume;
  }

  public synchronized void unmount() throws JefsException, IOException {
    if (raf != null) {
      raf.close();
      raf = null;
    }
  }

  public synchronized byte[] readPageChain(long chainStartPos) throws IOException, JefsException {
    ByteArrayOutputStream baos;
    Page pg;
    byte[] data = null;

    pg = _readPage(chainStartPos);

    return data;
  }

  private Page _readPage(long pagePos) throws IOException, JefsException {
    Page pg;
    byte[] rawPage;
    int readLen;

    pg = new Page();
    pg.setPageSize(pageSize);

    rawPage = new byte[pageSize];

    readLen = raf.read(rawPage);

    if (readLen != pageSize) {
      throw new JefsException("Page Under Run Error at Position: " + pagePos + "; Expected Size: " + pageSize + "; Actual Size: " + readLen);
    }

    pg.parse(rawPage);

    return pg;
  }

  public synchronized void writePageChain(byte[] data) throws IOException {
    byte[] rawPage, pageHeader, dataSegment;
    int dataSegMaxLen;
    int pgCnt;
    Page pg;
    boolean overflowDetected = false;

    dataSegMaxLen = (pageSize - Page.PAGE_HEADER_LEN);

    if (data.length <= dataSegMaxLen) {
      //Only requires one page...
      pg = _createPage(data);

      _advanceFreePagePosition();

      _writePage(pg);
      _writeHeader();
    }
    else {
      //More than one page required...
      pgCnt = data.length / dataSegMaxLen;

      if (pgCnt * dataSegMaxLen != data.length) {
        //One more page for overflow due to integer division
        pgCnt++;
        overflowDetected = true;
      }

      for (int i = 0; i < pgCnt; i++) {
        if (i == (pgCnt - 1) && overflowDetected) {
          //Last Page is Overflow
          pg = _createPage(data, i * dataSegMaxLen, data.length - ((pgCnt - 1) * dataSegMaxLen));
        }
        else {
          pg = _createPage(data, i * dataSegMaxLen, dataSegMaxLen);
        }

        _advanceFreePagePosition();

        _writePage(pg);
        _writeHeader();
      }
    }

  }

  private void _writePage(Page pg) throws IOException {
    byte[] rawPage;

    rawPage = pg.toRawPage();

    raf.seek(pg.getStartPos());
    raf.write(rawPage);
  }

  private void _advanceFreePagePosition() throws IOException {
    long nextFreePage;

    nextFreePage = _readNextFreePagePos();

    if (nextFreePage > 0) {
      freePageStartPos = nextFreePage;
    }
    else {
      //Advance to end of file
      freePageStartPos = raf.length() + pageSize;
    }
  }

  private long _readNextFreePagePos() {
    // TODO Auto-generated method stub
    return 0;
  }

  private Page _createPage(byte[] data) {
    Page pg = new Page();

    pg.setActive(true);
    pg.setPageSize(pageSize);
    pg.setDataSegLen(data.length);
    pg.setStartPos(freePageStartPos);
    pg.setDataSegment(data);

    return pg;
  }

  private Page _createPage(byte[] data, int offset, int len) {
    byte[] tmp;
    Page pg = new Page();

    pg.setActive(true);
    pg.setPageSize(pageSize);
    pg.setDataSegLen(len);
    pg.setStartPos(freePageStartPos);

    tmp = new byte[len];
    System.arraycopy(data, offset, tmp, 0, len);
    pg.setDataSegment(tmp);

    return pg;
  }

  private void _createVolume() throws IOException {
    raf = new RandomAccessFile(physicalVolumeFilePath, "rw");

    firstDataPageStartPos = 0;
    freePageStartPos = FIRST_DATA_PAGE_POS;

    _writeHeader();
  }

  private void _loadVolume() throws IOException {
    raf = new RandomAccessFile(physicalVolumeFilePath, "rw");
  }

  private void _writeHeader() throws IOException {
    byte[] header;

    header = new byte[HEADER_LEN];

    System.arraycopy(MAGIC_STRING, 0, header, MAGIC_STRING_START_POS, MAGIC_STRING_LEN);

    System.arraycopy(StringUtils.RPad(VERSION_STRING_1DOT0, '*', VERSION_STRING_LEN).getBytes(), 0, header, VERSION_STRING_START_POS, VERSION_STRING_LEN);

    System.arraycopy(StringUtils.LPad(String.valueOf(firstDataPageStartPos), '0', FIRST_DATA_PAGE_START_POS_LEN).getBytes(), 0, header, FIRST_DATA_PAGE_START_POS_START_POS,
        FIRST_DATA_PAGE_START_POS_LEN);

    System.arraycopy(StringUtils.LPad(String.valueOf(freePageStartPos), '0', FREE_PAGE_START_POS_LEN).getBytes(), 0, header, FREE_PAGE_START_POS_START_POS, FREE_PAGE_START_POS_LEN);

    raf.seek(0);
    raf.write(header);
  }

  public static void main(String[] args) {
    PageFileSystem pfs = null;
    int exitCd;
    byte[] tmpArr;

    try {
      pfs = new PageFileSystem();
      pfs.setAlwaysCreateNewVolume(true);

      pfs.mount("C:/Empire/page_test.jefs");

      tmpArr = "Robert C. Ilardi".getBytes();

      pfs.writePageChain(tmpArr);

      tmpArr = SystemUtils.GenerateRandomBytesFixedLen(9000);
      pfs.writePageChain(tmpArr);

      exitCd = 0;
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
      exitCd = 1;
    }
    finally {
      if (pfs != null) {
        try {
          pfs.unmount();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    System.exit(exitCd);
  }

}
