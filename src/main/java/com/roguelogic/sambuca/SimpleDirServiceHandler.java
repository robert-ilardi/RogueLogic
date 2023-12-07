/**
 * Created Nov 2, 2007
 */

/*
 Copyright 2007 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.roguelogic.sambuca;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * A simple directory/file based HTTP ServiceHandler Demo class used by the SambucaHttpServerDemo.
 * 
 * @author Robert C. Ilardi
 *
 */

public class SimpleDirServiceHandler implements ServiceHandler {

  public static final String[] HTML_EXTS = new String[] { "HTM", "HTML" };
  public static final String[] TEXT_EXTS = new String[] { "TXT", "XML", "CSS" };

  private String wwwRoot;
  private String indexFile;
  private Properties mimeTypes;
  private SambucaLogger logger;

  public SimpleDirServiceHandler() {}

  public String getWwwRoot() {
    return wwwRoot;
  }

  public void setWwwRoot(String wwwRoot) {
    this.wwwRoot = wwwRoot;
  }

  public String getIndexFile() {
    return indexFile;
  }

  public void setIndexFile(String indexFile) {
    this.indexFile = indexFile;
  }

  public Properties getMimeTypes() {
    return mimeTypes;
  }

  public void setMimeTypes(Properties mimeTypes) {
    this.mimeTypes = mimeTypes;
  }

  public SambucaLogger getLogger() {
    return logger;
  }

  public void setLogger(SambucaLogger logger) {
    this.logger = logger;
  }

  public void handle(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    //System.out.println(request);

    try {
      if ("/*DEBUG-REQUEST*".equalsIgnoreCase(request.getUrl())) {
        sendRequestDebugHtml(request, response);
      }
      else if (dirForbidden(request.getUrl())) {
        sendForbidden(request, response);
      }
      else if (!fileExists(request.getUrl())) {
        sendNotFound(request, response);
      }
      else if (isDir(request.getUrl())) {
        sendDirectory(request, response);
      }
      else {
        sendFile(request.getUrl(), response);
      }
    }
    finally {
      if (request != null) {
        response.close();
      }
    }
  }

  private void sendDirectory(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    String tmpUrl = null;

    if (!StringUtils.IsNVL(indexFile)) {
      tmpUrl = getCompleteUrl(request.getUrl(), indexFile);
    }

    if (fileExists(tmpUrl)) {
      sendFile(tmpUrl, response);
    }
    else {
      sendDirPage(request, response);
    }
  }

  private String getCompleteUrl(String baseUrl, String file) {
    StringBuffer sb = new StringBuffer();

    sb.append(baseUrl.trim());

    if (!baseUrl.trim().endsWith("/")) {
      sb.append("/");
    }

    sb.append(file.trim());

    return sb.toString();
  }

