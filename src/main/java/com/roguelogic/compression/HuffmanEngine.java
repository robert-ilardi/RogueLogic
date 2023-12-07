/*
 * HuffmanEngine.java
 *
 * Created on November 20, 2002, 11:46 PM
 */

package com.roguelogic.compression;

/**
 *
 * @author  Robert C. Ilardi
 */

import java.io.*;
import java.util.*;


//This is an implementation of the Huffman Codes Compression/Decompression
//Algorithm using Strings. It is highly inefficient, however it correctly
//implements the Algorithm itself. Work will be done to turn the String
//manipulation operations into bit-wise operations.


public class HuffmanEngine {
  private int[] charCounts;
  private float[] charProbabilities;
  private int totalBytes;
  private int distinctChars;
  private int[] charsOrder;
  
  public static final int MAX_CHAR=256;
  public static final int MAX_BUFFER=1024;
  
  public static final int COMPRESS=0;
  public static final int DECOMPRESS=1;
  
  
  private LengthNode root;
  private ArrayList huffmanCodes;
  
  private HuffmanCode padding;
  
  
  
  /** Creates a new instance of HuffmanEngine */
  public HuffmanEngine() {
    charCounts=new int[MAX_CHAR];
    charProbabilities=new float[MAX_CHAR];
    
    clearStats();
  }
  
  
  //Clear statistical data
  private void clearStats() {
    totalBytes=0;
    distinctChars=0;
    root=null;
    huffmanCodes=null;
    
    for (int i=0; i<MAX_CHAR; i++) {
      charCounts[i]=0;
      charProbabilities[i]=0.0f;
    }
  }
  
  
  //Get the counts of each instance of each unique ASCII
  //character in an array of bytes. This array can be
  //the data from a input stream such as a file.
  private void countCharacters(byte[] bArr, int len) {
    int iv;
    
    if (bArr!=null) {
      totalBytes+=len;
      
      for (int i=0; i<bArr.length && i<len; i++) {
        iv=bArr[i] & 0xff;
        charCounts[iv]++;
      }
    }
  }
  
