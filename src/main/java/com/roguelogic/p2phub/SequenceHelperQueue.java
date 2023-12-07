/**
 * Created Feb 4, 2007
 */
package com.roguelogic.p2phub;

import java.util.HashMap;

import com.roguelogic.util.AutoSizeArray;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class SequenceHelperQueue {

  private HashMap<String, AutoSizeArray> seqQueueMap;
  private HashMap<String, Integer> nextDequeueSeqMap;
  private Object sqmLock;

  public static final String MESG_PROP_SEQUENCE_SET_NAME = "_SHQ.SequenceSetName";
  public static final String MESG_PROP_SEQUENCE_NUMBER = "_SHQ.SequenceNumber";

  public SequenceHelperQueue() {
    seqQueueMap = new HashMap<String, AutoSizeArray>();
    nextDequeueSeqMap = new HashMap<String, Integer>();
    sqmLock = new Object();
  }

  public void enqueue(P2PHubMessage mesg) {
    String seqSetName, tmp;
    int seqNum = -1;
    AutoSizeArray seqQueue;

    seqSetName = mesg.getProperty(MESG_PROP_SEQUENCE_SET_NAME);
    tmp = mesg.getProperty(MESG_PROP_SEQUENCE_NUMBER);

    if (!StringUtils.IsNVL(seqSetName) && StringUtils.IsNumeric(tmp)) {
      seqNum = Integer.parseInt(tmp.trim());
    }

    if (seqNum >= 0) {
      synchronized (sqmLock) {
        //Make sure we have a queue for this Seq Set Name
        seqQueue = seqQueueMap.get(seqSetName);
        if (seqQueue == null) {
          seqQueue = new AutoSizeArray();
          seqQueueMap.put(seqSetName, seqQueue);
        }

        //Insert the Mesg into the correct index based on sequence number
        seqQueue.store(seqNum, mesg);
      } //End synchronized block on sqmLock
    } //End seqNum >= 0 check
  }

  public P2PHubMessage dequeue(String seqSetName) {
    P2PHubMessage nxtMesg = null;
    int nxtDqSeq;
    AutoSizeArray seqQueue;

    synchronized (sqmLock) {
      //Get Next Dequeue Sequence Number
      if (nextDequeueSeqMap.containsKey(seqSetName)) {
        nxtDqSeq = nextDequeueSeqMap.get(seqSetName);
      }
      else {
        nxtDqSeq = 0;
      }

      //Get Next Message
      seqQueue = seqQueueMap.get(seqSetName);
      if (seqQueue != null) {
        nxtMesg = (P2PHubMessage) seqQueue.remove(nxtDqSeq);

        //Increment and store Next Dequeue Sequence Number
        if (nxtMesg != null) {
          nxtDqSeq++;
          nextDequeueSeqMap.put(seqSetName, nxtDqSeq);
        }
      }
    } //End sqmLock synchronization block

    return nxtMesg;
  }

}
