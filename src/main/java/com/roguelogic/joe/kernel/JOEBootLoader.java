/**
 * Created May 23, 2008
 */
package com.roguelogic.joe.kernel;

import java.io.FileInputStream;
import java.util.Properties;

import com.roguelogic.joe.Version;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class JOEBootLoader {

  private JOEKernel kernel;

  private Properties kernelProps;

  public JOEBootLoader(Properties kernelProps) {
    this.kernelProps = kernelProps;
  }

  public synchronized void boot() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    System.out.println("Booting Java Operating Environment Kernel at: " + StringUtils.GetTimeStamp());

    kernel = new JOEKernel(kernelProps);
    kernel.loadRootLogger();
    kernel.loadKernelModules();
  }

  public static void PrintWelcome() {
    System.out.print((new StringBuffer()).append("\n").append(Version.GetInfo()));
  }

  public static void main(String[] args) {
    int exitCd;
    String kernelPropsFile;
    Properties kernelProps;
    FileInputStream fis = null;
    JOEBootLoader bootLoader;

    if (args.length != 1) {
      System.err.println("Usage: java " + JOEBootLoader.class.getName() + " [KERNEL_PROPERTIES_FILE]");
      exitCd = 1;
    }
    else {
      try {
        kernelPropsFile = args[0];

        PrintWelcome();

        System.out.println("Using Kernel Properties File: " + kernelPropsFile);

        fis = new FileInputStream(kernelPropsFile);
        kernelProps = new Properties();
        kernelProps.load(fis);
        fis.close();
        fis = null;

        bootLoader = new JOEBootLoader(kernelProps);

        bootLoader.boot();

        exitCd = 0;
      } //End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
      finally {
        if (fis != null) {
          try {
            fis.close();
          }
          catch (Exception e) {}
        }
      }
    }

    System.exit(exitCd);
  }

}
