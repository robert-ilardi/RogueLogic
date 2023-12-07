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

public class RootShell implements JOEKernelModule {

  private JOEKernel kernel;
  private SecurityManager secMan;

  public RootShell() {}

  /* (non-Javadoc)
   * @see com.roguelogic.joe.framework.JOEKernelModule#setKernel(com.roguelogic.joe.kernel.JOEKernel)
   */
  public void setKernel(JOEKernel kernel) {
    this.kernel = kernel;
  }

  public void setSecurityManager(SecurityManager secMan) {
    this.secMan = secMan;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.joe.framework.JOEKernelModule#execute()
   */
  public void execute() {}

}
