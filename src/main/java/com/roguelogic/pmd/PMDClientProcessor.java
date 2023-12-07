/**
 * Created Nov 20, 2007
 */
package com.roguelogic.pmd;

import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_CLOSE_FILE;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_FILE_LENGTH;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_FILE_LIST;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_GET_SHARES;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_LOGIN;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_OPEN_FILE;
import static com.roguelogic.pmd.PMDConstants.PMD_RLTCMD_READ_NEXT_FILE_CHUNK;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

/**
 * @author Robert C. Ilardi
 *
 */

public class PMDClientProcessor extends RLTalkSocketProcessor {

  private PMDClient client;

  public PMDClientProcessor() {
    super();
  }

  public void setClient(PMDClient client) {
    this.client = client;
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
    switch (cmDatPair.getCommand()) {
      case PMD_RLTCMD_LOGIN:
        client.processLogin(cmDatPair);
        break;
      case PMD_RLTCMD_GET_SHARES:
        client.processGetShares(cmDatPair);
        break;
      case PMD_RLTCMD_GET_FILE_LIST:
        client.processGetFileList(cmDatPair);
        break;
      case PMD_RLTCMD_GET_FILE_LENGTH:
        client.processGetFileLen(cmDatPair);
        break;
      case PMD_RLTCMD_OPEN_FILE:
        client.processOpenFile(cmDatPair);
        break;
      case PMD_RLTCMD_CLOSE_FILE:
        break;
      case PMD_RLTCMD_READ_NEXT_FILE_CHUNK:
        client.processReadNextFileChunk(cmDatPair);
        break;
    }
  }

}
