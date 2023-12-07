/*
 * Created on Sep 29, 2005
 */
package com.roguelogic.platform;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author rilardi
 */

public final class RLPlatform {

  private HashMap<PlatformModuleSignature, InterfaceModule> interfaceModules;
  private HashMap<PlatformModuleSignature, BusinessModule> businessModules;

  protected RLPlatform() throws PlatformException {
    interfaceModules = new HashMap<PlatformModuleSignature, InterfaceModule>();
    businessModules = new HashMap<PlatformModuleSignature, BusinessModule>();
  }

  protected void installInterfaceModule(InterfaceModule module) {
    if (module != null) {
      module.setPlatform(this);
      interfaceModules.put(module.getModuleSignature(), module);
    }
  }

  protected void installBusinessModule(BusinessModule module) {
    if (module != null) {
      module.setPlatform(this);
      businessModules.put(module.getModuleSignature(), module);
    }
  }

  /*
   * This method publishes an event from a Business or Interface Module
   * to destination Business or Interface Modules.
   */
  public synchronized void publishEvent(PlatformEvent event) {
    PlatformModule module;
    Collection modules;
    Iterator iter;
    PlatformModuleSignature[] destinations;

    if (event != null) {
      switch (event.getBroadcastMode()) {
        case PlatformEvent.BROADCAST_MODE_NONE:
          //Non-Broadcast Event

          //Send event to Select Destination Business and Interfaces Modules ONLY
          destinations = event.getDestinations();

          if (destinations != null) {
            for (int i = 0; i < destinations.length; i++) {
              if (destinations[i] != null) {
                switch (destinations[i].getType()) {
                  case PlatformModuleSignature.BUSINESS_MODULE:
                    module = (BusinessModule) businessModules.get(destinations[i]);
                    module.platformEventFired(event);
                    break;
                  case PlatformModuleSignature.INTERFACE_MODULE:
                    module = (InterfaceModule) interfaceModules.get(destinations[i]);
                    module.platformEventFired(event);
                    break;
                }
              }
            }
          }
          break;
        case PlatformEvent.BROADCAST_MODE_ALL:
          //Broadcast Event to ALL Modules

          //Broadcast Event to all Business Modules First
          modules = businessModules.values();
          iter = modules.iterator();

          while (iter.hasNext()) {
            module = (BusinessModule) iter.next();
            if (!module.getModuleSignature().equals(event.getSource())) {
              module.platformEventFired(event);
            }
          }

          //Broadcast Event to all Interface Modules Second
          modules = interfaceModules.values();
          iter = modules.iterator();

          while (iter.hasNext()) {
            module = (InterfaceModule) iter.next();
            if (!module.getModuleSignature().equals(event.getSource())) {
              module.platformEventFired(event);
            }
          }
          break;
        case PlatformEvent.BROADCAST_MODE_BUSINESS_MODULES_ONLY:
          //Broadcast Event to all Business Modules ONLY
          modules = businessModules.values();
          iter = modules.iterator();

          while (iter.hasNext()) {
            module = (BusinessModule) iter.next();
            if (!module.getModuleSignature().equals(event.getSource())) {
              module.platformEventFired(event);
            }
          }
          break;
        case PlatformEvent.BROADCAST_MODE_INTERFACE_MODULES_ONLY:
          //Broadcast Event to all Interface Modules ONLY
          modules = interfaceModules.values();
          iter = modules.iterator();

          while (iter.hasNext()) {
            module = (InterfaceModule) iter.next();
            if (!module.getModuleSignature().equals(event.getSource())) {
              module.platformEventFired(event);
            }
          }
          break;
      } //End Broadcast Mode Switch Statement
    } //End NULL event check
  }

}
