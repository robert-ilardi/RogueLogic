/**
 * Created: Oct 23, 2008 
 */
package com.roguelogic.mailmybox;

import com.roguelogic.mail.RLIncomingMail;

/**
 * @author Robert C. Ilardi
 * 
 */

public interface MailProcessorModule {
  public void setDaemon(MailMyBoxDaemon daemon);

  public void initMod() throws MMBException;

  public boolean accept(RLIncomingMail mail) throws MMBException;

  public void process(RLIncomingMail mail) throws MMBException;
}
