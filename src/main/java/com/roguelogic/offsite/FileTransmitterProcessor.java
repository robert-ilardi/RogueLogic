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

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 * 
 */

public class FileTransmitterProcessor extends RLTalkSocketProcessor {

  private FileTransmitter transmitter;

  public FileTransmitterProcessor() {}

  public void setTransmitter(FileTransmitter transmitter) {
    this.transmitter = transmitter;
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
    cmDatPair = transmitter.decrypt(cmDatPair);

    switch (cmDatPair.getCommand()) {
      case OFFS_RLTCMD_LOGIN:
        transmitter.processLoginResponse(cmDatPair);
        break;
      case OFFS_RLTCMD_GET_LOGIN_MASK:
        transmitter.processGetLoginMaskResponse(cmDatPair);
        break;
      case OFFS_RLTCMD_START_UPLOAD:
        transmitter.processOpenRemoteFileResponse(cmDatPair);
        break;
      case OFFS_RLTCMD_UPLOAD_COMPLETE:
        transmitter.processCloseRemoteFileResponse(cmDatPair);
        break;
      case OFFS_RLTCMD_GET_REMOTE_FILE_INFO:
        transmitter.processRemoteFileInfoResponse(cmDatPair);
        break;
      case OFFS_RLTCMD_SYNC_TOUCH_TS:
        transmitter.processSynchronizedRemoteTouchTsResponse(cmDatPair);
        break;
      case OFFS_RLTCMD_REMOVE_FROM_BACKUP:
        transmitter.processRemoveFromBackupResponse(cmDatPair);
        break;
      default:
        // Invalid Command - Close Connection
        userSession.endSessionAsync();
    }
  }
}
