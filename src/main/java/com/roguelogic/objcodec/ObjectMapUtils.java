package com.roguelogic.objcodec;

import java.lang.reflect.Field;

/**
 * @author rilardi
 */

public class ObjectMapUtils {

  public static Field FindTargetField(Class targetClass, String fieldName) {
    Field targetField = null;
    Field[] fields = GetFields(targetClass);

    for (int i = 0; i < fields.length; i++) {
      if (fields[i].getName().equals(fieldName)) {
        targetField = fields[i];
        break;
      }
    }

    return targetField;
  }

  public static Field[] GetFields(Class targetClass) {
    Field[] fields, superFields, allFields = null;
    Class superClass;

    fields = targetClass.getDeclaredFields();

    superClass = targetClass.getSuperclass();

    if (!superClass.getName().equalsIgnoreCase("JAVA.LANG.OBJECT")) {
      //superFields = superClass.getDeclaredFields();
      superFields = GetFields(superClass);

      allFields = new Field[fields.length + superFields.length];
      System.arraycopy(fields, 0, allFields, 0, fields.length);
      System.arraycopy(superFields, 0, allFields, fields.length, superFields.length);
    }
    else {
      allFields = fields;
    }

    return allFields;
  }

  public static String GetGetterName(Field field) {
    StringBuffer getterName = new StringBuffer();
    String name, type;

    if (field != null) {
      name = field.getName();
      if (field.getType().isArray()) {
        type = field.getType().getComponentType().getName();
      }
      else {
        type = field.getType().getName();
      }

      //if (type.equalsIgnoreCase("JAVA.LANG.BOOLEAN") || type.equalsIgnoreCase("BOOLEAN")) {
      if (type.equalsIgnoreCase("BOOLEAN")) {
        getterName.append("is");
      }
      else {
        getterName.append("get");
      }

      getterName.append(Character.toUpperCase(name.charAt(0)));
      if (name.length() > 1) {
        getterName.append(name.substring(1, name.length()));
      }
    }

    return getterName.toString();
  }

  public static String GetSetterName(Field field) {
    StringBuffer setterName = new StringBuffer();
    String name;

    if (field != null) {
      name = field.getName();
      setterName.append("set");
      setterName.append(Character.toUpperCase(name.charAt(0)));
      if (name.length() > 1) {
        setterName.append(name.substring(1, name.length()));
      }
    }

    return setterName.toString();
  }

  public static boolean IsArray(String fieldName) {
    return (fieldName.indexOf("[") >= 0 && fieldName.indexOf("]") >= 0);
  }

  public static boolean IsEncapsulated(String fieldName) {
    return fieldName.indexOf(".") >= 0;
  }

  /*public static void main(String[] args) {
   Field[] fields = GetFields(Organization.class);
   for (int i = 0; i < fields.length; i++) {
   System.out.println(fields[i]);
   }
   }*/

}

