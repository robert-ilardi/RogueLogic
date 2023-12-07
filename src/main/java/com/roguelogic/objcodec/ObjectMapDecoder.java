package com.roguelogic.objcodec;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.roguelogic.util.AutoSizeArray;

/**
 * @author rilardi
 */

public class ObjectMapDecoder {

  private boolean debug = false;

  public static final String ROOT_OBJECT = "<ROOT-OBJECT>";

  public ObjectMapDecoder() {}

  public void decode(HashMap source, Object target, String prefix, HashMap narrowUdoMap) throws IllegalArgumentException, InvocationTargetException, InstantiationException, IllegalAccessException,
      ClassNotFoundException {
    HashMap filteredSource = new HashMap();
    HashMap filteredNarrowUdoMap = null;
    String field, value;
    Set fields;
    Iterator iter;

    if (prefix != null) {
      prefix += ":";

      //Filter Source Map
      fields = source.keySet();
      iter = fields.iterator();

      while (iter.hasNext()) {
        field = (String) iter.next();
        if (field.startsWith(prefix)) {
          value = (String) source.get(field);
          field = field.substring(field.indexOf(":") + 1, field.length());
          filteredSource.put(field, value);
        }
      }

      //Filter Narrow Udo Map
      if (narrowUdoMap != null) {
        filteredNarrowUdoMap = new HashMap();
        fields = narrowUdoMap.keySet();
        iter = fields.iterator();

        while (iter.hasNext()) {
          field = (String) iter.next();
          if (field.startsWith(prefix)) {
            value = (String) narrowUdoMap.get(field);
            field = field.substring(field.indexOf(":") + 1, field.length());
            filteredNarrowUdoMap.put(field, value);
          }
        }
      }

      //Decode the Object
      decode(filteredSource, target, filteredNarrowUdoMap);
    }
    else {
      decode(source, target, narrowUdoMap);
    }
  }

  public void decode(HashMap source, Object target, HashMap narrowUdoMap) throws IllegalArgumentException, InvocationTargetException, InstantiationException, IllegalAccessException,
      ClassNotFoundException {
    Set srcFields;
    Iterator iter;
    String srcField, srcValue;
    String[] tokens;
    ObjectTreeNode root, node;

    //Create Object Tree
    root = new ObjectTreeNode();
    root.setName(ROOT_OBJECT);

    srcFields = source.keySet();
    iter = srcFields.iterator();

    while (iter.hasNext()) {
      srcField = (String) iter.next();

      if (ObjectMapUtils.IsEncapsulated(srcField)) {
        tokens = srcField.split("\\.");
        if (tokens.length > 0) {
          node = root.findBuild(tokens);
          srcValue = (String) source.get(srcField);
          node.addPrimitive(tokens[tokens.length - 1], srcValue);
        }
      }
      else {
        srcValue = (String) source.get(srcField);
        root.addPrimitive(srcField, srcValue);
      }
    } //End while iter.hasNext

    //Start Decoding from Object Tree to Target Object
    if (debug) {
      System.out.println(root.toString(0));
    }
    decode(target, root, null, narrowUdoMap);
  }

