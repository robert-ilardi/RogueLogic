package com.roguelogic.containercore;

public abstract class ContainerResponse {

  private TransportId srcTransportId;
  private boolean terminateSessionFlag;

  public ContainerResponse() {
    terminateSessionFlag = false;
  }

  public TransportId getSrcTransportId() {
    return srcTransportId;
  }

  public void setSrcTransportId(TransportId srcTransportId) {
    this.srcTransportId = srcTransportId;
  }

  public abstract void putTransportSessionItem(Object key, Object value);

  public abstract void removeTransportSessionItem(Object key);

  public boolean isTerminateSessionFlag() {
    return terminateSessionFlag;
  }

  public void setTerminateSessionFlag(boolean terminateSessionFlag) {
    this.terminateSessionFlag = terminateSessionFlag;
  }

}
