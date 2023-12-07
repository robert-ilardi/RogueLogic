/**
 * Created Aug 5, 2008
 */
package com.roguelogic.net.rltalk;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class RLTalkObjectEnvelop implements Serializable {

  public static final Integer TRANSACTION_UNKNOWN_STATE = new Integer(0);
  public static final Integer TRANSACTION_SUCCEEDED = new Integer(1);
  public static final Integer TRANSACTION_FAILED = new Integer(2);
  public static final Integer TRANSACTION_FAILED_WITH_EXCEPTION = new Integer(3);

  private String asyncTransId;
  private Integer asyncState;

  private Serializable[] objects;

  public RLTalkObjectEnvelop() {}

  public String getAsyncTransId() {
    return asyncTransId;
  }

  public void setAsyncTransId(String asyncTransId) {
    this.asyncTransId = asyncTransId;
  }

  public Serializable[] getObjects() {
    return objects;
  }

  public void setObjects(Serializable[] objects) {
    this.objects = objects;
  }

  public Integer getAsyncState() {
    return asyncState;
  }

  public void setAsyncState(Integer asyncState) {
    this.asyncState = asyncState;
  }

}
