/**
 * Created Oct 8, 2006
 */
package com.roguelogic.roguenet;

import static com.roguelogic.roguenet.RNAConstants.PLUG_IN_INFO_COPYRIGHT_PROP;
import static com.roguelogic.roguenet.RNAConstants.PLUG_IN_INFO_DESCRIPTION_PROP;
import static com.roguelogic.roguenet.RNAConstants.PLUG_IN_INFO_DEVELOPER_PROP;
import static com.roguelogic.roguenet.RNAConstants.PLUG_IN_INFO_DEVELOPER_PROP_PREFIX;
import static com.roguelogic.roguenet.RNAConstants.PLUG_IN_INFO_LOGICAL_NAME_PROP;
import static com.roguelogic.roguenet.RNAConstants.PLUG_IN_INFO_URL_PROP;
import static com.roguelogic.roguenet.RNAConstants.PLUG_IN_INFO_VERSION_PROP;

import java.io.Serializable;
import java.util.Properties;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNAPlugInInfo implements Serializable {

  private String logicalName;
  private String version;
  private String description;
  private String developer;
  private String url;
  private String copyright;

  private Properties dvlprProps;

  public RNAPlugInInfo() {}

  public String getCopyright() {
    return copyright;
  }

  public void setCopyright(String copyright) {
    this.copyright = copyright;
  }

  public String getDeveloper() {
    return developer;
  }

  public void setDeveloper(String developer) {
    this.developer = developer;
  }

  public String getLogicalName() {
    return logicalName;
  }

  public void setLogicalName(String name) {
    this.logicalName = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Properties getDvlprProps() {
    return dvlprProps;
  }

  public void setDvlprProps(Properties userProps) {
    this.dvlprProps = userProps;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Properties getMessageProperties() {
    Properties tmpProps, mesgProps = new Properties();

    mesgProps.setProperty(PLUG_IN_INFO_LOGICAL_NAME_PROP, logicalName);
    mesgProps.setProperty(PLUG_IN_INFO_VERSION_PROP, version);
    mesgProps.setProperty(PLUG_IN_INFO_DESCRIPTION_PROP, description);
    mesgProps.setProperty(PLUG_IN_INFO_DEVELOPER_PROP, developer);
    mesgProps.setProperty(PLUG_IN_INFO_URL_PROP, url);
    mesgProps.setProperty(PLUG_IN_INFO_COPYRIGHT_PROP, copyright);

    tmpProps = StringUtils.PrefixPropNames(dvlprProps, PLUG_IN_INFO_DEVELOPER_PROP_PREFIX);
    if (tmpProps != null) {
      mesgProps.putAll(tmpProps);
    }

    return mesgProps;
  }

}
