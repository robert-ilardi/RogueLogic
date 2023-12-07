/**
 * Created Sep 19, 2007
 */
package com.roguelogic.storage.querylang;

/**
 * @author Robert C. Ilardi
 *
 */

public class Keyword implements QueryLangModelObject {

  private String keyword;

  public Keyword() {}

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String toString() {
    return keyword;
  }

}
