/**
 * Created Aug 17, 2006
 */
package com.roguelogic.dhtable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Robert C. Ilardi
 *
 */

public class KeyList implements Serializable {

  private ArrayList<Serializable> keys;

  public KeyList() {
    keys = new ArrayList<Serializable>();
  }

  public int size() {
    return keys.size();
  }

  public Serializable getKey(int index) {
    Serializable key = null;

    if (index >= 0 && index < keys.size()) {
      key = keys.get(index);
    }

    return key;
  }

  public void addKey(Serializable key) {
    keys.add(key);
  }

  public Serializable removeKey(int index) {
    Serializable key = null;

    if (index >= 0 && index < keys.size()) {
      key = keys.remove(index);
    }

    return key;
  }

  public void clear() {
    keys.clear();
  }

  public void addAll(KeyList keyList) {
    Serializable key;

    if (keyList != null) {
      for (int i = 0; i < keyList.size(); i++) {
        key = keyList.getKey(i);
        addKey(key);
      }
    }
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < keys.size(); i++) {
      if (i > 0) {
        sb.append("\n");
      }

      sb.append("Key ");
      sb.append(i);
      sb.append(": ");
      sb.append(keys.get(i));
    }

    return sb.toString();
  }

}
