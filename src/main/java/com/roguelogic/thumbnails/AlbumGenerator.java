/**
 * Created Jan 6, 2007
 */
package com.roguelogic.thumbnails;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Robert C. Ilardi
 *
 */

public class AlbumGenerator {

  public static final String GENERATOR_STRING = "RogueLogic Album Generator - Version 1.0";

  public AlbumGenerator() {}

  private static FileFilter BasicFileFilter = new FileFilter() {
    public boolean accept(File f) {
      return (f != null && !f.isDirectory() && f.getName().toUpperCase().endsWith(".JPG"));
    }
  };

  public static void main(String[] args) {
    AlbumGenerator albumGen;
    int exitCode;
    String inputDir, outputDir, title, linkPath;
    int tnWidth, tnHeight, cols, rows;

    if (args.length != 8) {
      System.out.println("java " + AlbumGenerator.class.getName() + " [INPUT_DIRECTORY] [OUTPUT_DIRECTORY] [TITLE] [TN_WIDTH] [TN_HEIGHT] [COLUMNS] [ROWS] [LINK_PATH]");
      exitCode = 1;
    }
    else {
      try {
        inputDir = args[0];
        outputDir = args[1];
        title = args[2];
        tnWidth = Integer.parseInt(args[3]);
        tnHeight = Integer.parseInt(args[4]);
        cols = Integer.parseInt(args[5]);
        rows = Integer.parseInt(args[6]);
        linkPath = args[7];

        albumGen = new AlbumGenerator();
        albumGen.generateAlbum(inputDir, outputDir, title, tnWidth, tnHeight, cols, rows, linkPath);

        exitCode = 0;
      } //End try block
      catch (Exception e) {
        exitCode = 1;
        e.printStackTrace();
      }
    }

    System.out.println("\nExit Status = " + exitCode);
    System.exit(exitCode);
  }

  private void generateAlbum(String inputDir, String outputDir, String title, int tnWidth, int tnHeight, int cols, int rows, String linkPath) throws IOException {
    String[] fileList;
    int imgCnt = 1, colCnt = 1, rowCnt = 1, pageCnt = 1;
    StringBuffer sb;
    String outputFile, outputPath, inputPath;
    PageParameters pageParams;

    fileList = getFileList(inputDir);

    if (fileList == null || fileList.length == 0) {
      System.out.println("Directory does NOT contain any supported images...");
      return;
    }

    pageParams = new PageParameters();
    pageParams.setTitle(title);
    pageParams.setOutputDir(outputDir);
    pageParams.setMaxCols(cols);
    pageParams.setMaxRows(rows);
    pageParams.setPageCnt(pageCnt);
    pageParams.setColCnt(colCnt);
    pageParams.setRowCnt(rowCnt);
    pageParams.setLastPage(false);
    pageParams.setLinkPath(linkPath);

    for (String inputFile : fileList) {
      sb = new StringBuffer();
      sb.append(inputDir);
      sb.append("/");
      sb.append(inputFile);
      inputPath = sb.toString();

      sb = new StringBuffer();
      sb.append("tn_");
      sb.append(inputFile);
      outputFile = sb.toString();

      sb = new StringBuffer();
      sb.append(outputDir);
      sb.append("/");
      sb.append(outputFile);
      outputPath = sb.toString();

      resizeImage(imgCnt++, fileList.length, inputFile, inputPath, tnWidth, tnHeight, outputPath);
      updateAlbumPage(pageParams, outputFile, inputFile);
    }

    pageParams.setLastPage(true);
    closeAlbumPageFile(pageParams);
  }

  private String[] getFileList(String reqDir) {
    String[] fileList = null;
    File dir;
    File[] files;

    dir = new File(reqDir);
    files = dir.listFiles(BasicFileFilter);

    if (files != null) {
      fileList = new String[files.length];

      for (int i = 0; i < files.length; i++) {
        fileList[i] = files[i].getName();
      }
    }

    return fileList;
  }

  private void resizeImage(int imgCnt, int totalCnt, String inputFile, String inputPath, int tnWidth, int tnHeight, String outputPath) throws IOException {
    BufferedImage origImg, scaledImg;
    Graphics2D gScaledImg;

    origImg = ImageIO.read(new File(inputPath));

    System.out.println("Resizing (" + imgCnt + " of " + totalCnt + ") " + inputFile + " (" + origImg.getWidth() + "x" + origImg.getHeight() + ") -> (" + tnWidth + "x" + tnHeight + ")");

    scaledImg = new BufferedImage(tnWidth, tnHeight, BufferedImage.TYPE_INT_RGB);
    gScaledImg = scaledImg.createGraphics();

    //Note the use of BILNEAR filtering to enable smooth scaling
    gScaledImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

    //Scale the original image into the temporary image
    gScaledImg.drawImage(origImg, 0, 0, tnWidth, tnHeight, null);

    //Save the scaled version out to the file
    ImageIO.write(scaledImg, "jpeg", new File(outputPath));
  }

