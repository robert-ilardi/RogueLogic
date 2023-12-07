/**
 * Created: Oct 23, 2008 
 */
package com.roguelogic.mailmybox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.roguelogic.mail.RLIncomingMail;
import com.roguelogic.mail.RLJavaMailService;
import com.roguelogic.mail.RLMailException;
import com.roguelogic.mail.RLTextMail;
import com.roguelogic.util.Base64Codec;
import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.SimpleXORCodec;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public final class MailMyBoxDaemon {

  private static final String _MMBLF = new String(new byte[] { 109, 97, 105, 108, 109, 121, 98, 111, 120, 46, 108, 105, 99 }); // "mailmybox.lic";

  private static final byte[] _MMBLK = new byte[] { -54, -65, 31, 11, 6, -39, -99, 62, -120, 57, -17, -50, 126, -80, -96, -26, 115, -49, 44, 98, 123, -84, -116, -122, -128, 21, 5, -48, 120, 90, -127,
      -22, 117, -38, 104, -27, -19, 96, 116, 109, -102, -103, 90, 14, 44, -69, 40, -21, -119, -67, 45, 18, -17, -126, -45, -76, 61, -108, 81, 126, -53, 19, 86, -128, -41, -2, -72, -14, 39, -46, 29,
      -90, -75, 43, 3, -94, 115, 6, -6, 26, -47, -16, -8, -70, -78, 104, -98, -24, 124, -108, -15, 125, -114, 101, -124, 63, -4, 25, 74, 67, -101, 69, 102, 60, -104, 82, -13, -77, 114, -98, -109, -3,
      17, 102, -77, -30, -55, -51, -52, -62, -94, 121, -53, 52, -64, 96, 108, 18, -1, -90, -9, 22, -18, -89, 4, 47, -21, -127, -9, 9, 5, -38, -60, 82, 89, -50, 52, -119, -4, -122, 59, 123, 67, 30,
      36, -71, 33, 47, 94, -60, -108, -122, 101, -9, -108, 95, -46, -113, -33, -96, -30, -67, -25, -117, -10, -7, -75, -127, 84, 96, 2, -123, 31, 101, -29, 97, -2, -98, -126, 103, 2, 23, -39, 25,
      -127, 27, 41, 46, 71, 34, 11, -74, 17, 30, 28, 111, -69, 40, 26, -69, 124, 69, 1, -31, -96, -79, -10, -21, -64, -1, 68, -102, 3, 22, 99, -107, 41, -4, 8, 79, 48, 71, -114, -50, 44, -29, -108,
      -31, -109, -28, -110, 18, 125, 3, 62, 17, -80, 115, 26, 54, 20, -56, 31, 39, -5, -87 };

  private static final int LICENSED_MODE_UNKNOWN = 0;
  private static final int LICENSED_MODE_EVALUATION = 1;
  private static final int LICENSED_MODE_REGISTERED = 2;

  private static final String LICENSED_MODE_EVALUATION_STR = "EVALUATION";
  private static final String LICENSED_MODE_REGISTERED_STR = "REGISTERED";
  private static final String LICENSED_MODE_UNKNOWN_STR = "UNKNOWN";

  private static final int REG_TIMESTAMP_INDEX = 2;
  private static final int REG_USER_INDEX = 3;
  private static final int LIC_MODE_INDEX = 6;

  public static final String DEFAULT_MMB_PROPERTIES_FILENAME = "mailmybox.properties";
  public static final String DEFAULT_MMB_HOME_DIRECTORY_NAME = ".mailmybox";

  public static final String PROP_ROOT_LOGGER = "RootLogger";
  public static final String PROP_PREFIX_MAIL_PROCESSOR_MODULE = "MailProcessorModule";
  public static final String PROP_MAIL_MY_BOX_HOME_OVERRIDE = "MailMyBoxHomeOverridePath";
  public static final String PROP_PREFIX_SECURITY_FILTER = "SecurityFilter";
  public static final String PROP_EMAIL_MONITOR_SLEEP_SECS = "EmailMonitorSleepSecs";
  public static final String PROP_MAIL_MY_BOX_SUBJECTS = "MailMyBoxSubjects";

  public static final String MMB_DATE_STORAGE_FORMAT = "yyyyMMdd HH:mm:ss";

  public static final String PROP_SMTP_HOST = "EmailServiceSmtpHost";
  public static final String PROP_SMTP_PORT = "EmailServiceSmtpPort";
  public static final String PROP_SMTP_USER = "EmailServiceSmtpUser";
  public static final String PROP_SMTP_PASSWD = "EmailServiceSmtpPasswd";
  public static final String PROP_SMTP_AUTH_REQUIRED = "EmailServiceSmtpAuthRequired";
  public static final String PROP_SMTP_START_TLS = "EmailServiceSmtpStartTLS";

  public static final String PROP_SENDER_EMAIL_ADDRESS = "EmailServiceSenderAddress";

  public static final String LAST_MESSAGES_FILE = "last_messages.dat";

  private Properties mmbProps;
  private MMBLogger logger;

  private ArrayList<SecurityFilter> securityFilters;
  private ArrayList<MailProcessorModule> mailProcMods;

  private String mailMyBoxHome;

  private String regUser;
  private String regTimestamp;

  private int licensedMode;

  private MailReader mailReader;

  private HashSet<String> lastMesgIds;
  private Date lastMesgDt;

  private Object emLock;
  private Thread emThread;
  private boolean monitorEmail;
  private boolean monitorIsRunning;

  private String[] mmbSubjects;
  private int emSleepSecs;
  private String senderEmailAddr;

  public MailMyBoxDaemon(Properties mmbProps) {
    this.mmbProps = mmbProps;
    emLock = new Object();
    monitorEmail = false;
    monitorIsRunning = false;
  }

  public synchronized boolean boot() throws InstantiationException, IllegalAccessException, ClassNotFoundException, MMBException, RLMailException, IOException, InterruptedException, ParseException {
    boolean booted = false;

    System.out.println("Booting Mail My Box Daemon at: " + StringUtils.GetTimeStamp());

    if (_validateLicense()) {
      if (_processLicensedMode()) {
        loadRootLogger();

        printRegistrationInfo();

        findHomeDirectory();

        readGeneralProperties();

        loadSecurityFilters();

        initializeMailReader();

        loadMailProcModules();

        loadLastMessagesFile();

        startMonitoringEmail();

        booted = true;
      }
    }

    return booted;
  }

  protected static void PrintWelcome() {
    System.out.print((new StringBuffer()).append("\n").append(Version.GetInfo()));
  }

  private void printRegistrationInfo() {
    log((new StringBuffer()).append("\n").append(Version.APP_TITLE).append(" Version ").append(Version.VERSION).append(" License Information -\n").append("Licensee: ").append(regUser).append(
        "\nLicensed Mode: ").append(getLicensedModeStr()).append("\nRegistration Date: ").append(regTimestamp).append("\n").toString());
  }

  private String getLicensedModeStr() {
    String modeStr;

    switch (licensedMode) {
      case LICENSED_MODE_REGISTERED:
        modeStr = LICENSED_MODE_REGISTERED_STR;
        break;
      case LICENSED_MODE_EVALUATION:
        modeStr = LICENSED_MODE_EVALUATION_STR;
        break;
      default:
        modeStr = LICENSED_MODE_UNKNOWN_STR;
    }

    return modeStr;
  }

  public void log(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.mmbLogInfo(lMesg);
    }
    else {
      System.out.println(lMesg);
    }
  }

  public void log(Exception e) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setThrowable(e);

    if (logger != null) {
      logger.mmbLogError(lMesg);
    }
    else {
      System.err.println(lMesg);
    }
  }

  public void logErr(String mesg) {
    LogMessage lMesg;

    lMesg = new LogMessage();
    lMesg.setMessage(mesg);

    if (logger != null) {
      logger.mmbLogError(lMesg);
    }
    else {
      System.err.println(lMesg);
    }
  }

  public String getProperty(String propName) {
    return mmbProps.getProperty(propName);
  }

  private boolean _validateLicense() {
    boolean go = false;
    String key, storedChkSum, licFile;
    byte[] data;
    FileInputStream fis = null;
    SimpleXORCodec codec;
    String[] tokens;
    StringBuffer fieldBuf, calcChkSum;
    MessageDigest md5;

    try {
      // Read License Key
      licFile = (new StringBuffer()).append(System.getProperty("user.dir")).append("/").append(_MMBLF).toString();

      System.out.println("Validating License File: " + licFile);

      fis = new FileInputStream(licFile);
      key = StringUtils.ReadTextFile(fis);

      // Decode from Base 64...
      data = Base64Codec.Decode(key, false);

      // Decrypt using XOR key...
      codec = new SimpleXORCodec();
      codec.setKeyData(_MMBLK);
      data = codec.decrypt(data);

      // Split Tokens
      key = new String(data);
      tokens = key.split("\\|");

      // Get Stored Check Sum
      storedChkSum = tokens[tokens.length - 1];

      // Build Field Buffer
      fieldBuf = new StringBuffer();

      for (int i = 0; i < tokens.length - 1; i++) {
        if (i > 0) {
          fieldBuf.append("|");
        }

        fieldBuf.append(tokens[i]);
      }

      // Recalculate MD5 Check Sum from Field Buffer
      md5 = MessageDigest.getInstance("MD5");
      data = md5.digest(fieldBuf.toString().getBytes());

      calcChkSum = new StringBuffer();

      for (byte b : data) {
        calcChkSum.append(Integer.toHexString(SystemUtils.GetAsciiFromByte(b)));
      }

      go = storedChkSum.equals(calcChkSum.toString());

      if (go) {
        _parseMmblFields(tokens);
      }
    } // End try block
    catch (Exception e) {}
    finally {
      try {
        if (fis != null) {
          fis.close();
        }
      }
      catch (Exception e) {}
    }

    return go;
  }

  private boolean _processLicensedMode() {
    boolean go;

    switch (licensedMode) {
      case LICENSED_MODE_REGISTERED:
        go = true;
        break;
      case LICENSED_MODE_EVALUATION:
        _showEvaluationScreen();
        go = true;
        break;
      default:
        go = false;
    }

    return go;
  }

  private void _parseMmblFields(String[] tokens) {
    regTimestamp = tokens[REG_TIMESTAMP_INDEX];
    regUser = tokens[REG_USER_INDEX];

    if (LICENSED_MODE_EVALUATION_STR.equals(tokens[LIC_MODE_INDEX])) {
      licensedMode = LICENSED_MODE_EVALUATION;
    }
    else if (LICENSED_MODE_REGISTERED_STR.equals(tokens[LIC_MODE_INDEX])) {
      licensedMode = LICENSED_MODE_REGISTERED;
    }
    else {
      licensedMode = LICENSED_MODE_UNKNOWN;
    }
  }

  private void _showEvaluationScreen() {

  }

  private void readGeneralProperties() {
    String tmp;
    String[] tmpArr;

    tmp = mmbProps.getProperty(PROP_EMAIL_MONITOR_SLEEP_SECS);
    emSleepSecs = Integer.parseInt(tmp);

    tmp = mmbProps.getProperty(PROP_MAIL_MY_BOX_SUBJECTS);
    tmpArr = tmp.split("\\|");
    mmbSubjects = StringUtils.Trim(tmpArr);

    senderEmailAddr = mmbProps.getProperty(PROP_SENDER_EMAIL_ADDRESS);
  }

  private void loadRootLogger() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    String rootLoggerClass = mmbProps.getProperty(PROP_ROOT_LOGGER);

    System.out.println("Loading Root Logger: " + rootLoggerClass);

    logger = (MMBLogger) Class.forName(rootLoggerClass).newInstance();

    System.out.println();
  }

  private void loadMailProcModules() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    String[] orderedModNames;
    Properties mpMods;
    String mpmClass;
    int mpmIdx;
    MailProcessorModule mpMod;

    log("Loading Mail Processor Modules - ");

    mailProcMods = new ArrayList<MailProcessorModule>();

    mpMods = StringUtils.GetPrefixedPropNames(mmbProps, PROP_PREFIX_MAIL_PROCESSOR_MODULE);

    orderedModNames = new String[mpMods.size()];

    for (Object val : mpMods.keySet()) {
      String mpmName = (String) val;
      mpmIdx = Integer.parseInt(mpmName.substring(PROP_PREFIX_MAIL_PROCESSOR_MODULE.length()));
      orderedModNames[mpmIdx] = mpmName;
    }

    for (String mpmName : orderedModNames) {
      try {
        mpmClass = mpMods.getProperty(mpmName);

        log((new StringBuffer()).append("Loading ").append(mpmClass).toString());

        mpMod = (MailProcessorModule) Class.forName(mpmClass).newInstance();
        mailProcMods.add(mpMod);

        mpMod.setDaemon(this);
        mpMod.initMod();
      } // End try block
      catch (Exception e) {
        logErr("Error loading module! Skipping... - Exception:");
        log(e);
      }
    }
  }

  public MailProcessorModule getLoadedKernelModule(String mpmClass) {
    MailProcessorModule retMpm = null;

    for (MailProcessorModule mpm : mailProcMods) {
      if (mpm.getClass().getName().equals(mpmClass)) {
        retMpm = mpm;
        break;
      }
    }

    return retMpm;
  }

  public void findHomeDirectory() throws MMBException {
    String home;
    File dir;

    log("Finding Mail My Box Home Directory - ");

    mailMyBoxHome = null;
    home = mmbProps.getProperty(PROP_MAIL_MY_BOX_HOME_OVERRIDE);

    if (!StringUtils.IsNVL(home)) {
      home = home.trim();
      dir = new File(home);

      if (!dir.exists() || !dir.isDirectory()) {
        log("  >> Creating Override Home Directory: " + home);
        dir.mkdirs();
      }

      if (dir.exists() && dir.isDirectory()) {
        log("Using Override Home Directory:");
        mailMyBoxHome = home;
      }
      else {
        log("Warning! Home Override Path is set but is INVALID or could NOT be created! Default Home will be used instead!");
      }
    }

    if (mailMyBoxHome == null) {
      log("Using Default Home Directory:");

      mailMyBoxHome = (new StringBuffer()).append(System.getProperty("user.home")).append("/").append(DEFAULT_MMB_HOME_DIRECTORY_NAME).toString();

      dir = new File(mailMyBoxHome);

      if (!dir.exists() || !dir.isDirectory()) {
        log("  >> Creating Home Directory: " + mailMyBoxHome);
        dir.mkdirs();
      }
    }

    dir = new File(mailMyBoxHome);

    if (!dir.exists() || !dir.isDirectory()) {
      throw new MMBException("Could NOT Establish Mail My Box Home Directory! Bailing Out!");
    }

    log("  >> Setting Mail My Box Home Directory to: " + mailMyBoxHome);
  }

  public String getMailMyBoxHome() {
    return mailMyBoxHome;
  }

  private void loadSecurityFilters() throws InstantiationException, IllegalAccessException, ClassNotFoundException, MMBException {
    String[] orderedModNames;
    Properties secFilterProps;
    String sfClass;
    int sfIdx;
    SecurityFilter secFilter;

    log("Loading Security Filters - ");

    securityFilters = new ArrayList<SecurityFilter>();

    secFilterProps = StringUtils.GetPrefixedPropNames(mmbProps, PROP_PREFIX_SECURITY_FILTER);

    orderedModNames = new String[secFilterProps.size()];

    for (Object val : secFilterProps.keySet()) {
      String mpmName = (String) val;
      sfIdx = Integer.parseInt(mpmName.substring(PROP_PREFIX_SECURITY_FILTER.length()));
      orderedModNames[sfIdx] = mpmName;
    }

    for (String mpmName : orderedModNames) {
      sfClass = secFilterProps.getProperty(mpmName);

      log((new StringBuffer()).append("Loading ").append(sfClass).toString());

      secFilter = (SecurityFilter) Class.forName(sfClass).newInstance();
      securityFilters.add(secFilter);

      secFilter.setDaemon(this);
      secFilter.initFilter();
    }
  }

  private void initializeMailReader() throws MMBException {
    StringBuffer sb;

    log("Initializing Mail Reader...");

    mailReader = new MailReader(this);

    sb = new StringBuffer();
    sb.append("  >> Using Mail Reader Method: ");

    switch (mailReader.getReaderMethod()) {
      case MailReader.READER_MODE_POP3:
        sb.append("POP3");
        break;
      case MailReader.READER_MODE_IMAP:
        sb.append("IMAP");
        break;
      default:
        sb.append("UNKNOWN");
    }

    log(sb.toString());

    sb = new StringBuffer();
    sb.append("  >> Monitoring email of '");
    sb.append(mailReader.getUser());
    sb.append("' on server ");
    sb.append(mailReader.getHost());
    sb.append(":");
    sb.append(mailReader.getPort());

    log(sb.toString());
  }

  private File getLastMessagesFile() {
    return new File((new StringBuffer()).append(mailMyBoxHome).append("/").append(LAST_MESSAGES_FILE).toString());
  }

  private void loadLastMessagesFile() throws RLMailException, IOException, ParseException {
    File lastMesgsFile;
    RandomAccessFile raf = null;
    String tmp;

    try {
      log("Loading Last Messages File...");

      lastMesgsFile = getLastMessagesFile();

      lastMesgIds = new HashSet<String>();

      if (!lastMesgsFile.exists() || lastMesgsFile.length() == 0) {
        lastMesgDt = new Date();
        raf = new RandomAccessFile(lastMesgsFile, "rw");
        raf.setLength(0);
        writeLastMessageDate(raf);
      }
      else {
        //Use the stored last message id.
        raf = new RandomAccessFile(lastMesgsFile, "r");

        //Date is first
        tmp = raf.readLine();

        lastMesgDt = StringUtils.ParseDate(tmp, MMB_DATE_STORAGE_FORMAT);

        //Processed Message Ids
        while (tmp != null) {
          lastMesgIds.add(tmp.trim());
          tmp = raf.readLine();
        }
      } //End else block

      log("Loaded Last Message Timestamp = " + lastMesgDt);
    } //End try block
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }
  }

  private Runnable emRunner = new Runnable() {
    public void run() {
      try {
        log("Checking Email Every " + emSleepSecs + " Seconds...");

        synchronized (emLock) {
          monitorIsRunning = true;
          emLock.notifyAll();
        }

        while (monitorEmail) {
          try {
            scanNewEmails();
            SystemUtils.Sleep(emSleepSecs);
          } //End try block
          catch (Exception e) {
            log(e);
          }
        } //End while monitorEmail
      } //End try block
      finally {
        synchronized (emLock) {
          monitorIsRunning = false;
          emLock.notifyAll();
        }
      }
    }
  };

  private void startMonitoringEmail() throws InterruptedException {
    synchronized (emLock) {
      log("Starting Email Monitoring Thread...");

      monitorEmail = true;

      emThread = new Thread(emRunner);
      emThread.start();

      while (!monitorIsRunning) {
        emLock.wait();
      }
    }
  }

  public void stopMonitoringEmail() throws InterruptedException {
    synchronized (emLock) {
      monitorEmail = false;

      while (monitorIsRunning) {
        emLock.wait();
      }
    }
  }

  private void waitWhileMonitoringEmail() throws InterruptedException {
    synchronized (emLock) {
      while (monitorIsRunning) {
        emLock.wait();
      }
    }
  }

  private void scanNewEmails() {
    ArrayList<RLIncomingMail> mails;

    try {
      //log("Scanning for New Emails - ");
      //log("  >> Using Last Message Timestamp = " + lastMesgDt);
      mails = mailReader.retrieveNewMessages(lastMesgDt);

      if (mails != null) {
        mails = filterByIdStore(mails);

        if (!mails.isEmpty()) {
          log("  >> Scan found new email messages...");
          log("    >> Used Last Message Timestamp = " + lastMesgDt);

          log(" >> Start Processing Messages ------------->\n");
          processNewEmails(mails);
          log(" >> Finished Processing Messages ---------->\n");
        }
      }

      //log("Scanning Completed...");
    } //End try block
    catch (Exception e) {
      log(e);
    }
  }

  private ArrayList<RLIncomingMail> filterByIdStore(ArrayList<RLIncomingMail> mails) {
    ArrayList<RLIncomingMail> filtered = new ArrayList<RLIncomingMail>();

    for (RLIncomingMail mail : mails) {
      if (!lastMesgIds.contains(mail.getId())) {
        filtered.add(mail);
      }
    }

    return filtered;
  }

  private void processNewEmails(ArrayList<RLIncomingMail> mails) throws IOException {
    RLIncomingMail mail = null;
    File lastMesgsFile;
    RandomAccessFile raf = null;

    try {
      log("Processing " + mails.size() + " New Email " + (mails.size() != 1 ? "Messages" : "Message"));

      lastMesgDt = getMaxMessageSentDate(mails);

      lastMesgsFile = getLastMessagesFile();
      raf = new RandomAccessFile(lastMesgsFile, "rw");
      raf.setLength(0);

      writeLastMessageDate(raf);

      lastMesgIds.clear();

      for (int i = 0; i < mails.size(); i++) {
        try {
          mail = mails.get(i);

          log(" >> Processing Message " + (i + 1) + " of " + mails.size() + ":\n" + mail.toShortString());

          process(mail);
        }
        catch (Exception e) {
          logErr("An error occurred while attempting to process message " + (i + 1) + "! Skipping...");
          log(e);
        }
        finally {
          //Update Last Messages Processed Data Stores
          addToLastMessages(mail, raf);
        } //End finally block
      } //End for i loop through mails
    }//End try block
    finally {
      if (raf != null) {
        try {
          raf.close();
        }
        catch (Exception e) {}
      }
    }
  }

  private Date getMaxMessageSentDate(ArrayList<RLIncomingMail> mails) {
    Date max = null;

    for (RLIncomingMail mail : mails) {
      if (max == null) {
        max = mail.getSentTs();
      }
      else if (max.before(mail.getSentTs())) {
        max = mail.getSentTs();
      }
    }

    return max;
  }

  private void writeLastMessageDate(RandomAccessFile raf) throws IOException {
    raf.write((new StringBuffer()).append(StringUtils.QuickDateFormat(lastMesgDt, MMB_DATE_STORAGE_FORMAT)).append("\n").toString().getBytes());
  }

  private void addToLastMessages(RLIncomingMail mail, RandomAccessFile raf) {
    try {
      if (mail != null) {
        if (!StringUtils.IsNVL(mail.getId())) {
          lastMesgIds.add(mail.getId());

          raf.seek(raf.length());
          raf.write((new StringBuffer()).append(mail.getId()).append("\n").toString().getBytes());
        }
      }
    }//End try block
    catch (Exception e) {
      log(e);
    }
  }

  private void process(RLIncomingMail mail) throws IOException, MMBException {
    if (isMessageForMyMailBox(mail)) {
      log(" >> Message is intended for Mail My Box!");

      if (passedSecurity(mail)) {
        log(" >> Message Passed Security!");
        sendToModules(mail);
      }
      else {
        log(" !!!! WARNING - This message did NOT pass Security!");
      }
    } //End if (isMessageForMyMailBox(box))
    else {
      //Ignore Message:
      log(" >> Message is NOT intended for Mail My Box. Skipping!");
    }
  }

  private void sendToModules(RLIncomingMail mail) throws MMBException {
    int mpmCnt = 0;

    log(" >> Passing Message to Processor Modules...");

    for (MailProcessorModule mpMod : mailProcMods) {
      if (mpMod.accept(mail)) {
        mpMod.process(mail);
        mpmCnt++;
      }
    }

    log(" >> Message Processed by " + mpmCnt + (mpmCnt != 1 ? " Processor Modules!" : " Processor Module!"));
  }

  private boolean isMessageForMyMailBox(RLIncomingMail mail) {
    boolean is4Mmb = false;

    if (mail != null && mail.getSubject() != null) {
      is4Mmb = StringUtils.EqualsOne(mail.getSubject().trim(), mmbSubjects, true);
    }

    return is4Mmb;
  }

  private boolean passedSecurity(RLIncomingMail mail) {
    boolean passed = true;

    for (SecurityFilter secFilter : securityFilters) {
      if (!secFilter.passedSecurity(mail)) {
        passed = false;
        break;
      }
    }

    return passed;
  }

  public void sendReply(RLIncomingMail origMail, String mesgBody) throws RLMailException, AddressException {
    RLTextMail email;
    ArrayList<InternetAddress> toList;
    RLJavaMailService mailService;
    Properties props;

    email = new RLTextMail();
    email.setFrom(new InternetAddress(senderEmailAddr));

    toList = new ArrayList<InternetAddress>();
    toList.add(new InternetAddress(origMail.getFrom()));
    email.setToList(toList);

    email.setSubject((new StringBuffer()).append("Re: ").append(origMail.getSubject()).toString());
    email.setMessage(mesgBody);

    email.addHeader("In-Reply-To", origMail.getId());

    props = new Properties();
    props.setProperty(RLJavaMailService.MAIL_SMTP_HOST, mmbProps.getProperty(PROP_SMTP_HOST));
    props.setProperty(RLJavaMailService.MAIL_SMTP_PORT, mmbProps.getProperty(PROP_SMTP_PORT));
    props.setProperty(RLJavaMailService.MAIL_SMTP_USER, mmbProps.getProperty(PROP_SMTP_USER));
    props.setProperty(RLJavaMailService.MAIL_SMTP_PASSWORD, mmbProps.getProperty(PROP_SMTP_PASSWD));
    props.setProperty(RLJavaMailService.MAIL_SMTP_AUTH, mmbProps.getProperty(PROP_SMTP_AUTH_REQUIRED));
    props.setProperty(RLJavaMailService.MAIL_SMTP_STARTTLS, mmbProps.getProperty(PROP_SMTP_START_TLS));

    mailService = new RLJavaMailService(props);

    mailService.send(email);
  }

  public static void main(String[] args) {
    int exitCd;
    String mmbPropsFile;
    FileInputStream fis = null;
    Properties mmbProps;
    MailMyBoxDaemon daemon;

    if (args.length > 0 && !FilenameUtils.IsValidFile(args[0])) {
      System.err.println("Usage: java " + MailMyBoxDaemon.class.getName() + " <MMB_PROPERTIES_FILE>");
      exitCd = 1;
    }
    else {
      try {
        if (args.length > 0 && FilenameUtils.IsValidFile(args[0])) {
          // Use Override MMB Properties File
          mmbPropsFile = args[0];
        }
        else {
          // Use Default MMB Properties File in User's Home Directory
          mmbPropsFile = (new StringBuffer()).append(System.getProperty("user.home")).append("/").append(DEFAULT_MMB_PROPERTIES_FILENAME).toString();
        }

        PrintWelcome();

        System.out.println("Using Mail My Box Properties File: " + mmbPropsFile);

        fis = new FileInputStream(mmbPropsFile);
        mmbProps = new Properties();
        mmbProps.load(fis);
        fis.close();
        fis = null;

        daemon = new MailMyBoxDaemon(mmbProps);

        if (daemon.boot()) {
          // Boot successful
          daemon.waitWhileMonitoringEmail();
          exitCd = 0;
        }
        else {
          // Boot Failure
          System.err.print("\nBoot Failed without explicit error! Check License File! - Bailing Out...\n");
          exitCd = 1;
        }
      } // End try block
      catch (Exception e) {
        exitCd = 1;
        e.printStackTrace();
      }
    }

    System.exit(exitCd);
  }

}
