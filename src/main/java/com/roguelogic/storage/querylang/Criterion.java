/**
 * Created Sep 19, 2007
 */
package com.roguelogic.storage.querylang;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class Criterion implements QueryLangModelObject {

  private String preBoolOp;

  private String lTableHandle;
  private String lOperand;

  private String operator;

  private int rOperandType;
  private String rTableHandle;
  private String rOperand;

  public static final char[] OPERATORS_SYMBOLS_ONLY_AND_SPACE = { ' ', '=', '!', '>', '<' };

  public static final char[] OPERATORS_SYMBOLS_ONLY = { '=', '!', '>', '<' };

  public static final int RIGHT_OPERAND_TYPE_UNKNOWN = 0;
  public static final int RIGHT_OPERAND_TYPE_STRING_LITERAL = 1;
  public static final int RIGHT_OPERAND_TYPE_INTEGER_LITERAL = 2;
  public static final int RIGHT_OPERAND_TYPE_TABLE_FIELD = 3;
  public static final int RIGHT_OPERAND_TYPE_DOUBLE_LITERAL = 4;

  public Criterion() {}

  public static Criterion Parse(String s) {
    Criterion crit = new Criterion();
    String tmp;
    String[] tmpArr;
    int pos = 0;
    char ch;

    //Do We have a boolean operator?
    tmp = StringUtils.GetNextWord(s, pos, OPERATORS_SYMBOLS_ONLY_AND_SPACE).trim();

    if ("AND".equalsIgnoreCase(tmp) || "OR".equalsIgnoreCase(tmp)) {
      crit.setPreBoolOp(tmp.toUpperCase());
      pos = StringUtils.GetPositionAfterNextWord(s, pos, OPERATORS_SYMBOLS_ONLY_AND_SPACE);
    }

    //Get Left Operand
    tmp = StringUtils.GetNextWord(s, pos, OPERATORS_SYMBOLS_ONLY_AND_SPACE);
    pos = StringUtils.GetPositionAfterNextWord(s, pos, OPERATORS_SYMBOLS_ONLY_AND_SPACE);

    if (tmp.indexOf(".") != -1) {
      tmpArr = tmp.split("\\.", 2);
      tmpArr = StringUtils.Trim(tmpArr);

      crit.setLTableHandle(tmpArr[0]);
      crit.setLOperand(tmpArr[1]);
    }
    else {
      crit.setLOperand(tmp.trim());
    }

    //Get Operator
    tmp = StringUtils.GetNextWordNegativeDelimiter(s, pos, OPERATORS_SYMBOLS_ONLY);
    pos = StringUtils.GetPositionAfterNextWordNegativeDelimiter(s, pos, OPERATORS_SYMBOLS_ONLY);
    crit.setOperator(tmp.trim());

    //Get Right Operand
    ch = StringUtils.GetFirstNonWhiteSpaceChar(s, pos);

    tmp = StringUtils.GetNextWordRespectSingleQuotes(s, pos, '\\');
    pos = StringUtils.GetPositionAfterNextWordRespectSingleQuotes(s, pos, '\\');

    if (Character.isLetter(ch)) {
      crit.setROperandType(RIGHT_OPERAND_TYPE_TABLE_FIELD);

      if (tmp.indexOf(".") != -1) {
        tmpArr = tmp.split("\\.", 2);
        tmpArr = StringUtils.Trim(tmpArr);

        crit.setRTableHandle(tmpArr[0]);
        crit.setROperand(tmpArr[1]);
      }
      else {
        crit.setROperand(tmp.trim());
      }
    }
    else {
      crit.setROperand(tmp.trim());

      if (ch == '\'') {
        crit.setROperandType(RIGHT_OPERAND_TYPE_STRING_LITERAL);
      }
      else if (StringUtils.IsNumeric(crit.getROperand())) {
        crit.setROperandType(RIGHT_OPERAND_TYPE_INTEGER_LITERAL);
      }
      else if (StringUtils.IsDouble(crit.getROperand())) {
        crit.setROperandType(RIGHT_OPERAND_TYPE_DOUBLE_LITERAL);
      }
      else {
        crit.setROperandType(RIGHT_OPERAND_TYPE_UNKNOWN);
      }
    }

    return crit;
  }

  public String getLOperand() {
    return lOperand;
  }

  public void setLOperand(String operand) {
    lOperand = operand;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getROperand() {
    return rOperand;
  }

  public void setROperand(String operand) {
    rOperand = operand;
  }

  public String getLTableHandle() {
    return lTableHandle;
  }

  public void setLTableHandle(String tableHandle) {
    lTableHandle = tableHandle;
  }

  public String getPreBoolOp() {
    return preBoolOp;
  }

  public void setPreBoolOp(String preBoolOp) {
    this.preBoolOp = preBoolOp;
  }

  public String getRTableHandle() {
    return rTableHandle;
  }

  public void setRTableHandle(String tableHandle) {
    rTableHandle = tableHandle;
  }

  public int getROperandType() {
    return rOperandType;
  }

  public void setROperandType(int operandType) {
    rOperandType = operandType;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    if (!StringUtils.IsNVL(preBoolOp)) {
      if ("AND".equals(preBoolOp)) {
        sb.append("&& ");
      }
      else if ("OR".equals(preBoolOp)) {
        sb.append("|| ");
      }
    }

    if (!StringUtils.IsNVL(lTableHandle)) {
      sb.append(lTableHandle);
      sb.append("->");
    }

    sb.append(lOperand);

    sb.append(" ");
    sb.append(operator);
    sb.append(" ");

    if (!StringUtils.IsNVL(rTableHandle)) {
      sb.append(rTableHandle);
      sb.append("->");
    }

    if (rOperandType == RIGHT_OPERAND_TYPE_STRING_LITERAL) {
      sb.append("\"");
      sb.append(rOperand);
      sb.append("\"");
    }
    else {
      sb.append(rOperand);
    }

    return sb.toString();
  }

}
