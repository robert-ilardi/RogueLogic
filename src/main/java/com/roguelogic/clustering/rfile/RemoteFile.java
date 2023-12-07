/**
 * Created Aug 15, 2008
 */
package com.roguelogic.clustering.rfile;

import static com.roguelogic.clustering.rfile.RemoteFileConstants.*;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_FAILED_WITH_EXCEPTION;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_SUCCEEDED;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkObjectEnvelop;
import com.roguelogic.net.rltalk.RLTalkObjectEnvelopAsyncHelper;

/**
 * @author Robert C. Ilardi
 *
 */

public class RemoteFile implements SocketProcessorCustomizer, SocketSessionSweeper {

  private SocketClient sockClient;

  private RLTalkObjectEnvelopAsyncHelper rltoeAsyncHelper;

  private Object arvmLock;
  private HashMap<String, Object> asyncRetValMap;

  private String filePath;
  private String host;
  private int port;

  public RemoteFile(String filePath, String host, int port, String username, String password) throws RemoteFileException {
    this.filePath = filePath;
    this.host = host;
    this.port = port;

    arvmLock = new Object();
    asyncRetValMap = new HashMap<String, Object>();
    rltoeAsyncHelper = new RLTalkObjectEnvelopAsyncHelper();

    connect();

    if (!login(username, password)) {
      close();
      throw new RemoteFileException("Login to Remote File Server FAILED for username '" + username + "'");
    }
  }

  public String getFilePath() {
    return filePath;
  }

  private void connect() throws RemoteFileException {
    try {
      sockClient = new SocketClient();
      sockClient.setSocketProcessorClass(RemoteFileClientProcessor.class);
      sockClient.setSocketProcessorCustomizer(this);
      sockClient.setSocketSessionSweeper(this);
      sockClient.connect(host, port);
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to connect to the Remote File Server: " + e.getMessage(), e);
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    RemoteFileClientProcessor rfcProc = (RemoteFileClientProcessor) processor;
    rfcProc.setRemoteFile(this);
  }

  public void cleanup(SocketSession userSession) {}

  private Object getAsyncReturnVal(String transId) {
    synchronized (arvmLock) {
      return asyncRetValMap.get(transId);
    }
  }

  private void completeTransaction(String transId) {
    synchronized (arvmLock) {
      asyncRetValMap.remove(transId);
    }

    rltoeAsyncHelper.removeTransactionId(transId);
  }

  public void asyncReturn(String transId, Integer state, Object obj) {
    synchronized (arvmLock) {
      asyncRetValMap.put(transId, obj);
    }

    rltoeAsyncHelper.setTransactionState(transId, state);
  }

  private boolean login(String username, String password) throws RemoteFileException {
    boolean loginOk = false;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;
    Boolean tmp;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_LOGIN);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { username, password });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Login operation FAILED with the Remote Exception:\n" + getAsyncReturnVal(transId));
      }
      else if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Login operation FAILED!");
      }

      tmp = (Boolean) getAsyncReturnVal(transId);
      if (tmp != null) {
        loginOk = tmp.booleanValue();
      }
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to Perform Login for username = " + username, e);
    }
    finally {
      completeTransaction(transId);
    }

    return loginOk;
  }

  public synchronized void close() throws RemoteFileException {
    try {
      if (sockClient != null) {
        sockClient.close();
        sockClient = null;
      }
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to disconnect from the Remote File Server: " + e.getMessage(), e);
    }
  }

  public boolean exists() throws RemoteFileException {
    boolean found = false;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;
    Boolean tmp;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_EXISTS);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { filePath });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Exists operation FAILED with the Remote Exception:\n" + getAsyncReturnVal(transId));
      }
      else if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Exists operation FAILED!");
      }

      tmp = (Boolean) getAsyncReturnVal(transId);
      if (tmp != null) {
        found = tmp.booleanValue();
      }
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to Perform Remote Exists on File: " + filePath, e);
    }
    finally {
      completeTransaction(transId);
    }

    return found;
  }

  public boolean isDirectory() throws RemoteFileException {
    boolean yes = false;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;
    Boolean tmp;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_IS_DIRECTORY);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { filePath });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Is Directory operation FAILED with the Remote Exception:\n" + getAsyncReturnVal(transId));
      }
      else if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Is Directory operation FAILED!");
      }

      tmp = (Boolean) getAsyncReturnVal(transId);
      if (tmp != null) {
        yes = tmp.booleanValue();
      }
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to Perform Remote Is Directory Check on Path: " + filePath, e);
    }
    finally {
      completeTransaction(transId);
    }

    return yes;
  }

  public boolean isFile() throws RemoteFileException {
    boolean yes = false;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;
    Boolean tmp;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_IS_FILE);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { filePath });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Is File operation FAILED with the Remote Exception:\n" + getAsyncReturnVal(transId));
      }
      else if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Is File operation FAILED!");
      }

      tmp = (Boolean) getAsyncReturnVal(transId);
      if (tmp != null) {
        yes = tmp.booleanValue();
      }
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to Perform Remote Is File Check on Path: " + filePath, e);
    }
    finally {
      completeTransaction(transId);
    }

    return yes;
  }

  public void createDirectory() throws RemoteFileException {
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_CREATE_DIRECTORY);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { filePath });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Directory Creation operation FAILED with the Remote Exception:\n" + getAsyncReturnVal(transId));
      }
      else if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Directory Creation operation FAILED!");
      }
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to Perform Remote Directory Creation for Path: " + filePath, e);
    }
    finally {
      completeTransaction(transId);
    }
  }

  public void rename(String toName) throws RemoteFileException {
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_RENAME);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { filePath, toName });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Rename operation FAILED with the Remote Exception:\n" + getAsyncReturnVal(transId));
      }
      else if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Rename operation FAILED!");
      }
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to Perform Remote Rename from: '" + filePath + "' to: '" + toName + "'", e);
    }
    finally {
      completeTransaction(transId);
    }
  }

  public void delete() throws RemoteFileException {
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_DELETE);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { filePath });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Delete operation FAILED with the Remote Exception:\n" + getAsyncReturnVal(transId));
      }
      else if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("Delete operation FAILED!");
      }
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to Perform Remote Delete of '" + filePath + "'", e);
    }
    finally {
      completeTransaction(transId);
    }
  }

  public RDirEntry[] list() throws RemoteFileException {
    RDirEntry[] ls = null;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_LIST);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { filePath });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("List operation FAILED with the Remote Exception:\n" + getAsyncReturnVal(transId));
      }
      else if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new RemoteFileException("List operation FAILED!");
      }

      ls = (RDirEntry[]) getAsyncReturnVal(transId);
    }
    catch (Exception e) {
      throw new RemoteFileException("An error occurred while attempting to Retrieve Remote Directory Listing for Path: " + filePath, e);
    }
    finally {
      completeTransaction(transId);
    }

    return ls;
  }

  public boolean isAccessible() throws RemoteFileException {
    return false;
  }

  public InputStream getInputStream() throws RemoteFileException {
    return null;
  }

  public OutputStream getOutputStream() throws RemoteFileException {
    return null;
  }

}