  private void decode(Object target, ObjectTreeNode root, String parentName, HashMap narrowUdoMap) throws IllegalArgumentException, InvocationTargetException, InstantiationException,
      IllegalAccessException, ClassNotFoundException {
    String[] primitiveNames, arr;
    String setterName, scalar, udoName;
    Class targetClass, arrType, targetFieldClass, trueType;
    Method setter;
    Field targetField;
    Object objVal, udoInstance;
    HashMap userDefObjs;
    Set udoNames;
    Iterator iter;
    AutoSizeArray udoArr;
    ObjectTreeNode udo;
    Object[] otnArr, objArr;
    StringBuffer sb;
    String fqTargetName;

    //Process Root Primatives
    targetClass = target.getClass();
    primitiveNames = root.getPrimitiveNames();

    if (debug) {
      if (root.getArrayIndex() == -1) {
        System.out.println("Processing Object: " + root.getName());
      }
      else {
        System.out.println("Processing Object: " + root.getName() + "[" + root.getArrayIndex() + "]");
      }
    }

    //Loop through all primitive fields restoring the state for each.
    //The restore is a best try. If the field cannot be restored,
    //but not reflection exception has occurred, then
    //the decoding will continue.
    for (int i = 0; i < primitiveNames.length; i++) {
      setter = null;
      objVal = null;
      targetField = ObjectMapUtils.FindTargetField(targetClass, primitiveNames[i]);

      //Obtain Reflection Reference to Setter Method for field primitiveNames[i]
      if (targetField != null) {
        try {
          setterName = ObjectMapUtils.GetSetterName(targetField);
          if (debug) {
            System.out.println("Invoking: " + setterName);
          }
          setter = targetClass.getMethod(setterName, new Class[] { targetField.getType() });
        }
        catch (NoSuchMethodException e) {
          //Just Ignore, but we won't be able to do stores/loads on this field
          System.err.println("Warning: Field '" + primitiveNames[i] + "' can NOT be access...");
        }

        if (setter != null) {
          //Narrow or Type-Cast String representation to the Actual Object
          if (root.isPrimitiveArray(primitiveNames[i])) {
            //Array
            arr = root.getArray(primitiveNames[i]);
            trueType = getTrueTargetType(targetField.getType(), narrowUdoMap, parentName, primitiveNames[i], i);
            objVal = narrow(arr, trueType);
          }
          else {
            //Scalar
            scalar = root.getScalar(primitiveNames[i]);
            if (scalar != null) {
              trueType = getTrueTargetType(targetField.getType(), narrowUdoMap, parentName, primitiveNames[i]);
              objVal = narrow(scalar, trueType);
            }
          } //End Is Primitive Array Check ELSE-BLOCK

          //Invoke the Setter Method restoring the Object's State for the primitiveNames[i] field
          if (objVal != null) {
            setter.invoke(target, new Object[] { objVal });
          }
        } //End NULL setter check
      } //End NULL targetField check
    } //End for(i) through primitive names

    //Process Encapsulated User Defined Objects (Java Beans)
    userDefObjs = root.getChildren();
    udoNames = userDefObjs.keySet();
    iter = udoNames.iterator();

    //Loop through each Scalar/Array User Defined Object
    while (iter.hasNext()) {
      objVal = null;
      setter = null;
      udoName = (String) iter.next();
      udoArr = (AutoSizeArray) userDefObjs.get(udoName);
      otnArr = udoArr.getObjectArray();

      //Create a Reference to this User Defined Object
      //and set in to the target object

      //Obtain a Reflection Reference to the UDO's Setter method on the target object
      targetField = ObjectMapUtils.FindTargetField(targetClass, udoName);
      if (targetField != null) {

        fqTargetName = targetField.getName();

        if (parentName != null) {
          sb = new StringBuffer();
          sb.append(parentName);
          sb.append(".");
          sb.append(fqTargetName);
          fqTargetName = sb.toString();
          sb = null;
        }

        setterName = ObjectMapUtils.GetSetterName(targetField);
        if (debug) {
          System.out.println("Invoking: " + setterName);
        }

        //Try "NARROWED" Type First...
        if (narrowUdoMap != null && narrowUdoMap.containsKey(fqTargetName)) {
          try {
            targetFieldClass = Class.forName((String) narrowUdoMap.get(fqTargetName));
            setter = targetClass.getMethod(setterName, new Class[] { targetFieldClass });
          }
          catch (NoSuchMethodException e) {}
        }

        //If setter not found try "SPECIFIED" Type Second...
        if (setter == null) {
          try {
            targetFieldClass = targetField.getType();
            setter = targetClass.getMethod(setterName, new Class[] { targetFieldClass });
          }
          catch (NoSuchMethodException e) {
            //Just Ignore, but we won't be able to do stores/loads on this UDO field
            System.err.println("Warning: User Defined Object Field '" + udoName + "' can NOT be access...");
          }
        }

        if (setter != null) {
          //Create a new instance of this UDO or an array of it
          //if (otnArr.length == 1) {
          if (!targetField.getType().isArray()) {
            //Scalar UDO
            //If it is a scalar field, then it in array index = 0 of objArr
            udo = (ObjectTreeNode) otnArr[0];
            udoInstance = obtainUDOInstance(targetField, fqTargetName, narrowUdoMap);

            setter.invoke(target, new Object[] { udoInstance });
            decode(udoInstance, udo, fqTargetName, narrowUdoMap);
          } //End if (objArr.length == 1)
          else {
            //else if (otnArr.length > 1) {
            //Array UDO
            //Create Array Instance and set it!

            udoInstance = obtainUDOArrayInstance(targetField, otnArr.length, fqTargetName, narrowUdoMap);
            setter.invoke(target, new Object[] { udoInstance });

            //Loop through all array elements of a specific User Defined Object
            objArr = (Object[]) udoInstance;
            for (int i = 0; i < otnArr.length; i++) {
              udo = (ObjectTreeNode) otnArr[i];

              if (udo != null) {
                //Get Fully Qualified Array Element Name
                sb = new StringBuffer();
                sb.append(fqTargetName);
                sb.append("[");
                sb.append(i);
                sb.append("]");

                objArr[i] = obtainUDOInstance(targetField, sb.toString(), narrowUdoMap);
                //sb = null;
                //decode(objArr[i], udo, fqTargetName, narrowUdoMap);
                decode(objArr[i], udo, sb.toString(), narrowUdoMap);
                sb = null;
              } //End null udo check
            } //End for(i) through objArr
          } //End else if (objArr.length > 1)
        } //End NULL setter check 
      } //End NULL targetField check
    } //End while iter.hasNext

  }

