/*
 * HuffmanCode.java
 *
 * Created on November 22, 2002, 12:27 AM
 */

package com.roguelogic.compression;

/**
 *
 * @author  Robert C. Ilardi
 */

public class HuffmanCode {
  private int ascii;
  private String binary;
  
  /** Creates a new instance of HuffmanCode */
  public HuffmanCode(int ascii, String binary) {
    this.ascii=ascii;
    this.binary=binary;
  }
  
  
  public int getAscii()
  {return ascii;}
  
  public String getBinary()
  {return binary;}
  
  
  public String toString() {
    StringBuffer sb=new StringBuffer();
    
    sb.append("ASCII: ");
    sb.append(getAscii());
    sb.append("\n");
    sb.append("Binary: ");
    sb.append(getBinary());
    sb.append("\n");
    
    return sb.toString();
  }
}
