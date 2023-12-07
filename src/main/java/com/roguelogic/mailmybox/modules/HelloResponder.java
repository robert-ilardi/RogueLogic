package com.roguelogic.mailmybox.modules;

import com.roguelogic.mail.RLIncomingMail;
import com.roguelogic.mailmybox.MMBException;
import com.roguelogic.mailmybox.MailMyBoxDaemon;
import com.roguelogic.mailmybox.MailProcessorModule;
import com.roguelogic.util.StringUtils;

public class HelloResponder implements MailProcessorModule {

  public static final String HELLO_COMPUTER_REG_EX = "(?i)^Hello\\s*Computer.*";

  private MailMyBoxDaemon daemon;

  public HelloResponder() {}

  @Override
  public void setDaemon(MailMyBoxDaemon daemon) {
    this.daemon = daemon;
  }

  @Override
  public void initMod() throws MMBException {
  // TODO Auto-generated method stub

  }

  @Override
  public boolean accept(RLIncomingMail mail) throws MMBException {
    String[] lines;
    boolean accepted = false;

    try {
      lines = StringUtils.StringOfLinesToArray(mail.getMesg());

      for (String line : lines) {
        if (line.trim().matches(HELLO_COMPUTER_REG_EX)) {
          accepted = true;
          break;
        }
      }
    }
    catch (Exception e) {
      throw new MMBException(e);
    }

    return accepted;
  }

  @Override
  public void process(RLIncomingMail mail) throws MMBException {
    String[] lines, tmpArr;
    String name = null;

    try {
      lines = StringUtils.StringOfLinesToArray(mail.getMesg());

      for (String line : lines) {
        tmpArr = line.split("=", 2);

        if (tmpArr.length == 2) {
          tmpArr = StringUtils.Trim(tmpArr);
          if ("Name".equalsIgnoreCase(tmpArr[0])) {
            name = tmpArr[1];
            break;
          }
        }
      }

      if (StringUtils.IsNVL(name)) {
        name = mail.getFrom();
      }

      daemon.sendReply(mail, (new StringBuffer()).append("Hello ").append(name).toString());
    }
    catch (Exception e) {
      throw new MMBException(e);
    }
  }

}
