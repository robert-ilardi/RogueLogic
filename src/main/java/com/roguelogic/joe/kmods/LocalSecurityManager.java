/**
 * Created Aug 6, 2008
 */
package com.roguelogic.joe.kmods;

import com.roguelogic.joe.framework.JOEKernelModule;
import com.roguelogic.joe.framework.SecurityManager;
import com.roguelogic.joe.kernel.JOEKernel;

/**
 * @author Robert C. Ilardi
 *
 */
public class LocalSecurityManager implements JOEKernelModule, SecurityManager {

  private JOEKernel kernel;

  public LocalSecurityManager() {}

  /* (non-Javadoc)
   * @see com.roguelogic.joe.framework.JOEKernelModule#setKernel(com.roguelogic.joe.kernel.JOEKernel)
   */
  public void setKernel(JOEKernel kernel) {
    this.kernel = kernel;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.joe.framework.JOEKernelModule#execute()
   */
  public void execute() {
    RootShell rtSh = (RootShell) kernel.getLoadedKernelModule(RootShell.class.getName());
    rtSh.setSecurityManager(this);
  }

  public boolean login(String username, String password) {
    return false;
  }

}
