/**
 * Created Aug 4, 2008
 */
package com.roguelogic.clustering.smap;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public interface SharedMapImplementation {

  public void close() throws SharedMemoryException;

  public void setSMemSecurityManager(SMemSecurityManager secMan);

  public boolean login(String username, String password) throws SharedMemoryException;

  public Serializable get(Serializable key) throws SharedMemoryException;

  public Serializable put(Serializable key, Serializable value) throws SharedMemoryException;

  public Serializable remove(Serializable key) throws SharedMemoryException;

  public boolean containsKey(Serializable key) throws SharedMemoryException;

  public void clear() throws SharedMemoryException;

  public boolean lockEntry(Serializable key, long timeout) throws SharedMemoryException;

  public void unlockEntry(Serializable key) throws SharedMemoryException;

  public boolean isEntryLocked(Serializable key) throws SharedMemoryException;

}
