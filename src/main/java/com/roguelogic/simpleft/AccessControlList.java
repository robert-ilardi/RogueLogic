/**
 * Created Apr 1, 2009
 */
package com.roguelogic.simpleft;

import java.util.ArrayList;

import com.roguelogic.util.FilenameUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class AccessControlList {

  private ArrayList<Share> shares;

  public AccessControlList() {
    shares = new ArrayList<Share>();
  }

  public void addShare(Share s) {
    shares.add(s);
  }

  public Share getShare(int index) {
    Share s = null;

    if (index >= 0 && index < shares.size()) {
      s = shares.get(index);
    }

    return s;
  }

  public void clear() {
    shares.clear();
  }

  public int count() {
    return shares.size();
  }

  public boolean isAccessible(String path) {
    boolean hasAccess = false;

    if (path.indexOf("..") > 0) {
      hasAccess = false;
    }
    else {
      for (Share s : shares) {
        hasAccess = path.startsWith(s.getName()) && FilenameUtils.IsValidDirectory(path);

        if (hasAccess) {
          break;
        }
      }
    }

    return hasAccess;
  }

  public boolean isReadable(String path) {
    boolean hasAccess = false;

    for (Share s : shares) {
      hasAccess = path.startsWith(s.getName()) && s.isRead();

      if (hasAccess) {
        break;
      }
    }

    return hasAccess;
  }

  public boolean isWriteable(String path) {
    boolean hasAccess = false;

    for (Share s : shares) {
      hasAccess = path.startsWith(s.getName()) && s.isWrite();

      if (hasAccess) {
        break;
      }
    }

    return hasAccess;
  }

  public boolean isDeleteable(String path) {
    boolean hasAccess = false;

    for (Share s : shares) {
      hasAccess = path.startsWith(s.getName()) && s.isDelete();

      if (hasAccess) {
        break;
      }
    }

    return hasAccess;
  }

  public boolean isDirMakeable(String path) {
    boolean hasAccess = false;

    for (Share s : shares) {
      hasAccess = path.startsWith(s.getName()) && s.isMakeDir();

      if (hasAccess) {
        break;
      }
    }

    return hasAccess;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    Share s;

    sb.append("Access Control List:");

    for (int i = 0; i < count(); i++) {
      s = getShare(i);

      sb.append("\n  ");
      sb.append(s);
    }

    return sb.toString();
  }

}
