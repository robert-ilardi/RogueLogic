package com.roguelogic.containercore;

import java.util.ArrayList;
import java.util.HashMap;

public class CTMediator {

  private Container container;

  private HashMap<Transport, TransportId> transportToIdMap;
  private HashMap<TransportId, Transport> idToTransportMap;

  public CTMediator(Container container, ArrayList<Transport> transports) throws ContainerKernelException {
    int idNum = 1000;
    TransportId tId;

    if (container == null) {
      throw new ContainerKernelException("Can NOT create Container-Transport Mediator using NULL Container!");
    }

    if (transports == null || transports.size() == 0) {
      throw new ContainerKernelException("Can NOT create Container-Transport Mediator using NULL or Empty Transport List!");
    }

    this.container = container;

    transportToIdMap = new HashMap<Transport, TransportId>();
    idToTransportMap = new HashMap<TransportId, Transport>();

    for (Transport transport : transports) {
      tId = new TransportId(idNum++);

      transportToIdMap.put(transport, tId);
      idToTransportMap.put(tId, transport);
    }
  }

  public void sendRequest(Transport transport, ContainerRequest request) throws ContainerException {
    TransportId tId;

    if (transport != null && request != null) {
      //Set Source Transport Id
      tId = transportToIdMap.get(transport);
      if (tId != null) {
        request.setSrcTransportId(tId);

        //Pass the Request to the Container...
        container.processRequest(request);
      }
      else {
        System.err.println("Could NOT resolve Source Transport Id of given Transport for return trip! Aborting Request... ");
      }
    } //End null transport and null request check
  }

  public void sendResponse(ContainerResponse response) throws TransportLayerException {
    TransportId tId;
    Transport transport;

    if (response != null) {
      //Get the Source Transport
      tId = response.getSrcTransportId();
      if (tId != null) {
        transport = idToTransportMap.get(tId);
        if (transport != null) {
          //Pass the Request to the Source Transport...
          transport.processResponse(response);
        }
        else {
          System.err.println("Could NOT resolve Source Transport using Source Transport Id set in Response: " + response);
        }
      } //End NULL Transport Id Check
      else {
        System.err.println("Invalid Source Transport Id set in Response: " + response);
      }
    } //End null transport and null request check
  }

}
