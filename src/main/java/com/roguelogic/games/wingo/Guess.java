/**
 * Created Oct 18, 2007
 */
package com.roguelogic.games.wingo;

import java.io.Serializable;

/**
 * @author Robert C. Ilardi
 *
 */

public class Guess implements Serializable {

  public static final int STATE_NEW = 0;
  public static final int STATE_CORRECT = 1;
  public static final int STATE_INCORRECT_AND_VALID_WORD = 2;
  public static final int STATE_INCORRECT_AND_INVALID_WORD = 3;

  public static final int CHAR_STATE_NOT_CONTAINED = 0;
  public static final int CHAR_STATE_OUT_OF_POSITION = 1;
  public static final int CHAR_STATE_CORRECT_POSITION = 2;

  private String word;
  private int state;
  private int[] charStates;

  public Guess() {}

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public int[] getCharStates() {
    return charStates;
  }

  public void setCharStates(int[] charStates) {
    this.charStates = charStates;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[Guess - Word: ");
    sb.append(word);
    sb.append(" ; State: ");
    sb.append(state);
    sb.append("]");

    return sb.toString();
  }

}
