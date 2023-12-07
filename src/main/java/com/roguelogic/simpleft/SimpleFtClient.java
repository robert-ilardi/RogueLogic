/**
 * Created Dec 9, 2008
 */
package com.roguelogic.simpleft;

import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_CHANGE_DIRECTORY;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_DATETIME;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_DOWNLOAD_DATA;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_ECHO;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_END_DOWNLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_END_UPLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_GET_LOGIN_MASK;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_LIST_DIRECTORY;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_LIST_SHARES;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_LOGIN;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_PING;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_START_DOWNLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_START_SDOWNLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_START_UPLOAD;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_UPLOAD_DATA;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_UPLOAD_DATA_NO_REPLY;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.STATUS_CODE_SUCCESS;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkUtils;
import com.roguelogic.net.rltalk.RLTalkXorCodec;
import com.roguelogic.util.SimpleXORCodec;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class SimpleFtClient implements SocketProcessorCustomizer {

  public static final String CLIENT_APP_TITLE = "SimpleFT Client";

  public static final String PROP_SERVER_ADDRESS = "ServerAddress";
  public static final String PROP_SERVER_PORT = "ServerPort";
  public static final String PROP_USERNAME = "ServerUsername";
  public static final String PROP_PASSWORD = "ServerPassword";

  public static final String PROP_TRANSPORT_XOR_FILE = "TransportXorFile";

  public static final long GET_LOGIN_MASK_REQUEST_TTL = 60000;
  public static final long LOGIN_REQUEST_TTL = 60000;
  public static final long GET_LIST_SHARES_REQUEST_TTL = 60000;
  public static final long GET_LIST_DIR_REQUEST_TTL = 60000;
  public static final long GET_CHANGE_DIR_REQUEST_TTL = 60000;
  public static final long PING_REQUEST_TTL = 60000;
  public static final long ECHO_REQUEST_TTL = 60000;
  public static final long START_UPLOAD_REQUEST_TTL = 60000;
  public static final long END_UPLOAD_REQUEST_TTL = 60000;
  public static final long UPLOAD_DATA_REQUEST_TTL = 60000;
  public static final long START_DOWNLOAD_REQUEST_TTL = 60000;
  public static final long END_DOWNLOAD_REQUEST_TTL = 60000;
  public static final long DOWNLOAD_DATA_REQUEST_TTL = 60000;
  public static final long DATETIME_REQUEST_TTL = 60000;

  private SimpleFtClientUI cui;

  private Properties sftcProps;

  private String serverAddr;
  private int serverPort;

  private String username;
  private String password;

  private SocketClient sockClient;

  private String transportXorFile;
  private RLTalkXorCodec transportXorCodec;

  private Object syncReqLock;

  private boolean getLoginMaskResRec;
  private boolean getLoginMaskOk;
  private byte[] loginMask;

  private boolean loginResRec;
  private boolean loginOk;

  private boolean listSharesResRec;
  private boolean listSharesOk;
  private String listSharesData;

  private boolean listDirResRec;
  private boolean listDirOk;
  private String listDirData;

  private boolean changeDirResRec;
  private boolean changeDirOk;

  private boolean pingResRec;
  private boolean pingOk;

  private boolean echoResRec;
  private boolean echoOk;
  private String echoData;

  private boolean startUploadResRec;
  private boolean startUploadOk;

  private boolean endUploadResRec;
  private boolean endUploadOk;

  private boolean uploadDataResRec;
  private boolean uploadDataOk;

  private boolean startDownloadResRec;
  private boolean startDownloadOk;
  private long startDownloadData;

  private boolean endDownloadResRec;
  private boolean endDownloadOk;

  private boolean downloadDataResRec;
  private boolean downloadDataOk;
  private byte[] downloadData;

  private boolean dateTimeResRec;
  private boolean dateTimeOk;
  private String dateTimeData;

  public SimpleFtClient(Properties sftcProps) {
    this.sftcProps = sftcProps;

    syncReqLock = new Object();
  }

  public void boot() throws IOException {
    System.out.println("Booting " + CLIENT_APP_TITLE + " at: " + StringUtils.GetTimeStamp());

    readProperties();
    initTransportXorCodec();

    System.out.println("Ready!");
  }

  private void readProperties() {
    String tmp;

    tmp = sftcProps.getProperty(PROP_SERVER_PORT);
    serverPort = Integer.parseInt(tmp);

    serverAddr = sftcProps.getProperty(PROP_SERVER_ADDRESS);

    username = sftcProps.getProperty(PROP_USERNAME);
    password = sftcProps.getProperty(PROP_PASSWORD);

    transportXorFile = sftcProps.getProperty(PROP_TRANSPORT_XOR_FILE);
  }

  public static void PrintWelcome() {
    System.out.print(Version.GetInfo());
  }

  public void shutdown() {}

  public void waitWhileRunning() throws InterruptedException {
    cui.waitOnMenuLoop();
  }

  public String getProperty(String propName) {
    return sftcProps.getProperty(propName);
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    SimpleFtClientProcessor ftPrc;

    if (processor instanceof SimpleFtClientProcessor) {
      ftPrc = (SimpleFtClientProcessor) processor;
      ftPrc.setClient(this);
    }
  }

  public CommandDataPair decrypt(CommandDataPair cmDatPair) throws RLTalkException {
    return transportXorCodec.decrypt(cmDatPair);
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

  public void ensureConnection() throws RLNetException, InterruptedException, SimpleFtException {
    if (sockClient == null || !sockClient.isConnected() || !loginOk) {
      connect();
      getLoginMask();
      login();
    }
  }

  public void close() {
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
    sockClient.setSocketProcessorClass(SimpleFtClientProcessor.class);
    sockClient.setSocketProcessorCustomizer(this);

    sockClient.connect(serverAddr, serverPort);
  }

  private void login() throws RLNetException, InterruptedException, SimpleFtException {
    CommandDataPair request;
    long startTime;

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_LOGIN);
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
        throw new SimpleFtException("Login Request Timed Out!");
      }

      if (!loginOk) {
        throw new SimpleFtException("Login Failed!");
      }
    }
  }

  public void processLoginResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      loginResRec = true;

      loginOk = response.getStatusCode() == STATUS_CODE_SUCCESS;

      syncReqLock.notifyAll();
    }
  }

  private void getLoginMask() throws RLNetException, InterruptedException, SimpleFtException {
    CommandDataPair request;
    long startTime;

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_GET_LOGIN_MASK);

    synchronized (syncReqLock) {
      getLoginMaskResRec = false;
      getLoginMaskOk = false;
      loginMask = null;
      startTime = System.currentTimeMillis();

      // RLTalkUtils.RLTalkSend(sockClient.getUserSession(), request);
      sendEncrypted(request);

      while (!getLoginMaskResRec && System.currentTimeMillis() < startTime + GET_LOGIN_MASK_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!getLoginMaskResRec) {
        throw new SimpleFtException("Get Login Mask Request Timed Out!");
      }

      if (!getLoginMaskOk) {
        throw new SimpleFtException("Get Login Mask Failed!");
      }
    }
  }

  public void processGetLoginMaskResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      getLoginMaskResRec = true;

      getLoginMaskOk = response.getStatusCode() == STATUS_CODE_SUCCESS;
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

  public void showCommandLine() {
    cui = new SimpleFtClientUI(this);
    cui.startMenuLoop();
  }

  public String[] listShares() throws SimpleFtException, RLTalkException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;
    String[] shares = null;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_LIST_SHARES);

    synchronized (syncReqLock) {
      listSharesResRec = false;
      listSharesOk = false;
      listSharesData = null;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!listSharesResRec && System.currentTimeMillis() < startTime + GET_LIST_SHARES_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!listSharesResRec) {
        throw new SimpleFtException("List Shares Request Timed Out!");
      }

      if (!listSharesOk) {
        throw new SimpleFtException("List Shares Failed!");
      }

      if (!StringUtils.IsNVL(listSharesData)) {
        shares = listSharesData.split("\\|");
      }
    }

    return shares;
  }

  public void processListSharesResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      listSharesResRec = true;

      listSharesOk = response.getStatusCode() == STATUS_CODE_SUCCESS;
      listSharesData = response.getString();

      syncReqLock.notifyAll();
    }
  }

  public void processListDirResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      listDirResRec = true;

      listDirOk = response.getStatusCode() == STATUS_CODE_SUCCESS;
      listDirData = response.getString();

      syncReqLock.notifyAll();
    }
  }

  public String[] listDir() throws SimpleFtException, RLTalkException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;
    String[] ls = null;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_LIST_DIRECTORY);

    synchronized (syncReqLock) {
      listDirResRec = false;
      listDirOk = false;
      listDirData = null;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!listDirResRec && System.currentTimeMillis() < startTime + GET_LIST_DIR_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!listDirResRec) {
        throw new SimpleFtException("List Dir Request Timed Out!");
      }

      if (!listDirOk) {
        throw new SimpleFtException("List Dir Failed!");
      }

      if (!StringUtils.IsNVL(listDirData)) {
        ls = listDirData.split("\\|");
      }
    }

    return ls;
  }

  public void changeDir(String dir) throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_CHANGE_DIRECTORY);
    request.setData(dir.trim());

    synchronized (syncReqLock) {
      changeDirResRec = false;
      changeDirOk = false;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!changeDirResRec && System.currentTimeMillis() < startTime + GET_CHANGE_DIR_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!changeDirResRec) {
        throw new SimpleFtException("Change Dir Request Timed Out!");
      }

      if (!changeDirOk) {
        throw new SimpleFtException("Change Dir Failed!");
      }
    }
  }

  public void processChangeDirResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      changeDirResRec = true;

      changeDirOk = response.getStatusCode() == STATUS_CODE_SUCCESS;

      syncReqLock.notifyAll();
    }
  }

  public void ping() throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_PING);

    synchronized (syncReqLock) {
      pingResRec = false;
      pingOk = false;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!pingResRec && System.currentTimeMillis() < startTime + PING_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!pingResRec) {
        throw new SimpleFtException("Ping Request Timed Out!");
      }

      if (!pingOk) {
        throw new SimpleFtException("Ping Failed!");
      }
    }
  }

  public void processPingResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      pingResRec = true;

      pingOk = response.getStatusCode() == STATUS_CODE_SUCCESS;

      syncReqLock.notifyAll();
    }
  }

  public String echo(String mesg) throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;
    String ret;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_ECHO);
    request.setData(mesg.trim());

    synchronized (syncReqLock) {
      echoResRec = false;
      echoOk = false;
      echoData = null;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!echoResRec && System.currentTimeMillis() < startTime + ECHO_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!echoResRec) {
        throw new SimpleFtException("Echo Request Timed Out!");
      }

      if (!echoOk) {
        throw new SimpleFtException("Echo Failed!");
      }

      ret = echoData;
    }

    return ret;
  }

  public void processEchoResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      echoResRec = true;

      echoOk = response.getStatusCode() == STATUS_CODE_SUCCESS;
      echoData = response.getString();

      syncReqLock.notifyAll();
    }
  }

  public void upload(String path, String remoteFileName, boolean syncWrite) throws SimpleFtException, RLNetException, InterruptedException, IOException {
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    byte[] buf, buf2;
    int len;

    try {
      fis = new FileInputStream(path);
      bis = new BufferedInputStream(fis);

      startUpload(remoteFileName);

      buf = new byte[10240];

      len = bis.read(buf);

      while (len != -1) {
        if (len == buf.length) {
          uploadData(buf, syncWrite);
        }
        else {
          buf2 = new byte[len];
          System.arraycopy(buf, 0, buf2, 0, len);
          uploadData(buf2, syncWrite);
        }

        len = bis.read(buf);
      } //while (len != -1)
    }//End try block
    finally {
      if (bis != null) {
        try {
          bis.close();
        }
        catch (Exception e) {}
      }

      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
      }

      try {
        endUpload();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void startUpload(String remoteFileName) throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_START_UPLOAD);
    request.setData(remoteFileName);

    synchronized (syncReqLock) {
      startUploadResRec = false;
      startUploadOk = false;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!startUploadResRec && System.currentTimeMillis() < startTime + START_UPLOAD_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!startUploadResRec) {
        throw new SimpleFtException("Start Upload Request Timed Out!");
      }

      if (!startUploadOk) {
        throw new SimpleFtException("Start Upload Failed!");
      }
    }
  }

  public void processStartUploadResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      startUploadResRec = true;

      startUploadOk = response.getStatusCode() == STATUS_CODE_SUCCESS;

      syncReqLock.notifyAll();
    }
  }

  public void endUpload() throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_END_UPLOAD);

    synchronized (syncReqLock) {
      endUploadResRec = false;
      endUploadOk = false;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!endUploadResRec && System.currentTimeMillis() < startTime + END_UPLOAD_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!endUploadResRec) {
        throw new SimpleFtException("End Upload Request Timed Out!");
      }

      if (!endUploadOk) {
        throw new SimpleFtException("End Upload Failed!");
      }
    }
  }

  public void processEndUploadResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      endUploadResRec = true;

      endUploadOk = response.getStatusCode() == STATUS_CODE_SUCCESS;

      syncReqLock.notifyAll();
    }
  }

  public void uploadData(byte[] data, boolean syncWrite) throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    request = new CommandDataPair();
    request.setCommand(syncWrite ? SFT_RLTCMD_UPLOAD_DATA : SFT_RLTCMD_UPLOAD_DATA_NO_REPLY);
    request.setData(data);

    if (syncWrite) {
      synchronized (syncReqLock) {
        uploadDataResRec = false;
        uploadDataOk = false;
        startTime = System.currentTimeMillis();

        sendEncrypted(request);

        while (!uploadDataResRec && System.currentTimeMillis() < startTime + UPLOAD_DATA_REQUEST_TTL) {
          syncReqLock.wait(1000);
        }

        if (!uploadDataResRec) {
          throw new SimpleFtException("Upload Data Request Timed Out!");
        }

        if (!uploadDataOk) {
          throw new SimpleFtException("Upload Data Failed!");
        }
      }
    }
    else {
      sendEncrypted(request);
    }
  }

  public void processUploadDataResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      uploadDataResRec = true;

      uploadDataOk = response.getStatusCode() == STATUS_CODE_SUCCESS;

      syncReqLock.notifyAll();
    }
  }

  public void download(String remoteFileName, String localPath, boolean syncRead) throws SimpleFtException, RLNetException, InterruptedException, IOException {
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    long fileLen;

    try {
      fos = new FileOutputStream(localPath);
      bos = new BufferedOutputStream(fos);

      fileLen = startDownload(remoteFileName, syncRead);

      if (syncRead) {
        pullData(fileLen, bos);
      }
      else {
        waitForDownloadPush();
      }
    }//End try block
    finally {
      if (bos != null) {
        try {
          bos.close();
        }
        catch (Exception e) {}
      }

      if (fos != null) {
        try {
          fos.close();
        }
        catch (Exception e) {}
      }

      if (syncRead) {
        try {
          endDownload();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public long startDownload(String remoteFileName, boolean syncRead) throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime, fileLen;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(syncRead ? SFT_RLTCMD_START_SDOWNLOAD : SFT_RLTCMD_START_DOWNLOAD);
    request.setData(remoteFileName);

    synchronized (syncReqLock) {
      startDownloadResRec = false;
      startDownloadOk = false;
      startDownloadData = 0;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!startDownloadResRec && System.currentTimeMillis() < startTime + START_DOWNLOAD_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!startDownloadResRec) {
        throw new SimpleFtException("Start Download Request Timed Out!");
      }

      if (!startDownloadOk) {
        throw new SimpleFtException("Start Download Failed!");
      }

      fileLen = startDownloadData;
    }

    return fileLen;
  }

  public void processStartDownloadResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      startDownloadResRec = true;

      startDownloadOk = response.getStatusCode() == STATUS_CODE_SUCCESS;
      startDownloadData = response.getLong();

      syncReqLock.notifyAll();
    }
  }

  public void endDownload() throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_END_DOWNLOAD);

    synchronized (syncReqLock) {
      endDownloadResRec = false;
      endDownloadOk = false;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!endDownloadResRec && System.currentTimeMillis() < startTime + END_DOWNLOAD_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!endDownloadResRec) {
        throw new SimpleFtException("End Download Request Timed Out!");
      }

      if (!endDownloadOk) {
        throw new SimpleFtException("End Download Failed!");
      }
    }
  }

  public void processEndDownloadResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      endDownloadResRec = true;

      endDownloadOk = response.getStatusCode() == STATUS_CODE_SUCCESS;

      syncReqLock.notifyAll();
    }
  }

  public byte[] downloadData() throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;
    byte[] data = null;

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_DOWNLOAD_DATA);

    synchronized (syncReqLock) {
      downloadDataResRec = false;
      downloadDataOk = false;
      downloadData = null;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!downloadDataResRec && System.currentTimeMillis() < startTime + DOWNLOAD_DATA_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!downloadDataResRec) {
        throw new SimpleFtException("Download Data Request Timed Out!");
      }

      if (!downloadDataOk) {
        throw new SimpleFtException("Download Data Failed!");
      }

      data = downloadData;
    }

    return data;
  }

  public void processDownloadDataResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      downloadDataResRec = true;

      downloadDataOk = response.getStatusCode() == STATUS_CODE_SUCCESS;
      downloadData = response.getData();

      syncReqLock.notifyAll();
    }
  }

  public void pullData(long fileLen, BufferedOutputStream bos) throws IOException, SimpleFtException, RLNetException, InterruptedException {
    long dloadLen = 0;
    byte[] buf;

    while (dloadLen < fileLen) {
      buf = downloadData();

      if (buf != null && buf.length > 0) {
        bos.write(buf);
        dloadLen += buf.length;
      }
    }
  }

  public String datetime() throws SimpleFtException, RLNetException, InterruptedException {
    CommandDataPair request;
    long startTime;
    String ret;

    ensureConnection();

    request = new CommandDataPair();
    request.setCommand(SFT_RLTCMD_DATETIME);

    synchronized (syncReqLock) {
      dateTimeResRec = false;
      dateTimeOk = false;
      dateTimeData = null;
      startTime = System.currentTimeMillis();

      sendEncrypted(request);

      while (!dateTimeResRec && System.currentTimeMillis() < startTime + DATETIME_REQUEST_TTL) {
        syncReqLock.wait(1000);
      }

      if (!dateTimeResRec) {
        throw new SimpleFtException("Date Time Request Timed Out!");
      }

      if (!dateTimeOk) {
        throw new SimpleFtException("Date Time Failed!");
      }

      ret = dateTimeData;
    }

    return ret;
  }

  public void processDateTimeResponse(CommandDataPair response) {
    synchronized (syncReqLock) {
      dateTimeResRec = true;

      dateTimeOk = response.getStatusCode() == STATUS_CODE_SUCCESS;
      dateTimeData = response.getString();

      syncReqLock.notifyAll();
    }
  }

  private void waitForDownloadPush() {
  // TODO Auto-generated method stub 
  }

  public void processDownloadDataAsyncResponse(CommandDataPair response) {
  // TODO Auto-generated method stub
  }

  public static void main(String[] args) {
    Properties props;
    int exitCd;
    FileInputStream fis = null;
    SimpleFtClient client = null;

    if (args.length != 1) {
      System.err.println("Usage: java " + SimpleFtClient.class.getName() + " [CLIENT_PROPERTIES_FILE]");
      exitCd = 1;
    }
    else {
      try {
        PrintWelcome();

        System.out.println("Using " + CLIENT_APP_TITLE + " Properties File: " + args[0]);

        fis = new FileInputStream(args[0]);
        props = new Properties();
        props.load(fis);
        fis.close();
        fis = null;

        client = new SimpleFtClient(props);
        client.boot();

        client.showCommandLine();

        client.waitWhileRunning();

        exitCd = 0;
      } // End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
      finally {
        if (client != null) {
          client.close();
        }

        if (fis != null) {
          try {
            fis.close();
          }
          catch (Exception e) {}
        }
      }
    }

    System.exit(exitCd);
  }

}
