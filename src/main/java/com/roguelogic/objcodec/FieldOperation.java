package com.roguelogic.objcodec;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author rilardi
 */

public class FieldOperation implements Serializable {

  private String name;
  private int operation;

  private boolean checkMods;
  private boolean modFinal;
  private boolean modStatic;

  public static final int OPERATION_INCLUDE = 0;
  public static final int OPERATION_EXCLUDE = 1;

  public FieldOperation() {}

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the operation.
   */
  public int getOperation() {
    return operation;
  }

  /**
   * @param operation The operation to set.
   */
  public void setOperation(int operation) {
    this.operation = operation;
  }

  public boolean modifiersMatch(Field field) {
    boolean match = true;
    int mod = field.getModifiers();

    match = (match && modFinal == Modifier.isFinal(mod));
    match = (match && modStatic == Modifier.isStatic(mod));

    return match;
  }

  /**
   * @return Returns the checkMods.
   */
  public boolean isCheckMods() {
    return checkMods;
  }

  /**
   * @param checkMods The checkMods to set.
   */
  public void setCheckMods(boolean checkMods) {
    this.checkMods = checkMods;
  }

  /**
   * @return Returns the modFinal.
   */
  public boolean isModFinal() {
    return modFinal;
  }

  /**
   * @param modFinal The modFinal to set.
   */
  public void setModFinal(boolean modFinal) {
    this.modFinal = modFinal;
  }

  /**
   * @return Returns the modStatic.
   */
  public boolean isModStatic() {
    return modStatic;
  }

  /**
   * @param modStatic The modStatic to set.
   */
  public void setModStatic(boolean modStatic) {
    this.modStatic = modStatic;
  }

}

