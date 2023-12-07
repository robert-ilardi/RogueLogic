/**
 * Created Jan 18, 2007
 */
package com.roguelogic.propexchange;

import static com.roguelogic.propexchange.PropExchangeCommandCodes.PECC_ASYNC_SEND_PROPERTIES;
import static com.roguelogic.propexchange.PropExchangeCommandCodes.PECC_SYNC_SEND_PROPERTIES;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;

import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class PropExchangeUtils {

  public static CommandDataPair CreateAsyncCmDatPair(PropExchangePayload payload) throws RLTalkException {
    CommandDataPair cmDatPair;
    String encode;

    encode = EncodePayload(payload);

    cmDatPair = new CommandDataPair();
    cmDatPair.setCommand(PECC_ASYNC_SEND_PROPERTIES);
    cmDatPair.setData(encode);

    return cmDatPair;
  }

  public static CommandDataPair CreateSyncCmDatPair(PropExchangePayload payload) throws RLTalkException {
    CommandDataPair cmDatPair;
    String encode;

    encode = EncodePayload(payload);

    cmDatPair = new CommandDataPair();
    cmDatPair.setCommand(PECC_SYNC_SEND_PROPERTIES);
    cmDatPair.setMultiplexerIndex(payload.getSyncId());
    cmDatPair.setData(encode);

    return cmDatPair;
  }

  public static String EncodePayload(PropExchangePayload payload) {
    StringBuffer encode = new StringBuffer();
    Iterator iter;
    String name, value;
    Properties props;

    if (payload != null) {
      //Encode Request Id
      encode.append(payload.getRequestId());
      encode.append("\n");

      //Encode Properties
      props = payload.getProps();
      if (props != null) {
        iter = props.keySet().iterator();

        while (iter.hasNext()) {
          name = (String) iter.next();

          if (name != null) {
            value = props.getProperty(name);

            name = name.trim();
            name = name.replaceAll("\n", "");

            if (value != null) {
              value = value.trim();
              value = value.replaceAll("\n", "");
            }

            encode.append(name);
            encode.append("=");
            encode.append(value);
            encode.append("\n");
          }
        }
      } //End null props check
    } //End null payload check

    return encode.toString();
  }

  public static PropExchangePayload DecodePayload(CommandDataPair cmDatPair) throws IOException {
    PropExchangePayload payload = null;
    Properties props = null;
    StringReader strRdr;
    BufferedReader bufRdr;
    String line, data;
    String[] tmpArr;
    int syncId = -1;

    payload = new PropExchangePayload();

    syncId = cmDatPair.getMultiplexerIndex();

    data = cmDatPair.getString();
    if (!StringUtils.IsNVL(data)) {
      strRdr = new StringReader(data);
      bufRdr = new BufferedReader(strRdr);

      //Check for Synchronous Transaction
      if (cmDatPair.getCommand() == PECC_SYNC_SEND_PROPERTIES) {
        //Synchronous Transaction Requested...
        payload.setSynchronous(true);
        payload.setSyncId(syncId);
      }

      //Read Request Id
      line = bufRdr.readLine();
      if (line != null) {
        payload.setRequestId(line.trim());
      }

      //Decode Properties
      props = new Properties();

      line = bufRdr.readLine();
      while (line != null) {
        tmpArr = line.trim().split("=", 2);
        tmpArr = StringUtils.Trim(tmpArr);

        props.setProperty(tmpArr[0], tmpArr[1]);

        line = bufRdr.readLine();
      }

      payload.setProps(props);
    } //End !IsNVL check on data
    return payload;
  }

  public static String GenerateRequestId() {
    StringBuffer reqId = new StringBuffer();

    reqId.append(StringUtils.GetRandomChars(4));
    reqId.append(StringUtils.GenerateTimeUniqueId());
    reqId.append(StringUtils.GetRandomChars(4));

    return reqId.toString();
  }

}
