/**
 * Created Jan 6, 2007
 */
package com.roguelogic.thumbnails;

import java.io.FileOutputStream;

/**
 * @author Robert C. Ilardi
 *
 */

public class PageParameters {

  private FileOutputStream albumPageFile;
  private String title;
  private String outputDir;
  private int maxCols;
  private int maxRows;
  private int colCnt;
  private int rowCnt;
  private int pageCnt;
  private String prevFile;
  private String curFile;
  private boolean lastPage;
  private String linkPath;

  public PageParameters() {}

  public FileOutputStream getAlbumPageFile() {
    return albumPageFile;
  }

  public void setAlbumPageFile(FileOutputStream albumPageFile) {
    this.albumPageFile = albumPageFile;
  }

  public int getColCnt() {
    return colCnt;
  }

  public void setColCnt(int colCnt) {
    this.colCnt = colCnt;
  }

  public int getMaxCols() {
    return maxCols;
  }

  public void setMaxCols(int maxCols) {
    this.maxCols = maxCols;
  }

  public int getMaxRows() {
    return maxRows;
  }

  public void setMaxRows(int maxRows) {
    this.maxRows = maxRows;
  }

  public String getOutputDir() {
    return outputDir;
  }

  public void setOutputDir(String outputDir) {
    this.outputDir = outputDir;
  }

  public int getPageCnt() {
    return pageCnt;
  }

  public void setPageCnt(int pageCnt) {
    this.pageCnt = pageCnt;
  }

  public int getRowCnt() {
    return rowCnt;
  }

  public void setRowCnt(int rowCnt) {
    this.rowCnt = rowCnt;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void incrementRowCnt() {
    rowCnt++;
  }

  public void incrementColCnt() {
    colCnt++;
  }

  public void incrementPageCnt() {
    pageCnt++;
  }

  public String getCurFile() {
    return curFile;
  }

  public void setCurFile(String curFile) {
    this.curFile = curFile;
  }

  public String getPrevFile() {
    return prevFile;
  }

  public void setPrevFile(String prevFile) {
    this.prevFile = prevFile;
  }

  public boolean isLastPage() {
    return lastPage;
  }

  public void setLastPage(boolean lastPage) {
    this.lastPage = lastPage;
  }

  public String getLinkPath() {
    return linkPath;
  }

  public void setLinkPath(String linkPath) {
    this.linkPath = linkPath;
  }

}
