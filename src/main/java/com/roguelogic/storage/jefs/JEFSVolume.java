/**
 * Created Jan 3, 2008
 */
package com.roguelogic.storage.jefs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class JEFSVolume {

  public static final boolean DEBUG = false;

  private String physicalFilePath;
  private JEFSVolHeader _header;

  public JEFSVolume(String physicalFilePath) {
    this.physicalFilePath = physicalFilePath;
  }

  public void formatVolume() throws IOException, JEFSException {
    makeVolume(_header);
  }

  public void makeVolume(JEFSVolHeader header) throws IOException, JEFSException {
    FileOutputStream fos = null;
    int headerLen;

    try {
      //Create or Overwrite File...
      fos = new FileOutputStream(physicalFilePath);

      //Reset Header 
      header._setDirIndex(JEFSVolHeader.INIT_DIR_INDEX);
      header._setFileIndex(JEFSVolHeader.INIT_FILE_INDEX);

      //Default Pages Addresses...
      headerLen = JEFSVolHeader.GetHeaderLen(header.getVersion());

      header._setFirstTrackingPageAddress(headerLen);
      header._setFirstFreePageAddress(headerLen);
      header._setLastFreePageAddress(headerLen);

      fos.close();
      fos = null;

      //Write Out Header
      writeHeader(header);

      //Create Root Directory
      createRootDir(header);
    }
    finally {
      if (fos != null) {
        try {
          fos.close();
        }
        catch (Exception e) {}
        fos = null;
      }
    }
  }

  public void writeHeader(JEFSVolHeader header) throws IOException {
    RandomAccessFile raf = null;

    try {
      raf = new RandomAccessFile(physicalFilePath, "rw");
      writeHeader(header, raf);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
        raf = null;
      }
    }
  }

  private void writeHeader(JEFSVolHeader header, RandomAccessFile raf) throws IOException {
    byte[] data;

    data = header.getBytes();
    raf.seek(0);
    raf.write(data);
  }

  private void createRootDir(JEFSVolHeader header) throws IOException, JEFSException {
    JEFSPageInfo page;
    JEFSFileEntry dir;
    RandomAccessFile raf = null;
    int dirIndex;

    try {
      raf = new RandomAccessFile(physicalFilePath, "rw");

      page = allocateTrackingPage(raf, header);

      dir = new JEFSFileEntry();
      dir._setStartingAddress(0);
      dir.setName("/");
      dir.setFileType(JEFSFileType.Directory);

      dirIndex = popNextDirIndex(raf, header);
      dir._setDirIndex(dirIndex);
      dir._setParentDirIndex(0); //Only the root directory can have ZERO as it's parent directory index

      writeFileEntry(dir, page, header, raf);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }
  }

  private void writeFileEntry(JEFSFileEntry entry, JEFSPageInfo page, JEFSVolHeader header, RandomAccessFile raf) throws IOException, JEFSException {
    long nfeOffset;
    byte[] data;

    if (page.getPageType() != JEFSPageType.Tracking) {
      throw new JEFSException("Can NOT write File Entry to non-tracking page!");
    }

    if (JEFSFileEntry.GetFileEntryDataLen(header) > page.getRemainingUserSegmentLen(header)) {
      throw new JEFSException("File Entry Write Failed! Tracking Page User Data Segment Overflow!");
    }

    //Write File Entry to Page
    nfeOffset = calcOffsetForNextFileEntry(raf, header, page) + entry.getStartingAddress();

    raf.seek(nfeOffset);
    data = entry.getBytes();
    raf.write(data);

    //Update Page Used User Segment Len
    page._setUsedUserSegment(page.getUsedUserSegment() + JEFSFileEntry.GetFileEntryDataLen(header));

    writePageInfo(header, page, raf, false);
  }

  private int popNextDirIndex(RandomAccessFile raf, JEFSVolHeader header) throws IOException {
    int nextDirIndex = header.getDirIndex();

    try {
      header._setDirIndex(nextDirIndex + 1); //Increment
      writeHeader(header, raf);
    }
    catch (IOException e) {
      header._setDirIndex(nextDirIndex); //Push Back
      throw e;
    }

    return nextDirIndex;
  }

  private long calcOffsetForNextFileEntry(RandomAccessFile raf, JEFSVolHeader header, JEFSPageInfo page) throws IOException, JEFSException {
    long nfeOffset;

    nfeOffset = page.getStartingAddress() + JEFSPageInfo.GetUserSegmentOffset(header); //First byte of possible user segment offset on page

    return nfeOffset;
  }

  private JEFSPageInfo allocateTrackingPage(RandomAccessFile raf, JEFSVolHeader header) throws IOException, JEFSException {
    JEFSPageInfo page = null;

    //First, try to obtain a tracking page that is already allocated but as free space
    page = obtainPrevAllocTrkPgWithFreeSpace(raf, header);

    //Second, if there is no previous allocated page with free space
    //try to obtain a free page to use as a new tracking page
    if (page == null) {
      page = obtainNewTrkPgFromFreePages(header);

      //Finally, if there is no free pages in the volume
      //try to create a new tracking page with newly allocated space in the volume
      if (page == null) {
        page = createNewPage(raf, JEFSPageType.Tracking, header);
      }
    }

    return page;
  }

  private JEFSPageInfo obtainPrevAllocTrkPgWithFreeSpace(RandomAccessFile raf, JEFSVolHeader header) throws JEFSException, IOException {
    JEFSPageInfo page;

    page = readNextTrackingPage(header, raf, null);

    while (page != null) {
      debugPrintln(page);

      if (JEFSFileEntry.GetFileEntryDataLen(header) <= page.getRemainingUserSegmentLen(header)) {
        break; //We found a page with free space!
      }

      page = readNextTrackingPage(header, raf, page);
    }

    return page;
  }

  private JEFSPageInfo obtainNewTrkPgFromFreePages(JEFSVolHeader header) {
    // TODO Auto-generated method stub
    return null;
  }

  private JEFSPageInfo createNewPage(RandomAccessFile raf, JEFSPageType pageType, JEFSVolHeader header) throws IOException {
    JEFSPageInfo page = new JEFSPageInfo();

    page._setPageType(pageType);
    page._setStartingAddress(header.getFirstFreePageAddress());
    page._setNextPageChainAddress(0);
    page._setUsedUserSegment(0);

    //Only free page in volume
    header._setFirstFreePageAddress(header.getFirstFreePageAddress() + header.getPageSize());
    header._setLastFreePageAddress(header.getFirstFreePageAddress());

    //Update Header
    writeHeader(header, raf);

    //Update Page Info
    writePageInfo(header, page, raf, true);

    return page;
  }

  private void writePageInfo(JEFSVolHeader header, JEFSPageInfo page, RandomAccessFile raf, boolean fullPageWrite) throws IOException {
    byte[] data, data2;

    raf.seek(page.getStartingAddress());

    data = page.getBytes();

    if (fullPageWrite) {
      data2 = new byte[header.getPageSize()];
      SystemUtils.FillArray(data2, (byte) 0);
      System.arraycopy(data, 0, data2, 0, data.length);

      raf.write(data2);
    }
    else {
      raf.write(data);
    }
  }

  public void mount() throws IOException, JEFSException {
    readHeader();
  }

  private void readHeader() throws IOException, JEFSException {
    RandomAccessFile raf = null;

    try {
      raf = new RandomAccessFile(physicalFilePath, "r");
      _header = JEFSVolHeader.ReadHeader(raf);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
        raf = null;
      }
    }
  }

  private String normalizePath(String path) {
    String[] tokens;
    StringBuffer sb;

    //Normalize
    path = path.trim();
    path = path.replaceAll("//", "/");

    while (path.startsWith("/")) {
      path = path.substring(1, path.length()).trim();
    }

    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1).trim();
    }

    tokens = path.split("/");

    sb = new StringBuffer();

    for (int i = 0; i < tokens.length; i++) {
      sb.append("/");
      sb.append(tokens[i].trim());
    }

    path = sb.toString();

    return path;
  }

  private int determineParentDirIndex(ArrayList<JEFSFileEntry> entryPath) throws JEFSException {
    JEFSFileEntry entry;

    if (entryPath == null || entryPath.size() == 0) {
      throw new JEFSException("Can not determine parent directory index from NULL or Emptry Entry Path!");
    }

    entry = entryPath.get(entryPath.size() - 1); //Last entry

    return entry.getDirIndex();
  }

  public void unmount() {
    _header = null;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("JEFS Volume -\n");

    sb.append("Volume File: ");
    sb.append(physicalFilePath);
    sb.append("\n");

    if (_header != null) {
      sb.append("Status: Volume Mounted\n");
      sb.append(_header);
    }
    else {
      sb.append("Status: Volume NOT Mounted\n");
    }

    return sb.toString();
  }

  public OutputStream openOutputStream(String path) {
    OutputStream outs = null;

    return outs;
  }

  public InputStream openInputStream(String path) {
    InputStream ins = null;

    return ins;
  }

  public void deleteFile(String path) {}

  public boolean fileExists(String path) {
    boolean exists = false;

    return exists;
  }

  public boolean dirExists(String path) throws IOException, JEFSException {
    boolean exists = false;
    ArrayList<JEFSFileEntry> entryPath;
    RandomAccessFile raf = null;

    try {
      raf = new RandomAccessFile(physicalFilePath, "rw");

      entryPath = getFileEntryPath(_header, raf, path);
      exists = entryPath != null && entryPath.size() > 0;
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
        raf = null;
      }
    }

    return exists;
  }

  private boolean dirExists(ArrayList<JEFSFileEntry> entryPath) throws IOException, JEFSException {
    boolean exists = false;

    exists = entryPath != null && entryPath.size() > 0;

    return exists;
  }

  private ArrayList<JEFSFileEntry> getFileEntryPath(JEFSVolHeader header, RandomAccessFile raf, String path) throws IOException, JEFSException {
    //Scan tracking pages in one pass and put possibilities into a hashmap.
    //This method will order the entries into the arraylist it returns.
    //The scan method should take a File Type for dir or file.
    //Need to figure out a way to stop the scan when the hashmap
    //as all possible entries for the path, so that we don't scan
    //the entire tracking page set if we get lucky...

    JEFSPageInfo page;
    JEFSFileEntry entry;
    ArrayList<JEFSFileEntry> pageEntries;
    ArrayList<JEFSFileEntry> pathEntries = new ArrayList<JEFSFileEntry>();
    String[] dirs;
    int dirIndex = 0;
    boolean foundSeg;

    if (!path.equals("/")) {
      dirs = path.split("/");
      dirs[0] = "/"; //Since the split removes the delimiter, add the root directory!
    }
    else {
      dirs = new String[] { "/" };
    }

    page = readNextTrackingPage(header, raf, null);

    while (page != null) {
      debugPrintln(page);

      foundSeg = false;
      pageEntries = readTrackingPageEntries(header, raf, page);

      for (int i = 0; i < pageEntries.size(); i++) {
        entry = pageEntries.get(i);
        debugPrintln("  " + entry);

        if (entry.getFileType() == JEFSFileType.Directory && dirs[dirIndex].equals(entry.getName())) {
          pathEntries.add(entry);

          foundSeg = true;
          dirIndex++;

          break;
        }
      } //End for i loop

      if (dirIndex >= dirs.length) {
        break; //break out of the while loop, were done!
      }
      else if (foundSeg) {
        //Reset to first tracking page to scan for next segment of path
        page = readNextTrackingPage(header, raf, null);
      }
      else {
        //Move on to next tracking page
        page = readNextTrackingPage(header, raf, page);
      }
    } //End while loop

    //If the pathEntries do not contain the same number of entries as segments in the path clear!
    if (pathEntries.size() != dirs.length) {
      pathEntries.clear();
    }

    return pathEntries;
  }

  private ArrayList<JEFSFileEntry> readTrackingPageEntries(JEFSVolHeader header, RandomAccessFile raf, JEFSPageInfo page) throws JEFSException, IOException {
    ArrayList<JEFSFileEntry> pageEntries = new ArrayList<JEFSFileEntry>();
    JEFSFileEntry entry;
    byte[] data;
    int len, totalReadLen;

    //Read Raw Data
    totalReadLen = 0;
    data = new byte[JEFSFileEntry.GetFileEntryDataLen(header)];
    raf.seek(page.getStartingAddress() + JEFSPageInfo.GetUserSegmentOffset(header));

    while (totalReadLen < page.getUsedUserSegment()) {
      len = raf.read(data);

      if (len != data.length) {
        throw new JEFSException("Tracking Page File Entry Read Error (Under Run) at byte: " + raf.getFilePointer());
      }

      entry = JEFSFileEntry.Decode(data, header);

      pageEntries.add(entry);

      totalReadLen += len;
    }

    return pageEntries;
  }

  private JEFSPageInfo readNextTrackingPage(JEFSVolHeader header, RandomAccessFile raf, JEFSPageInfo prevPage) throws IOException, JEFSException {
    JEFSPageInfo page = null;
    long offset;

    if (prevPage == null) {
      offset = header.getFirstTrackingPageAddress();
    }
    else if (prevPage != null && prevPage.getNextPageChainAddress() > 0) {
      offset = prevPage.getNextPageChainAddress();
    }
    else {
      //No more pages in page chain...
      return null;
    }

    page = readPageHeader(raf, header, offset);

    return page;
  }

  private JEFSPageInfo readPageHeader(RandomAccessFile raf, JEFSVolHeader header, long offset) throws IOException, JEFSException {
    JEFSPageInfo page = null;
    byte[] data;
    int len;

    if (offset < raf.length()) {
      //Read Raw Data
      data = new byte[JEFSPageInfo.GetPageHeaderLen(header)];

      raf.seek(offset);
      len = raf.read(data);

      if (len != data.length) {
        throw new JEFSException("Page Header Read Error (Under Run) at byte: " + raf.getFilePointer());
      }

      page = JEFSPageInfo.Decode(data, header);
    }

    return page;
  }

  private int getNextFreeUserSegmentOffset(JEFSPageInfo page, JEFSVolHeader header) throws JEFSException {
    int offset;

    offset = page.getUsedUserSegment();

    return offset;
  }

  public void deleteDir(String path) throws JEFSException, IOException {
    JEFSPageInfo page;
    JEFSFileEntry dir;
    RandomAccessFile raf = null;
    ArrayList<JEFSFileEntry> entryPath;

    try {
      if (StringUtils.IsNVL(path)) {
        throw new JEFSException("Directory Path can NOT be NULL or Empty!");
      }

      path = normalizePath(path);

      raf = new RandomAccessFile(physicalFilePath, "rw");

      if (dirExists(getFileEntryPath(_header, raf, path))) {
        throw new JEFSException("Directory '" + path + "' Already Exists!");
      }

      /*page = allocateTrackingPage(raf, _header);

      dir = new JEFSFileEntry();
      dir._setStartingAddress(getNextFreeUserSegmentOffset(page, _header));
      dir.setName(tokens[tokens.length - 1].trim());
      dir.setFileType(JEFSFileType.Directory);

      dirIndex = popNextDirIndex(raf, _header);
      parentDirIndex = determineParentDirIndex(entryPath);

      dir._setDirIndex(dirIndex);
      dir._setParentDirIndex(parentDirIndex);

      writeFileEntry(dir, page, _header, raf);*/
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }
  }

  public void makeDir(String path) throws JEFSException, IOException {
    JEFSPageInfo page;
    JEFSFileEntry dir;
    RandomAccessFile raf = null;
    int dirIndex, parentDirIndex;
    String[] tokens;
    String childDir, parentDir;
    ArrayList<JEFSFileEntry> entryPath;

    try {
      if (StringUtils.IsNVL(path)) {
        throw new JEFSException("Directory Path can NOT be NULL or Empty!");
      }

      path = normalizePath(path);

      //Tokenize / Split into separate directories
      tokens = path.split("/");
      childDir = tokens[tokens.length - 1];

      if (tokens.length - 1 == 1) {
        parentDir = "/"; //Root 
      }
      else {
        parentDir = StringUtils.CombineWithDelimiter(tokens, "/", tokens.length - 1, false, false);
      }

      if (childDir.length() > JEFSFileEntry.MAX_NAME_LEN) {
        throw new JEFSException("New Directory Name can NOT be NULL or Empty!");
      }

      raf = new RandomAccessFile(physicalFilePath, "rw");

      entryPath = getFileEntryPath(_header, raf, parentDir);

      if (!dirExists(entryPath)) {
        throw new JEFSException("Parent Directory \"" + parentDir + "\" does NOT Exists!");
      }

      if (dirExists(getFileEntryPath(_header, raf, path))) {
        throw new JEFSException("Directory '" + path + "' Already Exists!");
      }

      page = allocateTrackingPage(raf, _header);

      dir = new JEFSFileEntry();
      dir._setStartingAddress(getNextFreeUserSegmentOffset(page, _header));
      dir.setName(tokens[tokens.length - 1].trim());
      dir.setFileType(JEFSFileType.Directory);

      dirIndex = popNextDirIndex(raf, _header);
      parentDirIndex = determineParentDirIndex(entryPath);

      dir._setDirIndex(dirIndex);
      dir._setParentDirIndex(parentDirIndex);

      writeFileEntry(dir, page, _header, raf);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }
  }

  public void renameFile(String path) {}

  public void renameDir(String path) {}

  public ArrayList<JEFSFileEntry> list(String path) {
    ArrayList<JEFSFileEntry> ls = null;

    return ls;
  }

  private void debugPrintln(Object obj) {
    if (DEBUG) {
      System.out.println(obj);
    }
  }

  public static void main(String[] args) throws Exception {
    JEFSVolume vol = new JEFSVolume("C:/Empire/rilardi.jefs");
    JEFSVolHeader header = new JEFSVolHeader();

    header.setName("Robert's Drive");
    vol.makeVolume(header);

    vol.mount();
    System.out.println(vol);

    vol.makeDir("/tmp");

    vol.makeDir("/tmp/robert");

    vol.makeDir("/tmp/junk/");

    for (int i = 1; i <= 100; i++) {
      vol.makeDir(new StringBuffer().append("/tmp/junk/").append(StringUtils.GenerateTimeUniqueId()).toString());
    }

    vol.deleteDir("/tmp/robert");

    /*String[] sArr = new String[10000];
     long[] lArr = new long[sArr.length];
     int[] iArr = new int[sArr.length];

     for (int i = 0; i < sArr.length; i++) {
     StringBuffer sb = new StringBuffer();

     for (int j = 1; j <= 32; j++) {
     sb.append('A' + Math.random() * 26);
     }

     sArr[i] = sb.toString();
     iArr[i] = i;
     lArr[i] = (long) i;

     sb = null;

     if (i % 1000 == 0) {
     System.gc();
     }
     }*/

    /*JEFSFileEntry[] feArr = new JEFSFileEntry[10000];

     for (int i = 0; i < feArr.length; i++) {
     feArr[i] = new JEFSFileEntry();

     StringBuffer sb = new StringBuffer();

     for (int j = 1; j <= 32; j++) {
     sb.append('A' + Math.random() * 26);
     }

     feArr[i].setName(sb.toString());

     sb = null;

     if (i % 1000 == 0) {
     System.gc();
     }
     }

     System.gc();

     Thread.sleep(30000);*/
  }

}
