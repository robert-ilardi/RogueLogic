package com.roguelogic.rltalkecho;
import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

public class RLTalkEchoProcessor extends RLTalkSocketProcessor {

  public RLTalkEchoProcessor() {
    super();
  }

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    _rlTalkSend(cmDatPair); //Simply Echo It!
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {
    System.out.println("Got A Connection!!!!!!!!!!!!!!!!!");
  }

}
