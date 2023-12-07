/*
 * Created on Mar 3, 2006
 */
package com.roguelogic.net.rltalk;

import com.roguelogic.net.RLNetException;

public class RLTalkCmdLineClientProcessor extends RLTalkSocketProcessor {

  public RLTalkCmdLineClientProcessor() {
    super();
  }

  @Override
  protected void _rlTalkHandshake() throws RLNetException {}

  @Override
  protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
    StringBuffer sb = new StringBuffer();

    sb.append("\nIncoming RL-Talk Data:\n");

    sb.append("Command = ");
    sb.append(cmDatPair.getCommand());
    sb.append("\n");

    sb.append("Status Code = ");
    sb.append(cmDatPair.getStatusCode());
    sb.append("\n");

    sb.append("Multiplexer Index = ");
    sb.append(cmDatPair.getMultiplexerIndex());
    sb.append("\n");

    sb.append("Data[");
    sb.append(cmDatPair.dataLen());
    sb.append(" byte(s)] = ");
    sb.append(new String(cmDatPair.getData()));
    sb.append("\n");

    System.out.println(sb.toString());
  }

}
