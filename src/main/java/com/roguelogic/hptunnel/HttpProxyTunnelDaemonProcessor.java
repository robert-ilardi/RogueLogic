/**
 * Created Jan 15, 2008
 */
package com.roguelogic.hptunnel;

import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_DATA_STREAM;
import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_EK_SHIFT;
import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_LOGIN;
import static com.roguelogic.hptunnel.HPTConstants.STATUS_CODE_FAILURE;
import static com.roguelogic.hptunnel.HPTConstants.STATUS_CODE_SUCESS;

import java.util.Random;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;
import com.roguelogic.net.rltalk.RLTalkXorCodec;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class HttpProxyTunnelDaemonProcessor extends RLTalkSocketProcessor {

  public static final String USOBJ_HTTP_CLIENT = "HttpClient";
  public static final String USOBJ_USERNAME = "Username";
  public static final String USOBJ_XOR_CODEC = "XORCodec";

  private HttpProxyTunnelDaemon daemon;

  public HttpProxyTunnelDaemonProcessor() {
    super();
  }

  public void setDaemon(HttpProxyTunnelDaemon daemon) {
    this.daemon = daemon;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandshake()
   */
  @Override
  protected void _rlTalkHandshake() throws RLNetException {
    byte[] keyData = daemon.getEncryptionKey();
    RLTalkXorCodec codec = new RLTalkXorCodec();
    codec.setKeyData(keyData);
    userSession.putUserItem(USOBJ_XOR_CODEC, codec);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.net.rltalk.RLTalkSocketProcessor#_rlTalkHandle(com.roguelogic.net.rltalk.CommandDataPair)
   */
  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    RLTalkXorCodec codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_XOR_CODEC);

    switch (cmDatPair.getCommand()) {
      case HPT_RLTCMD_EK_SHIFT:
        processEkShift(cmDatPair);
        break;
      case HPT_RLTCMD_LOGIN:
        if (codec != null) {
          cmDatPair = codec.decrypt(cmDatPair);
        }

        processLogin(cmDatPair);
        break;
      case HPT_RLTCMD_DATA_STREAM:
        if (codec != null) {
          cmDatPair = codec.decrypt(cmDatPair);
        }

        processDataStream(cmDatPair);
        break;
      default:
        //Invalid Command - Close Connection
        userSession.endSession();
    }
  }

  private void processEkShift(CommandDataPair cmDatPair) throws RLNetException {
    Random rnd;
    int ekIndex;
    CommandDataPair reply;
    RLTalkXorCodec codec;

    //Choose random index for encryption key
    rnd = new Random();
    ekIndex = rnd.nextInt(daemon.getEncryptionKey().length);

    //Move Key Indexes of Codec 
    codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_XOR_CODEC);
    codec._setKeyIndexes(ekIndex);

    reply = new CommandDataPair();
    reply.setCommand(HPT_RLTCMD_EK_SHIFT);
    reply.setStatusCode(STATUS_CODE_SUCESS);
    reply.setData(ekIndex);

    _rlTalkSend(reply); //Send unencrypted
  }

  private void processLogin(CommandDataPair cmDatPair) throws RLNetException {
    String[] tokens;
    String tmp;
    CommandDataPair reply;

    userSession.removeUserItem(USOBJ_USERNAME);

    tmp = cmDatPair.getString();
    tokens = tmp.split("\\|", 2);

    if (tokens != null && tokens.length == 2) {
      tokens = StringUtils.Trim(tokens);

      if (daemon.getUsername().equals(tokens[0]) && daemon.getPassword().equals(tokens[1])) {
        //Login OK!
        userSession.putUserItem(USOBJ_USERNAME, tokens[0]);

        reply = new CommandDataPair();
        reply.setCommand(HPT_RLTCMD_LOGIN);
        reply.setStatusCode(STATUS_CODE_SUCESS);
        reply.setData("Login OK");

        sendEncrypted(reply);
      }
      else {
        //Invalid Username or Password

        reply = new CommandDataPair();
        reply.setCommand(HPT_RLTCMD_LOGIN);
        reply.setStatusCode(STATUS_CODE_FAILURE);
        reply.setData("Invalid Username or Password");

        sendEncrypted(reply);

        userSession.endSession(); //Invalid username or password - Close Connection
      }
    }
    else {
      userSession.endSession(); //Invalid login CMDATPAIR - Close Connection
    }
  }

  private boolean loginOk(boolean sendLoginInvalidMesg, boolean disconnectOnInvalid) throws RLNetException {
    boolean lOk = true;
    CommandDataPair reply;

    if (userSession.getUserItem(USOBJ_USERNAME) == null) {
      lOk = false;

      reply = new CommandDataPair();
      reply.setCommand(HPT_RLTCMD_LOGIN);
      reply.setStatusCode(STATUS_CODE_FAILURE);
      reply.setData("User NOT logged in!");

      sendEncrypted(reply);

      if (disconnectOnInvalid) {
        userSession.endSession();
      }
    }

    return lOk;
  }

  private void processDataStream(CommandDataPair cmDatPair) throws RLNetException {
    HttpClient client;

    if (!loginOk(true, true)) {
      return;
    }

    client = (HttpClient) userSession.getUserItem(USOBJ_HTTP_CLIENT);

    if (client == null) {
      client = new HttpClient();
      client.setProxyConnection(userSession);

      userSession.putUserItem(USOBJ_HTTP_CLIENT, client);
    }

    client.sendData(cmDatPair.getData());
  }

  private void sendEncrypted(CommandDataPair request) throws RLNetException {
    RLTalkXorCodec codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_XOR_CODEC);
    CommandDataPair cipherRequest = codec.encrypt(request);
    _rlTalkSend(cipherRequest);
  }

}
