/**
 * Created Nov 19, 2006
 */
package com.roguelogic.roguenet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class FileTransferRequest implements Serializable {

  private File requestFile;
  private String replySubject;
  private String requestor;
  private String clientTransferId;
  private String serverTransferId;

  private FileInputStream fInpStream;
  private FileOutputStream fOutStream;

  private boolean streamsPermClosed = false;

  public FileTransferRequest() {}

  public String getReplySubject() {
    return replySubject;
  }

  public void setReplySubject(String replySubject) {
    this.replySubject = replySubject;
  }

  public File getRequestFile() {
    return requestFile;
  }

  public void setRequestFile(File requestFile) {
    this.requestFile = requestFile;
  }

  public String getRequestor() {
    return requestor;
  }

  public void setRequestor(String requestor) {
    this.requestor = requestor;
  }

  public String getClientTransferId() {
    return clientTransferId;
  }

  public void setClientTransferId(String transferId) {
    this.clientTransferId = transferId;
  }

  public String getServerTransferId() {
    return serverTransferId;
  }

  public void setServerTransferId(String serverTransferId) {
    this.serverTransferId = serverTransferId;
  }

  public int hashCode() {
    return (serverTransferId != null ? serverTransferId.hashCode() : 0);
  }

  public boolean equals(Object obj) {
    boolean same = false;
    FileTransferRequest other;

    if (obj instanceof FileTransferRequest) {
      other = (FileTransferRequest) obj;
      same = ((this.serverTransferId != null && this.serverTransferId.equals(other.serverTransferId)) || (this.serverTransferId == null && other.serverTransferId == null));
    }

    return same;
  }

  /**
   * Opens the File Input Stream IF ONLY IF it is not already opened
   * @throws IOException
   */
  public synchronized void openInputFile() throws IOException {
    if (streamsPermClosed) {
      throw new IOException("File Transfer Request has locked stream to closed!");
    }

    if (fInpStream == null) {
      fInpStream = new FileInputStream(requestFile);
    }
  }

  public InputStream getInputStream() throws IOException {
    openInputFile(); //Make sure the file is opened
    return fInpStream;
  }

  public synchronized void closeFile() throws IOException {
    if (fInpStream != null) {
      fInpStream.close();
      fInpStream = null;
    }

    if (fOutStream != null) {
      fOutStream.close();
      fOutStream = null;
    }
  }

  public void setStreamsPermClosed(boolean streamsPermClosed) {
    this.streamsPermClosed = streamsPermClosed;
  }

  public boolean isStreamsPermClosed() {
    return streamsPermClosed;
  }

  /**
   * Opens the File Output Stream IF ONLY IF it is not already opened
   * @throws IOException
   */
  public synchronized void openOutputFile() throws IOException {
    if (streamsPermClosed) {
      throw new IOException("File Transfer Request has locked stream to closed!");
    }

    if (fOutStream == null) {
      fOutStream = new FileOutputStream(requestFile);
    }
  }

  public OutputStream getOutputStream() throws IOException {
    openOutputFile(); //Make sure the file is opened
    return fOutStream;
  }

  public synchronized void invalidate() {
    requestFile = null;
    replySubject = null;
    requestor = null;
    clientTransferId = null;
    serverTransferId = null;
  }

}
