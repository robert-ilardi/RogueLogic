/**
 * Created Aug 6, 2008
 */
package com.roguelogic.joe.framework;

import java.util.Properties;

import com.roguelogic.joe.kmods.RootShell;

/**
 * @author Robert C. Ilardi
 *
 */

public interface JOEProgram {

  public void setRootShell(RootShell rtSh);

  public void execute(Properties env, String[] args);

}
