package com.roguelogic.objcodec;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author rilardi
 */

public class MapOptimizer implements Serializable {

  private HashMap fieldOperations;
  private ArrayList globalOperations;

  public MapOptimizer() {
    fieldOperations = new HashMap();
    globalOperations = new ArrayList();
  }

  public void addFieldOperation(FieldOperation fo) {
    fieldOperations.put(fo.getName(), fo);
  }

  /*public void addFieldOperation(String name, int operation) {
   FieldOperation fo = new FieldOperation();
   fo.setName(name);
   fo.setOperation(operation);
   fieldOperations.put(name, fo);
   }*/

  public void addGlobalOperations(FieldOperation fo) {
    globalOperations.add(fo);
  }

  /*public void addGlobalOperations(String name, int operation) {
   FieldOperation fo = new FieldOperation();
   fo.setName(name);
   fo.setOperation(operation);
   globalOperations.add(fo);
   }*/

  public int determineOperation(String fieldName, Field field) {
    int operation = FieldOperation.OPERATION_INCLUDE;
    FieldOperation fo = new FieldOperation();

    //Prepare Field Name
    fieldName = removeArrayNotation(fieldName);

    //Global Operations First
    //Loop through all global operations, setting result each time
    //an Operation Applies. The overrides are in the same order
    //as the Field Operatiosn Added.
    for (int i = 0; i < globalOperations.size(); i++) {
      fo = (FieldOperation) globalOperations.get(i);
      if (fo.getName().equals("*") || fo.getName().equals(fieldName)) {
        if (fo.isCheckMods()) {
          if (fo.modifiersMatch(field)) {
            operation = fo.getOperation();
          }
        }
        else {
          operation = fo.getOperation();
        }
      }
    }

    //Field Specific Operations Override Global
    fo = (FieldOperation) fieldOperations.get(fieldName);
    if (fo != null) {
      if (fo.isCheckMods()) {
        if (fo.modifiersMatch(field)) {
          operation = fo.getOperation();
        }
      }
      else {
        operation = fo.getOperation();
      }
    }

    return operation;
  }

  private String removeArrayNotation(String fieldName) {
    StringBuffer naFieldName = new StringBuffer();
    char c;
    boolean inArr = false;

    for (int i = 0; i < fieldName.length(); i++) {
      c = fieldName.charAt(i);
      switch (c) {
      case '[':
        inArr = true;
        break;
      case ']':
        inArr = false;
        break;
      default:
        if (!inArr) {
          naFieldName.append(c);
        }
      }
    }

    return naFieldName.toString();
  }

}

