/**
 * Created Oct 11, 2006
 */
package com.roguelogic.roguenet.gui;

import java.awt.Image;
import java.util.Properties;

import com.roguelogic.roguenet.RNAConstants;

/**
 * @author Robert C. Ilardi
 *
 */

public class PlugInDetailsPropTableDialog extends PropertiesTableDialog {

  public PlugInDetailsPropTableDialog(Properties props, String ptdTitle, Image icon) {
    super(props, ptdTitle, icon);
  }

  protected boolean filteredOut(String propName) {
    return (!propName.startsWith(RNAConstants.PLUG_IN_INFO_PROP_PREFIX) && !propName.startsWith(RNAConstants.PLUG_IN_INFO_DEVELOPER_PROP_PREFIX));
  }

}
