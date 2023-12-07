/**
 * Created Jan 30, 2007
 */
package com.roguelogic.rni;

import java.io.Serializable;
import java.util.Properties;

import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class RNIAppInfo implements Serializable {

  private String name;
  private String version;
  private String developer;
  private String userString;

  public static final String APP_INFO_PROP_NAME = "AppInfo.Name";
  public static final String APP_INFO_PROP_VERSION = "AppInfo.Version";
  public static final String APP_INFO_PROP_DEVELOPER = "AppInfo.Developer";
  public static final String APP_INFO_PROP_USERSTRING = "AppInfo.UserString";

  public static final int APP_INFO_TOKEN_INDEX_NAME = 0;
  public static final int APP_INFO_TOKEN_INDEX_VERSION = 1;
  public static final int APP_INFO_TOKEN_INDEX_DEVELOPER = 2;
  public static final int APP_INFO_TOKEN_INDEX_USERSTRING = 3;

  public RNIAppInfo() {}

  public String getDeveloper() {
    return developer;
  }

  public void setDeveloper(String appDeveloper) {
    this.developer = appDeveloper;
  }

  public String getName() {
    return name;
  }

  public void setName(String appName) {
    this.name = appName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String appVersion) {
    this.version = appVersion;
  }

  public String getUserString() {
    return userString;
  }

  public void setUserString(String userString) {
    this.userString = userString;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("[RNIAppInfo - Name: ");
    sb.append(name);
    sb.append(", Version: ");
    sb.append(version);
    sb.append(", Developer: ");
    sb.append(developer);
    sb.append(", UserString: ");
    sb.append(userString);
    sb.append("]");

    return sb.toString();
  }

  public Properties toProperties() {
    Properties props = new Properties();

    if (!StringUtils.IsNVL(name)) {
      props.setProperty(APP_INFO_PROP_NAME, name);
    }

    if (!StringUtils.IsNVL(version)) {
      props.setProperty(APP_INFO_PROP_VERSION, version);
    }

    if (!StringUtils.IsNVL(developer)) {
      props.setProperty(APP_INFO_PROP_DEVELOPER, developer);
    }

    if (!StringUtils.IsNVL(userString)) {
      props.setProperty(APP_INFO_PROP_USERSTRING, userString);
    }

    return props;
  }

  public String assembleAppInfoStr() {
    return AssembleAppInfoStr(toProperties());
  }

  public static String AssembleAppInfoStr(Properties props) {
    StringBuffer sb = new StringBuffer();
    String tmp;

    tmp = props.getProperty(APP_INFO_PROP_NAME);
    if (!StringUtils.IsNVL(tmp)) {
      sb.append(tmp);
    }

    sb.append("|");

    tmp = props.getProperty(APP_INFO_PROP_VERSION);
    if (!StringUtils.IsNVL(tmp)) {
      sb.append(tmp);
    }

    sb.append("|");

    tmp = props.getProperty(APP_INFO_PROP_DEVELOPER);
    if (!StringUtils.IsNVL(tmp)) {
      sb.append(tmp);
    }

    sb.append("|");

    tmp = props.getProperty(APP_INFO_PROP_USERSTRING);
    if (!StringUtils.IsNVL(tmp)) {
      sb.append(tmp);
    }

    return sb.toString();
  }

  public static RNIAppInfo DessembleAppInfoStr(String appInfoStr) {
    RNIAppInfo appInfo = null;
    String[] tokens;

    if (!StringUtils.IsNVL(appInfoStr)) {
      tokens = appInfoStr.split("\\|");

      if (tokens != null) {
        appInfo = new RNIAppInfo();

        if (tokens.length > APP_INFO_TOKEN_INDEX_NAME) {
          appInfo.setName(tokens[APP_INFO_TOKEN_INDEX_NAME]);
        }

        if (tokens.length > APP_INFO_TOKEN_INDEX_VERSION) {
          appInfo.setVersion(tokens[APP_INFO_TOKEN_INDEX_VERSION]);
        }

        if (tokens.length > APP_INFO_TOKEN_INDEX_DEVELOPER) {
          appInfo.setDeveloper(tokens[APP_INFO_TOKEN_INDEX_DEVELOPER]);
        }

        if (tokens.length > APP_INFO_TOKEN_INDEX_USERSTRING) {
          appInfo.setUserString(tokens[APP_INFO_TOKEN_INDEX_USERSTRING]);
        }
      }
    }

    return appInfo;
  }

}