  //Same as the method directly above.
  private void countCharacters(byte[] bArr) {
    if (bArr!=null) {
      countCharacters(bArr, bArr.length);
    }
  }
  
  
  //Analyze an Input Stream.
  //Read each byte from the stream and collect
  //statistical information such as character counts
  //and probabilities. Also sort the character order
  //with the most frequent characters first!
  private void analyzeStream(InputStream ins) throws IOException {
    byte[] ba=new byte[MAX_BUFFER];
    int cnt;
    
    do {
      cnt=ins.read(ba);
      
      if (cnt>0) {
        countCharacters(ba, cnt);
      }
    } while(cnt!=-1);
    
    calculateProbabilities();
    sortCharOrder();
  }
  
  
  //From the character counts, we can compare these
  //counts with the total size of the file
  //and get the frequency that they occur
  private void calculateProbabilities() {
    distinctChars=0;
    float temp=0f;
    
    for (int i=0; i<MAX_CHAR; i++) {
      if (charCounts[i]!=0) {
        charProbabilities[i] = charCounts[i] / (float)totalBytes;
        distinctChars++;
        temp+=charProbabilities[i];
      }
    }
  }
  
  
  //Sort the character counts
  //in order from highest count to lowest.
  private void sortCharOrder() {
    int cnt=0;
    int[] countArr=new int[MAX_CHAR];
    int[] indexArr=new int[MAX_CHAR];
    int largest, temp;
    
    charsOrder=new int[distinctChars];
    System.arraycopy(charCounts, 0, countArr, 0,  MAX_CHAR);
    
    //Populate Indexes
    for (int i=0; i<MAX_CHAR; i++) {
      indexArr[i]=i;
    }
    
    //Selection Sort because it is a small array to sort
    for (int i=0; i<MAX_CHAR; i++) {
      largest=i;
      
      for (int j=i+1; j<MAX_CHAR; j++) {
        if (countArr[j]>countArr[largest]) {
          largest=j;
        }
      }
      
      //swap
      if (largest!=i) {
        temp=countArr[i];
        countArr[i]=countArr[largest];
        countArr[largest]=temp;
        
        temp=indexArr[i];
        indexArr[i]=indexArr[largest];
        indexArr[largest]=temp;
      }
      
      //If the smallest if not ZERO added
      //its index to the char order!
      if (countArr[i]!=0) {
        charsOrder[cnt]=indexArr[i];
        cnt++;
      }
    }
  }
  
  
  //Build the Tree with Frequency in the leafs
  //and build up to the root the root node has a value of 1
  //We count the edges between the root and each leaf to
  //get the lengths of bits, with ONE for each edge
  //down from the root and ZERO for each edge UP.
  private void buildLengthTree() {
    ArrayList roots=new ArrayList();
    LengthNode ln=null, smallest, secondSmallest=null;
    float p;
    
    //Build leaf nodes
    for (int i=charsOrder.length-1; i>=0; i--) {
      p=charProbabilities[charsOrder[i]];
      ln=new LengthNode(null, null, charsOrder[i], p);
      
      roots.add(ln);
    }
    
    //Build the Tree
    ln=null;
    while (!roots.isEmpty()) {
      if (ln!=null) {
        roots.add(ln);
      }
      
      smallest=removeSmallest(roots);
      secondSmallest=removeSmallest(roots);
      
      if (smallest!=null && secondSmallest!=null) {
        p=smallest.getValue()+secondSmallest.getValue();
        ln=new LengthNode(smallest, secondSmallest, p);
      }
    }
    
    root=ln;
  }
  
  
  //Removes the smallest valued length node from
  //the array list of nodes. Used when
  //combining the two smallest values of nodes in the
  //"Length Tree."
  private LengthNode removeSmallest(ArrayList nodes) {
    int smallest;
    LengthNode ln, ln2;
    
    if (nodes.size()>0) {
      smallest=0;
      ln=(LengthNode)nodes.get(0);
      
      for (int i=0; i<nodes.size(); i++) {
        ln2=(LengthNode)nodes.get(i);
        
        if (ln2.getValue() < ln.getValue()) {
          smallest=i;
          ln=ln2;
        }
      }
      
      ln=(LengthNode)nodes.remove(smallest);
    }
    else {
      ln=null;
    }
    
    return ln;
  }
  
  
  //Creates the BIT Maps (Huffman Codes) for each character by
  //traversing the "Length Tree." ZEROS for the nodes up from the root
  //and ONES for the nodes down from the root. This is a recursive function.
  private ArrayList traverseLengths(LengthNode ln, String stor) {
    StringBuffer sb=new StringBuffer(stor);
    StringBuffer sb2=new StringBuffer(stor);
    ArrayList al=new ArrayList();
    HuffmanCode hc;
    
    if (ln!=null && ln.getUp()!=null) {
      sb.append("0");
      al.addAll(traverseLengths(ln.getUp(), sb.toString()));
    }
    
    if (ln!=null && ln.getDown()!=null) {
      sb2.append("1");
      al.addAll(traverseLengths(ln.getDown(), sb2.toString()));
    }
    
    if (ln!=null && ln.getUp()==null && ln.getDown()==null) {
      hc=new HuffmanCode(ln.getAscii(), sb.toString());
      al.add(hc);
    }
    
    return al;
  }
  
  
  //Reads the input stream, maps the Huffman Code that represents
  //each byte from the stream and writes those BITS to the output stream.
  //Because the output stream only works with BYTES we have to combine
  //the bit strings from multiple Huffman Codes to write a correct
  //ASCII character to the stream. We do this by appending a StringBuffer of
  //ZERO's and ONE's. The final character may or may not complete a 8-bit BYTE.
  //so we padd this last byte with trailing ZERO's. We need to keep track
  //of the number of bytes decompressed later one when we read and decode
  //the Huffman Code bit strings from the input stream.
  //This information is encoded in the compressed file header.
  private void writeHuffmans(InputStream ins, OutputStream outs) throws IOException {
    byte[] ba=new byte[MAX_BUFFER];
    int cnt;
    byte[] baOut;
    StringBuffer binary=new StringBuffer();
    HuffmanCode hc;
    int byteCnt=0;
    
    do {
      cnt=ins.read(ba);
      
      if (cnt>0) {
        binary.append(translateTo(ba, cnt));
        
        if (binary.length()>=8) {
          baOut=binaryBufferToBytes(binary);
          outs.write(baOut);
          byteCnt+=baOut.length;
        }
      }
    } while(cnt!=-1);
    
    //Pad the Last Group of Bits with ZEROS
    //To make a single 8-Bit Byte!
    if (binary.length()>0) {
      byteIze(binary);
      baOut=binaryBufferToBytes(binary);
      outs.write(baOut);
      byteCnt+=baOut.length;
    }
    
    System.out.println("Compressed Data Section is: "+byteCnt+" Bytes");
    System.out.println("Compression Ratio: "+(100-(((float)byteCnt/totalBytes)*100))+"%");
  }
  
  
  //Add ZERO's to Binary Buffer until
  //The length of the Buffer is EIGTH!
  private void byteIze(StringBuffer binary) {
    while (binary.length()<8) {
      binary.append("0");
    }
  }
  
  
  //Writes the header to the compressed file.
  //The header is made up of the total length in bytes
  //of the decompressed file, useful when detecting the padding
  //bits of the last character. And a table of Huffman Codes
  //and their corresponding ASCII characters.
  private void writeHeader(OutputStream outs) throws IOException {
    StringBuffer sb=new StringBuffer();
    byte[] ba;
    HuffmanCode hc;
    int binInt;
    
    //Total Decompressed Size
    sb.append(totalBytes);
    sb.append("|");
    
    //Huffman Code Table
    for(int i=0; i<huffmanCodes.size(); i++) {
      hc=(HuffmanCode)huffmanCodes.get(i);
      
      if (i>0) {
        sb.append(",");
      }
      
      sb.append(hc.getAscii());
      sb.append(",");
      sb.append(hc.getBinary());
    }
    sb.append("|");
    
    //END Header
    sb.append("$");
    
    ba=sb.toString().getBytes();
    outs.write(ba);
  }
  
  
  //Reads the header from the file. Picks up the total
  //number of decompressed bytes and creates
  //the Huffman Code table in memory for decoding
  //the string of bits stored as ASCII characters in the file.
  private void readHeader(InputStream ins) throws IOException {
    StringBuffer sb=new StringBuffer();
    int b;
    char c;
    int cnt=0;
    StringTokenizer st;
    HuffmanCode hc;
    
    do {
      b=ins.read();
      
      if (b!=-1) {
        c=(char)b;
        
        if (c=='$') {
          //END HEADER
          break;
        }
        else if (c=='|') {
          //Token Terminated
          if (cnt==0) {
            //Total Decompressed Bytes
            try {
              totalBytes=Integer.parseInt(sb.toString());
            }
            catch(NumberFormatException e) {
              //Header Corrupted
              System.err.println("Header is corrupt! Could not read the decompressed byte total...");
              System.exit(1);
            }
          }
          else if (cnt==1) {
            //Huffman Code Table
            st=new StringTokenizer(sb.toString(), ",");
            
            if (st.countTokens() % 2 == 0) {
              huffmanCodes=new ArrayList();
              
              while (st.hasMoreTokens()) {
                try {
                  hc=new HuffmanCode(Integer.parseInt(st.nextToken()), st.nextToken());
                  huffmanCodes.add(hc);
                }
                catch(NumberFormatException e) {
                  //Header Corrupted
                  System.err.println("Header is corrupt! Values in the Huffman Code Table are invalid...");
                  System.exit(1);
                }
              }
            }
            else {
              //Header Corrupted!
              System.err.println("Header is corrupt! Invalid Huffman Code Table detected...");
              System.exit(1);
            }
          }
          
          cnt++;
          sb.setLength(0);
        }
        else {
          sb.append(c);
        }
      }
    } while (b!=-1);
  }
  
  
  //Translates an array of bytes into a String of BITS!
  private String translateTo(byte[] bArr, int len) {
    StringBuffer sb=new StringBuffer();
    HuffmanCode hc;
    int ascii;
    
    for (int i=0; i<len; i++) {
      ascii=bArr[i] & 0xff;
      
      for (int j=0; j<huffmanCodes.size(); j++) {
        hc=(HuffmanCode)huffmanCodes.get(j);
        if (hc.getAscii()==ascii) {
          
          sb.append(hc.getBinary());
          break;
        }
      }
    }
    
    return sb.toString();
  }
  
  
  //Converts a String of BITS into an array of Bytes.
  private byte[] binaryBufferToBytes(StringBuffer binary) {
    String eightBits;
    int ascii, cnt=0;
    byte[] ba=new byte[(binary.length() / 8)];
    
    while (binary.length()>=8) {
      eightBits=binary.substring(0, 8);
      binary.delete(0, 8);
      
      ascii=Integer.parseInt(eightBits, 2);
      
      ba[cnt]=(byte)ascii;
      cnt++;
    }
    
    return ba;
  }
  
  
  //Puts the Huffman Codes Table in Order from
  //smallest number of bits to largest.
  //Not really needed but it is easy to read when debugging!
  private void orderCodes() {
    ArrayList newHCOrder=new ArrayList();
    HuffmanCode hc;
    
    for (int i=0; i<charsOrder.length; i++) {
      for (int j=0; j<huffmanCodes.size(); j++) {
        hc=(HuffmanCode)huffmanCodes.get(j);
        
        if (hc.getAscii()==charsOrder[i]) {
          newHCOrder.add(hc);
          huffmanCodes.remove(j);
          break;
        }
      }
    }
    
    huffmanCodes=newHCOrder;
  }
  
  
  //The Compression driver method, which calls all required methods
  //for compression of a file!
  public void compress(String input, String output) throws IOException, FileNotFoundException {
    FileInputStream fis=null;
    FileOutputStream fos=null;
    
    try {
      System.out.println("Starting Huffman Compression...");
      
      fis=new FileInputStream(input);
      
      analyzeStream(fis);
      
      System.out.println("There is a total of "+totalBytes+" Bytes to compress...");
      
      buildLengthTree();
      huffmanCodes=traverseLengths(root, "");
      orderCodes();
      
      fis.close();
      fis=new FileInputStream(input);
      
      if (root!=null) {
        fos=new FileOutputStream(output);
        writeHeader(fos);
        writeHuffmans(fis, fos);
      }
      else {
        System.out.println("Nothing to Compress...");
      }
    }
    finally {
      try {
        if (fis!=null) {
          fis.close();
        }
      }
      catch(Exception e) {
        e.printStackTrace();
      }
      
      
      try {
        if (fos!=null) {
          fos.close();
        }
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  
  //The Decompression driver method, which calls all required methods
  //for decompression of a file!
  public void decompress(String input, String output) throws IOException, FileNotFoundException {
    FileInputStream fis=null;
    FileOutputStream fos=null;
    
    try {
      System.out.println("Starting Huffman Decompression...");
      
      fis=new FileInputStream(input);
      fos=new FileOutputStream(output);
      
      readHeader(fis);
      
      System.out.println("Decompressed File Size: "+totalBytes+" Bytes");
      
      readHuffmans(fis, fos);
    }
    finally {
      try {
        if (fis!=null) {
          fis.close();
        }
      }
      catch(Exception e) {
        e.printStackTrace();
      }
      
      try {
        if (fos!=null) {
          fos.close();
        }
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  
  //Reads a huffman code compressed Data Block from our compressed file format
  //and writes the decompressed/decoded ASCII characters into the output stream.
  //We need to keep track of the total number of bytes decompressed, so when
  //we hit the last byte that needs to be decompressed, we can detect when we
  //hit the padding characters!
  private void readHuffmans(InputStream ins, OutputStream outs) throws IOException {
    byte[] ba=new byte[MAX_BUFFER];
    int cnt, mLen;
    byte[] baOut;
    HuffmanCode hc;
    StringBuffer sb=new StringBuffer();
    int byteCnt=0;
    
    do {
      cnt=ins.read();
      
      if (cnt>=0) {
        sb.append(intToBinary(cnt));
        
        do {
          hc=findMatchingHuffmanCode(sb);
          
          if (hc!=null) {
            
            outs.write((byte)hc.getAscii());
            byteCnt++;
            
            //Stop Decompressing, We hit the Padding bits!
            if (byteCnt>=totalBytes) {
              break;
            }
            
          }
        } while (sb.length()>0 && hc!=null);
      }
    } while(cnt!=-1);
    
    
    System.out.println("There were "+byteCnt+" Bytes decompressed...");
  }
  
  
  //Properly converts integers into binary strings
  //Built in java functions to do so, truncate leading
  //zero's which is a NO-NO in Huffman Codes
  private String intToBinary(int num) {
    StringBuffer binary=new StringBuffer();
    binary.append(Integer.toBinaryString(num));
    
    for (int i=binary.length(); i<8; i++) {
      binary.insert(0, "0");
    }
    
    return binary.toString();
  }
  
  
  //Based on a String of BITS, this function will
  //find the corresponding Huffman Code, which
  //can be used to find what ASCII character the string represents.
  private HuffmanCode findMatchingHuffmanCode(StringBuffer binary) {
    HuffmanCode hc=null;
    
    for (int i=0; i<huffmanCodes.size(); i++) {
      hc=(HuffmanCode)huffmanCodes.get(i);
      
      if (binary.indexOf(hc.getBinary())==0) {
        binary.delete(0, hc.getBinary().length());
        break;
      }
      else {
        hc=null;
      }
    }
    
    return hc;
  }
  
  
  //The simple toString method, useful for debugging,
  //will print out to STDOUT everything that the
  //engine keeps track of while it is compressing or decompressing!
  public String toString() {
    StringBuffer sb=new StringBuffer();
    
    sb.append("\n**Huffman Compression Engine**\n");
    sb.append("Total Bytes: ");
    sb.append(totalBytes);
    sb.append("\n");
    
    
    sb.append("\n=====================================\n");
    sb.append("Character Counts:\n");
    for (int i=0; i<charCounts.length; i++) {
      if (i % 4 == 0) {
        sb.append("\n");
      }
      else if (i>0) {
        sb.append("\t");
      }
      
      sb.append("char[");
      sb.append(i);
      sb.append("]: ");
      sb.append(charCounts[i]);
    }
    sb.append("\n=====================================\n");
    
    
    sb.append("\n=====================================\n");
    sb.append("Character Probabilities:\n");
    for (int i=0; i<charProbabilities.length; i++) {
      if (i % 4 == 0) {
        sb.append("\n");
      }
      else if (i>0) {
        sb.append("\t");
      }
      
      sb.append("char[");
      sb.append(i);
      sb.append("]: ");
      sb.append(charProbabilities[i]);
    }
    sb.append("\n=====================================\n");
    
    
    sb.append("\nDistinct Characters: ");
    sb.append(distinctChars);
    sb.append("\n");
    
    
    sb.append("\n=====================================\n");
    sb.append("Character Order:\n");
    for (int i=0; i<charsOrder.length; i++) {
      if (i % 4 == 0) {
        sb.append("\n");
      }
      else if (i>0) {
        sb.append("\t");
      }
      
      sb.append(charsOrder[i]);
    }
    sb.append("\n=====================================\n");
    
    
    sb.append("\n=====================================\n");
    sb.append("Length Tree:\n");
    sb.append((root!=null ? root.toString() : "NULL"));
    sb.append("\n=====================================\n");
    
    
    sb.append("\n=====================================\n");
    sb.append("Huffman Code Table:\n");
    for (int i=0; huffmanCodes!=null && i<huffmanCodes.size(); i++) {
      sb.append(huffmanCodes.get(i).toString());
    }
    sb.append("\n=====================================\n");
    
    
    return sb.toString();
  }
  
  
  //Main Driver Method! Command Line Interface.
  public static final void main(String[] args) {
    if (args.length!=3) {
      System.out.println("Usage java HuffmanEngine [COMPRESS | DECOMPRESS] [INPUT FILE] [OUTPUT FILE]");
      System.exit(1);
    }
    else {
      String input, output;
      HuffmanEngine he=null;
      int mode;
      
      try {
        
        //Make Sure we only have Compress or Decompress Requests
        if (!args[0].equalsIgnoreCase("COMPRESS") && !args[0].equalsIgnoreCase("DECOMPRESS")) {
          System.out.println("Invalid Operation! Compress or Decompress ONLY!");
          System.out.println("Usage java HuffmanEngine [COMPRESS | DECOMPRESS] [INPUT FILE] [OUTPUT FILE]");
          System.exit(1);
        }
        
        System.out.println();
        
        mode=(args[0].equalsIgnoreCase("COMPRESS") ? COMPRESS : DECOMPRESS);
        input=args[1];
        output=args[2];
        
        he=new HuffmanEngine();
        
        if (mode==COMPRESS) {
          he.compress(input, output);
          //System.out.println(he);   //For Debugging
        }
        else {
          he.decompress(input, output);
        }
        
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
}
