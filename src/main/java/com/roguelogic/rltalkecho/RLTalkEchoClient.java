package com.roguelogic.rltalkecho;
import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;

public class RLTalkEchoClient {

  public RLTalkEchoClient() {}

  public static void main(String[] args) throws RLNetException {
    SocketClient client;
    RLTalkEchoValidationProcessor initialProcessor;
    RLTalkEchoValidationClient validationClient;

    client = new SocketClient();
    client.setSocketProcessorClass(RLTalkEchoValidationProcessor.class);
    client.connect("localhost", Integer.parseInt(args[0]));

    initialProcessor = new RLTalkEchoValidationProcessor();
    initialProcessor.pushSession(client.getUserSession());

    validationClient = RLTalkEchoValidationClient.GetInstance();
    validationClient.setProcessor(initialProcessor);
    validationClient.sendMessage();
  }

}
