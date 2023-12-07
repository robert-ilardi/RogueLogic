/**
 * Created Sep 24, 2006
 */
package com.roguelogic.roguenet;

import java.util.ArrayList;
import java.util.HashMap;

import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.client.P2PHubMessageObserver;

/**
 * @author Robert C. Ilardi
 *
 */

public class MessageProcessor implements P2PHubMessageObserver, Runnable {

  private ArrayList<P2PHubMessage> messageQueue;
  private Object mqLock;

  private PlugInManager plugInManager;

  private boolean halt;

  private ArrayList<String> globalListeners;
  private HashMap<String, ArrayList<String>> listenerMap;

  private Thread mesgProcessorThread;

  private long mesgReceivedCnt;

  public MessageProcessor() {
    halt = true;
    mesgReceivedCnt = 0;

    messageQueue = new ArrayList<P2PHubMessage>();
    mqLock = new Object();

    globalListeners = new ArrayList<String>();
    listenerMap = new HashMap<String, ArrayList<String>>();
  }

  public boolean isHalt() {
    return halt;
  }

  public void setHalt(boolean halt) {
    synchronized (mqLock) {
      this.halt = halt;
      mqLock.notifyAll();
    }
  }

  public void start() {
    synchronized (mqLock) {
      if (halt) {
        halt = false;

        mesgProcessorThread = new Thread(this);
        mesgProcessorThread.start();
      }
    }
  }

  public void setPlugInManager(PlugInManager plugInManager) {
    this.plugInManager = plugInManager;
  }

  public synchronized void registerListener(String listenerName, String subject) {
    ArrayList<String> listenerList;

    //Add new Listener for Subject if needed...
    if (RNAConstants.ALL_MESSAGE_SUBJECT.equals(subject)) {
      //Generic Listeners that listen to ALL Subjects...
      if (!globalListeners.contains(listenerName)) {
        globalListeners.add(listenerName);
      }
    }
    else {
      //Subject Specific Listeners
      listenerList = listenerMap.get(subject);

      if (listenerList == null) {
        listenerList = new ArrayList<String>();
        listenerMap.put(subject, listenerList);
      }

      if (!listenerList.contains(listenerName)) {
        listenerList.add(listenerName);
      }
    }
  }

  public synchronized void unregisterListener(String listenerName, String subject) {
    ArrayList<String> listenerList;

    if (RNAConstants.ALL_MESSAGE_SUBJECT.equals(subject)) {
      //Generic Listeners that listen to ALL Subjects...
      globalListeners.remove(listenerName);
    }
    else {
      //Subject Specific Listeners
      listenerList = listenerMap.get(subject);

      if (listenerList == null) {
        listenerList.remove(listenerName);
      }
    }
  }

  public synchronized ArrayList<String> getListeners(String subject) {
    ArrayList<String> subjectListeners, listenerList = new ArrayList<String>();

    //First add Global Listeners
    listenerList.addAll(globalListeners);

    //Second add everyone else specifically registered for this subject
    subjectListeners = listenerMap.get(subject);
    if (subjectListeners != null) {
      listenerList.addAll(subjectListeners);
    }

    return listenerList;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.p2phub.client.P2PHubMessageObserver#onMessage(com.roguelogic.p2phub.P2PHubMessage)
   */
  public void onMessage(P2PHubMessage mesg) {
    if (halt) {
      return;
    }

    mesgReceivedCnt++;

    enqueue(mesg);
  }

  private void enqueue(P2PHubMessage mesg) {
    synchronized (mqLock) {
      messageQueue.add(mesg);
      mqLock.notifyAll();
    }
  }

  private P2PHubMessage dequeue() throws InterruptedException {
    P2PHubMessage mesg = null;

    synchronized (mqLock) {
      while (messageQueue.isEmpty() && !halt) {
        mqLock.wait();
      }

      if (!halt) {
        mesg = messageQueue.remove(0);
      }
    }

    return mesg;
  }

  public void run() {
    P2PHubMessage mesg;
    ArrayList<String> listenerList;

    while (!halt) {
      try {
        mesg = dequeue();

        if (mesg != null && !halt) {
          listenerList = getListeners(mesg.getSubject());

          if (plugInManager != null) {
            for (String listenerName : listenerList) {
              plugInManager.notifyListener(listenerName, mesg);
            }
          }
        } //End null mesg and !halt check
      } //End try block
      catch (Exception e) {
        RNALogger.GetLogger().log(e);
      }
    } //End while !halt
  }

  public long getMesgReceivedCnt() {
    return mesgReceivedCnt;
  }

}