  private Object narrow(String sVal, Class objType) {
    Object obj = null;
    String typeName = objType.getName();

    try {
      if (typeName.equalsIgnoreCase("JAVA.LANG.INTEGER") || typeName.equalsIgnoreCase("INT")) {
        obj = new Integer(sVal);
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.SHORT") || typeName.equalsIgnoreCase("SHORT")) {
        obj = new Short(sVal);
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.FLOAT") || typeName.equalsIgnoreCase("FLOAT")) {
        obj = new Float(sVal);
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.DOUBLE") || typeName.equalsIgnoreCase("DOUBLE")) {
        obj = new Double(sVal);
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.LONG") || typeName.equalsIgnoreCase("LONG")) {
        obj = new Long(sVal);
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.BOOLEAN") || typeName.equalsIgnoreCase("BOOLEAN")) {
        obj = new Boolean("TRUE".equalsIgnoreCase(sVal.trim()));
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.CHARACTER") || typeName.equalsIgnoreCase("CHAR")) {
        obj = new Character(sVal.trim().charAt(0));
      }
      else if (typeName.equalsIgnoreCase("JAVA.UTIL.DATE")) {
        obj = new Date(Long.parseLong(sVal));
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.STRING")) {
        obj = sVal;
      }
      else {
        //Unsupported Type
        obj = null;
      }
    } //End try Block
    catch (Exception e) {
      //Ignore Data-Type Conversion Exceptions for best chance at loading an Object! 
      System.err.println("!!!!!!!!!!!> ERROR Converting Data to Java Object - Ignoring Value: " + sVal);
      e.printStackTrace();
      obj = null;
    }

    return obj;
  }

  private Object narrow(String[] sVals, Class objType) {
    Object obj = null;
    Object[] arr = null;
    int[] iArr;
    short[] shArr;
    float[] fArr;
    double[] dArr;
    long[] lArr;
    boolean[] boolArr;
    char[] cArr;
    String typeName;

    try {
      if (objType.isArray()) {
        typeName = objType.getComponentType().getName();
      }
      else {
        typeName = objType.getName();
      }

      if (typeName.equalsIgnoreCase("JAVA.LANG.INTEGER")) {
        arr = new Integer[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          arr[i] = new Integer(sVals[i]);
        }
        obj = arr;
      }
      else if (typeName.equalsIgnoreCase("INT")) {
        iArr = new int[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          iArr[i] = Integer.parseInt(sVals[i]);
        }
        obj = iArr;
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.SHORT")) {
        arr = new Short[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          arr[i] = new Short(sVals[i]);
        }
        obj = arr;
      }
      else if (typeName.equalsIgnoreCase("SHORT")) {
        shArr = new short[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          shArr[i] = Short.parseShort(sVals[i]);
        }
        obj = shArr;
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.FLOAT")) {
        arr = new Float[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          arr[i] = new Float(sVals[i]);
        }
        obj = arr;
      }
      else if (typeName.equalsIgnoreCase("FLOAT")) {
        fArr = new float[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          fArr[i] = Float.parseFloat(sVals[i]);
        }
        obj = fArr;
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.DOUBLE")) {
        arr = new Double[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          arr[i] = new Double(sVals[i]);
        }
        obj = arr;
      }
      else if (typeName.equalsIgnoreCase("DOUBLE")) {
        dArr = new double[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          dArr[i] = Double.parseDouble(sVals[i]);
        }
        obj = dArr;
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.LONG")) {
        arr = new Long[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          arr[i] = new Long(sVals[i]);
        }
        obj = arr;
      }
      else if (typeName.equalsIgnoreCase("LONG")) {
        lArr = new long[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          lArr[i] = Long.parseLong(sVals[i]);
        }
        obj = lArr;
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.BOOLEAN")) {
        arr = new Boolean[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          arr[i] = new Boolean(sVals[i].trim());
        }
        obj = arr;
      }
      else if (typeName.equalsIgnoreCase("BOOLEAN")) {
        boolArr = new boolean[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          boolArr[i] = "TRUE".equalsIgnoreCase(sVals[i].trim());
        }
        obj = boolArr;
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.CHARACTER")) {
        arr = new Character[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          arr[i] = new Character(sVals[i].trim().charAt(0));
        }
        obj = arr;
      }
      else if (typeName.equalsIgnoreCase("CHAR")) {
        cArr = new char[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          cArr[i] = sVals[i].trim().charAt(0);
        }
        obj = cArr;
      }
      else if (typeName.equalsIgnoreCase("JAVA.UTIL.DATE")) {
        arr = new Date[sVals.length];
        for (int i = 0; i < sVals.length; i++) {
          arr[i] = new Date(Long.parseLong(sVals[i]));
        }
        obj = arr;
      }
      else if (typeName.equalsIgnoreCase("JAVA.LANG.STRING")) {
        obj = sVals;
      }
      else {
        //Unsupported Type
        obj = null;
      }
    } //End try Block
    catch (Exception e) {
      //Ignore Data-Type Conversion Exceptions for best chance at loading an Object! 
      System.err.println("!!!!!!!!!!!> ERROR Converting Data to Java Object Array - Ignoring Values -");

      for (int i = 0; sVals != null && i < sVals.length; i++) {
        System.err.println("Value[" + i + "] : " + sVals[i]);
      }

      e.printStackTrace();
      obj = null;
    }

    return obj;
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

  private Object obtainUDOInstance(Field targetField, String fqTargetName, HashMap narrowUdoMap) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    Object udoInstance = null;
    String specificName;

    specificName = (String) narrowUdoMap.get(fqTargetName);
    if (specificName != null) {
      if (debug) {
        System.out.println("Mapping Abstract Type '" + targetField.getType().getName() + "' to '" + specificName + "'");
      }
      udoInstance = Class.forName(specificName).newInstance();
    }
    else {
      if (targetField.getType().isArray()) {
        udoInstance = targetField.getType().getComponentType().newInstance();
      }
      else {
        udoInstance = targetField.getType().newInstance();
      }
    }

    return udoInstance;
  }

