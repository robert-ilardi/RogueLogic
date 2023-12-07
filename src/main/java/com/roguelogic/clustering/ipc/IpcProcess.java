/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

/**
 * @author Robert C. Ilardi
 * 
 */

public interface IpcProcess {
  /**
   * This method should be implemented by any Class that wants to participate in
   * an IPC conversation. Upon invocation of this method by the
   * AmgIpcProcessHost an implementing class MUST process the Event. If the
   * Event's requiresIpcAck is set to FALSE an implementing class CAN choose to
   * delay processing of the event by storing it in it's own queuing mechanism.
   * HOWEVER if requiresIpcAck is set to TRUE an implementing class MUST process
   * the event immediately or if processing is postponed, then returning of TRUE
   * by this method MUST mean that the storing of the event for later processing
   * was successful.
   * 
   * @param event
   * @return It MUST return TRUE if the event was processed successfully, and
   *         false if otherwise. However this is only returned to the orginating
   *         process if the Event's requiresIpcAck is set to TRUE.
   */
  public boolean handleAmgIpcEvent(IpcEvent event);
}
