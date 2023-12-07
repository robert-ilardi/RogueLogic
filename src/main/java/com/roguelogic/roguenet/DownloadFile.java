package com.roguelogic.roguenet;

/**
 * Created Nov 29, 2006
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class DownloadFile implements Serializable {

  private String remoteDir;
  private String remoteFile;

  private File localFile;
  private String clientTransferId;
  private String serverTransferId;

  private BufferedOutputStream bfOutStream;

  private long currentSize;
  private long totalSize;

  private String ssoSessionToken;
  private String p2phSessionToken;

  private boolean aborted = false;
  private boolean reverse;

  private String replySubject;

  public DownloadFile() {}

  public String getClientTransferId() {
    return clientTransferId;
  }

  public void setClientTransferId(String clientTransferId) {
    this.clientTransferId = clientTransferId;
  }

  public File getLocalFile() {
    return localFile;
  }

  public void setLocalFile(File localFile) {
    this.localFile = localFile;
  }

  public String getServerTransferId() {
    return serverTransferId;
  }

  public void setServerTransferId(String serverTransferId) {
    this.serverTransferId = serverTransferId;
  }

  public long getCurrentSize() {
    return currentSize;
  }

  public void setCurrentSize(long currentSize) {
    this.currentSize = currentSize;
  }

  public long getTotalSize() {
    return totalSize;
  }

  public void setTotalSize(long totalSize) {
    this.totalSize = totalSize;
  }

  public long incrementCurrentSize(int additionalLen) {
    currentSize += additionalLen;

    return currentSize;
  }

  public String getRemoteDir() {
    return remoteDir;
  }

  public void setRemoteDir(String remoteDir) {
    this.remoteDir = remoteDir;
  }

  public String getRemoteFile() {
    return remoteFile;
  }

  public void setRemoteFile(String remoteFile) {
    this.remoteFile = remoteFile;
  }

  public String getP2phSessionToken() {
    return p2phSessionToken;
  }

  public void setP2phSessionToken(String sessionToken) {
    p2phSessionToken = sessionToken;
  }

  public String getSsoSessionToken() {
    return ssoSessionToken;
  }

  public void setSsoSessionToken(String ssoSessionToken) {
    this.ssoSessionToken = ssoSessionToken;
  }

  public int hashCode() {
    return (clientTransferId != null ? clientTransferId.hashCode() : 0);
  }

  public boolean equals(Object obj) {
    boolean same = false;
    DownloadFile other;

    if (obj instanceof FileTransferRequest) {
      other = (DownloadFile) obj;
      same = ((this.clientTransferId != null && this.clientTransferId.equals(other.clientTransferId)) || (this.clientTransferId == null && other.clientTransferId == null));
    }

    return same;
  }

  /**
   * Opens the File Input Stream IF ONLY IF it is not already opened
   * @throws IOException
   */
  public synchronized void openFile() throws IOException {
    if (bfOutStream == null) {
      bfOutStream = new BufferedOutputStream(new FileOutputStream(localFile));
    }
  }

  public OutputStream getOutputStream() throws IOException {
    openFile(); //Make sure the file is opened
    return bfOutStream;
  }

  public synchronized void closeFile() throws IOException {
    if (bfOutStream != null) {
      bfOutStream.close();
      bfOutStream = null;
    }
  }

  public boolean isStreamOpened() {
    return bfOutStream != null;
  }

  public boolean isAborted() {
    return aborted;
  }

  public void setAborted(boolean aborted) {
    this.aborted = aborted;
  }

  public boolean isReverse() {
    return reverse;
  }

  public void setReverse(boolean reverse) {
    this.reverse = reverse;
  }

  public String getReplySubject() {
    return replySubject;
  }

  public void setReplySubject(String replySubject) {
    this.replySubject = replySubject;
  }

}