  private Object obtainUDOArrayInstance(Field targetField, int length, String fqTargetName, HashMap narrowUdoMap) throws NegativeArraySizeException, ClassNotFoundException {
    Object udoInstance = null;
    String specificName;

    specificName = (String) narrowUdoMap.get(fqTargetName);
    if (specificName != null) {
      if (debug) {
        System.out.println("Mapping Abstract Type '" + targetField.getType().getComponentType().getName() + "' to '" + specificName + "'");
      }
      udoInstance = Array.newInstance(Class.forName(specificName), length);
    }
    else {
      udoInstance = Array.newInstance(targetField.getType().getComponentType(), length);
    }

    return udoInstance;
  }

  private Class getTrueTargetType(Class assumedType, HashMap narrowUdoMap, String parentName, String primitiveNames, int index) throws ClassNotFoundException {
    Class trueType = null;
    String baseType, specificType;
    StringBuffer sb;

    baseType = assumedType.getComponentType().getName();

    if (baseType.equalsIgnoreCase("JAVA.LANG.OBJECT")) {
      sb = new StringBuffer();
      sb.append(parentName);
      sb.append(".");
      sb.append(primitiveNames);
      sb.append("[");
      sb.append(index);
      sb.append("]");

      specificType = (String) narrowUdoMap.get(sb.toString());
      if (specificType != null) {
        if (debug) {
          System.out.println("Mapping Base Type '" + baseType + "' to '" + specificType + "'");
        }
        trueType = Class.forName(specificType);
      }
      else {
        trueType = assumedType;
      }
    }
    else {
      trueType = assumedType;
    }

    return trueType;
  }

  private Class getTrueTargetType(Class assumedType, HashMap narrowUdoMap, String parentName, String primitiveNames) throws ClassNotFoundException {
    Class trueType = null;
    String baseType, specificType;
    StringBuffer sb;

    baseType = assumedType.getName();

    if (baseType.equalsIgnoreCase("JAVA.LANG.OBJECT")) {
      sb = new StringBuffer();
      sb.append(parentName);
      sb.append(".");
      sb.append(primitiveNames);

      specificType = (String) narrowUdoMap.get(sb.toString());
      if (specificType != null) {
        if (debug) {
          System.out.println("Mapping Base Type '" + baseType + "' to '" + specificType + "'");
        }
        trueType = Class.forName(specificType);
      }
      else {
        trueType = assumedType;
      }
    }
    else {
      trueType = assumedType;
    }

    return trueType;
  }

}

