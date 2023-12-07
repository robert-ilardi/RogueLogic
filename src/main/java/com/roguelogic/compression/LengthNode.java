/*
 * LengthNode.java
 *
 * Created on November 21, 2002, 8:57 PM
 */

package com.roguelogic.compression;

/**
 *
 * @author  Robert C. Ilardi
 */

public class LengthNode {
  private LengthNode up;
  private LengthNode down;
  
  private float value;
  private int ascii;
  
  /** Creates a new instance of LengthNode */
  public LengthNode(LengthNode down, LengthNode up, int ascii, float value) {
    this.up=up;
    this.down=down;
    this.value=value;
    this.ascii=ascii;
  }
  
  
  public LengthNode(LengthNode down, LengthNode up, float value) {
    this(down, up, -1, value);
  }
  
  
  public float getValue()
  {return value;}
  
  public LengthNode getUp()
  {return up;}
  
  public LengthNode getDown()
  {return down;}
  
  public int getAscii()
  {return ascii;}
  
  
  public String toString() {
    StringBuffer sb=new StringBuffer();
    
    sb.append("Value: ");
    sb.append(getValue());
    sb.append("\n");
    sb.append("ASCII: ");
    sb.append(getAscii());
    sb.append("\n");
    
    sb.append("Down: ");
    if (down!=null) {
      sb.append(down.toString());
    }
    else {
      sb.append("NULL");
    }
    sb.append("\n");
    
    sb.append("Up: ");
    if (up!=null) {
      sb.append(up.toString());
    }
    else {
      sb.append("NULL");
    }
    sb.append("\n");
    
    return sb.toString();
  }
}
