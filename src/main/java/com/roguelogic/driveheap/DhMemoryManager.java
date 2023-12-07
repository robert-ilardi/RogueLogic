/**
 * Created Sep 24, 2011
 */
package com.roguelogic.driveheap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class DhMemoryManager {

  private static final int HEAP_BASE_NODE_SIZE = 1024;
  private static final int HEAP_START_ADDRESS = 1024;

  private static final int HEAP_NODE_HEADER_CHAIN_NEXT_ADDRESS_LEN = 16;
  private static final int HEAP_NODE_HEADER_CHAIN_NEXT_ADDRESS_OFFSET = 0;

  private static final int HEAP_NODE_HEADER_SIZE = HEAP_NODE_HEADER_CHAIN_NEXT_ADDRESS_LEN;
  private static final int HEAP_NODE_DATA_SPACE_SIZE = HEAP_BASE_NODE_SIZE - HEAP_NODE_HEADER_SIZE;
  private static final int HEAP_NODE_DATA_OFFSET = HEAP_NODE_HEADER_SIZE;
  private static final int HEAP_NODE_TOTAL_SIZE = HEAP_NODE_HEADER_SIZE + HEAP_NODE_DATA_SPACE_SIZE;

  private static DhMemoryManager instance = null;

  private boolean inited;
  private String filePath;

  private RandomAccessFile heapFile;
  private long nextAddress;
  private long deallocatedChainAddress;

  private ArrayList<DhPointer> allocatedPtrs;

  private DhMemoryManager() {
    allocatedPtrs = new ArrayList<DhPointer>();

    inited = false;
    filePath = null;
    heapFile = null;
    nextAddress = HEAP_START_ADDRESS;
    deallocatedChainAddress = 0;
  }

  public static synchronized DhMemoryManager getInstance() {
    if (instance == null) {
      instance = new DhMemoryManager();
    }

    return instance;
  }

  public synchronized void init(String filePath) throws DhException, IOException {
    if (inited) {
      throw new DhException("Drive Heap Memory Manager Already Initialized!");
    }

    this.filePath = filePath;

    openHeapFile();
    heapFile.setLength(0);

    inited = true;
  }

  public synchronized DhPointer malloc(int len) throws IOException, DhException {
    DhPointer ptr;
    int blocks;
    long address;

    blocks = _getBlocksRequired(len);

    address = _allocateBlocks(blocks);

    ptr = new DhPointer(address, len, blocks);
    allocatedPtrs.add(ptr);

    return ptr;
  }

  public synchronized void free(DhPointer ptr) throws DhException, IOException {
    allocatedPtrs.remove(ptr);
    _addPointerToDeallocationChain(ptr);
  }

  public synchronized void closeHeapFile() throws IOException {
    if (heapFile != null) {
      heapFile.close();
      heapFile = null;
    }
  }

  public synchronized void openHeapFile() throws FileNotFoundException {
    if (heapFile == null) {
      heapFile = new RandomAccessFile(filePath, "rw");
    }
  }

  public synchronized void destroyHeap() {
    if (allocatedPtrs != null) {
      allocatedPtrs.clear();
      allocatedPtrs = null;
    }

    if (heapFile != null) {
      try {
        closeHeapFile();
      }
      catch (Exception e) {}
    }

    if (filePath != null) {
      File f = new File(filePath);
      f.delete();
    }

    heapFile = null;
    filePath = null;
    nextAddress = 0;
    deallocatedChainAddress = 0;
    inited = false;
    instance = null;
  }

  protected synchronized byte[] dereference(long address) throws DhException, IOException {
    byte[] data = _readDataChain(address);
    return data;
  }

  protected synchronized void pushData(long address, byte[] data) throws DhException, IOException {
    if (data == null || data.length == 0) {
      return;
    }

    _writeDataChainToHeap(address, data);
  }

  //Internals
  //-------------------------------------------------->

  private byte[] _readHeader(long address) throws IOException, DhException {
    byte[] data;
    int cnt;

    data = new byte[HEAP_NODE_HEADER_SIZE];

    heapFile.seek(address);
    cnt = heapFile.read(data);

    if (cnt != data.length) {
      throw new DhException("Segmentation Fault!");
    }

    return data;
  }

  private void _writeDataBlockToHeap(long address, byte[] data) throws IOException {
    heapFile.seek(address);
    heapFile.write(data);
  }

  private void _setNextChainNodeAddress(byte[] node, long nextNodeAddress) {
    String str;
    byte[] bArr;

    str = StringUtils.LPad(String.valueOf(nextNodeAddress), '0', HEAP_NODE_HEADER_CHAIN_NEXT_ADDRESS_LEN);
    bArr = str.getBytes();

    System.arraycopy(bArr, 0, node, HEAP_NODE_HEADER_CHAIN_NEXT_ADDRESS_OFFSET, HEAP_NODE_HEADER_CHAIN_NEXT_ADDRESS_LEN);
  }

  private long _parseNextChainNodeAddress(byte[] header) {
    String tmp = new String(header, HEAP_NODE_HEADER_CHAIN_NEXT_ADDRESS_OFFSET, HEAP_NODE_HEADER_CHAIN_NEXT_ADDRESS_LEN);

    long nxtAddr = Long.parseLong(tmp);

    return nxtAddr;
  }

  private byte[] _readDataChain(long address) throws IOException, DhException {
    ByteArrayOutputStream baos;
    byte[] data, header;
    int cnt;
    long nxtAddr;

    data = new byte[HEAP_NODE_TOTAL_SIZE];

    baos = new ByteArrayOutputStream();

    heapFile.seek(address);
    cnt = heapFile.read(data);

    if (cnt != data.length) {
      throw new DhException("Segmentation Fault!");
    }

    header = new byte[HEAP_NODE_HEADER_SIZE];
    System.arraycopy(data, 0, header, 0, HEAP_NODE_HEADER_SIZE);

    baos.write(data, HEAP_NODE_DATA_OFFSET, data.length - HEAP_NODE_HEADER_SIZE);

    nxtAddr = _parseNextChainNodeAddress(header);

    if (nxtAddr > 0) {
      data = _readDataChain(nxtAddr);
      baos.write(data);
    }

    data = baos.toByteArray();
    baos.close();
    baos = null;

    return data;
  }

  private int _getBlocksRequired(int len) {
    if (len <= HEAP_NODE_DATA_SPACE_SIZE) {
      return 1;
    }
    else {
      return len / HEAP_NODE_DATA_SPACE_SIZE + (len % HEAP_NODE_DATA_SPACE_SIZE > 0 ? 1 : 0);
    }
  }

  private void _addPointerToDeallocationChain(DhPointer ptr) throws DhException, IOException {
    long lastNodeAddr;
    byte[] header;

    if (deallocatedChainAddress == 0) {
      deallocatedChainAddress = ptr.getAddress();
    }
    else {
      lastNodeAddr = _getAddressOfLastNodeInChain(deallocatedChainAddress);

      header = _readHeader(lastNodeAddr);
      _setNextChainNodeAddress(header, ptr.getAddress());

      _writeDataBlockToHeap(lastNodeAddr, header);
    }
  }

  private long _getAddressOfLastNodeInChain(long address) throws DhException, IOException {
    byte[] header = _readHeader(address);
    long lastNodeAddr = _parseNextChainNodeAddress(header);

    if (lastNodeAddr == 0) {
      return address;
    }
    else {
      return _getAddressOfLastNodeInChain(lastNodeAddr);
    }
  }

  private long _allocateBlocks(int blocks) throws DhException, IOException {
    long address, lastAddress;
    byte[] header;

    if (deallocatedChainAddress == 0) {
      //Need all new blocks
      address = nextAddress;
      _allocateNewBlocks(blocks);
    }
    else {
      //Reuse as many deallocated blocks as possible
      address = deallocatedChainAddress;
      lastAddress = deallocatedChainAddress;

      while (deallocatedChainAddress > 0 && blocks > 0) {
        blocks--;
        lastAddress = deallocatedChainAddress;

        //Advance deallocatedChainAddress
        header = _readHeader(deallocatedChainAddress);
        deallocatedChainAddress = _parseNextChainNodeAddress(header);
      }

      if (blocks == 0) {
        //All blocks obtained from deallocated pool
        header = _readHeader(lastAddress);
        _setNextChainNodeAddress(header, 0);
        _writeDataBlockToHeap(lastAddress, header);
      }
      else {
        //Need some additional blocks
        header = _readHeader(lastAddress);
        _setNextChainNodeAddress(header, nextAddress);
        _writeDataBlockToHeap(lastAddress, header);

        _allocateNewBlocks(blocks);
      }
    }

    return address;
  }

  private void _allocateNewBlocks(int blocks) throws IOException {
    byte[] node;

    node = new byte[HEAP_NODE_TOTAL_SIZE];

    for (int i = 0; i < blocks; i++) {
      if (i < blocks - 1) {
        _setNextChainNodeAddress(node, nextAddress + HEAP_NODE_TOTAL_SIZE);
      }
      else {
        _setNextChainNodeAddress(node, 0);
      }

      _writeDataBlockToHeap(nextAddress, node);

      nextAddress += HEAP_NODE_TOTAL_SIZE;
    }
  }

  private void _writeDataChainToHeap(long address, byte[] data) throws IOException, DhException {
    byte[] block, header;
    int index, bytesLeft;

    if (data.length <= HEAP_NODE_DATA_SPACE_SIZE) {
      //Data fits in a single block!
      _writeDataBlockToHeap(address + HEAP_NODE_DATA_OFFSET, data);
    }
    else {
      //More than one block is needed!
      index = 0;
      bytesLeft = data.length;
      block = new byte[HEAP_NODE_DATA_SPACE_SIZE];

      while (bytesLeft > HEAP_NODE_DATA_SPACE_SIZE) {
        System.arraycopy(data, index, block, 0, HEAP_NODE_DATA_SPACE_SIZE);
        _writeDataBlockToHeap(address + HEAP_NODE_DATA_OFFSET, block);

        header = _readHeader(address);
        address = _parseNextChainNodeAddress(header);

        if (address == 0) {
          throw new DhException("Segmentation Fault!");
        }

        index += HEAP_NODE_DATA_SPACE_SIZE;
        bytesLeft -= HEAP_NODE_DATA_SPACE_SIZE;
      }

      System.arraycopy(data, index, block, 0, bytesLeft);
      _writeDataBlockToHeap(address + HEAP_NODE_DATA_OFFSET, block);
    }
  }

}
