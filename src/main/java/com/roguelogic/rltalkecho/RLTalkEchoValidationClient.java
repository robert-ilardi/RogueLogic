package com.roguelogic.rltalkecho;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;

public class RLTalkEchoValidationClient {

  private static RLTalkEchoValidationClient ClientInstance = null;

  private CommandDataPair prevCDP;

  private RLTalkEchoValidationProcessor processor;

  private RLTalkEchoValidationClient() {}

  public static RLTalkEchoValidationClient GetInstance() {
    synchronized (RLTalkEchoValidationClient.class) {
      if (ClientInstance == null) {
        ClientInstance = new RLTalkEchoValidationClient();
      }
    }

    return ClientInstance;
  }

  public CommandDataPair getPreviousCDP() {
    return prevCDP;
  }

  public void setPreviousCDP(CommandDataPair prevCDP) {
    this.prevCDP = prevCDP;
  }

  public void sendMessage() throws RLNetException {
    CommandDataPair cmDatPair = null;
    StringBuffer sb;
    int datLen;

    cmDatPair = new CommandDataPair();

    cmDatPair.setCommand(1000 + ((int) (Math.random() * 1000)));
    cmDatPair.setMultiplexerIndex((int) (Math.random() * 999));

    sb = new StringBuffer();
    datLen = 1 + ((int) (Math.random() * 25000));

    for (int i = 1; i <= datLen; i++) {
      sb.append((char) (65 + ((int) (Math.random() * 26))));
    }

    cmDatPair.setData(sb.toString());

    setPreviousCDP(cmDatPair);

    processor.send(cmDatPair);
  }

  public void setProcessor(RLTalkEchoValidationProcessor processor) {
    this.processor = processor;
  }

  public void validate(CommandDataPair cmDatPair) {
    System.out.print("Validating " + prevCDP + " == " + cmDatPair + "  -->  ");

    if (!prevCDP.equals(cmDatPair)) {
      System.out.println("NOT EQUAL!");

      System.out.println((new java.util.Date()).toString());

      System.out.println(prevCDP);
      if (prevCDP.getData() != null) {
        System.out.println("DATA = " + new String(prevCDP.getData()));
      }
      else {
        System.out.println("[NULL DATA]");
      }

      System.out.println(cmDatPair);
      if (cmDatPair.getData() != null) {
        System.out.println("DATA = " + new String(cmDatPair.getData()));
      }
      else {
        System.out.println("[NULL DATA]");
      }

      System.exit(1);
    }
    else {
      System.out.println("EQUAL!");
    }

    //SystemUtils.SleepTight(100 + (int) (Math.random() * 150));
  }

}
