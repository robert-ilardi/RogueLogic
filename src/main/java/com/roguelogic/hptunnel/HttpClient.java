/**
 * Created Jan 15, 2008
 */
package com.roguelogic.hptunnel;

import static com.roguelogic.hptunnel.HPTConstants.HPT_RLTCMD_DATA_STREAM;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkUtils;
import com.roguelogic.util.BinaryLineReader;
import com.roguelogic.net.rltalk.RLTalkXorCodec;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class HttpClient implements SocketProcessorCustomizer, SocketSessionSweeper {

  private ByteArrayOutputStream reqBuffer;
  private ByteArrayOutputStream resBuffer;

  private SocketClient sockClient;
  private SocketSession proxyConnection;

  private boolean relayedResponseHeader = false;

  public HttpClient() {}

  public synchronized void connect(String address, int port) throws HPTException, RLNetException {
    if (sockClient != null) {
      throw new HPTException("Client Already Connected!");
    }

    sockClient = new SocketClient();
    sockClient.setSocketProcessorClass(HttpClientProcessor.class);
    sockClient.setSocketProcessorCustomizer(this);
    sockClient.setSocketSessionSweeper(this);
    sockClient.setLingerTimeOverride(10);
    sockClient.setReuseAddress(true);
    sockClient.connect(address, port);
  }

  public synchronized void close() {
    if (sockClient != null) {
      sockClient.close();
      sockClient = null;
    }
  }

  public synchronized boolean isConnected() {
    return sockClient != null ? sockClient.isConnected() : false;
  }

  public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
    HttpClientProcessor hcPrc;

    if (processor instanceof HttpClientProcessor) {
      hcPrc = (HttpClientProcessor) processor;
      hcPrc.setClient(this);
    }
  }

  public synchronized void cleanup(SocketSession userSession) {
    //proxyConnection.drainRawData();

    System.out.println("HTTP Server Closed?");

    proxyConnection.clearUserData();
    proxyConnection.endSession();
    proxyConnection = null;
  }

  public void setProxyConnection(SocketSession proxyConnection) {
    this.proxyConnection = proxyConnection;
  }

  public synchronized void sendDataThroughTunnel(byte[] data) throws RLNetException {
    CommandDataPair request;

    if (!relayedResponseHeader) {
      if (resBuffer == null) {
        resBuffer = new ByteArrayOutputStream();
      }

      resBuffer.write(data, 0, data.length);
      data = resBuffer.toByteArray();

      if (hasCompleteHttpHeader(data)) {
        relayedResponseHeader = true;
        resBuffer = null;

        data = rewriteHttpResponseHeader(data);

        request = new CommandDataPair();
        request.setCommand(HPT_RLTCMD_DATA_STREAM);
        request.setData(data);

        sendEncrypted(proxyConnection, request);
      }
    }
    else {
      request = new CommandDataPair();
      request.setCommand(HPT_RLTCMD_DATA_STREAM);
      request.setData(data);

      sendEncrypted(proxyConnection, request);
    }

    //System.out.println("\n===============================\n");
    //System.out.println(new String(data));
  }

  private void sendEncrypted(SocketSession userSession, CommandDataPair request) throws RLNetException {
    RLTalkXorCodec codec = (RLTalkXorCodec) userSession.getUserItem(HttpProxyTunnelDaemonProcessor.USOBJ_XOR_CODEC);

    if (codec != null) {
      CommandDataPair cipherRequest = codec.encrypt(request);
      RLTalkUtils.RLTalkSend(userSession, cipherRequest);
    }
  }

  public synchronized void sendData(byte[] data) {
    URL url;
    int hhLen;
    byte[] data2;

    try {
      if (sockClient == null) {
        if (reqBuffer == null) {
          reqBuffer = new ByteArrayOutputStream();
        }

        reqBuffer.write(data, 0, data.length);
        data = reqBuffer.toByteArray();

        if (hasCompleteHttpHeader(data)) {
          reqBuffer = null;

          if (!isHttpConnectMethod(data)) {
            data = rewriteHttpRequestHeader(data);
            url = getUrl(data);

            if (url != null) {
              System.out.println("<" + StringUtils.GetTimeStamp() + "> Proxying Connection to: " + url.toString());

              if (url.getPort() <= 0) {
                connect(url.getHost(), 80);
              }
              else {
                connect(url.getHost(), url.getPort());
              }

              sockClient.send(data);
            }
          }
          else {
            //Tunnel for HTTPS using the CONNECT Http Method
            url = getUrl(data);

            if (url != null) {
              System.out.println("<" + StringUtils.GetTimeStamp() + "> Proxying (HTTP Tunnel Mode) Connection to: " + url.toString());

              if (url.getPort() <= 0) {
                connect(url.getHost(), 80);
              }
              else {
                connect(url.getHost(), url.getPort());
              }

              //Send Connection established to client...

              sendDataThroughTunnel("HTTP/1.1 200 Connection established".getBytes());
              sendDataThroughTunnel(new byte[] { 13, 10, 13, 10 });

              hhLen = getHttpHeaderLength(data);

              if (data.length > hhLen) {
                data2 = new byte[data.length - hhLen];
                System.arraycopy(data, hhLen, data2, 0, data2.length);

                //System.out.println(new String(data2));
                sockClient.send(data2);
              }
            }
          }
        }
      }
      else {
        //Connection already opened...
        //System.out.println(new String(data));
        sockClient.send(data);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean isHttpConnectMethod(byte[] data) {
    BinaryLineReader blr = null;
    String line;
    boolean connectMethod = false;

    try {
      blr = new BinaryLineReader(data);
      line = blr.readLine();
      connectMethod = (line != null && line.trim().toUpperCase().startsWith("CONNECT"));
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (blr != null) {
        blr.clear();
        blr = null;
      }
    }

    return connectMethod;
  }

  private int getHttpHeaderLength(byte[] data) {
    BinaryLineReader blr = null;
    String line;
    int hhLen = 0;

    try {
      blr = new BinaryLineReader(data);

      line = blr.readLine();
      while (line != null) {
        //System.out.println("Line: |" + line + "|");
        if (line.length() == 0) {
          hhLen = blr.getPosition();
          break;
        }

        line = blr.readLine();
      } //End while line!=null
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (blr != null) {
        blr.clear();
        blr = null;
      }
    }

    return hhLen;
  }

  private URL getUrl(byte[] data) throws MalformedURLException {
    URL url = null;
    String urlStr;
    int sIndex, eIndex;

    for (int i = 0; i < data.length; i++) {
      if (data[i] == 13 || data[i] == 10) {
        urlStr = new String(data, 0, i - 1);

        sIndex = urlStr.indexOf(' ');
        eIndex = urlStr.lastIndexOf(" HTTP/");

        if (sIndex >= 0 && eIndex >= 0) {
          urlStr = urlStr.substring(sIndex, eIndex);

          if (urlStr.indexOf(":/") == -1) {
            urlStr = new StringBuffer().append("http://").append(urlStr.trim()).toString();
          }

          url = new URL(urlStr.trim());
        }

        break;
      }
    }

    return url;
  }

  private boolean hasCompleteHttpHeader(byte[] data) {
    //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Size: " + getHttpHeaderLength(data));
    return getHttpHeaderLength(data) > 0;
  }

  private byte[] rewriteHttpResponseHeader(byte[] data) {
    BinaryLineReader blr = null;
    String line;
    int b;
    ByteArrayOutputStream baos;

    try {
      blr = new BinaryLineReader(data);
      baos = new ByteArrayOutputStream();

      //System.out.println(new String(data));

      line = blr.readLine();

      if (line != null && !line.trim().toUpperCase().startsWith("HTTP/")) {
        //Inject Default HTTP Response header...
        baos.write("HTTP/1.1 200 OK".getBytes());
        baos.write(13);
        baos.write(10);
      }

      while (line != null) {
        //System.out.println("|" + line + "|");

        if (line.trim().toUpperCase().startsWith("CONNECTION:") || line.trim().toUpperCase().startsWith("KEEP-ALIVE:") || line.trim().toUpperCase().startsWith("PROXY-CONNECTION:")) {
          //Skip
        }
        else if (line.trim().length() == 0) {
          break;
        }
        else {
          baos.write(line.getBytes());
          baos.write(13);
          baos.write(10);
        }

        line = blr.readLine();
      } //End while line!=null

      //Add connection close
      baos.write("Connection: close".getBytes());
      baos.write(13);
      baos.write(10);

      baos.write(13);
      baos.write(10);

      //System.out.print(new String(baos.toByteArray()));

      b = blr.readByte();
      while (b != -1) {
        baos.write(b);
        b = blr.readByte();
      }

      data = baos.toByteArray();
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (blr != null) {
        blr.clear();
        blr = null;
      }
    }

    //System.out.println("===============================");
    //System.out.println(new String(data));
    //System.out.println(extractHttpHeader(data));

    return data;
  }

  protected String extractHttpHeader(byte[] data) {
    BinaryLineReader blr = null;
    String line;
    StringBuffer header = new StringBuffer();

    try {
      blr = new BinaryLineReader(data);

      line = blr.readLine();
      while (line != null) {
        if (line.length() == 0) {
          break;
        }
        else {
          header.append(line);
          header.append("\n");
        }

        line = blr.readLine();
      } //End while line!=null
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (blr != null) {
        blr.clear();
        blr = null;
      }
    }

    return header.toString();
  }

  private byte[] rewriteHttpRequestHeader(byte[] data) {
    BinaryLineReader blr = null;
    String line;
    int b;
    ByteArrayOutputStream baos;

    try {
      blr = new BinaryLineReader(data);
      baos = new ByteArrayOutputStream();

      //System.out.println(new String(data));

      line = blr.readLine();
      while (line != null) {
        //System.out.println("|" + line + "|");

        if (line.trim().toUpperCase().startsWith("CONNECTION:") || line.trim().toUpperCase().startsWith("KEEP-ALIVE:") || line.trim().toUpperCase().startsWith("PROXY-CONNECTION:")
            || line.trim().toUpperCase().startsWith("IF-MODIFIED-SINCE:") || line.trim().toUpperCase().startsWith("IF-NONE-MATCH:")) {
          //Skip
        }
        else if (line.trim().length() == 0) {
          break;
        }
        else {
          baos.write(line.getBytes());
          baos.write(13);
          baos.write(10);
        }

        line = blr.readLine();
      } //End while line!=null

      //Add connection close
      baos.write("Connection: close".getBytes());
      baos.write(13);
      baos.write(10);

      baos.write(13);
      baos.write(10);

      //System.out.print(new String(baos.toByteArray()));

      b = blr.readByte();
      while (b != -1) {
        baos.write(b);
        b = blr.readByte();
      }

      data = baos.toByteArray();
    } //End try block
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (blr != null) {
        blr.clear();
        blr = null;
      }
    }

    //System.out.println("===============================");
    //System.out.println(new String(data));
    //System.out.println(extractHttpHeader(data));

    return data;
  }

}
