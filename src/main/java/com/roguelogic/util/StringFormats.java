/*
 * Created on Mar 20, 2004 - 6:53:06 PM
 * 
 * 
 */
package com.roguelogic.util;

/**
 * @author Administrator
 *
 */
public class StringFormats implements java.io.Serializable {

  /**
   * Stores String Format Options for use with the StringUtil.Stringize method
   */

  public static final int NEGATIVE_NUMBER_FORMAT_NORMAL = 0;
  public static final int NEGATIVE_NUMBER_FORMAT_PARENTHESES = 1;

  private int precision;
  private String nullFormat;
  private String dateFormat;
  private int negFormat;
  private boolean commatize;
	private String negHtmlStart;
	private String negHtmlEnd;

  private static StringFormats DefaultInstance = new StringFormats();

  public StringFormats() {
    //Set Some Defaults
    precision = 6;
    nullFormat = "";
    dateFormat = "yyyyMMdd";
    negFormat = NEGATIVE_NUMBER_FORMAT_NORMAL;
    commatize = false;
  }

  /**
   * @return
   */
  public String getDateFormat() {
    return dateFormat;
  }

  public static StringFormats GetDefaultInstance() {
    return DefaultInstance;
  }

  /**
   * @return
   */
  public String getNullFormat() {
    return nullFormat;
  }

  /**
   * @return
   */
  public int getPrecision() {
    return precision;
  }

  /**
   * @param string
   */
  public void setDateFormat(String df) {
    dateFormat = df;
  }

  /**
   * @param string
   */
  public void setNullFormat(String nf) {
    nullFormat = nf;
  }

  /**
   * @param i
   */
  public void setPrecision(int p) {
    precision = p;
  }

  public void setNegativeNumberFormat(int negFormat) {
    this.negFormat = negFormat;
  }

  public void setCommatize(boolean commatize) {
    this.commatize = commatize;
  }

  public int getNegativeNumberFormat() {
    return negFormat;
  }

  public boolean getCommatize() {
    return commatize;
  }

  /**
   * @return
   */
  public String getNegHtmlEnd() {
    return negHtmlEnd;
  }

  /**
   * @return
   */
  public String getNegHtmlStart() {
    return negHtmlStart;
  }

  /**
   * @param html
   */
  public void setNegHtmlEnd(String html) {
    negHtmlEnd = html;
  }

  /**
   * @param string
   */
  public void setNegHtmlStart(String html) {
    negHtmlStart = html;
  }

}
