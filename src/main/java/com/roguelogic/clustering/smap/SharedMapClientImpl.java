/**
 * Created Aug 5, 2008
 */
package com.roguelogic.clustering.smap;

import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_CLEAR;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_CONTAINS_KEY;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_GET;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_IS_ENTRY_LOCKED;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_LOCK_ENTRY;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_LOGIN;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_PUT;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_REMOVE;
import static com.roguelogic.clustering.smap.SharedMapConstants.CMD_UNLOCK_ENTRY;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_SUCCEEDED;

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
public class SharedMapClientImpl implements SharedMapImplementation, SocketProcessorCustomizer, SocketSessionSweeper {

  private String host;
  private int port;

  private SocketClient sockClient;
  private RLTalkObjectEnvelopAsyncHelper rltoeAsyncHelper;

  private Object arvmLock;
  private HashMap<String, Object> asyncRetValMap;

  public SharedMapClientImpl(String host, int port) throws SharedMemoryException {
    this.host = host;
    this.port = port;

    arvmLock = new Object();
    asyncRetValMap = new HashMap<String, Object>();
    rltoeAsyncHelper = new RLTalkObjectEnvelopAsyncHelper();

    connect();
  }

  private void connect() throws SharedMemoryException {
    try {
      sockClient = new SocketClient();
      sockClient.setSocketProcessorClass(SharedMapClientProcessor.class);
      sockClient.setSocketProcessorCustomizer(this);
      sockClient.setSocketSessionSweeper(this);
      sockClient.connect(host, port);
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to connect to the Shared Map Server: " + e.getMessage(), e);
    }
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    SharedMapClientProcessor smcProc = (SharedMapClientProcessor) processor;
    smcProc.setSmClient(this);
  }

  public void cleanup(SocketSession userSession) {}

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#setSMemSecurityManager(com.roguelogic.smem.SMemSecurityManager)
   */
  public void setSMemSecurityManager(SMemSecurityManager secMan) {}

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#close()
   */
  public void close() throws SharedMemoryException {
    try {
      if (sockClient != null) {
        sockClient.close();
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to disconnect from the Shared Map Server: " + e.getMessage(), e);
    }
  }

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

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#login(java.lang.String, java.lang.String)
   */
  public boolean login(String username, String password) throws SharedMemoryException {
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

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Login operation FAILED!");
      }

      tmp = (Boolean) getAsyncReturnVal(transId);
      if (tmp != null) {
        loginOk = tmp.booleanValue();
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Login for username = " + username, e);
    }
    finally {
      completeTransaction(transId);
    }

    return loginOk;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#put(java.io.Serializable, java.io.Serializable)
   */
  public Serializable put(Serializable key, Serializable value) throws SharedMemoryException {
    Serializable oldVal = null;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_PUT);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { key, value });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Put operation FAILED!");
      }

      oldVal = (Serializable) getAsyncReturnVal(transId);
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Remote Put: " + e.getMessage(), e);
    }
    finally {
      completeTransaction(transId);
    }

    return oldVal;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#get(java.io.Serializable)
   */
  public Serializable get(Serializable key) throws SharedMemoryException {
    Serializable value = null;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_GET);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { key });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Get operation FAILED!");
      }

      value = (Serializable) getAsyncReturnVal(transId);
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Remote Get: " + e.getMessage(), e);
    }
    finally {
      completeTransaction(transId);
    }

    return value;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#remove(java.io.Serializable)
   */
  public Serializable remove(Serializable key) throws SharedMemoryException {
    Serializable value = null;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_REMOVE);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { key });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Remove operation FAILED!");
      }

      value = (Serializable) getAsyncReturnVal(transId);
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Remote Remove: " + e.getMessage(), e);
    }
    finally {
      completeTransaction(transId);
    }

    return value;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#clear()
   */
  public void clear() throws SharedMemoryException {
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_CLEAR);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Clear operation FAILED!");
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Remote Clear", e);
    }
    finally {
      completeTransaction(transId);
    }
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#containsKey(java.io.Serializable)
   */
  public boolean containsKey(Serializable key) throws SharedMemoryException {
    boolean contains = false;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;
    Boolean tmp;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_CONTAINS_KEY);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { key });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Contains Key operation FAILED!");
      }

      tmp = (Boolean) getAsyncReturnVal(transId);
      if (tmp != null) {
        contains = tmp.booleanValue();
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Remote Contains Key", e);
    }
    finally {
      completeTransaction(transId);
    }

    return contains;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#lockEntry(java.io.Serializable, long)
   */
  public boolean lockEntry(Serializable key, long timeout) throws SharedMemoryException {
    boolean locked = false;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;
    Boolean tmp;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_LOCK_ENTRY);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { key, new Long(timeout) });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Lock Entry operation FAILED!");
      }

      tmp = (Boolean) getAsyncReturnVal(transId);
      if (tmp != null) {
        locked = tmp.booleanValue();
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Remote Lock Entry", e);
    }
    finally {
      completeTransaction(transId);
    }

    return locked;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.smem.SharedMapImplementation#unlockEntry(java.io.Serializable)
   */
  public void unlockEntry(Serializable key) throws SharedMemoryException {
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_UNLOCK_ENTRY);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);

      envelop.setObjects(new Serializable[] { key });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Unlock Entry operation FAILED!");
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Remote Unlock Entry", e);
    }
    finally {
      completeTransaction(transId);
    }
  }

  public boolean isEntryLocked(Serializable key) throws SharedMemoryException {
    boolean locked = false;
    CommandDataPair cmDatPair;
    RLTalkObjectEnvelop envelop;
    String transId = null;
    Boolean tmp;

    try {
      transId = rltoeAsyncHelper.obtainTransactionId();

      cmDatPair = new CommandDataPair();
      cmDatPair.setCommand(CMD_IS_ENTRY_LOCKED);

      envelop = new RLTalkObjectEnvelop();
      envelop.setAsyncTransId(transId);
      envelop.setObjects(new Serializable[] { key });

      cmDatPair.setData(envelop);

      //Send and Wait for Response
      rltoeAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transId);

      if (!TRANSACTION_SUCCEEDED.equals(rltoeAsyncHelper.getTransactionFlag(transId))) {
        throw new SharedMemoryException("Is Entry Locked operation FAILED!");
      }

      tmp = (Boolean) getAsyncReturnVal(transId);
      if (tmp != null) {
        locked = tmp.booleanValue();
      }
    }
    catch (Exception e) {
      throw new SharedMemoryException("An error occurred while attempting to Perform Remote Is Entry Locked", e);
    }
    finally {
      completeTransaction(transId);
    }

    return locked;
  }

}
