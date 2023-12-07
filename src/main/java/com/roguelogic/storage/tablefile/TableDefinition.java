/**
 * Created Sep 18, 2007
 */
package com.roguelogic.storage.tablefile;

import java.io.Serializable;
import java.util.HashMap;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class TableDefinition implements Serializable {

  private String name;
  private String version = TableFile.IMPL_VERSION; //Default should be the current implementation's version
  private TFField[] fields;
  private HashMap<String, TFField> fMap;

  public TableDefinition() {}

  public TFField[] getFields() {
    return fields;
  }

  public void setFields(TFField[] fields) {
    this.fields = fields;

    if (fMap != null) {
      fMap.clear();
      fMap = null;
    }

    if (fields != null) {
      fMap = new HashMap<String, TFField>();

      for (TFField field : fields) {
        fMap.put(field.getName().toUpperCase(), field);
      }
    }

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean valid() {
    return !StringUtils.IsNVL(name) && fields != null && fields.length > 0;
  }

  public int getRecordLen() {
    int len = 0;

    for (TFField field : fields) {
      len += field.getLength();
    }

    return len;
  }

  public TFField getField(String fName) {
    return (fMap != null && fName != null ? fMap.get(fName.trim().toUpperCase()) : null);
  }

}
