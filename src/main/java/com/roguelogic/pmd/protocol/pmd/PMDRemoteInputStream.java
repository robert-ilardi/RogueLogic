/**
 * Created Nov 21, 2007
 */
package com.roguelogic.pmd.protocol.pmd;

import java.io.IOException;
import java.io.InputStream;

import com.roguelogic.pmd.PMDClient;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */
public class PMDRemoteInputStream extends InputStream {

  private PMDClient client;

  private byte[] buffer;
  private int bufPntr;

  public PMDRemoteInputStream(PMDClient client) {
    super();

    this.client = client;
    bufPntr = 0;
    buffer = null;
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    if (client == null) {
      System.out.println("Client Is Null");
      return -1;
    }

    if (buffer == null || bufPntr >= buffer.length) {
      try {
        buffer = client.readNextFileChunk();
        bufPntr = 0;
      }
      catch (Exception e) {
        throw new IOException(StringUtils.GetStackTraceString(e));
      }
    }

    //System.out.println(bufPntr);

    if (buffer != null && buffer.length > 0) {
      //System.out.println((int) buffer[bufPntr] & 0xff);
      return (int) buffer[bufPntr++] & 0xff;
    }
    else {
      System.out.println("END-OF-STREAM");
      return -1;
    }

    //return buffer != null && buffer.length > 0 ? (int) buffer[bufPntr++] : -1;
  }

  @Override
  public void close() throws IOException {
    if (client != null) {
      try {
        client.closeFile();
        client = null;
      }
      catch (Exception e) {
        throw new IOException(StringUtils.GetStackTraceString(e));
      }
    }
  }

}
