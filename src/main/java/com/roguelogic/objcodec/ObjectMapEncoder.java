package com.roguelogic.objcodec;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * @author rilardi
 */

public class ObjectMapEncoder {

  private boolean debug = false;

  public ObjectMapEncoder() {}

  public HashMap encode(Object target, String prefix, MapOptimizer optimizer, HashMap narrowsMap) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    HashMap objMap, _narrowsMap;
    HashMap prefixedObjMap = null;
    String field, value;
    Set fields;
    Iterator iter;
    StringBuffer fieldSb;

    //Get Object Map

    if (narrowsMap != null) {
      _narrowsMap = new HashMap();
    }
    else {
      _narrowsMap = null;
    }

    objMap = _encode(target, null, optimizer, _narrowsMap);

    if (prefix != null) {
      //Add the Prefix to each Field
      prefix += ":";
      prefixedObjMap = new HashMap();
      fields = objMap.keySet();
      iter = fields.iterator();

      while (iter.hasNext()) {
        field = (String) iter.next();
        value = (String) objMap.get(field);
        fieldSb = new StringBuffer(prefix);
        fieldSb.append(field);
        field = fieldSb.toString();
        fieldSb = null;

        prefixedObjMap.put(field, value);
      }

      //Add the Prefix to the Narrows Map
      if (narrowsMap != null) {
        fields = _narrowsMap.keySet();
        iter = fields.iterator();

        while (iter.hasNext()) {
          field = (String) iter.next();
          value = (String) _narrowsMap.get(field);

          fieldSb = new StringBuffer(prefix);
          fieldSb.append(field);
          field = fieldSb.toString();
          fieldSb = null;

          narrowsMap.put(field, value);
        }
      }

    } //End prefix null check
    else {
      if (narrowsMap != null) {
        narrowsMap.putAll(_narrowsMap);
      }
    }

