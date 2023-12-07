/*
 Copyright 2008 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*
 * Created on Apr 11, 2008
 */
package com.roguelogic.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author rilardi
 */

public class JBeanDescriber {

  public static final String[] BUILT_IN_DATA_TYPES = { "int", "java.lang.Integer", "short", "java.lang.Short", "long", "java.lang.Long", "String", "java.lang.String", "boolean", "java.lang.Boolean",
      "double", "java.lang.Double", "float", "java.lang.Float", "byte", "java.lang.Byte", "char", "java.lang.Character" };

  public static final String VERSION = "1.0";
  public static final String FORMAT_VERSION = "XML:1.0";

  public static final int SETTER_INDEX = 0;
  public static final int GETTER_INDEX = 1;

  private HashSet<Class> analyzedClasses;

  public JBeanDescriber() {
    analyzedClasses = new HashSet<Class>();
  }

  public String describe(String className) throws ClassNotFoundException {
    Class clazz = Class.forName(className);

    return describe(clazz);
  }

  public String describe(Object obj) {
    return describe(obj.getClass());
  }

  public synchronized String describe(Class clazz) {
    StringBuffer description = new StringBuffer();

    analyzedClasses.clear();

    description.append(generateDescriptionHeader(clazz));

    analyze(clazz, description, 1);

    description.append(generateDescriptionFooter(clazz));

    return description.toString();
  }

  private String analyze(Class clazz, StringBuffer description, int nestingLevel) {
    ArrayList<Field> fields;
    String[] jbMthdNms;
    Class type;

    analyzedClasses.add(clazz);
    description.append(generateClassHeader(clazz, nestingLevel));

    fields = getFields(clazz);

    if (fields != null) {
      for (Field f : fields) {
        jbMthdNms = generateJBeanMethodNames(f);

        description.append(generateFieldDescription(clazz, f, jbMthdNms, nestingLevel + 1));
      }

      description.append(generateClassFooter(clazz, nestingLevel));

      for (Field f : fields) {
        if (f.getType().isArray()) {
          type = f.getType().getComponentType();
        }
        else {
          type = f.getType();
        }

        if (!analyzedClasses.contains(type) && isUserDefinedClass(type)) {
          analyze(type, description, nestingLevel);
        }
      }
    }

    return description.toString();
  }

  private ArrayList<Field> getFields(Class clazz) {
    ArrayList<Field> fieldList = null, tmpLst;
    Field[] fields;
    Class superClazz;

    if (!"JAVA.LANG.OBJECT".equalsIgnoreCase(clazz.getName())) {
      fields = clazz.getDeclaredFields();
      fieldList = new ArrayList<Field>();

      for (Field f : fields) {
        if (!f.isSynthetic()) {
          fieldList.add(f);
        }
      }

      superClazz = clazz.getSuperclass();
      tmpLst = getFields(superClazz);

      if (tmpLst != null && tmpLst.size() > 0) {
        fieldList.addAll(tmpLst);
      }
    }

    return fieldList;
  }

  private String[] generateJBeanMethodNames(Field f) {
    String[] jbMthdNms = new String[2];

    jbMthdNms[SETTER_INDEX] = generateSetterName(f);
    jbMthdNms[GETTER_INDEX] = generateGetterName(f);

    return jbMthdNms;
  }

  private String generateSetterName(Field f) {
    StringBuffer setter = new StringBuffer();
    String name;

    name = f.getName();

    setter.append("set");

    setter.append(Character.toUpperCase(name.charAt(0)));
    if (name.length() > 1) {
      setter.append(name.substring(1, name.length()));
    }

    return setter.toString();
  }

