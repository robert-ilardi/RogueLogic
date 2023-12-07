/**
 * Created Aug 6, 2008
 */
package com.roguelogic.joe.progs;

import java.util.Properties;

import com.roguelogic.joe.framework.JOEProgram;
import com.roguelogic.joe.kmods.RootShell;

/**
 * @author Robert C. Ilardi
 *
 */

public class RemoteShellServer implements JOEProgram {

  private RootShell rtSh;

  public RemoteShellServer() {}

  /* (non-Javadoc)
   * @see com.roguelogic.joe.framework.JOEProgram#setRootShell(com.roguelogic.joe.kmods.RootShell)
   */
  public void setRootShell(RootShell rtSh) {
    this.rtSh = rtSh;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.joe.framework.JOEProgram#execute(java.util.Properties, java.lang.String[])
   */
  public void execute(Properties env, String[] args) {
  // TODO Auto-generated method stub

  }

}