    return prefixedObjMap != null ? prefixedObjMap : objMap;
  }

  private HashMap _encode(Object target, String targetName, MapOptimizer optimizer, HashMap narrowsMap) throws SecurityException, IllegalAccessException, InvocationTargetException {
    HashMap objMap = new HashMap();
    Field[] fields;
    Object value;
    String name, getterName;
    Method getter;
    Class targetClass;
    StringBuffer sb;
    Object[] arr;

    targetClass = target.getClass();

    if (debug) {
      System.out.println("Introspecting: " + targetClass.getName());
    }

    fields = ObjectMapUtils.GetFields(targetClass);

    for (int i = 0; i < fields.length; i++) {
      name = fields[i].getName();

      if (optimizer != null && !fieldAllowed(fields[i], targetName, optimizer)) {
        //Skip Field if NOT allowed!
        continue;
      }

      getterName = ObjectMapUtils.GetGetterName(fields[i]);

      if (getterName.length() > 0) {
        getter = null;
        try {
          if (debug) {
            System.out.println("Invoking: " + getterName);
          }
          getter = targetClass.getMethod(getterName, null);
        }
        catch (NoSuchMethodException e) {
          //Just Ignore, but we won't be able to do stores/loads on this field
          System.err.println("Warning: Field '" + name + "' can NOT be access...");
        }

        if (getter != null) {
          if (targetName != null) {
            sb = new StringBuffer();
            sb.append(targetName);
            sb.append(".");
            sb.append(name);
            name = sb.toString();
            sb = null;
          }

          value = getter.invoke(target, null);

          if (value != null) {

            if (!processBuildInType(fields[i].getType(), value, name, objMap)) {
              if (value instanceof Object[]) {
                //Recursively Introspect User Defined Objects in the Array
                arr = (Object[]) value;

                for (int j = 0; j < arr.length; j++) {
                  if (arr[j] != null) {
                    sb = new StringBuffer();
                    sb.append(name);
                    sb.append("[");
                    sb.append(j);
                    sb.append("]");

                    if (narrowsMap != null
                        && (Modifier.isAbstract(fields[i].getType().getComponentType().getModifiers()) || !fields[i].getType().getComponentType().getName().equals(arr[j].getClass().getName()))) {
                      if (debug) {
                        System.out.println("Mapping Abstract Type '" + fields[i].getType().getComponentType().getName() + "' to '" + arr[j].getClass().getName() + "'");
                      }
                      narrowsMap.put(sb.toString(), arr[j].getClass().getName());
                    }

                    if (arr[j] != null) {
                      if (!processBuildInType(arr[j].getClass(), arr[j], sb.toString(), objMap)) {
                        objMap.putAll(_encode(arr[j], sb.toString(), optimizer, narrowsMap));
                      }
                    }
                    sb = null;
                  } //End arr[j] null check
                } //end for j loop
              }
              else {
                //Recursively Introspect User Defined Object
                if (narrowsMap != null && (Modifier.isAbstract(fields[i].getType().getModifiers()) || !fields[i].getType().getName().equals(value.getClass().getName()))) {
                  if (debug) {
                    System.out.println("Mapping Abstract Type '" + fields[i].getType().getName() + "' to '" + value.getClass().getName() + "'");
                  }
                  narrowsMap.put(name, value.getClass().getName());
                }

                objMap.putAll(_encode(value, name, optimizer, narrowsMap));
              }
            } //End processBuiltInType check and processing

          } //End value != null check
        } //End getter != null check
      } //End getterName.length() check
    } //End for(i) loop through fields

    return objMap;
  }

  private String stringize(Object obj) {
    String s = null;

    if (obj != null) {
      /*if (obj instanceof Integer) {
       //Integer
       s = obj.toString();
       }
       else if (obj instanceof Character) {
       //Character
       s = obj.toString();
       }
       else if (obj instanceof Short) {
       //Short
       s = obj.toString();
       }
       else if (obj instanceof Long) {
       //Long
       s = obj.toString();
       }
       else if (obj instanceof Float) {
       //Float
       s = obj.toString();
       }
       else if (obj instanceof Double) {
       //Double
       s = obj.toString();
       }
       else {
       s = obj.toString();
       }*/

      s = obj.toString();

    } //End obj != null check

    return s;
  }

  private void arrayPut(HashMap objMap, String name, Object[] arr) {
    StringBuffer sb;

    for (int j = 0; j < arr.length; j++) {
      sb = new StringBuffer();

      sb.append(name);
      sb.append("[");
      sb.append(j);
      sb.append("]");

      objMap.put(sb.toString(), stringize(arr[j]));

      sb = null;
    }
  }

  private void arrayPut(HashMap objMap, String name, int[] arr) {
    StringBuffer sb;

    for (int j = 0; j < arr.length; j++) {
      sb = new StringBuffer();

      sb.append(name);
      sb.append("[");
      sb.append(j);
      sb.append("]");

      objMap.put(sb.toString(), String.valueOf(arr[j]));

      sb = null;
    }
  }

  private void arrayPut(HashMap objMap, String name, char[] arr) {
    StringBuffer sb;

    for (int j = 0; j < arr.length; j++) {
      sb = new StringBuffer();

      sb.append(name);
      sb.append("[");
      sb.append(j);
      sb.append("]");

      objMap.put(sb.toString(), String.valueOf(arr[j]));

      sb = null;
    }
  }

  private void arrayPut(HashMap objMap, String name, short[] arr) {
    StringBuffer sb;

    for (int j = 0; j < arr.length; j++) {
      sb = new StringBuffer();

      sb.append(name);
      sb.append("[");
      sb.append(j);
      sb.append("]");

      objMap.put(sb.toString(), String.valueOf(arr[j]));

      sb = null;
    }
  }

  private void arrayPut(HashMap objMap, String name, long[] arr) {
    StringBuffer sb;

    for (int j = 0; j < arr.length; j++) {
      sb = new StringBuffer();

      sb.append(name);
      sb.append("[");
      sb.append(j);
      sb.append("]");

      objMap.put(sb.toString(), String.valueOf(arr[j]));

      sb = null;
    }
  }

  private void arrayPut(HashMap objMap, String name, float[] arr) {
    StringBuffer sb;

    for (int j = 0; j < arr.length; j++) {
      sb = new StringBuffer();

      sb.append(name);
      sb.append("[");
      sb.append(j);
      sb.append("]");

      objMap.put(sb.toString(), String.valueOf(arr[j]));

      sb = null;
    }
  }

  private void arrayPut(HashMap objMap, String name, double[] arr) {
    StringBuffer sb;

    for (int j = 0; j < arr.length; j++) {
      sb = new StringBuffer();

      sb.append(name);
      sb.append("[");
      sb.append(j);
      sb.append("]");

      objMap.put(sb.toString(), String.valueOf(arr[j]));

      sb = null;
    }
  }

  private void arrayPut(HashMap objMap, String name, boolean[] arr) {
    StringBuffer sb;

    for (int j = 0; j < arr.length; j++) {
      sb = new StringBuffer();

      sb.append(name);
      sb.append("[");
      sb.append(j);
      sb.append("]");

      objMap.put(sb.toString(), String.valueOf(arr[j]));

      sb = null;
    }
  }

  private boolean fieldAllowed(Field field, String targetName, MapOptimizer optimizer) {
    boolean allowed = true;
    StringBuffer sb;
    String name = field.getName();

    if (targetName != null) {
      sb = new StringBuffer();
      sb.append(targetName);
      sb.append(".");
      sb.append(name);
      name = sb.toString();
      sb = null;
    }

    allowed = (optimizer.determineOperation(name, field) == FieldOperation.OPERATION_INCLUDE);

    return allowed;
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
  }

  private boolean processBuildInType(Class field, Object value, String name, HashMap objMap) {
    boolean builtInType = true;
    Date dt;
    Date[] dtArr;
    String[] sArr;
    int[] iArr;
    Integer[] iOArr;
    long[] lArr;
    Long[] lOArr;
    Character[] cOArr;
    char[] cArr;
    short[] shArr;
    Short[] shOArr;
    float[] fArr;
    Float[] fOArr;
    double[] dArr;
    Double[] dOArr;
    boolean[] boolArr;
    Boolean[] boolOArr;

    if (Object.class.equals(field) || Object[].class.equals(field)) {
      builtInType = false;
    }
    else if (value instanceof String[]) {
      //String[]
      sArr = (String[]) value;
      arrayPut(objMap, name, sArr);
    }
    else if (value instanceof String) {
      //String
      objMap.put(name, stringize(value));
    }

    else if (value instanceof char[]) {
      //char[]
      cArr = (char[]) value;
      arrayPut(objMap, name, cArr);
    }
    else if (value instanceof Character[]) {
      //Character[]
      cOArr = (Character[]) value;
      arrayPut(objMap, name, cOArr);
    }
    else if (value instanceof Character) {
      //Character
      objMap.put(name, stringize(value));
    }

    else if (value instanceof boolean[]) {
      //boolean[]
      boolArr = (boolean[]) value;
      arrayPut(objMap, name, boolArr);
    }
    else if (value instanceof Boolean[]) {
      //Boolean[]
      boolOArr = (Boolean[]) value;
      arrayPut(objMap, name, boolOArr);
    }
    else if (value instanceof Boolean) {
      //Boolean
      objMap.put(name, stringize(value));
    }

    else if (value instanceof short[]) {
      //short[]
      shArr = (short[]) value;
      arrayPut(objMap, name, shArr);
    }
    else if (value instanceof Short[]) {
      //Short[]
      shOArr = (Short[]) value;
      arrayPut(objMap, name, shOArr);
    }
    else if (value instanceof Short) {
      //Short
      objMap.put(name, stringize(value));
    }

    else if (value instanceof int[]) {
      //int[]
      iArr = (int[]) value;
      arrayPut(objMap, name, iArr);
    }
    else if (value instanceof Integer[]) {
      //Integer[]
      iOArr = (Integer[]) value;
      arrayPut(objMap, name, iOArr);
    }
    else if (value instanceof Integer) {
      //Integer
      objMap.put(name, stringize(value));
    }

    else if (value instanceof long[]) {
      //long[]
      lArr = (long[]) value;
      arrayPut(objMap, name, lArr);
    }
    else if (value instanceof Long[]) {
      //Long[]
      lOArr = (Long[]) value;
      arrayPut(objMap, name, lOArr);
    }
    else if (value instanceof Long) {
      //Long
      objMap.put(name, stringize(value));
    }

    else if (value instanceof float[]) {
      //float[]
      fArr = (float[]) value;
      arrayPut(objMap, name, fArr);
    }
    else if (value instanceof Float[]) {
      //Float[]
      fOArr = (Float[]) value;
      arrayPut(objMap, name, fOArr);
    }
    else if (value instanceof Float) {
      //Float
      objMap.put(name, stringize(value));
    }

    else if (value instanceof double[]) {
      //double[]
      dArr = (double[]) value;
      arrayPut(objMap, name, dArr);
    }
    else if (value instanceof Double[]) {
      //Double[]
      dOArr = (Double[]) value;
      arrayPut(objMap, name, dOArr);
    }
    else if (value instanceof Double) {
      //Double
      objMap.put(name, stringize(value));
    }

    else if (value instanceof Date[]) {
      //Date[]
      dtArr = (Date[]) value;
      lOArr = new Long[dtArr.length];

      for (int j = 0; j < dtArr.length; j++) {
        lOArr[j] = new Long(dtArr[j].getTime());
      }

      arrayPut(objMap, name, lOArr);
      dtArr = null;
      lOArr = null;
    }
    else if (value instanceof Date) {
      //Date
      dt = (Date) value;
      objMap.put(name, String.valueOf(dt.getTime()));
    }
    else {
      builtInType = false;
    }

    return builtInType;
  }

}

