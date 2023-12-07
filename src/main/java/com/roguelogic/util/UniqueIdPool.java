/*
 * Created on Mar 7, 2006
 */

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

public class UniqueIdPool {

  private String name;
  private int maxId;
  private int[] idPool;

  public static final int ID_NOT_RESERVED = 0;
  public static final int ID_RESERVED = 1;

  public UniqueIdPool(String name, int maxId) throws RLException {
    if (maxId <= 0) {
      throw new RLException("Invalid Max Id! Max Id MUST be >= 1");
    }

    this.name = name;
    this.maxId = maxId;
    idPool = new int[maxId];
  }

  public synchronized int obtainId() throws OutOfUniqueIdsException {
    int uniqueId = -1;

    for (int i = 0; i < idPool.length; i++) {
      if (idPool[i] == ID_NOT_RESERVED) {
        idPool[i] = ID_RESERVED;
        uniqueId = i;
        break;
      }
    }

    if (uniqueId == -1) {
      throw new OutOfUniqueIdsException("Could NOT reserve Unique Id from Pool '" + name + "'! Max Number (" + (maxId + 1) + ") of Id's are currently checked out of the pool");
    }

    return uniqueId;
  }

  public synchronized void releaseId(int uniqueId) {
    if (uniqueId >= 0 && uniqueId < idPool.length) {
      idPool[uniqueId] = ID_NOT_RESERVED;
    }
  }

}
