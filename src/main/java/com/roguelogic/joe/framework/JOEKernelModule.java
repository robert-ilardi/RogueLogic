/**
 * Created Aug 6, 2008
 */
package com.roguelogic.joe.framework;

import com.roguelogic.joe.kernel.JOEKernel;

/**
 * @author Robert C. Ilardi
 *
 */

public interface JOEKernelModule {
  public void setKernel(JOEKernel kernel);

  public void execute();
}
