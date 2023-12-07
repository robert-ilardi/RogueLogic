/*
 * Created on Sep 29, 2005
 */
package com.roguelogic.platform;

/**
 * @author rilardi
 */

public interface PlatformModule {
  public PlatformModuleSignature getModuleSignature();

  public void setPlatform(RLPlatform platform);

  public void platformEventFired(PlatformEvent event);

  public boolean processPlatformEventAsynchronously();
}

