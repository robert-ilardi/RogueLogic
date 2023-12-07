/**
 * Created Sep 16, 2006
 */
package com.roguelogic.p2phub;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Robert C. Ilardi
 *
 */

public class P2PHubUtils {

  public static byte[] LoadKeyData(String keyFile) throws IOException {
    FileInputStream fis = null;
    ByteArrayOutputStream baos = null;
    byte[] buf;
    int len;
    byte[] keyData = null;

    try {
      fis = new FileInputStream(keyFile);
      buf = new byte[1024];
      baos = new ByteArrayOutputStream();

      len = fis.read(buf);
      while (len > 0) {
        baos.write(buf, 0, len);
        len = fis.read(buf);
      }

      keyData = baos.toByteArray();
    }
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
        fis = null;
      }

      if (baos != null) {
        try {
          baos.close();
        }
        catch (Exception e) {}
        baos = null;
      }
    }

    return keyData;
  }

  public static P2PHubMessage ReadMessageXML(String xml) throws SAXException, IOException {
    XMLReader xReader = null;
    P2PHubMessageXMLParser parser;
    StringReader sReader = null;
    InputSource ins;
    P2PHubMessage mesg = null;

    try {
      sReader = new StringReader(xml);
      xReader = XMLReaderFactory.createXMLReader();
      parser = new P2PHubMessageXMLParser();
      ins = new InputSource(sReader);

      xReader.setContentHandler(parser);
      xReader.setErrorHandler(parser);

      xReader.parse(ins);

      mesg = parser.getMessage();
    } //End try block
    finally {
      if (sReader != null) {
        sReader.close();
      }
    }

    return mesg;
  }

}
