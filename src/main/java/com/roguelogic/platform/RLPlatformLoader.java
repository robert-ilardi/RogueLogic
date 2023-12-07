/*
 * Created on Sep 29, 2005
 */
package com.roguelogic.platform;

/**
 * @author rilardi
 */

public final class RLPlatformLoader {

  private static RLPlatformLoader LoaderInstance = null;

  private RLPlatform platform;
  private boolean platformInitialized;

  private RLPlatformLoader() throws PlatformException {
    platform = new RLPlatform();
    platformInitialized = false;
  }

  public static synchronized RLPlatformLoader GetLoader() throws PlatformException {
    if (LoaderInstance == null) {
      LoaderInstance = new RLPlatformLoader();
    }

    return LoaderInstance;
  }

  public synchronized void initializePlatform(InterfaceModule[] interfaceModules, BusinessModule[] businessModules) throws PlatformException {
    if (platformInitialized) {
      throw new PlatformException("Platform is Already Initialized!");
    }

    if (interfaceModules == null || interfaceModules.length == 0) {
      throw new PlatformException("Platform Initialization Aborted! At least one Interface Module MUST be provided...");
    }

    if (businessModules == null || businessModules.length == 0) {
      throw new PlatformException("Platform Initialization Aborted! At least one Business Module MUST be provided...");
    }

    for (int i = 0; i < interfaceModules.length; i++) {
      if (interfaceModules[i] == null) {
        throw new PlatformException("Platform Initialization Aborted! Interface Module " + i + " is NULL...");
      }

      platform.installInterfaceModule(interfaceModules[i]);
    }

    for (int i = 0; i < businessModules.length; i++) {
      if (businessModules[i] == null) {
        throw new PlatformException("Platform Initialization Aborted! Business Module " + i + " is NULL...");
      }

      platform.installBusinessModule(businessModules[i]);
    }

    platformInitialized = true;
  }

  public synchronized boolean isPlatformInitialized() {
    return platformInitialized;
  }

  public static synchronized RLPlatform GetPlatform() throws PlatformException {
    if (LoaderInstance == null) {
      throw new PlatformException("Can NOT Obtain Platform Instance! Platform Loader Instance is NULL!");
    }

    if (!LoaderInstance.isPlatformInitialized()) {
      throw new PlatformException("Platform Instance is NOT Initialized!");
    }

    return LoaderInstance.platform;
  }

}

