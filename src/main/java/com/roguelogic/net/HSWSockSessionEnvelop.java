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

package com.roguelogic.net;

public class HSWSockSessionEnvelop {

  private SocketSession sockSession;
  private long insertTs;

  public HSWSockSessionEnvelop(SocketSession sockSession) {
    this.sockSession = sockSession;
    insertTs = System.currentTimeMillis();
  }

  public long getInsertTs() {
    return insertTs;
  }

  public SocketSession getSockSession() {
    return sockSession;
  }

  public int hashCode() {
    return sockSession.hashCode();
  }

  public boolean equals(Object obj) {
    return sockSession.equals(obj);
  }

}
