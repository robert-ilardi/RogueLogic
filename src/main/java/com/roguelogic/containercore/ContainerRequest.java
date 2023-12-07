package com.roguelogic.containercore;

public abstract class ContainerRequest {

  //We use TransportId instead of Transport direct incase in the
  //future we come up with a concept like "Remote Containers"...
  //Same reason TransportId is an Object instead of an int.
  //We may need to explain it to support Remote Containers!
  protected TransportId srcTransportId;

  public ContainerRequest() {}

  public TransportId getSrcTransportId() {
    return srcTransportId;
  }

  public void setSrcTransportId(TransportId srcTransportId) {
    this.srcTransportId = srcTransportId;
  }

  public abstract Object getTransportSessionItem(Object key);

}
