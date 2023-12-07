/**
 * Created 4/15/2006
 */
package com.roguelogic.containercore;

import java.util.Properties;

/**
 * @author Robert C. Ilardi
 *
 */

public interface Container {
  public void containerStart() throws ContainerException;

  public void containerStop() throws ContainerException;

  public void setContainerProperties(Properties props);

  public void initContainer() throws ContainerException;

  public void setCTMediator(CTMediator ctMediator);

  public void setContainerKernel(ContainerKernel cntnrKernel);

  public void processRequest(ContainerRequest request) throws ContainerException;
}
