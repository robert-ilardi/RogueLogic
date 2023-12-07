/**
 * Created Jan 8, 2009
 */
package com.roguelogic.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Robert C. Ilardi
 * 
 */

public class FileBackedQueue implements StorageBackedQueue {

  public static final int OBJ_BIN_LEN_MAX_LEN = 10;

  public static final byte PUSHED_FLAG = (byte) 49;
  public static final byte POPPED_FLAG = (byte) 48;

  private String queueFilePath;
  private int maxInMemQueueLen;
  private boolean reinitFromExistingQueueFile;

  private ArrayList<Serializable> memQ;
  private RandomAccessFile fileQ;

  private long headPos;
  private long tailPos;

  public FileBackedQueue(String queueFilePath, int maxInMemQueueLen, boolean reinitFromExistingQueueFile) {
    this.queueFilePath = queueFilePath;
    this.maxInMemQueueLen = maxInMemQueueLen;
    this.reinitFromExistingQueueFile = reinitFromExistingQueueFile;
  }

  // Start Public User Methods
  // ------------------------------------------------------->

  public void initQueue() throws StorageBackedQueueException {
    try {
      memQ = new ArrayList<Serializable>();

      fileQ = new RandomAccessFile(queueFilePath, "rw");

      if (reinitFromExistingQueueFile && fileQ.length() > 0) {
        _reinitExistingQueue();
      }
      else {
        _initNewQueue();
      }
    } // End try block
    catch (Exception e) {
      throw new StorageBackedQueueException(e);
    }
  }

  public void destroyQueue() throws StorageBackedQueueException {
    File f;

    try {
      if (memQ != null) {
        memQ.clear();
      }

      if (fileQ != null) {
        fileQ.close();
      }

      f = new File(queueFilePath);
      f.delete();

      memQ = null;
      fileQ = null;
    } // End try block
    catch (Exception e) {
      throw new StorageBackedQueueException(e);
    }
  }

  public synchronized void enqueue(Serializable element) throws StorageBackedQueueException {
    try {
      if (memQ.size() >= maxInMemQueueLen) {
        _appendElementToFile(element);
      }
      else {
        memQ.add(element);
      }
    } // End try block
    catch (Exception e) {
      throw new StorageBackedQueueException(e);
    }
  }

  public synchronized Serializable dequeue() throws StorageBackedQueueException {
    Serializable element = null;

    try {
      if (maxInMemQueueLen <= 0) {
        // Never In Memory
        element = _readNextFileElement();
        _markNextFileElementAsPopped();
      }
      else {
        // Could be in memory, if not load into memory first...
        _ensureMemoryIsLoaded();
        element = memQ.remove(0);
      }
    } // End try block
    catch (Exception e) {
      throw new StorageBackedQueueException(e);
    }

    return element;
  }

  public synchronized Serializable peek() throws StorageBackedQueueException {
    Serializable element = null;

    try {
      if (maxInMemQueueLen <= 0) {
        // Never In Memory
        element = _readNextFileElement();
      }
      else {
        // Could be in memory, if not load into memory first...
        _ensureMemoryIsLoaded();
        element = memQ.get(0);
      }
    } // End try block
    catch (Exception e) {
      throw new StorageBackedQueueException(e);
    }

    return element;
  }

  public synchronized void clear() throws StorageBackedQueueException {
    try {
      if (memQ != null) {
        memQ.clear();
      }

      if (fileQ != null) {
        fileQ.setLength(0);
      }
    } // End try block
    catch (Exception e) {
      throw new StorageBackedQueueException(e);
    }
  }

  public synchronized boolean isEmpty() {
    return memQ.isEmpty() && (headPos == tailPos);
  }

  public synchronized void closeQueue() throws StorageBackedQueueException {
    Serializable element;

    try {
      if (fileQ != null) {
        if (memQ != null) {
          while (!memQ.isEmpty()) {
            element = memQ.remove(0);
            _appendElementToFile(element);
          }
        }

        fileQ.close();
      }

      fileQ = null;
      memQ = null;
    } // End try block
    catch (Exception e) {
      throw new StorageBackedQueueException(e);
    }
  }

  // ------------------------------------------------------->
  // End Public User Methods

  // Start Private Internal Methods
  // ------------------------------------------------------->

  private void _appendElementToFile(Serializable element) throws IOException {
    byte[] fileElement;

    fileElement = _toFileElement(element);

    fileQ.seek(tailPos);
    fileQ.write(fileElement);

    tailPos += fileElement.length;
  }

