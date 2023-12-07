/**
 * Created Sep 14, 2007
 */
package com.roguelogic.storage.tablefile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import com.roguelogic.storage.querylang.Criterion;
import com.roguelogic.storage.querylang.InputField;
import com.roguelogic.storage.querylang.InputValue;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class TableFile {

  public static final String HEADER = "$RL-TableFile$";
  public static final String[] SUPPORTED_VERSIONS = { "1.0" };
  public static final String IMPL_VERSION = "1.0";

  public static final byte ACTIVE_ROW_BYTE = "A".getBytes()[0];
  public static final byte DELETED_ROW_BYTE = "D".getBytes()[0];
  public static final byte NEW_LINE_BYTE = 10;

  public static final int TABLE_HEADER_LINES_V1_0 = 4;

  private TableDefinition definition;

  private HashMap<String, HashMap<Object, Long>> memoryIndexes;

  public TableFile() {}

  public TableFile(TableDefinition definition) {
    this.definition = definition;
  }

  public synchronized void load(RandomAccessFile raf) throws IOException, TableFileException {
    String ver;

    checkHeader(raf); //Throws Exception if invalid File Header

    ver = checkVersionSupport(raf); //Throws Exception if unsupported Format Version

    if ("1.0".equals(ver)) {
      _load1_0(raf);
    }
    else {
      throw new TableFileException("Version " + ver + " Support NOT Implemented!");
    }
  }

  public void store(RandomAccessFile raf) throws IOException, TableFileException {
    String ver;

    ver = checkVersionSupport(definition); //Throws Exception if unsupported Format Version

    if ("1.0".equals(ver)) {
      _store1_0(raf);
    }
    else {
      throw new TableFileException("Version " + ver + " Support NOT Implemented!");
    }
  }

  private void _load1_0(RandomAccessFile raf) throws IOException, TableFileException {
    String fieldDefsLine, name;
    String[] fieldDefs, fieldDefTokens, tokens;
    TFField field;
    TFField[] fields;
    int startIndex, endIndex;

    name = getNextNonBlankLine(raf);

    if (StringUtils.IsNVL(name)) {
      throw new TableFileException("Corrupted TableFile Data Stream! - Name Missing.");
    }

    tokens = name.split("=");
    tokens = StringUtils.Trim(tokens);

    if (tokens != null && tokens.length == 2 && "Name".equals(tokens[0]) && !StringUtils.IsNVL(tokens[1])) {
      name = tokens[1].trim();
    }
    else {
      throw new TableFileException("Corrupted TableFile Data Stream! - Name Missing.");
    }

    fieldDefsLine = getNextNonBlankLine(raf);

    if (StringUtils.IsNVL(fieldDefsLine)) {
      throw new TableFileException("Corrupted TableFile Data Stream! - Field Definitions Missing.");
    }

    tokens = fieldDefsLine.split("=");
    tokens = StringUtils.Trim(tokens);

    if (tokens != null && tokens.length == 2 && "Fields".equals(tokens[0]) && !StringUtils.IsNVL(tokens[1])) {
      tokens[1] = tokens[1].trim();
    }
    else {
      throw new TableFileException("Corrupted TableFile Data Stream! - Field Definitions Missing.");
    }

    fieldDefs = tokens[1].split("\\|");
    fields = new TFField[fieldDefs.length];

    startIndex = 0;
    endIndex = 0;

    for (String fieldDef : fieldDefs) {
      fieldDefTokens = fieldDef.split(";");

      if (fieldDefTokens.length != 4) {
        throw new TableFileException("Corrupted TableFile Data Stream! - Field Definition '" + fieldDefTokens[0] + "' is Invalid.");
      }

      field = new TFField();

      field.setName(fieldDefTokens[0]);
      field.setIndex(Integer.parseInt(fieldDefTokens[1]));

      if ("String".equals(fieldDefTokens[2])) {
        field.setType(FieldTypes.String);
        field.setLength(Integer.parseInt(fieldDefTokens[3]));
      }
      else if ("Integer".equals(fieldDefTokens[2])) {
        field.setType(FieldTypes.Integer);
        field.setLength(String.valueOf(Long.MAX_VALUE).length());
      }
      else if ("Double".equals(fieldDefTokens[2])) {
        field.setType(FieldTypes.Double);
        field.setLength(String.valueOf(Double.MAX_VALUE).length());
      }
      else {
        throw new TableFileException("Corrupted TableFile Data Stream! - Unsupported Field Data Type '" + fieldDefTokens[2] + "' Found.");
      }

      startIndex = endIndex;
      endIndex += field.getLength();

      field.setStartIndex(startIndex);
      field.setEndIndex(endIndex);

      fields[field.getIndex()] = field;
    }

    definition = new TableDefinition();
    definition.setVersion(IMPL_VERSION);
    definition.setName(name);
    definition.setFields(fields);
  }

  private void _store1_0(RandomAccessFile raf) throws IOException {
    TFField[] fields;
    StringBuffer sb;

    //Write Header
    sb = new StringBuffer();
    sb.append(HEADER);
    sb.append("\n");
    raf.write(sb.toString().getBytes());

    //Write Version
    sb = new StringBuffer();
    sb.append("Version=");
    sb.append(definition.getVersion());
    sb.append("\n");
    raf.write(sb.toString().getBytes());

    //Write Table Name
    sb = new StringBuffer();
    sb.append("Name=");
    sb.append(definition.getName());
    sb.append("\n");
    raf.write(sb.toString().getBytes());

    //Write Field Definition
    sb = new StringBuffer();
    sb.append("Fields=");

    fields = definition.getFields();

    for (int i = 0; i < fields.length; i++) {
      if (i > 0) {
        sb.append("|");
      }

      sb.append(fields[i].getName());
      sb.append(";");
      sb.append(fields[i].getIndex());
      sb.append(";");
      sb.append(fields[i].getType().getName());
      sb.append(";");
      sb.append(fields[i].getLength());
    }

    sb.append("\n");
    raf.write(sb.toString().getBytes());
  }

  public synchronized void close() {
    definition = null;
  }

  public String getName() {
    return (definition != null ? definition.getName() : null);
  }

  private void checkHeader(RandomAccessFile raf) throws TableFileException, IOException {
    String line;

    line = getNextNonBlankLine(raf);

    if (line != null) {
      line = line.trim();
    }

    if (!HEADER.equals(line)) {
      throw new TableFileException("Invalid TableFile Data Stream!");
    }
  }

  private String checkVersionSupport(RandomAccessFile raf) throws TableFileException, IOException {
    String line;
    boolean supported = false;
    String retVer = null;
    String[] tokens;

    line = getNextNonBlankLine(raf);

    if (line != null) {
      tokens = line.split("=");
      tokens = StringUtils.Trim(tokens);

      if (tokens != null && "Version".equals(tokens[0]) && tokens.length == 2) {
        for (String ver : SUPPORTED_VERSIONS) {
          if (ver.equals(tokens[1])) {
            supported = true;
            retVer = ver;
            break;
          }
        }
      }
    }

    if (!supported) {
      throw new TableFileException("Unsupported TableFile Format Version!");
    }

    return retVer;
  }

  private String checkVersionSupport(TableDefinition def) throws TableFileException, IOException {
    boolean supported = false;
    String retVer = null;

    if (def != null) {
      for (String ver : SUPPORTED_VERSIONS) {
        if (ver.equals(def.getVersion())) {
          supported = true;
          retVer = ver;
          break;
        }
      }
    }

    if (!supported) {
      throw new TableFileException("Unsupported TableFile Format Version!");
    }

    return retVer;
  }

  private String getNextNonBlankLine(RandomAccessFile raf) throws IOException {
    String line = null;

    line = raf.readLine();

    while (line != null) {
      if (!StringUtils.IsNVL(line)) {
        break;
      }

      line = raf.readLine();
    }

    return line;
  }

  public void insert(RandomAccessFile raf, TableRecord rec) throws IOException, TableFileException {
    if (definition == null || raf == null || rec == null) {
      return;
    }

    if ("1.0".equals(definition.getVersion())) {
      _insert1_0(raf, rec);
    }
    else {
      throw new TableFileException("Version " + definition.getVersion() + " Insert Support NOT Implemented!");
    }
  }

  private void _insert1_0(RandomAccessFile raf, TableRecord rec) throws IOException, TableFileException {
    byte[] bRec, buf;
    long pos;

    //Get Data Record Bytes
    bRec = rec.toByteArray(definition);

    //Add Table File Row Header
    buf = new byte[bRec.length + 2];
    System.arraycopy(bRec, 0, buf, 1, bRec.length);
    buf[0] = ACTIVE_ROW_BYTE;
    buf[buf.length - 1] = NEW_LINE_BYTE;

    pos = findFirstDeletedRecord1_0(raf);

    if (pos == -1) {
      raf.seek(raf.length()); //Append at end of file
    }
    else {
      raf.seek(pos); //Overwrite previously deleted record
    }

    raf.write(buf);
  }

  private long findFirstDeletedRecord1_0(RandomAccessFile raf) throws IOException {
    long retPos = -1, curPos;
    String line;
    int recLen = definition.getRecordLen();

    //Skip Header Lines
    for (int i = 1; i <= TABLE_HEADER_LINES_V1_0; i++) {
      raf.readLine();
    }

    curPos = raf.getFilePointer();
    line = raf.readLine();

    while (line != null) {
      if (line.length() == recLen + 1 && line.charAt(0) == 'D') {
        retPos = curPos;
        break;
      }

      curPos = raf.getFilePointer();
      line = raf.readLine();
    }

    return retPos;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    if (definition != null) {
      sb.append("Table: ");
      sb.append(definition.getName());

      sb.append("\n-----Fields-----\n");

      for (TFField field : definition.getFields()) {
        if (field.getIndex() != 0) {
          sb.append("----------\n");
        }

        sb.append(field);
      }
    }
    else {
      sb.append("[NULL Table Definition]");
    }

    return sb.toString();
  }

  public int delete(RandomAccessFile raf, Query q) throws IOException, TableFileException {
    int recCnt = 0;

    if (definition == null || raf == null || q == null) {
      return 0;
    }

    if ("1.0".equals(definition.getVersion())) {
      recCnt = _delete1_0(raf, q);
    }
    else {
      throw new TableFileException("Version " + definition.getVersion() + " Delete Support NOT Implemented!");
    }

    return recCnt;
  }

  private int _delete1_0(RandomAccessFile raf, Query q) throws IOException {
    int recCnt = 0;
    TableRecord[] records;

    records = findRecords1_0(raf, q, true);

    for (TableRecord rec : records) {
      System.out.println("Deleting: " + rec);

      //Write DELETED ROW Marker Byte to the position of the record...
      raf.seek(rec.getPosition());
      raf.write(DELETED_ROW_BYTE);

      recCnt++;
    }

    return recCnt;
  }

  private TableRecord[] findRecords1_0(RandomAccessFile raf, Query q, boolean dataOnly) throws IOException {
    TableRecord[] records;
    ArrayList<TableRecord> recLst;
    long pos;
    int cnt, recLen;
    String line;
    TableRecord rec;

    recLen = definition.getRecordLen();
    recLst = new ArrayList<TableRecord>();

    //Skip Header Lines
    for (int i = 1; i <= TABLE_HEADER_LINES_V1_0; i++) {
      raf.readLine();
    }

    //Read Data Records
    pos = raf.getFilePointer();
    line = raf.readLine();

    while (line != null) {
      //System.out.println(line + " at " + pos);

      if (line.length() == recLen + 1 && line.charAt(0) == 'A') {
        //Process Correctly Formatted Active Records Only...
        line = line.substring(1);

        rec = TableRecord.GetRecord(definition, line, dataOnly);
        rec.setPosition(pos);

        if (matches_1_0(rec, q)) {
          recLst.add(rec);
        }
      }

      pos = raf.getFilePointer();
      line = raf.readLine();
    }

    cnt = 0;
    records = new TableRecord[recLst.size()];
    while (!recLst.isEmpty()) {
      records[cnt] = recLst.remove(0);
      cnt++;
    }
    recLst = null;

    return records;
  }

  private boolean matches_1_0(TableRecord rec, Query q) {
    boolean match = false;
    Criterion[] crits;
    TFField tfField;
    Object recVal;
    Object[] recValues;
    String op;
    boolean[] results;
    int cnt;

    //System.out.println(rec);

    crits = q.getCriteria();

    if (crits == null || crits.length == 0) {
      match = true; //No criteria given, so everything matches!
    }
    else {
      cnt = 0;
      recValues = rec.getValues();
      results = new boolean[crits.length];

      for (Criterion crit : crits) {
        tfField = definition.getField(crit.getLOperand().toUpperCase());

        if (tfField != null) {
          op = crit.getOperator().trim();
          recVal = recValues[tfField.getIndex()];

          if ("=".equals(op)) {
            switch (crit.getROperandType()) {
              case Criterion.RIGHT_OPERAND_TYPE_STRING_LITERAL:
                results[cnt] = recVal.equals(crit.getROperand());
                break;
              case Criterion.RIGHT_OPERAND_TYPE_INTEGER_LITERAL:
                results[cnt] = recVal.equals(new Integer(crit.getROperand()));
                break;
              case Criterion.RIGHT_OPERAND_TYPE_DOUBLE_LITERAL:
                results[cnt] = recVal.equals(new Double(crit.getROperand()));
                break;
            }
          }
        } //End tfField null check

        cnt++;
      } //End foreach loop through crits

      //Evaluate results array
      cnt = 0;
      for (Criterion crit : crits) {
        if (StringUtils.IsNVL(crit.getPreBoolOp())) {
          match = results[cnt];
        }
        else if ("OR".equalsIgnoreCase(crit.getPreBoolOp().trim())) {
          match = match || results[cnt];
        }
        else {
          //Default is "AND"
          match = match && results[cnt];
        }

        cnt++;
      }
    } //End else block for empty crits

    return match;
  }

  public TableRecordSet select(RandomAccessFile raf, Query q) throws IOException, TableFileException {
    TableRecordSet ts = null;

    if (definition == null || raf == null || q == null) {
      return null;
    }

    if ("1.0".equals(definition.getVersion())) {
      ts = _select1_0(raf, q);
    }
    else {
      throw new TableFileException("Version " + definition.getVersion() + " Select Support NOT Implemented!");
    }

    return ts;
  }

  private TableRecordSet _select1_0(RandomAccessFile raf, Query q) throws IOException {
    TableRecordSet ts = null;
    TableRecord[] records;

    ts = new TableRecordSet();
    ts.setFields(definition.getFields());

    records = findRecords1_0(raf, q, true);
    ts.setRecords(records);

    return ts;
  }

  public void reorg(RandomAccessFile srcRaf, RandomAccessFile destRaf) throws IOException, TableFileException {
    if ("1.0".equals(definition.getVersion())) {
      _reorg1_0(srcRaf, destRaf);
    }
    else {
      throw new TableFileException("Version " + definition.getVersion() + " Reorg Support NOT Implemented!");
    }
  }

  private void _reorg1_0(RandomAccessFile srcRaf, RandomAccessFile destRaf) throws IOException {
    String line;
    int recLen;
    byte[] bRec, buf;

    _store1_0(destRaf); //Write Table Definition to destination

    //Write Active Records to destination
    recLen = definition.getRecordLen();

    //Skip Header Lines
    for (int i = 1; i <= TABLE_HEADER_LINES_V1_0; i++) {
      srcRaf.readLine();
    }

    //Read Data Records
    line = srcRaf.readLine();

    while (line != null) {
      if (line.length() == recLen + 1 && line.charAt(0) == 'A') {
        //Process Correctly Formatted Active Records Only...

        bRec = line.getBytes();
        buf = new byte[bRec.length + 1];
        System.arraycopy(bRec, 0, buf, 0, bRec.length);
        buf[buf.length - 1] = NEW_LINE_BYTE;

        destRaf.write(buf);
      }

      line = srcRaf.readLine();
    }
  }

  public int update(RandomAccessFile raf, Query q) throws IOException, TableFileException {
    int recCnt = 0;

    if (definition == null || raf == null || q == null) {
      return 0;
    }

    if ("1.0".equals(definition.getVersion())) {
      recCnt = _update1_0(raf, q);
    }
    else {
      throw new TableFileException("Version " + definition.getVersion() + " Update Support NOT Implemented!");
    }

    return recCnt;
  }

  private int _update1_0(RandomAccessFile raf, Query q) throws IOException, TableFileException {
    int recCnt = 0;
    TableRecord[] records;
    byte[] bRec, buf;
    HashMap<String, Object> updateMap;
    InputField[] inpfs;
    InputValue[] inpvs;

    //Create Update Map
    inpfs = q.getInputFields();
    inpvs = q.getInputValues();
    updateMap = new HashMap<String, Object>();

    if (inpfs != null && inpvs != null) {
      for (int i = 0; i < inpfs.length; i++) {
        updateMap.put(inpfs[i].getName(), inpvs[i].getValueAsTypeObject());
      }
    }

    //Find Records to be updated
    records = findRecords1_0(raf, q, false);

    for (TableRecord rec : records) {
      System.out.println("Updating: " + rec);

      //Update Record
      rec.update(updateMap);

      //Get Record Bytes
      bRec = rec.toByteArray(definition);

      //Added table specific bytes
      buf = new byte[bRec.length + 2];
      System.arraycopy(bRec, 0, buf, 1, bRec.length);
      buf[0] = ACTIVE_ROW_BYTE;
      buf[buf.length - 1] = NEW_LINE_BYTE;

      //Write update to table file
      raf.seek(rec.getPosition());
      raf.write(buf);

      recCnt++;
    }

    return recCnt;
  }

  public void validate(TableRecord rec) throws TableFileException {
    TFField tff;
    Object[] values;
    int cnt;

    if (rec.getFields() == null || rec.getValues() == null || rec.getFields().length != rec.getValues().length) {
      throw new TableFileException("Incomplete or Corrupted Table Record: " + rec);
    }

    cnt = 0;
    values = rec.getValues();

    for (String fName : rec.getFields()) {
      tff = definition.getField(fName);

      if (tff == null) {
        throw new TableFileException("Field Name '" + fName + "' is Invalid!");
      }
      else {
        if (!tff.validateType(values[cnt])) {
          throw new TableFileException("Value for Field '" + fName + "' is of Invalid Type!");
        }
      }

      cnt++;
    }
  }

  public void createMemoryIndexOn(RandomAccessFile raf, String fieldName) throws TableFileException, IOException {
    HashMap<Object, Long> index;

    if (memoryIndexes == null) {
      memoryIndexes = new HashMap<String, HashMap<Object, Long>>();
    }

    index = memoryIndexes.get(fieldName);
    if (index != null) {
      index.clear();
    }
    else {
      index = new HashMap<Object, Long>();
      memoryIndexes.put(fieldName, index);
    }

    loadMemoryIndex(raf, fieldName, index);

    System.out.println("!!!!!!!!!!!!!");
  }

  private void loadMemoryIndex(RandomAccessFile raf, String fieldName, HashMap<Object, Long> index) throws TableFileException, IOException {
    TableRecordSet records;
    Query q;
    Object val;
    int valIndex;
    long pos;

    q = QueryBuilder.BuildQuery("SELECT");

    records = select(raf, q);

    if (records != null) {
      valIndex = findIndexForField(records, fieldName);

      if (valIndex >= 0) {
        for (TableRecord rec : records.getRecords()) {
          val = rec.getValues()[valIndex];
          pos = rec.getPosition();

          index.put(val, pos);
        }
      }
    }
  }

  private int findIndexForField(TableRecordSet records, String fieldName) {
    int index = -1;
    TFField f;

    if (records != null && fieldName != null) {
      for (int i = 0; i < records.getFields().length; i++) {
        f = records.getFields()[i];
        if (fieldName.equalsIgnoreCase(f.getName())) {
          index = i;
          break;
        }
      }
    }

    return index;
  }

}
