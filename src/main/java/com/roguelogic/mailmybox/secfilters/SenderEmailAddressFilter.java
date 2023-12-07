/**
 * Created Feb 15, 2009
 */
package com.roguelogic.mailmybox.secfilters;

import com.roguelogic.mail.RLIncomingMail;
import com.roguelogic.mailmybox.MMBException;
import com.roguelogic.mailmybox.MailMyBoxDaemon;
import com.roguelogic.mailmybox.SecurityFilter;
import com.roguelogic.util.StringUtils;

/**
 * @author Robert C. Ilardi
 *
 */

public class SenderEmailAddressFilter implements SecurityFilter {

  public static final String PROP_ALLOWED_EMAIL_ADDRS = "AllowedEmailAddresses";

  private MailMyBoxDaemon daemon;

  private String[] allowedEmailAddresses;

  public SenderEmailAddressFilter() {}

  /* (non-Javadoc)
   * @see com.roguelogic.mailmybox.SecurityFilter#setDaemon(com.roguelogic.mailmybox.MailMyBoxDaemon)
   */
  @Override
  public void setDaemon(MailMyBoxDaemon daemon) {
    this.daemon = daemon;
  }

  /* (non-Javadoc)
   * @see com.roguelogic.mailmybox.SecurityFilter#initFilter()
   */
  @Override
  public void initFilter() throws MMBException {
    String tmp;
    String[] tmpArr;

    tmp = daemon.getProperty(PROP_ALLOWED_EMAIL_ADDRS);
    tmpArr = tmp.split(";");
    allowedEmailAddresses = StringUtils.Trim(tmpArr);
  }

  /* (non-Javadoc)
   * @see com.roguelogic.mailmybox.SecurityFilter#passedSecurity(com.roguelogic.mail.RLIncomingMail)
   */
  @Override
  public boolean passedSecurity(RLIncomingMail mail) {
    return StringUtils.EqualsOne(mail.getFrom().trim(), allowedEmailAddresses, true);
  }

}
