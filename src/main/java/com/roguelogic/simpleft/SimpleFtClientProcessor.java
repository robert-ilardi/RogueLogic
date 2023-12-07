/**
 * Created Dec 11, 2008
 */
package com.roguelogic.simpleft;

import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_CHANGE_DIRECTORY;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_DATETIME;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_DOWNLOAD_DATA;
import static com.roguelogic.simpleft.SimpleFtProtocolConstants.SFT_RLTCMD_DOWNLOAD_DATA_ASYNC;
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

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 * 
 */

public class SimpleFtClientProcessor extends RLTalkSocketProcessor {

  private SimpleFtClient client;

  public SimpleFtClientProcessor() {}

  public void setClient(SimpleFtClient client) {
    this.client = client;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandshake()
   */
  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  /*
   * (non-Javadoc)
   * 
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandle(com.roguelogic.net.rltalk.CommandDataPair)
   */
  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    cmDatPair = client.decrypt(cmDatPair);

    switch (cmDatPair.getCommand()) {
      case SFT_RLTCMD_LOGIN:
        client.processLoginResponse(cmDatPair);
        break;
      case SFT_RLTCMD_GET_LOGIN_MASK:
        client.processGetLoginMaskResponse(cmDatPair);
        break;
      case SFT_RLTCMD_LIST_SHARES:
        client.processListSharesResponse(cmDatPair);
        break;
      case SFT_RLTCMD_LIST_DIRECTORY:
        client.processListDirResponse(cmDatPair);
        break;
      case SFT_RLTCMD_CHANGE_DIRECTORY:
        client.processChangeDirResponse(cmDatPair);
        break;
      case SFT_RLTCMD_PING:
        client.processPingResponse(cmDatPair);
        break;
      case SFT_RLTCMD_ECHO:
        client.processEchoResponse(cmDatPair);
        break;
      case SFT_RLTCMD_DATETIME:
        client.processDateTimeResponse(cmDatPair);
        break;
      case SFT_RLTCMD_START_UPLOAD:
        client.processStartUploadResponse(cmDatPair);
        break;
      case SFT_RLTCMD_UPLOAD_DATA:
        client.processUploadDataResponse(cmDatPair);
        break;
      case SFT_RLTCMD_END_UPLOAD:
        client.processEndUploadResponse(cmDatPair);
        break;
      case SFT_RLTCMD_START_DOWNLOAD:
        client.processStartDownloadResponse(cmDatPair);
        break;
      case SFT_RLTCMD_START_SDOWNLOAD:
        client.processStartDownloadResponse(cmDatPair);
        break;
      case SFT_RLTCMD_DOWNLOAD_DATA:
        client.processDownloadDataResponse(cmDatPair);
        break;
      case SFT_RLTCMD_DOWNLOAD_DATA_ASYNC:
        client.processDownloadDataAsyncResponse(cmDatPair);
        break;
      case SFT_RLTCMD_END_DOWNLOAD:
        client.processEndDownloadResponse(cmDatPair);
        break;
      default:
        // Invalid Command - Close Connection
        userSession.endSessionAsync();
    }
  }

}
