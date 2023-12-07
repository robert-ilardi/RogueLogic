/**
 * Created Aug 6, 2008
 */
package com.roguelogic.joe.kernel;

import java.util.ArrayList;
import java.util.Properties;

import com.roguelogic.joe.framework.JOEKernelModule;
import com.roguelogic.joe.framework.JOELogger;
import com.roguelogic.joe.framework.LogMessage;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class JOEKernel {

  public static final String PROP_ROOT_LOGGER = "RootLogger";
  public static final String PROP_PREFIX_KERNEL_MODULE = "KernelModule";

  private Properties kernelProps;

  private JOELogger logger;

  private ArrayList<JOEKernelModule> kernelModules;

  protected JOEKernel(Properties kernelProps) {
    this.kernelProps = kernelProps;
  }

  protected void loadRootLogger() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    String rootLoggerClass = kernelProps.getProperty(PROP_ROOT_LOGGER);

    System.out.println("Loading Root Logger: " + rootLoggerClass);

    logger = (JOELogger) Class.forName(rootLoggerClass).newInstance();

    System.out.println();
  }

  private void log(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.joeLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  private void log(Exception e) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setThrowable(e);

    if (logger != null) {
      logger.joeLogError(lMesg);
    }
    else {
      System.err.println(lMesg);
    }
  }

  protected void loadKernelModules() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    String[] orderedModNames;
    Properties kMods;
    String kmClass;
    int kmpIdx;
    JOEKernelModule kMod;

    log("Loading Kernel Modules - ");

    kernelModules = new ArrayList<JOEKernelModule>();

    kMods = StringUtils.GetPrefixedPropNames(kernelProps, PROP_PREFIX_KERNEL_MODULE);

    orderedModNames = new String[kMods.size()];

    for (Object val : kMods.keySet()) {
      String kmpName = (String) val;
      kmpIdx = Integer.parseInt(kmpName.substring(PROP_PREFIX_KERNEL_MODULE.length()));
      orderedModNames[kmpIdx] = kmpName;
    }

    for (String kmpName : orderedModNames) {
      try {
        kmClass = kMods.getProperty(kmpName);

        log((new StringBuffer()).append("Loading ").append(kmClass).toString());

        kMod = (JOEKernelModule) Class.forName(kmClass).newInstance();
        kernelModules.add(kMod);
        kMod.setKernel(this);

        kMod.execute();
      } //End try block
      catch (Exception e) {
        log(e);
      }
    }
  }

  public JOEKernelModule getLoadedKernelModule(String kmClass) {
    JOEKernelModule retKm = null;

    for (JOEKernelModule km : kernelModules) {
      if (km.getClass().getName().equals(kmClass)) {
        retKm = km;
        break;
      }
    }

    return retKm;
  }

}