  private void updateAlbumPage(PageParameters pageParams, String tnFile, String fullFile) throws IOException {
    StringBuffer sb;
    String tmp, prevFile;

    if (pageParams.getColCnt() == 1 && pageParams.getRowCnt() == 1 && pageParams.getPageCnt() == 1) {
      //Start of album; use index.htm
      sb = new StringBuffer();
      sb.append(pageParams.getOutputDir());
      sb.append("/index.htm");
      tmp = sb.toString();

      pageParams.setCurFile("index.htm");
      System.out.println(">>>> Creating Album Page File: index.htm");
      pageParams.setAlbumPageFile(new FileOutputStream(tmp));

      writeStart(pageParams);
    }
    else if (pageParams.getColCnt() > pageParams.getMaxCols() && pageParams.getRowCnt() == pageParams.getMaxRows()) {
      //Start new page
      pageParams.setColCnt(1);
      pageParams.setRowCnt(1);

      sb = new StringBuffer();
      sb.append("page");
      sb.append(pageParams.getPageCnt() + 1);
      sb.append(".htm");
      tmp = sb.toString();

      prevFile = pageParams.getCurFile();
      pageParams.setCurFile(tmp);

      closeAlbumPageFile(pageParams);

      System.out.println(">>>> Creating Album Page File: " + tmp);

      sb = new StringBuffer();
      sb.append(pageParams.getOutputDir());
      sb.append("/");
      sb.append(tmp);
      tmp = sb.toString();

      pageParams.setAlbumPageFile(new FileOutputStream(tmp));
      pageParams.incrementPageCnt();
      pageParams.setPrevFile(prevFile);

      writeStart(pageParams);
    }
    else if (pageParams.getColCnt() > pageParams.getMaxCols()) {
      //Start new row
      pageParams.incrementRowCnt();
      pageParams.setColCnt(1);

      pageParams.getAlbumPageFile().write("    </TR>\n    <TR>\n".getBytes());
    }

    sb = new StringBuffer();
    sb.append("      <TD><A Href=\"");

    if (pageParams.getLinkPath() != null && pageParams.getLinkPath().trim().length() > 0) {
      sb.append(pageParams.getLinkPath().trim());
      sb.append("/");
    }

    sb.append(fullFile);

    sb.append("\"><IMG SRC=\"");
    sb.append(tnFile);
    sb.append("\" Border=\"0\"></A></TD>\n");

    pageParams.getAlbumPageFile().write(sb.toString().getBytes());
    pageParams.incrementColCnt();
  }

  private void writeStart(PageParameters pageParams) throws IOException {
    StringBuffer html;

    html = new StringBuffer();

    html.append("<HTML>\n");
    html.append("<HEAD>\n");
    html.append("  <TITLE>");
    html.append(pageParams.getTitle());
    html.append("</TITLE>\n");
    html.append("  <META NAME=\"generator\" CONTENT=\"");
    html.append(GENERATOR_STRING);
    html.append("\">\n");
    html.append("</HEAD>\n");
    html.append("<BODY>\n");

    html.append("  <H2>");
    html.append(pageParams.getTitle());
    html.append(" - Page ");
    html.append(pageParams.getPageCnt());
    html.append("</H2>\n");

    html.append("  <TABLE align=\"center\" cellspacing=\"5\">\n");
    html.append("    <TR>\n");

    pageParams.getAlbumPageFile().write(html.toString().getBytes());
  }

  private void closeAlbumPageFile(PageParameters pageParams) throws IOException {
    if (pageParams.getAlbumPageFile() == null) {
      return;
    }

    try {
      writeEnd(pageParams);
    }
    finally {
      try {
        pageParams.getAlbumPageFile().close();
      }
      catch (Exception e) {}
    }
  }

  private void writeEnd(PageParameters pageParams) throws IOException {
    StringBuffer html;

    html = new StringBuffer();

    html.append("    </TR>\n");
    html.append("  </TABLE>\n");

    html.append("  <P align=\"center\">\n");

    if (pageParams.getPageCnt() > 1) {
      html.append("    <A Href=\"");
      html.append(pageParams.getPrevFile());
      html.append("\">&lt; Previous Page</A>\n");
    }

    if (!pageParams.isLastPage()) {
      html.append("    &nbsp;&nbsp;&nbsp;&nbsp;\n");
      html.append("    <A Href=\"");
      html.append(pageParams.getCurFile());
      html.append("\">Next Page &gt;</A>\n");
    }

    html.append("  </P>\n");

    html.append("</BODY>\n");
    html.append("</HTML>\n");

    pageParams.getAlbumPageFile().write(html.toString().getBytes());
  }

}
