/**
 * Created Sep 16, 2006
 */

package com.roguelogic.p2phub;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Properties;

import com.roguelogic.util.Base64Codec;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class P2PHubMessage implements Serializable {

  public static final String MESG_VERSION = "1.2";
  public static final String MESG_TYPE_USER = "User";
  public static final String MESG_TYPE_SYSTEM = "System";

  private String version = MESG_VERSION;
  private String[] recipients;
  private String type = MESG_TYPE_USER;
  private String subject;
  private long messageId;
  private Properties properties;
  private String sender;

  private byte[] data;
  private String base64Data;

  public P2PHubMessage() {}

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String name) {
    this.subject = name;
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public String getBase64Data() {
    return base64Data;
  }

  public void setBase64Data(String base64Data) {
    this.base64Data = base64Data;
  }

  public long getMessageId() {
    return messageId;
  }

  public void setMessageId(long messageId) {
    this.messageId = messageId;
  }

  public String getProperty(String name) {
    String value = null;

    if (name != null && properties != null) {
      value = properties.getProperty(name);
    }

    return value;
  }

  public String[] getRecipients() {
    return recipients;
  }

  public void setRecipients(String[] recipients) {
    this.recipients = recipients;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[Message - Type: ");
    sb.append(type);
    sb.append(", Subject: ");
    sb.append(subject);
    sb.append(", Sender: ");
    sb.append(sender);
    sb.append(", Recipients: ");
    sb.append(StringUtils.ArrayToDelimitedString(recipients, ", ", false));
    sb.append(", MesgVer: ");
    sb.append(version);
    sb.append(", Id: ");
    sb.append(messageId);
    sb.append(", Properties: ");
    sb.append(properties);

    sb.append(", Data-Len: ");

    if (data != null) {
      sb.append(data.length);
    }
    else if (base64Data != null) {
      sb.append(base64Data.length());
    }
    else {
      sb.append("0");
    }

    sb.append("]");

    return sb.toString();
  }

  public String toXML() {
    StringBuffer xml = new StringBuffer();

    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append(getXMLNode());

    return xml.toString();
  }

  public String getXMLNode() {
    StringBuffer xml = new StringBuffer();
    Iterator iter;
    String name, value;

    //Start of P2P Hub Message XML Document
    xml.append("<P2P-HUB-MESSAGE mesgVer=\"");
    xml.append((version != null ? version : MESG_VERSION));
    xml.append("\"");

    xml.append(" type=\"");
    xml.append((type != null ? type : MESG_TYPE_USER));
    xml.append("\"");

    if (sender != null) {
      xml.append(" sender=\"");
      xml.append(sender.trim());
      xml.append("\"");
    }

    xml.append(" subject=\"");
    xml.append((subject != null ? StringUtils.SimpleXMLTextEncoder(subject.trim()) : ""));
    xml.append("\"");

    xml.append(" recipients=\"");
    xml.append(StringUtils.ArrayToDelimitedString(recipients, ",", false));
    xml.append("\"");

    xml.append(" mesgId=\"");
    xml.append(messageId);
    xml.append("\"");

    xml.append(">\n");

    //Properties
    if (properties != null && properties.size() > 0) {
      xml.append("\t<PROPERTIES>\n");

      iter = properties.keySet().iterator();
      while (iter.hasNext()) {
        name = (String) iter.next();
        value = properties.getProperty(name);

        xml.append("\t\t<PROPERTY name=\"");
        xml.append(name);
        xml.append("\" value=\"");
        xml.append(StringUtils.SimpleXMLTextEncoder(value));
        xml.append("\"/>\n");
      }

      xml.append("\t</PROPERTIES>\n");
    }

    //Data
    if (data != null && data.length > 0) {
      xml.append("\t<DATA>");
      xml.append(Base64Codec.Encode(data, false));
      xml.append("</DATA>\n");
    }
    else if (base64Data != null && base64Data.length() > 0) {
      xml.append("\t<DATA>");
      xml.append(base64Data);
      xml.append("</DATA>\n");
    }

    //END of P2P Hub Message XML Document
    xml.append("</P2P-HUB-MESSAGE>\n");

    return xml.toString();
  }

}
