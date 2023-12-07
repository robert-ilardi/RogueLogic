package com.roguelogic.rltalkecho;
import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;

public class RLTalkEchoValidationProcessor extends RLTalkSocketProcessor {

  public RLTalkEchoValidationProcessor() {
    super();
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    RLTalkEchoValidationClient client = RLTalkEchoValidationClient.GetInstance();
    client.setProcessor(this);
    client.validate(cmDatPair);
    client.sendMessage();
  }

  public void send(CommandDataPair cmDatPair) throws RLNetException {
    _rlTalkSend(cmDatPair);
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

}
