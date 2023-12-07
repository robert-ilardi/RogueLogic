/**
 * Created Aug 15, 2008
 */
package com.roguelogic.clustering.rfile;

import static com.roguelogic.clustering.rfile.RemoteFileConstants.*;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_FAILED_WITH_EXCEPTION;
import static com.roguelogic.net.rltalk.RLTalkObjectEnvelop.TRANSACTION_SUCCEEDED;

import java.io.IOException;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkObjectEnvelop;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 *
 */

public class RemoteFileClientProcessor extends RLTalkSocketProcessor {

  private RemoteFile rFile;

  public RemoteFileClientProcessor() {
    super();
  }

  public void setRemoteFile(RemoteFile rFile) {
    this.rFile = rFile;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandshake()
   */
  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandle(com.roguelogic.net.rltalk.CommandDataPair)
   */
  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    try {
      switch (cmDatPair.getCommand()) {
        case CMD_LOGIN:
          processLogin(cmDatPair);
          break;
        case CMD_EXISTS:
          processExists(cmDatPair);
          break;
        case CMD_IS_DIRECTORY:
          processIsDirectory(cmDatPair);
          break;
        case CMD_IS_FILE:
          processIsFile(cmDatPair);
          break;
        case CMD_CREATE_DIRECTORY:
          processCreateDirectory(cmDatPair);
          break;
        case CMD_RENAME:
          processRename(cmDatPair);
          break;
        case CMD_DELETE:
          processDelete(cmDatPair);
          break;
        case CMD_LIST:
          processList(cmDatPair);
          break;
        default:
          //Error or HACK Attempt so disconnect
          userSession.endSession();
      } //End switch block
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void processLogin(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Boolean loginOk;
    String stackTrace;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      loginOk = (Boolean) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), loginOk);
    }
    else if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(envelop.getAsyncState())) {
      stackTrace = (String) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), stackTrace);
    }
    else {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
  }

  private void processExists(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Boolean found;
    String stackTrace;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      found = (Boolean) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), found);
    }
    else if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(envelop.getAsyncState())) {
      stackTrace = (String) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), stackTrace);
    }
    else {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
  }

  private void processIsDirectory(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Boolean yes;
    String stackTrace;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      yes = (Boolean) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), yes);
    }
    else if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(envelop.getAsyncState())) {
      stackTrace = (String) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), stackTrace);
    }
    else {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
  }

  private void processIsFile(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    Boolean yes;
    String stackTrace;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      yes = (Boolean) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), yes);
    }
    else if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(envelop.getAsyncState())) {
      stackTrace = (String) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), stackTrace);
    }
    else {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
  }

  private void processCreateDirectory(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    String stackTrace;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
    else if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(envelop.getAsyncState())) {
      stackTrace = (String) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), stackTrace);
    }
    else {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
  }

  private void processRename(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    String stackTrace;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
    else if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(envelop.getAsyncState())) {
      stackTrace = (String) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), stackTrace);
    }
    else {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
  }

  private void processDelete(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    String stackTrace;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
    else if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(envelop.getAsyncState())) {
      stackTrace = (String) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), stackTrace);
    }
    else {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
  }

  private void processList(CommandDataPair cmDatPair) throws IOException, ClassNotFoundException {
    RLTalkObjectEnvelop envelop;
    String stackTrace;
    RDirEntry[] ls;

    envelop = (RLTalkObjectEnvelop) cmDatPair.getObject();

    if (TRANSACTION_SUCCEEDED.equals(envelop.getAsyncState())) {
      ls = (RDirEntry[]) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), ls);
    }
    else if (TRANSACTION_FAILED_WITH_EXCEPTION.equals(envelop.getAsyncState())) {
      stackTrace = (String) envelop.getObjects()[0];
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), stackTrace);
    }
    else {
      rFile.asyncReturn(envelop.getAsyncTransId(), envelop.getAsyncState(), null);
    }
  }

}
