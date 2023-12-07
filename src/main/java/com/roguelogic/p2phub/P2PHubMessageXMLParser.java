package com.roguelogic.p2phub;

import java.io.CharArrayWriter;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class P2PHubMessageXMLParser extends DefaultHandler {

  public static final String ELMT_DATA_RELAY_MESSAGE = "P2P-HUB-MESSAGE";
  public static final String ELMT_PROPERTIES = "PROPERTIES";
  public static final String ELMT_PROPERTY = "PROPERTY";
  public static final String ELMT_DATA = "DATA";

  public static final String ATTR_NAME = "name";
  public static final String ATTR_VALUE = "value";
  public static final String ATTR_TYPE = "type";
  public static final String ATTR_SENDER = "sender";
  public static final String ATTR_SUBJECT = "subject";
  public static final String ATTR_VERSION = "mesgVer";
  public static final String ATTR_RECIPIENTS = "recipients";
  public static final String ATTR_MESSAGE_ID = "mesgId";

  private P2PHubMessage message;

  private CharArrayWriter text;

  public P2PHubMessageXMLParser() {
    super();
    text = new CharArrayWriter();
  }

  public P2PHubMessage getMessage() {
    return message;
  }

  public void startDocument() {
    message = null;
  }

  public void endDocument() {}

  public void startElement(String uri, String localName, String qName, Attributes attrs) {
    String elementName = (uri == null || uri.trim().length() == 0 ? qName : localName);
    Properties props;
    String name, value;

    //System.out.println("!!!!!!!!!!!!!> Processing Element: " + elementName);

    text.reset();

    if (ELMT_DATA_RELAY_MESSAGE.equalsIgnoreCase(elementName)) {
      message = new P2PHubMessage();
      message.setVersion(attrs.getValue(ATTR_VERSION));
      message.setType(attrs.getValue(ATTR_TYPE));
      message.setSender(attrs.getValue(ATTR_SENDER));
      message.setSubject(attrs.getValue(ATTR_SUBJECT));
      message.setMessageId(Long.parseLong(attrs.getValue(ATTR_MESSAGE_ID)));

      value = attrs.getValue(ATTR_RECIPIENTS);
      if (value != null && value.trim().length() > 0) {
        message.setRecipients(value.trim().split(","));
      }
    }
    else if (ELMT_PROPERTY.equalsIgnoreCase(elementName)) {
      props = message.getProperties();
      if (props == null) {
        props = new Properties();
        message.setProperties(props);
      }

      name = attrs.getValue(ATTR_NAME);
      value = attrs.getValue(ATTR_VALUE);

      props.setProperty(name, value);
    }
  }

  public void endElement(String uri, String localName, String qName) {
    String elementName = (uri == null || uri.trim().length() == 0 ? qName : localName);

    if (ELMT_DATA.equalsIgnoreCase(elementName)) {
      message.setBase64Data(getText());
    }
  }

  public void characters(char[] ch, int start, int length) {
    text.write(ch, start, length);
  }

  public String getText() {
    return text.toString().trim();
  }

}
