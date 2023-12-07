package com.roguelogic.objcodec;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.roguelogic.util.StringUtils;

/**
 * @author rilardi
 */

public class ObjectMapCodec {

  private boolean debug = false;

  private ObjectMapEncoder omEncoder;
  private ObjectMapDecoder omDecoder;

  public ObjectMapCodec() {
    omEncoder = new ObjectMapEncoder();
    omDecoder = new ObjectMapDecoder();
  }

  /*public HashMap encode(Object target) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
   return omEncoder.encode(target);
   }

   public HashMap encode(Object target, String prefix) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
   return omEncoder.encode(target, prefix);
   }

   public HashMap encode(Object target, MapOptimizer optimizer) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
   return omEncoder.encode(target, optimizer);
   }

   public HashMap encode(Object target, String prefix, MapOptimizer optimizer) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
   return omEncoder.encode(target, prefix, optimizer);
   }*/

  public HashMap encode(Object target, String prefix, MapOptimizer optimizer, HashMap narrowsMap) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    return omEncoder.encode(target, prefix, optimizer, narrowsMap);
  }

  /*public void decode(HashMap source, Object target) throws IllegalArgumentException, InvocationTargetException, InstantiationException, IllegalAccessException {
   omDecoder.decode(source, target);
   }

   public void decode(HashMap source, Object target, String prefix) throws IllegalArgumentException, InvocationTargetException, InstantiationException, IllegalAccessException {
   omDecoder.decode(source, target, prefix);
   }*/

  public void decode(HashMap source, Object target, String prefix, HashMap narrowsMap) throws IllegalArgumentException, InvocationTargetException, InstantiationException, IllegalAccessException,
      ClassNotFoundException {
    omDecoder.decode(source, target, prefix, narrowsMap);
  }

  public boolean verifyCompatibility(Object target) throws SecurityException, IllegalArgumentException, InvocationTargetException, InstantiationException, IllegalAccessException,
      ClassNotFoundException {
    return verifyCompatibility(target, null);
  }

  public boolean verifyCompatibility(Object target, MapOptimizer optimizer) throws SecurityException, IllegalArgumentException, InvocationTargetException, InstantiationException,
      IllegalAccessException, ClassNotFoundException {
    boolean compatible = true;
    HashMap objMap, objMapCopy, narrowMap;
    Object targetCopy;
    Set varNames;
    Iterator iter;
    String varName, varVal, varValCopy;

    //Create Copy of Target Object through Codec
    narrowMap = new HashMap();
    objMap = encode(target, null, optimizer, narrowMap);

    if (debug) {
      System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
      StringUtils.PrintMap("Original Object Map", objMap);
    }

    targetCopy = target.getClass().newInstance();
    decode(objMap, targetCopy, null, narrowMap);
    objMapCopy = encode(targetCopy, null, optimizer, narrowMap);

    if (debug) {
      System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
      StringUtils.PrintMap("Introspectively Cloned Object Map", objMapCopy);
    }

    //Compare Object Maps
    varNames = objMap.keySet();
    iter = varNames.iterator();

    while (iter.hasNext()) {
      varName = (String) iter.next();
      varVal = (String) objMap.get(varName);
      varValCopy = (String) objMapCopy.get(varName);

      if ((varVal == null && varValCopy == null) || (varVal != null && !varVal.equals(varValCopy))) {
        if (debug) {
          System.out.println("\nTarget Object is NOT Compatible at variable '" + varName + "' : " + varVal + " != " + varValCopy + "\n");
        }
        compatible = false;
        break;
      }
    }

    if (debug) {
      if (compatible) {
        System.out.println("Target Object is Compatible!");
      }
      else {
        System.out.println("Target Object is NOT Compatible!");
      }
    }

    return compatible;
  }

  /**
   * @return Returns the debug.
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * @param debug The debug to set.
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
    omEncoder.setDebug(debug);
    omDecoder.setDebug(debug);
  }

  /*public static void main(String[] args) throws Throwable {
   ObjectMapCodec codec = new ObjectMapCodec();
   NARequest naReq = (NARequest) SystemUtils.LoadObject("C:/Domain/EAM/NARequest.obj");
   MapOptimizer optimizer;
   FieldOperation fo;

   codec.setDebug(true);
   optimizer = new MapOptimizer();

   //Ignore Static Final Fields (Constants)
   fo = new FieldOperation();
   fo.setName("*");
   fo.setCheckMods(true);
   fo.setModStatic(true);
   fo.setModFinal(true);
   fo.setOperation(FieldOperation.OPERATION_EXCLUDE);
   optimizer.addGlobalOperations(fo);

   fo = new FieldOperation();
   fo.setName("accountMap");
   fo.setOperation(FieldOperation.OPERATION_EXCLUDE);
   optimizer.addFieldOperation(fo);

   fo = new FieldOperation();
   fo.setName("clientMap");
   fo.setOperation(FieldOperation.OPERATION_EXCLUDE);
   optimizer.addFieldOperation(fo);

   fo = new FieldOperation();
   fo.setName("user.groups");
   fo.setOperation(FieldOperation.OPERATION_EXCLUDE);
   optimizer.addFieldOperation(fo);

   System.out.println("Is Object Compatible? " + (codec.verifyCompatibility(naReq, optimizer) ? "YES" : "NO"));
   }*/

}