  private String generateGetterName(Field f) {
    StringBuffer getter = new StringBuffer();
    String name, type;

    name = f.getName();
    if (f.getType().isArray()) {
      type = f.getType().getComponentType().getName();
    }
    else {
      type = f.getType().getName();
    }

    if ("BOOLEAN".equalsIgnoreCase(type)) {
      if (!name.startsWith("is") || name.length() <= 2 || !Character.isUpperCase(name.charAt(2))) {
        getter.append("is");

        getter.append(Character.toUpperCase(name.charAt(0)));
        if (name.length() > 1) {
          getter.append(name.substring(1, name.length()));
        }
      }
      else {
        getter.append(name);
      }
    }
    else {
      getter.append("get");

      getter.append(Character.toUpperCase(name.charAt(0)));
      if (name.length() > 1) {
        getter.append(name.substring(1, name.length()));
      }
    }

    return getter.toString();
  }

  protected String generateDescriptionHeader(Class clazz) {
    StringBuffer buf = new StringBuffer();

    buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

    buf.append("<RL-JBEAN-DESCRIBER root-jbean=\"");
    buf.append(clazz.getName());
    buf.append("\" describer-version=\"");
    buf.append(VERSION);
    buf.append("\" format-version=\"");
    buf.append(FORMAT_VERSION);
    buf.append("\">\n");

    return buf.toString();
  }

  protected String generateDescriptionFooter(Class clazz) {
    return "</RL-JBEAN-DESCRIBER>\n";
  }

  protected String generateClassHeader(Class clazz, int nestingLevel) {
    StringBuffer buf = new StringBuffer();
    String packageName, name;

    for (int i = 1; i <= nestingLevel; i++) {
      buf.append("  "); //2 whitespaces as a tab
    }

    buf.append("<JBEAN package=\"");

    if (clazz.getName().indexOf(".") >= 0) {
      packageName = clazz.getName().substring(0, clazz.getName().lastIndexOf("."));
      name = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1, clazz.getName().length());

      buf.append(packageName);
    }
    else {
      name = clazz.getName();
    }

    buf.append("\" name=\"");
    buf.append(name);
    buf.append("\">\n");

    return buf.toString();
  }

  protected String generateClassFooter(Class clazz, int nestingLevel) {
    StringBuffer buf = new StringBuffer();

    for (int i = 1; i <= nestingLevel; i++) {
      buf.append("  "); //2 whitespaces as a tab
    }

    buf.append("</JBEAN>\n");

    return buf.toString();
  }

  protected String generateFieldDescription(Class clazz, Field f, String[] jbMthdNms, int nestingLevel) {
    StringBuffer buf = new StringBuffer();
    Class type;

    for (int i = 1; i <= nestingLevel; i++) {
      buf.append("  "); //2 whitespaces as a tab
    }

    buf.append("<FIELD data-type=\"");

    type = f.getType();

    if (type.isArray()) {
      buf.append(type.getComponentType().getName());
      buf.append("\" is-array=\"TRUE\" name=\"");
    }
    else {
      buf.append(type.getName());
      buf.append("\" is-array=\"FALSE\" name=\"");
    }

    buf.append(f.getName());

    buf.append("\" setter=\"");
    buf.append(jbMthdNms[SETTER_INDEX]);

    buf.append("\" getter=\"");
    buf.append(jbMthdNms[GETTER_INDEX]);

    buf.append("\"/>\n");

    return buf.toString();
  }

  private boolean isUserDefinedClass(Class clazz) {
    boolean isUdc = true;

    for (int i = 0; i < BUILT_IN_DATA_TYPES.length; i++) {
      if (BUILT_IN_DATA_TYPES[i].equals(clazz.getName())) {
        isUdc = false;
        break;
      }
    }

    return isUdc;
  }

  public static void main(String[] args) {
    JBeanDescriber jbd;
    int exitCd;

    if (args.length != 1) {
      System.err.println("Usage: java " + JBeanDescriber.class.getName() + " [CLASS_NAME]");
      exitCd = 1;
    }
    else {
      try {
        jbd = new JBeanDescriber();
        String description = jbd.describe(args[0]);
        System.out.println(description);

        exitCd = 0;
      } //End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
    }

    System.exit(exitCd);
  }

}
