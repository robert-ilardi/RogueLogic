/**
 * Created Jan 15, 2009
 */
package com.roguelogic.clustering.ipc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 * 
 */
public class IpcPeerRegistry implements Serializable {

  private HashMap<String, IpcPeer> peerMap;

  public IpcPeerRegistry() {
    peerMap = new HashMap<String, IpcPeer>();
  }

  public static IpcPeerRegistry LoadRegistryFile(IpcProcessHost processHost, String peerRegistryFile) throws IOException {
    IpcPeerRegistry registry = null;
    String[] lines, tokens;
    FileInputStream fis = null;
    IpcPeer peer;

    try {
      fis = new FileInputStream(peerRegistryFile);
      lines = StringUtils.ReadLinesIgnoreComments(fis, "#");

      registry = new IpcPeerRegistry();

      for (String line : lines) {
        tokens = line.trim().split("\\|");
        tokens = StringUtils.Trim(tokens);

        peer = new IpcPeer(tokens[0], tokens[1], Integer.parseInt(tokens[2]), processHost);
        registry.addPeer(peer);
      }
    } // End try block
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {
        }
      }
    }

    return registry;
  }

  public void addPeer(IpcPeer peer) {
    peerMap.put(peer.getProcessName(), peer);
  }

  public IpcPeer getPeer(String processName) {
    return peerMap.get((processName != null ? processName.trim().toUpperCase() : null));
  }

  public IpcPeer removePeer(String processName) {
    return peerMap.remove((processName != null ? processName.trim().toUpperCase() : null));
  }

  public void clear() {
    peerMap.clear();
  }

  public void closeAll() {
    for (IpcPeer peer : peerMap.values()) {
      try {
        peer.close();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
