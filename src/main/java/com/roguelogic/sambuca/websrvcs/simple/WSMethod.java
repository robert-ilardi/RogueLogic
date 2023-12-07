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

public class WSMethod {

  private String name;
  private WSParameter returnType;
  private ArrayList<WSParameter> parameters;

  private String signatureKey;

  public WSMethod() {
    parameters = new ArrayList<WSParameter>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList<WSParameter> getParameters() {
    return parameters;
  }

  public void setParameters(ArrayList<WSParameter> parameters) {
    this.parameters = parameters;
  }

  public WSParameter getReturnType() {
    return returnType;
  }

  public void setReturnType(WSParameter returnType) {
    this.returnType = returnType;
  }

  public void addParameter(WSParameter param) {
    parameters.add(param);
  }

  public String getSignatureKey() {
    return signatureKey;
  }

  public void setSignatureKey(String signatureKey) {
    this.signatureKey = signatureKey;
  }

}
