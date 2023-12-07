/**
 * Created Oct 19, 2006
 */
package com.roguelogic.gui;

import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

/**
 * @author Robert C. Ilardi
 *
 */

public class RLComboBox extends JComboBox {

  public RLComboBox() {
    super(new DefaultComboBoxModel());

    setUI(new MetalComboBoxUI() {
      protected ComboPopup createPopup() {
        BasicComboPopup popup = new BasicComboPopup(comboBox) {
          protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
            FontMetrics fm = comboBox.getGraphics().getFontMetrics();
            int valWidth = 0, tmp;
            DefaultComboBoxModel cModel;

            cModel = (DefaultComboBoxModel) comboBox.getModel();
            for (int i = 0; i < cModel.getSize(); i++) {
              tmp = fm.stringWidth(comboBox.getSelectedItem().toString().trim()) + 5;

              if (tmp > valWidth) {
                valWidth = tmp;
              }
            }

            return super.computePopupBounds(px, py, Math.max(valWidth, pw), ph);
          }
        };
        popup.getAccessibleContext().setAccessibleParent(comboBox);
        return popup;
      }
    });
  }

}
