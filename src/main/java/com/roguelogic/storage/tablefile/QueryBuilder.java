/**
 * Created Sep 20, 2007
 */
package com.roguelogic.storage.tablefile;

import java.util.ArrayList;

import com.roguelogic.storage.querylang.Criterion;
import com.roguelogic.storage.querylang.InputField;
import com.roguelogic.storage.querylang.InputValue;
import com.roguelogic.storage.querylang.Keyword;
import com.roguelogic.storage.querylang.OutputField;
import com.roguelogic.storage.querylang.QueryLangModelObject;
import com.roguelogic.storage.querylang.SqlLikeParsers;
import com.roguelogic.storage.querylang.Table;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class QueryBuilder {

  public QueryBuilder() {}

  public static Query BuildQuery(String stmt) throws TableFileException {
    Query query = null;
    QueryLangModelObject[] tokens;
    String[] sTokens;
    Keyword kWord;

    //System.out.println(stmt);
    tokens = SqlLikeParsers.ParseQuery(stmt);
    sTokens = StringUtils.GetToStringArray(tokens);
    //StringUtils.PrintArray("QLTokens", sTokens);

    if (tokens != null && tokens.length > 0) {
      query = new Query();

      if (tokens.length >= 1 && tokens[0] instanceof Keyword) {
        kWord = (Keyword) tokens[0];

        if ("SELECT".equalsIgnoreCase(kWord.getKeyword())) {
          query.setType(Query.QUERY_TYPE_SELECT);
        }
        else if ("DELETE".equalsIgnoreCase(kWord.getKeyword())) {
          query.setType(Query.QUERY_TYPE_DELETE);
        }
        else if ("UPDATE".equalsIgnoreCase(kWord.getKeyword())) {
          query.setType(Query.QUERY_TYPE_UPDATE);
        }
        else {
          throw new TableFileException("Unsupport Query Type: " + kWord.getKeyword());
        }
      }
      else {
        throw new TableFileException("Query Command Expected as token[0]; found: " + sTokens[0]);
      }

      switch (query.getType()) {
        case Query.QUERY_TYPE_SELECT:
          BuildSelect(tokens, query);
          break;
        case Query.QUERY_TYPE_DELETE:
          BuildDelete(tokens, query);
          break;
        case Query.QUERY_TYPE_UPDATE:
          BuildUpdate(tokens, query);
          break;
        default:
          throw new TableFileException("Unsupport Query Type: " + query.getType());
      }
    }

    return query;
  }

  private static void BuildSelect(QueryLangModelObject[] tokens, Query query) {
    int i;
    ArrayList<OutputField> ofLst;
    OutputField outf;
    OutputField[] outfs;
    ArrayList<Table> tbLst;
    Table tab;
    Table[] tabs;
    ArrayList<Criterion> critLst;
    Criterion crit;
    Criterion[] crits;

    //Collect Output Fields
    ofLst = new ArrayList<OutputField>();

    for (i = 1; i < tokens.length; i++) {
      if (tokens[i] instanceof OutputField) {
        outf = (OutputField) tokens[i];
        ofLst.add(outf);
      }
      else {
        break;
      }
    }

    outfs = new OutputField[ofLst.size()];
    outfs = ofLst.toArray(outfs);
    query.setOutputFields(outfs);
    ofLst.clear();
    ofLst = null;

    //Collect Tables
    if (i < tokens.length && tokens[i] instanceof Keyword && "FROM".equalsIgnoreCase(((Keyword) tokens[i]).getKeyword())) {
      tbLst = new ArrayList<Table>();

      for (i++; i < tokens.length; i++) {
        if (tokens[i] instanceof Table) {
          tab = (Table) tokens[i];
          tbLst.add(tab);
        }
        else {
          break;
        }
      }

      tabs = new Table[tbLst.size()];
      tabs = tbLst.toArray(tabs);
      query.setTables(tabs);
      tbLst.clear();
      tbLst = null;
    }

    //Collect Criteria
    if (i < tokens.length && tokens[i] instanceof Keyword && "WHERE".equalsIgnoreCase(((Keyword) tokens[i]).getKeyword())) {
      critLst = new ArrayList<Criterion>();

      for (i++; i < tokens.length; i++) {
        if (tokens[i] instanceof Criterion) {
          crit = (Criterion) tokens[i];
          critLst.add(crit);
        }
        else {
          break;
        }
      }

      crits = new Criterion[critLst.size()];
      crits = critLst.toArray(crits);
      query.setCriteria(crits);
      critLst.clear();
      critLst = null;
    }
  }

  private static void BuildDelete(QueryLangModelObject[] tokens, Query query) {
    int i;
    Table[] tabs;
    ArrayList<Criterion> critLst;
    Criterion crit;
    Criterion[] crits;

    //Get Table
    i = 1;

    if (i < tokens.length && tokens[i] instanceof Keyword && "FROM".equalsIgnoreCase(((Keyword) tokens[i]).getKeyword())) {
      i++;
      tabs = new Table[1];
      tabs[0] = (Table) tokens[i];
      query.setTables(tabs);
      i++;
    }

    //Collect Criteria
    if (i < tokens.length && tokens[i] instanceof Keyword && "WHERE".equalsIgnoreCase(((Keyword) tokens[i]).getKeyword())) {
      critLst = new ArrayList<Criterion>();

      for (i++; i < tokens.length; i++) {
        if (tokens[i] instanceof Criterion) {
          crit = (Criterion) tokens[i];
          critLst.add(crit);
        }
        else {
          break;
        }
      }

      crits = new Criterion[critLst.size()];
      crits = critLst.toArray(crits);
      query.setCriteria(crits);
      critLst.clear();
      critLst = null;
    }
  }

  private static void BuildUpdate(QueryLangModelObject[] tokens, Query query) {
    int i;
    Table[] tabs;
    ArrayList<Criterion> critLst;
    Criterion crit;
    Criterion[] crits;
    ArrayList<InputField> ifLst;
    InputField iField;
    InputField[] iFields;
    ArrayList<InputValue> ivLst;
    InputValue iValue;
    InputValue[] iValues;

    //Get Table
    i = 1;

    if (i < tokens.length && tokens[i] instanceof Table) {
      tabs = new Table[1];
      tabs[0] = (Table) tokens[i];
      query.setTables(tabs);
      i++;
    }

    //Collect Input Fields, Input Values, and Criteria 
    ifLst = new ArrayList<InputField>();
    ivLst = new ArrayList<InputValue>();
    critLst = new ArrayList<Criterion>();

    for (; i < tokens.length; i++) {
      if (tokens[i] instanceof Criterion) {
        crit = (Criterion) tokens[i];
        critLst.add(crit);
      }
      else if (tokens[i] instanceof InputField) {
        iField = (InputField) tokens[i];
        ifLst.add(iField);
      }
      else if (tokens[i] instanceof InputValue) {
        iValue = (InputValue) tokens[i];
        ivLst.add(iValue);
      }
    }

    crits = new Criterion[critLst.size()];
    crits = critLst.toArray(crits);
    query.setCriteria(crits);
    critLst.clear();
    critLst = null;

    iFields = new InputField[ifLst.size()];
    iFields = ifLst.toArray(iFields);
    query.setInputFields(iFields);
    ifLst.clear();
    ifLst = null;

    iValues = new InputValue[ivLst.size()];
    iValues = ivLst.toArray(iValues);
    query.setInputValues(iValues);
    ivLst.clear();
    ivLst = null;
  }

  public static TableRecord BuildRecord(String stmt) throws TableFileException {
    TableRecord record = null;
    QueryLangModelObject[] tokens;
    String[] sTokens;
    Keyword kWord;

    //System.out.println(stmt);
    tokens = SqlLikeParsers.ParseInsert(stmt);
    sTokens = StringUtils.GetToStringArray(tokens);
    //StringUtils.PrintArray("QLTokens", sTokens);

    if (tokens.length >= 1 && tokens[0] instanceof Keyword) {
      kWord = (Keyword) tokens[0];

      if ("INSERT".equalsIgnoreCase(kWord.getKeyword())) {
        record = BuildInsertRecord(tokens);
      }
      else {
        throw new TableFileException("Unsupport Update Type: " + sTokens[0]);
      }
    }

    return record;
  }

  private static TableRecord BuildInsertRecord(QueryLangModelObject[] tokens) {
    TableRecord record;
    InputField iField;
    InputValue iValue;
    ArrayList<String> fList = new ArrayList<String>();
    ArrayList<Object> vList = new ArrayList<Object>();
    String[] fields;
    Object[] values;

    for (QueryLangModelObject qlmObj : tokens) {
      if (qlmObj instanceof InputField) {
        iField = (InputField) qlmObj;
        fList.add(iField.getName());
      }
      else if (qlmObj instanceof InputValue) {
        iValue = (InputValue) qlmObj;

        switch (iValue.getType()) {
          case InputValue.TYPE_STRING_LITERAL:
            vList.add(iValue.getValue());
            break;
          case InputValue.TYPE_INTEGER_LITERAL:
            vList.add(Long.parseLong(iValue.getValue()));
            break;
          case InputValue.TYPE_DOUBLE_LITERAL:
            vList.add(Double.parseDouble(iValue.getValue()));
        }
      }
    }

    fields = new String[fList.size()];
    fields = fList.toArray(fields);

    values = new Object[vList.size()];
    values = vList.toArray(values);

    record = new TableRecord();
    record.setFields(fields);
    record.setValues(values);

    return record;
  }

}
