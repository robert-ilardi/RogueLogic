/**
 * Created Oct 9, 2006
 */
package com.roguelogic.roguenet.gui;

import javax.swing.ImageIcon;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNAErrorDialog {

  private static ImageIcon RNAIcon=null;
  
  private String title;
  private String message;
  private Throwable thrwbl;

  public RNAErrorDialog(String title, String message, Throwable thrwbl) {
    this.title=title;
    this.message=message;
    this.thrwbl=thrwbl;
    
    displayError();
  }
  
  private void displayError()
  {
    
  }

}
