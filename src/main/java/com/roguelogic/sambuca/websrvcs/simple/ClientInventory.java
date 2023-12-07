/**
 * Created Apr 6, 2008
 */

/*
 Copyright 2008 Robert C. Ilardi

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

package com.roguelogic.sambuca.websrvcs.simple;

import java.util.ArrayList;

/**
 * @author Robert C. Ilardi
 *
 */

public class ClientInventory {

  private String facadeName;
  private String version;

  private ArrayList<WSMethod> methods;

  public ClientInventory() {
    methods = new ArrayList<WSMethod>();
  }

  public String getFacadeName() {
    return facadeName;
  }

  public void setFacadeName(String facadeName) {
    this.facadeName = facadeName;
  }

  public ArrayList<WSMethod> getMethods() {
    return methods;
  }

  public void setMethods(ArrayList<WSMethod> methods) {
    this.methods = methods;
  }

  public void addMethod(WSMethod method) {
    methods.add(method);
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

}
