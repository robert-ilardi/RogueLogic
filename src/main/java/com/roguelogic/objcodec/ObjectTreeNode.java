package com.roguelogic.objcodec;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.roguelogic.util.AutoSizeArray;

/**
 * @author rilardi
 */

public class ObjectTreeNode implements Serializable {

  private String name;
  private int arrayIndex;
  private HashMap primitives;
  private HashMap children;

  public ObjectTreeNode() {
    arrayIndex = -1;
    primitives = new HashMap();
    children = new HashMap();
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the arrayIndex.
   */
  public int getArrayIndex() {
    return arrayIndex;
  }

  /**
   * @param arrayIndex The arrayIndex to set.
   */
  public void setArrayIndex(int arrayIndex) {
    this.arrayIndex = arrayIndex;
  }

  public void addPrimitive(String fieldName, String fieldValue) {
    AutoSizeArray asArr;

    if (ObjectMapUtils.IsArray(fieldName)) {
      addToArray(fieldName, fieldValue);
    }
    else {
      primitives.put(fieldName, fieldValue);
    }
  }

  public boolean isPrimitiveArray(String fieldName) {
    Object primitive = primitives.get(fieldName);
    return (primitive instanceof AutoSizeArray);
  }

  public String getScalar(String fieldName) {
    Object primitive;
    String scalar = null;

    primitive = primitives.get(fieldName);

    if (primitive instanceof String) {
      scalar = (String) primitive;
    }

    return scalar;
  }

  public String[] getArray(String fieldName) {
    Object primitive;
    String[] array = null;
    AutoSizeArray asArr;

    primitive = primitives.get(fieldName);

    if (primitive instanceof AutoSizeArray) {
      asArr = (AutoSizeArray) primitive;
      array = asArr.getStringArray();
    }

    return array;
  }

  public String[] getPrimitiveNames() {
    String[] names = new String[primitives.size()];
    Set s = primitives.keySet();
    Iterator iter = s.iterator();
    int cnt = 0;

    while (iter.hasNext()) {
      names[cnt++] = (String) iter.next();
    }

    return names;
  }

  public HashMap getChildren() {
    return children;
  }

  private void addToArray(String fieldName, String fieldValue) {
    AutoSizeArray asArr = null;
    String sIndex, baseName;
    int index;

    //Get Array Index and Remove Array Notation from Name
    sIndex = fieldName.substring(fieldName.indexOf("[") + 1, fieldName.indexOf("]"));
    index = Integer.parseInt(sIndex);
    baseName = fieldName.substring(0, fieldName.indexOf("["));
    //System.out.println("Base Name = " + baseName + " ; Index = " + index);

    //Store Value in ArrayList until needed
    if (primitives.containsKey(baseName)) {
      asArr = (AutoSizeArray) primitives.get(baseName);
    }
    else {
      asArr = new AutoSizeArray();
      primitives.put(baseName, asArr);
    }

    asArr.store(index, fieldValue);
  }

  public void addChild(ObjectTreeNode child) {
    AutoSizeArray asArr;

    if (child != null) {
      //Make sure we have an Auto Size Array for Storage
      asArr = (AutoSizeArray) children.get(child.getName());
      if (asArr == null) {
        asArr = new AutoSizeArray();
        children.put(child.getName(), asArr);
      }

      //Even if it's an only child (non-array) we still
      //store it in an Auto Size Array as element ZERO (0)!
      asArr.store((child.getArrayIndex() >= 0 ? child.getArrayIndex() : 0), child);
    }
  }

  public ObjectTreeNode findBuild(String[] objectPath) {
    ObjectTreeNode node = null, parent;
    String[] baseObjectPath, subObjectPath;
    String[] scalarObjectPath, scalarBaseObjectPath;
    String sIndex;
    int index;

    node = find(objectPath);

    if (node == null) {
      node = this; //Parent should be root...

      //Prepare Tokens
      baseObjectPath = getBaseObjectPath(objectPath);
      scalarObjectPath = removeArrayNotation(objectPath);
      scalarBaseObjectPath = getBaseObjectPath(scalarObjectPath);

      //Create Path
      for (int i = 0; i < baseObjectPath.length; i++) {
        parent = node; //Store Parent (Recursive Tree Structure)

        //Check if Sub Object Path Exists
        subObjectPath = new String[i + 1];
        System.arraycopy(baseObjectPath, 0, subObjectPath, 0, subObjectPath.length);
        node = find(subObjectPath, false);

        if (node == null) {
          node = new ObjectTreeNode();
          node.setName(scalarBaseObjectPath[i]);
          if (ObjectMapUtils.IsArray(objectPath[i])) {
            sIndex = objectPath[i].substring(objectPath[i].indexOf("[") + 1, objectPath[i].indexOf("]"));
            index = Integer.parseInt(sIndex);
            node.setArrayIndex(index);
          }
          parent.addChild(node);
        }
      } //End for(i) through baseObjectPath tokens
    } //End NULL node check

    return node;
  }

  public ObjectTreeNode find(String[] objectPath) {
    return find(objectPath, true);
  }

  public ObjectTreeNode find(String[] objectPath, boolean determineBase) {
    AutoSizeArray asArr;
    ObjectTreeNode node = null, parent;
    String[] scalarPath, baseObjectPath;
    String sIndex;
    int index;

    scalarPath = removeArrayNotation(objectPath);
    if (determineBase) {
      baseObjectPath = getBaseObjectPath(scalarPath);
    }
    else {
      baseObjectPath = scalarPath;
    }

    parent = this; //Parent should be root at first...

    for (int i = 0; i < baseObjectPath.length; i++) {
      //Get Array Index
      if (ObjectMapUtils.IsArray(objectPath[i])) {
        sIndex = objectPath[i].substring(objectPath[i].indexOf("[") + 1, objectPath[i].indexOf("]"));
        index = Integer.parseInt(sIndex);
      }
      else {
        index = 0;
      }

      //Get Node from Auto Size Array of Children
      asArr = (AutoSizeArray) parent.children.get(baseObjectPath[i]);
      if (asArr != null) {
        node = (ObjectTreeNode) asArr.get(index);

        if (node == null) {
          break;
        }
        else {
          parent = node;
        }
      } //End asArr != NULL check
      else {
        node = null;
        break;
      } //End asArr != NULL ELSE-BLOCK
    }

    return node;
  }

  private String[] removeArrayNotation(String[] objectPath) {
    String[] basePath = null;

    basePath = new String[objectPath.length];

    for (int i = 0; i < basePath.length; i++) {
      if (ObjectMapUtils.IsArray(objectPath[i])) {
        basePath[i] = objectPath[i].substring(0, objectPath[i].indexOf("["));
      }
      else {
        basePath[i] = objectPath[i];
      }
    }

    return basePath;
  }

  private String[] getBaseObjectPath(String[] basePath) {
    String[] baseObjectPath = null;

    baseObjectPath = new String[basePath.length - 1];
    System.arraycopy(basePath, 0, baseObjectPath, 0, baseObjectPath.length);

    return baseObjectPath;
  }

  private String getFullyQualifiedObjectName(String[] objectPath) {
    StringBuffer fqName = new StringBuffer();

    for (int i = 0; i < objectPath.length; i++) {
      if (i > 0) {
        fqName.append(".");
      }
      fqName.append(objectPath[i]);
    }

    return fqName.toString();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    String[] primitiveNames = getPrimitiveNames();
    String[] arr;

    sb.append("[Name = ");
    sb.append(name);

    sb.append("; Index = ");
    sb.append(arrayIndex);

    sb.append("; Primitives: ");

    for (int i = 0; i < primitiveNames.length; i++) {
      if (i > 0) {
        sb.append(" ; ");
      }

      sb.append(primitiveNames[i]);
      if (isPrimitiveArray(primitiveNames[i])) {
        arr = getArray(primitiveNames[i]);
        sb.append("[] = {");
        for (int j = 0; j < arr.length; j++) {
          if (j > 0) {
            sb.append("|");
          }
          sb.append(arr[j]);
        }
        sb.append("}");
      }
      else {
        sb.append(" = ");
        sb.append(getScalar(primitiveNames[i]));
      }
    }

    sb.append("]");

    return sb.toString();
  }

  public String toString(int nestedIndex) {
    StringBuffer sb = new StringBuffer();
    Collection childArrs;
    Iterator iter;
    ObjectTreeNode child;
    AutoSizeArray asArr;
    Object[] childObjs;

    for (int i = 1; i <= nestedIndex; i++) {
      sb.append("  ");
    }

    sb.append(toString());
    sb.append("\n");

    childArrs = children.values();
    iter = childArrs.iterator();

    while (iter.hasNext()) {
      asArr = (AutoSizeArray) iter.next();
      childObjs = asArr.getObjectArray();
      for (int i = 0; childObjs != null && i < childObjs.length; i++) {
        child = (ObjectTreeNode) childObjs[i];
        if (child != null) {
          sb.append(child.toString(nestedIndex + 1));
        }
      }
    }

    return sb.toString();
  }

}

