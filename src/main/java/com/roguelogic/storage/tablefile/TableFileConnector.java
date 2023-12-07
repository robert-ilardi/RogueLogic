/**
 * Created Sep 14, 2007
 */
package com.roguelogic.storage.tablefile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.roguelogic.util.FilenameUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class TableFileConnector {

  protected String url;
  protected TableFile tableFile;

  protected boolean debug = false;

  public TableFileConnector(String url) {
    this.url = url;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public String getUrl() {
    return url;
  }

  public String getName() {
    return (tableFile != null ? tableFile.getName() : null);
  }

  public synchronized void open() throws IOException, TableFileException {
    RandomAccessFile raf = null;

    try {
      raf = getRandomAccessFile();
      tableFile = new TableFile();
      tableFile.load(raf);

      debugPrintln(tableFile.toString());
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

  public synchronized void close() throws IOException {
    if (tableFile != null) {
      tableFile.close();
      tableFile = null;
    }
  }

  protected void store() throws IOException, TableFileException {
    RandomAccessFile raf = null;

    try {
      raf = getRandomAccessFile();
      tableFile.store(raf);
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

  protected RandomAccessFile getRandomAccessFile() throws IOException {
    RandomAccessFile raf = null;

    try {
      raf = new RandomAccessFile(url, "rw");
    }
    catch (IOException e) {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e2) {}
      }

      throw e;
    }

    return raf;
  }

  private void debugPrintln(String s) {
    if (debug) {
      System.out.println(s);
    }
  }

  public synchronized void create(TableDefinition def) throws TableFileException {
    if (def == null) {
      throw new TableFileException("NULL Table Definition");
    }

    if (!def.valid()) {
      throw new TableFileException("Invalid Table Definition");
    }

    try {
      tableFile = new TableFile(def);

      store();
    }
    catch (Exception e) {
      throw new TableFileException("An error occurred while attempting to write out Table File! System Message: " + e.getMessage(), e);
    }
  }

  public synchronized void drop() throws TableFileException, IOException {
    File tFile;

    tFile = new File(url);
    tFile.delete();

    close();
  }

  public synchronized TableRecordSet select(Query q) throws TableFileException, IOException {
    TableRecordSet ts = null;
    RandomAccessFile raf = null;

    try {
      raf = getRandomAccessFile();
      ts = tableFile.select(raf, q);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return ts;
  }

  public synchronized void insert(TableRecord rec) throws TableFileException, IOException {
    RandomAccessFile raf = null;

    try {
      if (rec != null) {
        tableFile.validate(rec);
        raf = getRandomAccessFile();
        tableFile.insert(raf, rec);
      }
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

  public synchronized int delete(Query q) throws TableFileException, IOException {
    int recCnt = 0;
    RandomAccessFile raf = null;

    try {
      raf = getRandomAccessFile();
      recCnt = tableFile.delete(raf, q);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return recCnt;
  }

  public synchronized int update(Query q) throws TableFileException, IOException {
    int recCnt = 0;
    RandomAccessFile raf = null;

    try {
      raf = getRandomAccessFile();
      recCnt = tableFile.update(raf, q);
    }
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }

    return recCnt;
  }

  public synchronized void reorg() throws IOException, TableFileException {
    RandomAccessFile srcRaf = null, destRaf = null;
    File tmpFile;

    try {
      tmpFile = File.createTempFile("rltf", null, new File(FilenameUtils.GetParentDirectory(url)));

      srcRaf = getRandomAccessFile();
      destRaf = new RandomAccessFile(tmpFile, "rw");

      tableFile.reorg(srcRaf, destRaf);

      srcRaf.close();
      srcRaf = null;
      destRaf.close();
      destRaf = null;

      (new File(url)).delete();
      tmpFile.renameTo(new File(url));
    }
    finally {
      if (srcRaf != null) {
        try {
          srcRaf.close();
        }
        catch (Exception e) {}
      }

      if (destRaf != null) {
        try {
          destRaf.close();
        }
        catch (Exception e) {}
      }
    }
  }

  public void createMemoryIndexOn(String fieldName) throws IOException, TableFileException {
    RandomAccessFile raf = null;

    try {
      raf = getRandomAccessFile();
      tableFile.createMemoryIndexOn(raf, fieldName);
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

}
