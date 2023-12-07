/**
 * Created: Nov 23, 2008 
 */
package com.roguelogic.mailmybox;

import com.roguelogic.mail.RLIncomingMail;

/**
 * @author Robert C. Ilardi
 * 
 */

public interface SecurityFilter {
  public void setDaemon(MailMyBoxDaemon daemon);

  public void initFilter() throws MMBException;

  public boolean passedSecurity(RLIncomingMail mail);
}
