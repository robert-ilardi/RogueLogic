/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

/**
 * @author Robert C. Ilardi
 * 
 */

public interface IpcChainProcess extends IpcProcess {
  public void setPreviousProcessLink(IpcProcess previousProcess);
}