  private void sendDirPage(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    StringBuffer sb = new StringBuffer();
    String html, path;
    byte[] bArr;
    File f, ef;
    String[] ls;

    sb.append("<HTML><HEAD><TITLE>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - Directory List");
    sb.append("</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\" TEXT=\"#000000\"><H1>");
    sb.append("Directory List: ");
    sb.append(request.getUrl());
    sb.append("</H1>\n\n");

    path = getFilePath(request.getUrl());
    f = new File(path);

    ls = f.list();

    sb.append("<UL>\n");

    for (String entry : ls) {
      sb.append("<LI><A Href=\"");
      sb.append(request.getUrl());

      if (!request.getUrl().endsWith("/")) {
        sb.append("/");
      }

      sb.append(entry);
      sb.append("\">");

      ef = new File(getFilePath(getCompleteUrl(request.getUrl(), entry)));

      if (ef.isDirectory()) {
        sb.append("<B>");
        sb.append(entry);
        sb.append("/</B>");
      }
      else {
        sb.append(entry);
      }

      sb.append("</A></LI>\n");
    }

    sb.append("</UL>\n");

    sb.append(getServerFooterHtml(request));

    sb.append("</BODY></HTML>");

    html = sb.toString();
    bArr = html.getBytes();

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_OK);
    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_HTML_TEXT);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private boolean isDir(String url) {
    boolean dir = false;
    String path;
    File d;

    path = getFilePath(url);
    d = new File(path);
    dir = d.isDirectory();

    return dir;
  }

  private void sendRequestDebugHtml(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    StringBuffer sb = new StringBuffer();
    String html;
    byte[] bArr;

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_OK);

    sb.append("<HTML><HEAD><TITLE>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - Request Debug");
    sb.append("</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\" TEXT=\"#000000\"><H1>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - Request Debug");
    sb.append("</H1><PRE>");
    sb.append(StringUtils.SimpleHTMLTextEncoder(request.toString()));
    sb.append("</PRE>");

    sb.append(getServerFooterHtml(request));

    sb.append("</BODY></HTML>");

    html = sb.toString();
    bArr = html.getBytes();

    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_HTML_TEXT);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private boolean dirForbidden(String url) {
    return url == null || url.trim().length() == 0 || url.indexOf("..") >= 0;
  }

  private boolean fileExists(String url) {
    boolean exists = false;
    String path;
    File f;

    if (!StringUtils.IsNVL(url)) {
      path = getFilePath(url);
      f = new File(path);
      exists = f.exists();
    }

    return exists;
  }

  private String getFilePath(String url) {
    String path;

    path = new StringBuffer().append(wwwRoot).append("/").append((url.startsWith("/") && url.length() > 1 ? url.substring(1) : url)).toString();

    return path;
  }

  private void sendForbidden(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    StringBuffer sb = new StringBuffer();
    String html;
    byte[] bArr;

    sb.append("<HTML><HEAD><TITLE>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_FORBIDDEN);
    sb.append("</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\" TEXT=\"#000000\"><H1>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_FORBIDDEN);
    sb.append("</H1><PRE>");
    sb.append(StringUtils.SimpleHTMLTextEncoder(request.getUrl()));
    sb.append("</PRE>");

    sb.append(getServerFooterHtml(request));

    sb.append("</BODY></HTML>");

    html = sb.toString();
    bArr = html.getBytes();

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_FORBIDDEN);
    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_HTML_TEXT);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private void sendNotFound(SambucaHttpRequest request, SambucaHttpResponse response) throws SambucaException {
    StringBuffer sb = new StringBuffer();
    String html;
    byte[] bArr;

    sb.append("<HTML><HEAD><TITLE>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_NOT_FOUND);
    sb.append("</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\" TEXT=\"#000000\"><H1>");
    sb.append(Version.APP_TITLE_SHORT);
    sb.append(" - ");
    sb.append(SambucaHttpResponse.STATUS_CODE_NOT_FOUND);
    sb.append("</H1><PRE>");
    sb.append(StringUtils.SimpleHTMLTextEncoder(request.getUrl()));
    sb.append("</PRE>");

    sb.append(getServerFooterHtml(request));

    sb.append("</BODY></HTML>");

    html = sb.toString();
    bArr = html.getBytes();

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_NOT_FOUND);
    response.setContentType(SambucaHttpResponse.CONTENT_TYPE_HTML_TEXT);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(bArr.length));
    response.send(bArr);
  }

  private void sendFile(String url, SambucaHttpResponse response) throws SambucaException {
    FileInputStream fis = null;
    String path;
    byte[] buf, partBuf;
    int len;
    File f;

    response.setStatusCode(SambucaHttpResponse.STATUS_CODE_OK);

    determineContentType(url, response);

    path = getFilePath(url);
    f = new File(path);
    response.addHeader(SambucaHttpResponse.HEADER_CONTENT_LENGTH, String.valueOf(f.length()));

    response.sendHeaders();

    try {
      fis = new FileInputStream(path);

      buf = new byte[2048];
      len = fis.read(buf);

      while (len != -1) {
        if (len != buf.length) {
          partBuf = new byte[len];
          System.arraycopy(buf, 0, partBuf, 0, len);
          response.send(partBuf);
        }
        else {
          response.send(buf);
        }

        len = fis.read(buf);
      }
    }
    catch (Exception e) {
      throw new SambucaException("An error occurred while attempting to send file data!", e);
    }
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (Exception e) {}
      }
    }
  }

  private void determineContentType(String url, SambucaHttpResponse response) {
    String mimeType = null, ext;
    int dotIdx = url.indexOf(".");

    if (dotIdx >= 0 && dotIdx < url.length() - 1) {
      ext = url.substring(url.lastIndexOf(".") + 1);
      mimeType = mimeTypes.getProperty(ext.toLowerCase());
    }

    if (mimeType != null) {
      response.setContentType(mimeType);
    }
    else {
      response.setContentType(SambucaHttpResponse.CONTENT_TYPE_OCTET_STREAM);
    }
  }

  private String getServerFooterHtml(SambucaHttpRequest request) {
    StringBuffer html = new StringBuffer();

    html.append("<BR><HR><P><B>");
    html.append(Version.APP_TITLE_SHORT);
    html.append(" </B><I>Version: ");
    html.append(Version.VERSION);
    html.append(" - listening on ");
    html.append(request.getServerAddress());
    html.append(" (");
    html.append(SystemUtils.GetOperatingSystemName());
    html.append(") on port ");
    html.append(request.getServerPort());
    html.append("</I></P>");

    return html.toString();
  }

}
