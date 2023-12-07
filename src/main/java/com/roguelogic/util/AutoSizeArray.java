/*
 Copyright 2007 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.roguelogic.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author rilardi
 */

public class AutoSizeArray implements Serializable {

  private ArrayList<IndexedItem> items;

  public AutoSizeArray() {
    items = new ArrayList<IndexedItem>();
  }

  public int findMaxIndex() {
    int maxIndex = 0;
    IndexedItem iItem;

    for (int i = 0; i < items.size(); i++) {
      iItem = (IndexedItem) items.get(i);
      if (iItem.getIndex() > maxIndex) {
        maxIndex = iItem.getIndex();
      }
    }

    return maxIndex;
  }

  public Object get(int index) {
    IndexedItem iItem = null;

    for (int i = 0; i < items.size(); i++) {
      iItem = (IndexedItem) items.get(i);
      if (iItem.getIndex() == index) {
        break;
      }
      else {
        iItem = null;
      }
    }

    return (iItem != null ? iItem.getItem() : null);
  }

  public void store(int index, Serializable item) {
    IndexedItem iItem;

    if (index >= 0) {
      iItem = new IndexedItem();
      iItem.setIndex(index);
      iItem.setItem(item);

      items.add(iItem);
    }
  }

  public Object[] getObjectArray() {
    Object[] arr = null;
    IndexedItem iItem;
    int maxIndex;

    maxIndex = findMaxIndex();
    arr = new Object[maxIndex + 1];

    for (int i = 0; i < arr.length && i < items.size(); i++) {
      iItem = (IndexedItem) items.get(i);
      arr[iItem.getIndex()] = iItem.getItem();
    }

    return arr;
  }

  public String[] getStringArray() {
    String[] arr = null;
    IndexedItem iItem;
    int maxIndex;
    Object obj;

    maxIndex = findMaxIndex();
    arr = new String[maxIndex + 1];

    for (int i = 0; i < arr.length && i < items.size(); i++) {
      iItem = (IndexedItem) items.get(i);
      obj = iItem.getItem();
      if (obj != null) {
        arr[iItem.getIndex()] = obj.toString();
      }
    }

    return arr;
  }

  public int size() {
    return items.size();
  }

  public Object remove(int index) {
    IndexedItem iItem = null;

    for (int i = 0; i < items.size(); i++) {
      iItem = (IndexedItem) items.get(i);
      if (iItem.getIndex() == index) {
        items.remove(i);
        break;
      }
      else {
        iItem = null;
      }
    }

    return (iItem != null ? iItem.getItem() : null);
  }

  public void clear() {
    items.clear();
  }

}
