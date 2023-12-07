package com.roguelogic.net.rltalk;

import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketSession;
import com.roguelogic.util.StringUtils;

public class RLTalkUtils {

  public static void RLTalkSend(SocketSession sockSession, CommandDataPair cmDatPair) throws RLNetException {
    Packet[] packets;

    //Synchronized to make sure a complete message
    //from a single thread will be sent before
    //the next message can be sent!
    if (sockSession != null && cmDatPair != null) {
      synchronized (sockSession.getSendLock()) {
        packets = PacketFactory.GetPackets(cmDatPair); //Convert CDP to Packets 

        //Send Packets across the network
        for (int i = 0; i < packets.length; i++) {
          sockSession.send(packets[i].toByteArray());
        }
      }
    }
  }

  public static Properties GetNameValuePairs(CommandDataPair cmDatPair) {
    Properties nvPairs = new Properties();
    String data, name, value;
    String[] tokensL1, tokensL2;

    if (cmDatPair == null) {
      return nvPairs;
    }

    data = cmDatPair.getString();

    if (data == null) {
      return nvPairs;
    }

    tokensL1 = data.split("&");

    for (int i = 0; i < tokensL1.length; i++) {
      tokensL2 = tokensL1[i].split("=", 2);

      if (tokensL2.length == 2) {
        name = tokensL2[0];
        value = tokensL2[1];

        nvPairs.setProperty(name, value);
      }
    }

    return nvPairs;
  }

  public static void SetNameValuePairs(CommandDataPair cmDatPair, Properties nvPairs) {
    StringBuffer data;
    String name, value;
    boolean first;

    if (cmDatPair == null || nvPairs == null) {
      return;
    }

    first = true;
    data = new StringBuffer();

    for (Object key : nvPairs.keySet()) {
      if (key != null) {
        name = key.toString();
        value = nvPairs.getProperty(name);

        if (!first) {
          data.append("&");
        }
        else {
          first = false;
        }

        data.append(name);
        data.append("=");
        data.append(StringUtils.BasicURLEncode(value));
      }
    }

    cmDatPair.setData(data.toString());
  }

}
