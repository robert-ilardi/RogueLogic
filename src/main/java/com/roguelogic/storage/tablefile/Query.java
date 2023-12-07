/**
 * Created Sep 18, 2007
 */
package com.roguelogic.storage.tablefile;

import java.io.Serializable;

import com.roguelogic.storage.querylang.Criterion;
import com.roguelogic.storage.querylang.InputField;
import com.roguelogic.storage.querylang.InputValue;
import com.roguelogic.storage.querylang.OutputField;
import com.roguelogic.storage.querylang.Table;

/**
 * @author Robert C. Ilardi
 *
 */

public class Query implements Serializable {

  public static final int QUERY_TYPE_UNKNOWN = 0;
  public static final int QUERY_TYPE_SELECT = 1;
  public static final int QUERY_TYPE_UPDATE = 2;
  public static final int QUERY_TYPE_DELETE = 3;

  private int type;
  private OutputField[] outputFields;
  private Table[] tables;
  private Criterion[] criteria;
  private InputField[] inputFields;
  private InputValue[] inputValues;

  public Query() {}

  public Criterion[] getCriteria() {
    return criteria;
  }

  public void setCriteria(Criterion[] critiera) {
    this.criteria = critiera;
  }

  public OutputField[] getOutputFields() {
    return outputFields;
  }

  public void setOutputFields(OutputField[] outputFields) {
    this.outputFields = outputFields;
  }

  public Table[] getTables() {
    return tables;
  }

  public void setTables(Table[] tables) {
    this.tables = tables;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public InputField[] getInputFields() {
    return inputFields;
  }

  public void setInputFields(InputField[] inputFields) {
    this.inputFields = inputFields;
  }

  public InputValue[] getInputValues() {
    return inputValues;
  }

  public void setInputValues(InputValue[] inputValues) {
    this.inputValues = inputValues;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    switch (type) {
      case QUERY_TYPE_SELECT:
        sb.append("SELECT ");
        break;
      case QUERY_TYPE_DELETE:
        sb.append("DELETE");
        break;
      case QUERY_TYPE_UPDATE:
        sb.append("UPDATE");
        break;
      default:
        sb.append("[UNKNOWN QUERY COMMAND]");
    }

    if (outputFields != null) {
      for (int i = 0; i < outputFields.length; i++) {
        if (i > 0) {
          sb.append(", ");
        }

        sb.append(outputFields[i]);
      }
    }

    if (tables != null && tables.length > 0) {
      if (type != QUERY_TYPE_UPDATE) {
        sb.append(" FROM ");
      }
      else {
        sb.append(" ");
      }

      for (int i = 0; i < tables.length; i++) {
        if (i > 0) {
          sb.append(", ");
        }

        sb.append(tables[i]);
      }
    }

    if (inputFields != null && inputValues != null) {
      sb.append(" SET ");
      for (int i = 0; i < inputFields.length; i++) {
        if (i > 0) {
          sb.append(", ");
        }

        sb.append(inputFields[i]);

        if (i < inputValues.length) {
          sb.append(" = ");
          sb.append(inputValues[i]);
        }
      }
    }

    if (criteria != null && criteria.length > 0) {
      sb.append(" WHERE ");

      for (int i = 0; i < criteria.length; i++) {
        if (i > 0) {
          sb.append(" ");
        }

        sb.append(criteria[i]);
      }
    }

    return sb.toString();
  }

}
