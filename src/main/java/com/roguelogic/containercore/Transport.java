/**
 * Created 4/15/2006 
 */
package com.roguelogic.containercore;

import java.util.Properties;

/**
 * @author Robert C. Ilardi
 *
 */

public interface Transport {
  public void transportStart() throws TransportLayerException;

  public void transportStop() throws TransportLayerException;

  public void setTransportProperties(Properties props);

  public void initTransport() throws TransportLayerException;

  public void setCTMediator(CTMediator ctMediator);

  public void processResponse(ContainerResponse response) throws TransportLayerException;
}
