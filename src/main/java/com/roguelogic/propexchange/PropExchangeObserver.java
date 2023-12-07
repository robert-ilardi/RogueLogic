/**
 * Created Jan 20, 2007
 */
package com.roguelogic.propexchange;

/**
 * @author Robert C. Ilardi
 *
 */

public interface PropExchangeObserver {

  public void register(PropExchangePayload payload);

  public void unregister(PropExchangePayload payload);

  public void receive(PropExchangePayload payload);

}
