/**
 * Created Dec 11, 2008
 */
package com.roguelogic.offsite;

import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_GET_LOGIN_MASK;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_GET_REMOTE_FILE_INFO;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_LOGIN;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_REMOVE_FROM_BACKUP;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_START_UPLOAD;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_SYNC_TOUCH_TS;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_UPLOAD_COMPLETE;
import static com.roguelogic.offsite.OffSiteProtocolConstants.OFFS_RLTCMD_UPLOAD_NEXT_FILE_CHUNK;
import static com.roguelogic.offsite.OffSiteProtocolConstants.STATUS_CODE_SUCESS;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkUtils;
import com.roguelogic.net.rltalk.RLTalkXorCodec;
import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.SimpleXORCodec;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class FileTransmitter implements SocketProcessorCustomizer {

  public static final long LOGIN_REQUEST_TTL = 60000;
  public static final long START_UPLOAD_REQUEST_TTL = 60000;
  public static final long UPLOAD_COMPLETE_REQUEST_TTL = 60000;
  public static final long REMOTE_FILE_INFO_REQUEST_TTL = 60000;
  public static final long SYNC_REMOTE_TOUCH_TS_REQUEST_TTL = 60000;
  public static final long GET_LOGIN_MASK_REQUEST_TTL = 60000;
  public static final long REMOVE_FROM_BACKUP_REQUEST_TTL = 60000;

  public static final int BUFFER_SIZE = 32768;

  public static final String PROP_BACKUP_SITE_ADDRESS = "BackupSiteAddress";
  public static final String PROP_BACKUP_SITE_PORT = "BackupSitePort";
  public static final String PROP_BACKUP_SITE_USERNAME = "BackupSiteUsername";
  public static final String PROP_BACKUP_SITE_PASSWORD = "BackupSitePassword";
  public static final String PROP_WINDOWS_DRIVE_LETTER_MAPPING = "WindowsDriveLetterMapping";
  public static final String PROP_TRANSPORT_XOR_FILE = "TransportXorFile";
  public static final String PROP_FILE_ENCRYPTION_PASS_PHASE = "FileEncryptionPassPhase";
  public static final String PROP_FILE_ENCRYPTION_SALT = "FileEncryptionSalt";
  public static final String PROP_FILE_ENCRYPTION_ITERATION_CNT = "FileEncryptionIterationCnt";

  private OffSitePublisher publisher;

  private SocketClient sockClient;

  private Object syncReqLock;

  private boolean loginResRec;
  private boolean loginOk;

  private boolean uploadStartResRec;
  private boolean uploadStartOk;

  private boolean uploadCompleteResRec;
  private boolean uploadCompleteOk;

  private boolean remoteFileInfoResRec;
  private boolean remoteFileInfoOk;

  @SuppressWarnings("unused")
  private long remoteFileSize;

  private long remoteFileTouchTs;

  private boolean syncRemoteTouchTsResRec;
  private boolean syncRemoteTouchTsOk;

  private boolean getLoginMaskResRec;
  private boolean getLoginMaskOk;
  private byte[] loginMask;

  private boolean removeFromBackupResRec;
  private boolean removeFromBackupOk;

  private String address;
  private int port;
  private String username;
  private String password;

  private HashMap<String, String> winDriveLetterMap;

  private RLTalkXorCodec transportXorCodec;
  private String transportXorFile;

  private String desPassPhrase;
  private byte[] desSalt;
  private int desIterCnt = -1;

  public FileTransmitter(OffSitePublisher publisher) {
    this.publisher = publisher;

    syncReqLock = new Object();

    winDriveLetterMap = new HashMap<String, String>();
  }

  public void init() throws IOException {
    publisher.log("Initializing File Transmitter:");

    readProperties();
    initTransportXorCodec();
  }

  private void readProperties() {
    String tmp;
    String[] tmpArr, tmpArr2;

    address = publisher.getProperty(PROP_BACKUP_SITE_ADDRESS);

    tmp = publisher.getProperty(PROP_BACKUP_SITE_PORT);
    port = Integer.parseInt(tmp);

    username = publisher.getProperty(PROP_BACKUP_SITE_USERNAME);
    password = publisher.getProperty(PROP_BACKUP_SITE_PASSWORD);

    tmp = publisher.getProperty(PROP_WINDOWS_DRIVE_LETTER_MAPPING);

    if (!StringUtils.IsNVL(tmp)) {
      tmpArr = tmp.split(";");
      tmpArr = StringUtils.Trim(tmpArr);

      for (int i = 0; i < tmpArr.length; i++) {
        tmpArr2 = tmpArr[i].split("=");
        winDriveLetterMap.put(tmpArr2[0].trim().toUpperCase(), tmpArr2[1].trim());
      }
    }

    transportXorFile = publisher.getProperty(PROP_TRANSPORT_XOR_FILE);

    desPassPhrase = publisher.getProperty(PROP_FILE_ENCRYPTION_PASS_PHASE);

    tmp = publisher.getProperty(PROP_FILE_ENCRYPTION_SALT);

    if (!StringUtils.IsNVL(tmp)) {
      tmpArr = tmp.split(",");
      tmpArr = StringUtils.Trim(tmpArr);

      desSalt = new byte[tmpArr.length];
      for (int i = 0; i < tmpArr.length; i++) {
        desSalt[i] = (byte) Integer.parseInt(tmpArr[i]);
      }
    }

    tmp = publisher.getProperty(PROP_FILE_ENCRYPTION_ITERATION_CNT);

    if (!StringUtils.IsNVL(tmp)) {
      desIterCnt = Integer.parseInt(tmp);
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    FileTransmitterProcessor ftPrc;

    if (processor instanceof FileTransmitterProcessor) {
      ftPrc = (FileTransmitterProcessor) processor;
      ftPrc.setTransmitter(this);
    }
  }

  public void initTransportXorCodec() throws IOException {
    byte[] keyData = SystemUtils.LoadDataFromFile(transportXorFile);
    transportXorCodec = new RLTalkXorCodec();
    transportXorCodec.setKeyData(keyData);
  }

  private synchronized void sendEncrypted(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
    CommandDataPair cipherCmDatPair;

    // Synchronized to make sure a complete message
    // from a single thread will be sent before
    // the next message can be sent!

    cipherCmDatPair = transportXorCodec.encrypt(cmDatPair);

    RLTalkUtils.RLTalkSend(sockClient.getUserSession(), cipherCmDatPair);
  }

  public synchronized void startSession() throws RLNetException, OffSiteException, InterruptedException {
    ensureConnection();
  }

  public synchronized void endSession() {
    close();
  }

  private void ensureConnection() throws RLNetException, OffSiteException, InterruptedException {
    if (sockClient == null || !sockClient.isConnected() || !loginOk) {
      connect();
      getLoginMask();
      login();
    }
  }

  private void close() {
    if (sockClient != null) {
      loginOk = false;
      sockClient.close();
      sockClient = null;
    }
  }

  private void connect() throws RLNetException {
    close();

    transportXorCodec._resetIndexes();

    sockClient = new SocketClient();
    sockClient.setSocketProcessorClass(FileTransmitterProcessor.class);
    sockClient.setSocketProcessorCustomizer(this);

    sockClient.connect(address, port);
  }

  private void login() throws RLNetException, InterruptedException, OffSiteException {
    CommandDataPair request;
    long startTime;

    request = new CommandDataPair();
    request.setCommand(OFFS_RLTCMD_LOGIN);
    request.setData(maskLogin(new StringBuffer().append(username).append("|").append(password).toString()));

    synchronized (syncReqLock) {
      loginResRec = false;
      loginOk = false;
      startTime = System.currentTimeMillis();

      // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
      sendEncrypted(request);

      while (!loginResRec && System.currentTimeMillis() < startTime + LOGIN_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!loginResRec) {
        throw new OffSiteException("Login Request Timed Out!");
      }

      if (!loginOk) {
        throw new OffSiteException("Login Failed!");
      }
    }
  }

  public void processLoginResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      loginResRec = true;

      loginOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      syncReqLock.notifyAll();
    }
  }

  private String relativeizeLocalFile(String absPath) {
    String relPath, driveLetter, driveLetterMapDir;

    absPath = FilenameUtils.NormalizeToUnix(absPath.trim());

    if (absPath.length() >= 3 && ":/".equals(absPath.substring(1, 3))) {
      // Contains Windows Drive Letter
      driveLetter = absPath.substring(0, 1).toUpperCase();
      driveLetterMapDir = winDriveLetterMap.get(driveLetter);

      if (!StringUtils.IsNVL(driveLetterMapDir)) {
        relPath = absPath.substring(3);
        relPath = (new StringBuffer()).append("/").append(driveLetterMapDir).append("/").append(relPath).toString();
      }
      else {
        relPath = absPath.substring(2);
      }

      // publisher.log("  >> Windows Drive Letter Detected. File Path Change: " + bakUpFilePath);
    }
    else {
      relPath = absPath;
    }

    return relPath;
  }

  public void transmitFile(LocalFile lf) throws RLNetException, OffSiteException, InterruptedException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
      NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    String bakUpFilePath, srcFilePath;
    long bytesTrnsfrd, startTime, endTime;
    double totalTime, bps;

    srcFilePath = lf.getName();

    bakUpFilePath = relativeizeLocalFile(srcFilePath);

    ensureConnection();

    startTime = System.currentTimeMillis();
    openRemoteFile(bakUpFilePath);

    bytesTrnsfrd = upload(srcFilePath, lf.isEncryptionOn());

    closeRemoteFile();
    endTime = System.currentTimeMillis();

    synchronizedRemoteTouchTs(srcFilePath);

    totalTime = (endTime - startTime) / 1000.0d;

    if (totalTime == 0.0d) {
      totalTime = 0.1d;
    }

    bps = (bytesTrnsfrd / 1000.0d) / totalTime;

    try {
      publisher.log("  >> Transferred " + bytesTrnsfrd + (bytesTrnsfrd == 1 ? " byte in" : " bytes in ") + StringUtils.FormatDouble(totalTime, 2) + " seconds. Avg Speed = "
          + StringUtils.FormatDouble(bps, 2) + " KB/sec");
    }
    catch (Exception e) {
      publisher.log(e);
    }
  }

  private void openRemoteFile(String relativeRemotePath) throws RLNetException, InterruptedException, OffSiteException {
    CommandDataPair request;
    long startTime;

    request = new CommandDataPair();
    request.setCommand(OFFS_RLTCMD_START_UPLOAD);
    request.setData(relativeRemotePath);

    synchronized (syncReqLock) {
      uploadStartResRec = false;
      uploadStartOk = false;
      startTime = System.currentTimeMillis();

      // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
      sendEncrypted(request);

      while (!uploadStartResRec && System.currentTimeMillis() < startTime + START_UPLOAD_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!uploadStartResRec) {
        throw new OffSiteException("Request to Start Upload Timed Out!");
      }

      if (!uploadStartOk) {
        throw new OffSiteException("Request to Start Upload Failed!");
      }
    }
  }

  public void processOpenRemoteFileResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      uploadStartResRec = true;

      uploadStartOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      syncReqLock.notifyAll();
    }
  }

  private void closeRemoteFile() throws RLNetException, InterruptedException, OffSiteException {
    CommandDataPair request;
    long startTime;

    request = new CommandDataPair();
    request.setCommand(OFFS_RLTCMD_UPLOAD_COMPLETE);

    synchronized (syncReqLock) {
      uploadCompleteResRec = false;
      uploadCompleteOk = false;
      startTime = System.currentTimeMillis();

      // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
      sendEncrypted(request);

      while (!uploadCompleteResRec && System.currentTimeMillis() < startTime + UPLOAD_COMPLETE_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!uploadCompleteResRec) {
        throw new OffSiteException("Request to Complete Upload Timed Out!");
      }

      if (!uploadCompleteOk) {
        throw new OffSiteException("Request to Complete Upload Failed!");
      }
    }
  }

  public void processCloseRemoteFileResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      uploadCompleteResRec = true;

      uploadCompleteOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      syncReqLock.notifyAll();
    }
  }

  private long upload(String srcFilePath, boolean useEncryption) throws IOException, RLNetException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    CipherInputStream cis = null;
    InputStream is;
    byte[] buf;
    int len;
    long totalLen = 0;

    try {
      fis = new FileInputStream(srcFilePath);
      bis = new BufferedInputStream(fis);

      if (useEncryption) {
        cis = new CipherInputStream(bis, createEncryptor());
        is = cis;
      }
      else {
        is = bis;
      }

      buf = new byte[BUFFER_SIZE];

      len = is.read(buf);

      while (len != -1) {
        totalLen += len;

        upload(buf, len);
        len = is.read(buf);
      }
    } // End try block
    finally {
      is = null;

      if (cis != null) {
        try {
          cis.close();
        }
        catch (Exception e) {}
      }

      if (bis != null) {
        try {
          bis.close();
        }
        catch (Exception e) {}

        if (fis != null) {
          try {
            fis.close();
          }
          catch (Exception e) {}
        }
      }
    }

    return totalLen;
  }

  private synchronized Cipher createEncryptor() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    KeySpec keySpec = new PBEKeySpec(desPassPhrase.toCharArray(), desSalt, desIterCnt);
    SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
    Cipher encryptor = Cipher.getInstance(key.getAlgorithm());

    AlgorithmParameterSpec paramSpec = new PBEParameterSpec(desSalt, desIterCnt);

    // Create the ciphers
    encryptor.init(Cipher.ENCRYPT_MODE, key, paramSpec);

    return encryptor;
  }

  private void upload(byte[] buf, int len) throws RLNetException {
    CommandDataPair request;
    byte[] buf2;

    request = new CommandDataPair();
    request.setCommand(OFFS_RLTCMD_UPLOAD_NEXT_FILE_CHUNK);

    if (len != buf.length) {
      buf2 = new byte[len];
      System.arraycopy(buf, 0, buf2, 0, len);

      request.setData(buf2);
    }
    else {
      request.setData(buf);
    }

    // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
    sendEncrypted(request);
  }

  public boolean localEqualsRemote(String localPath) throws RLNetException, InterruptedException, OffSiteException {
    boolean same = false;
    CommandDataPair request;
    long startTime;
    String relPath;
    File localFile;

    ensureConnection();

    relPath = relativeizeLocalFile(localPath);

    request = new CommandDataPair();
    request.setCommand(OFFS_RLTCMD_GET_REMOTE_FILE_INFO);
    request.setData(relPath);

    synchronized (syncReqLock) {
      remoteFileInfoResRec = false;
      remoteFileInfoOk = false;
      startTime = System.currentTimeMillis();

      // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
      sendEncrypted(request);

      while (!remoteFileInfoResRec && System.currentTimeMillis() < startTime + REMOTE_FILE_INFO_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!remoteFileInfoResRec) {
        throw new OffSiteException("Request for Remote File Info Timed Out!");
      }

      if (!remoteFileInfoOk) {
        throw new OffSiteException("Request for Remote File Info Failed!");
      }

      localFile = new File(localPath);

      // same = (localFile.length() == remoteFileSize && localFile.lastModified() == remoteFileTouchTs);
      same = (localFile.lastModified() == remoteFileTouchTs); // Only compare Timestamp due to encryption!
    }

    return same;
  }

  public void processRemoteFileInfoResponse(CommandDataPair response) {
    String tmp;
    String[] tokens;

    synchronized (syncReqLock) {
      remoteFileInfoResRec = true;

      remoteFileInfoOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      if (remoteFileInfoOk) {
        tmp = response.getString();
        tokens = tmp.split("\\|");

        remoteFileSize = Long.parseLong(tokens[0]);
        remoteFileTouchTs = Long.parseLong(tokens[1]);
      }

      syncReqLock.notifyAll();
    }
  }

  public boolean synchronizedRemoteTouchTs(String localPath) throws RLNetException, InterruptedException, OffSiteException {
    boolean same = false;
    CommandDataPair request;
    long startTime;
    String relPath;
    File localFile;

    relPath = relativeizeLocalFile(localPath);

    request = new CommandDataPair();
    request.setCommand(OFFS_RLTCMD_SYNC_TOUCH_TS);

    localFile = new File(localPath);
    request.setData((new StringBuffer()).append(relPath).append("|").append(localFile.lastModified()).toString());

    synchronized (syncReqLock) {
      syncRemoteTouchTsResRec = false;
      syncRemoteTouchTsOk = false;
      startTime = System.currentTimeMillis();

      // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
      sendEncrypted(request);

      while (!syncRemoteTouchTsResRec && System.currentTimeMillis() < startTime + SYNC_REMOTE_TOUCH_TS_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!syncRemoteTouchTsResRec) {
        throw new OffSiteException("Remote File Touch TS Sync Request Timed Out!");
      }

      if (!syncRemoteTouchTsOk) {
        throw new OffSiteException("Remote File Touch TS Sync Request Failed!");
      }
    }

    return same;
  }

  public void processSynchronizedRemoteTouchTsResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      syncRemoteTouchTsResRec = true;

      syncRemoteTouchTsOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      syncReqLock.notifyAll();
    }
  }

  public CommandDataPair decrypt(CommandDataPair cmDatPair) throws RLTalkException {
    return transportXorCodec.decrypt(cmDatPair);
  }

  private void getLoginMask() throws RLNetException, InterruptedException, OffSiteException {
    CommandDataPair request;
    long startTime;

    request = new CommandDataPair();
    request.setCommand(OFFS_RLTCMD_GET_LOGIN_MASK);

    synchronized (syncReqLock) {
      getLoginMaskResRec = false;
      getLoginMaskOk = false;
      startTime = System.currentTimeMillis();

      // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
      sendEncrypted(request);

      while (!getLoginMaskResRec && System.currentTimeMillis() < startTime + GET_LOGIN_MASK_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!getLoginMaskResRec) {
        throw new OffSiteException("Get Login Mask Request Timed Out!");
      }

      if (!getLoginMaskOk) {
        throw new OffSiteException("Get Login Mask Failed!");
      }
    }
  }

  public void processGetLoginMaskResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      getLoginMaskResRec = true;

      getLoginMaskOk = response.getStatusCode() == STATUS_CODE_SUCESS;
      loginMask = response.getData();

      syncReqLock.notifyAll();
    }
  }

  public byte[] maskLogin(String loginStr) {
    byte[] bArr;
    SimpleXORCodec codec;

    codec = new SimpleXORCodec();
    codec.setKeyData(loginMask);
    bArr = codec.encrypt(loginStr.getBytes());

    return bArr;
  }

  public void removeFromRemote(LocalFile lf) throws OffSiteException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;
    String relPath;

    ensureConnection();

    relPath = relativeizeLocalFile(lf.getName());

    request = new CommandDataPair();
    request.setCommand(OFFS_RLTCMD_REMOVE_FROM_BACKUP);
    request.setData(relPath);

    synchronized (syncReqLock) {
      removeFromBackupResRec = false;
      removeFromBackupOk = false;
      startTime = System.currentTimeMillis();

      // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
      sendEncrypted(request);

      while (!removeFromBackupResRec && System.currentTimeMillis() < startTime + REMOVE_FROM_BACKUP_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!removeFromBackupResRec) {
        throw new OffSiteException("Request for Remote Removal Timed Out!");
      }

      if (!removeFromBackupOk) {
        throw new OffSiteException("Request for Remote Removal Failed!");
      }
    }
  }

  public void processRemoveFromBackupResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      removeFromBackupResRec = true;

      removeFromBackupOk = response.getStatusCode() == STATUS_CODE_SUCESS;

      syncReqLock.notifyAll();
    }
  }

}