  private byte[] _toFileElement(Serializable element) throws IOException {
    byte[] objBin, fileElement, lenBin;

    objBin = SystemUtils.ToByteArray(element);

    lenBin = IntUtils.IntToBytes(objBin.length);
    lenBin = IntUtils.AddZeroPadding(lenBin, OBJ_BIN_LEN_MAX_LEN);

    fileElement = new byte[1 + objBin.length + lenBin.length];

    fileElement[0] = PUSHED_FLAG;
    System.arraycopy(lenBin, 0, fileElement, 1, lenBin.length);
    System.arraycopy(objBin, 0, fileElement, 1 + lenBin.length, objBin.length);

    return fileElement;
  }

  private synchronized void _ensureMemoryIsLoaded() throws IOException, ClassNotFoundException {
    Serializable element = null;

    while (memQ.size() < maxInMemQueueLen && headPos != tailPos) {
      element = _readNextFileElement();
      _markNextFileElementAsPopped();

      memQ.add(element);
    }

    _truncateFileIfNeeded();
  }

  private synchronized Serializable _readNextFileElement() throws IOException, ClassNotFoundException {
    Serializable element = null;
    byte[] header, objBin;
    int len;

    header = _readNextFileElementHeader();
    len = IntUtils.BytesToInt(header, 1);

    objBin = new byte[len];
    len = fileQ.read(objBin);

    if (len != objBin.length) {
      throw new IOException("File Element Object Data Read Under Run! Expected: " + objBin.length + " bytes ; Actual Read: " + len + " bytes");
    }

    element = (Serializable) SystemUtils.ToObject(objBin);

    return element;
  }

  private synchronized void _markNextFileElementAsPopped() throws IOException {
    fileQ.seek(headPos);
    fileQ.write(POPPED_FLAG);

    _advanceHeadPosition();
  }

  private void _advanceHeadPosition() throws IOException {
    byte[] header;
    int len;

    header = _readNextFileElementHeader();
    len = IntUtils.BytesToInt(header, 1);

    headPos += header.length + len;
  }

  private byte[] _readNextFileElementHeader() throws IOException {
    byte[] header = new byte[1 + OBJ_BIN_LEN_MAX_LEN];
    int len;

    fileQ.seek(headPos);
    len = fileQ.read(header);

    if (len != header.length) {
      throw new IOException("File Element Header Data Read Under Run! Expected: " + header.length + " bytes ; Actual Read: " + len + " bytes");
    }

    return header;
  }

  private void _truncateFileIfNeeded() throws IOException {
    if (headPos == tailPos && fileQ != null) {
      fileQ.setLength(0);
      headPos = 0;
      tailPos = 0;
    }
  }

  private void _reinitExistingQueue() throws IOException {
    byte[] header;
    long fLen = fileQ.length();

    // Find Head Position...
    headPos = 0;

    if (headPos != fLen) {
      header = _readNextFileElementHeader();
      _advanceHeadPosition();

      while (PUSHED_FLAG != header[0] && headPos != fLen) {
        header = _readNextFileElementHeader();
        _advanceHeadPosition();
      }
    }

    // Init Tail Position to end of file...
    tailPos = fLen;
  }

  private void _initNewQueue() throws IOException {
    fileQ.setLength(0);

    headPos = 0;
    tailPos = 0;
  }

  // ------------------------------------------------------->
  // End Private Internal Methods

  public static void main(String[] args) {
    FileBackedQueue queue = null;
    String tmp;

    try {
      queue = new FileBackedQueue("test.queue", 100, false);
      queue.initQueue();

      /*
       * while (!queue.isEmpty()) { System.out.println(queue.dequeue()); }
       * 
       * for (int i = 1; i <= 10; i++) { // tmp =
       * StringUtils.GenerateTimeUniqueId(); tmp = "Element" + i;
       * queue.enqueue(tmp); }
       * 
       * queue.closeQueue();
       * 
       * System.exit(0);
       * 
       * while (!queue.isEmpty()) { System.out.println(queue.dequeue()); }
       */

      for (int i = 1; i <= 20000; i++) {
        // tmp = StringUtils.GenerateTimeUniqueId();
        tmp = "Element" + i;
        queue.enqueue(tmp);
      }

      while (!queue.isEmpty()) {
        System.out.println(queue.dequeue());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (queue != null) {
        try {
          queue.destroyQueue();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

}
