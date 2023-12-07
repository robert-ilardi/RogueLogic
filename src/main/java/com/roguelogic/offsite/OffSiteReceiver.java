/**
 * Created Dec 9, 2008
 */
package com.roguelogic.offsite;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class OffSiteReceiver implements SocketProcessorCustomizer, SocketSessionSweeper {

	public static final String APP_TITLE = "Off Site Receiver";

	public static final String PROP_ROOT_LOGGER = "RootLogger";
	public static final String PROP_BIND_ADDRESS = "BindAddress";
	public static final String PROP_PORT = "Port";

	public static final String PROP_USERNAME = "Username";
	public static final String PROP_PASSWORD = "Password";

	public static final String PROP_BACKUP_ROOT_DIR = "BackupRootDir";
	public static final String PROP_TRANSPORT_XOR_FILE = "TransportXorFile";

	private Properties osrProps;

	private OffsLogger logger;

	private String bindAddr;
	private int port;

	private String username;
	private String password;

	private String backupRootDir;

	private Object serverLock;

	private SocketServer sockServer;

	private String transportXorFile;
	private byte[] transportXorKey;

	public OffSiteReceiver(Properties osrProps) {
		this.osrProps = osrProps;

		serverLock = new Object();
	}

	public void boot() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		System.out.println("Booting " + APP_TITLE + " at: " + StringUtils.GetTimeStamp());

		readProperties();
		initRootLogger();
		loadTransportXorKey();
	}

	private void readProperties() {
		String tmp;

		tmp = osrProps.getProperty(PROP_PORT);
		port = Integer.parseInt(tmp);

		bindAddr = osrProps.getProperty(PROP_BIND_ADDRESS);

		username = osrProps.getProperty(PROP_USERNAME);
		password = osrProps.getProperty(PROP_PASSWORD);

		backupRootDir = osrProps.getProperty(PROP_BACKUP_ROOT_DIR);
		backupRootDir = FilenameUtils.NormalizeToUnix(backupRootDir);

		transportXorFile = osrProps.getProperty(PROP_TRANSPORT_XOR_FILE);
	}

	private void initRootLogger() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String rootLoggerClass = osrProps.getProperty(PROP_ROOT_LOGGER);

		System.out.println("Loading Root Logger: " + rootLoggerClass);

		logger = (OffsLogger) Class.forName(rootLoggerClass).newInstance();

		System.out.println();
	}

	private void loadTransportXorKey() throws IOException {
		transportXorKey = SystemUtils.LoadDataFromFile(transportXorFile);
	}

	public static void PrintWelcome() {
		System.out.print((new StringBuffer()).append("\n").append(Version.GetInfo()));
	}

	public void listen() throws RLNetException {
		synchronized (serverLock) {
			sockServer = new SocketServer();

			sockServer.setSocketProcessorClass(OffSiteReceiverProcessor.class);
			sockServer.setSocketProcessorCustomizer(this);
			sockServer.setSocketSessionSweeper(this);

			if (StringUtils.IsNVL(bindAddr)) {
				sockServer.listen(port);
			}
			else {
				sockServer.listen(bindAddr, port);
			}

			serverLock.notifyAll();
		}
	}

	public void shutdown() {
		synchronized (serverLock) {
			if (sockServer == null) {
				return;
			}

			sockServer.close();
			sockServer = null;

			serverLock.notifyAll();
		}
	}

	public void waitWhileListening() throws InterruptedException {
		synchronized (serverLock) {
			while (sockServer.isListening()) {
				serverLock.wait(10000);
			}
		}
	}

	public byte[] getTransportXorKey() {
		return transportXorKey;
	}

	public void log(String mesg) {
		LogMessage lMesg;

		lMesg = new LogMessage();
		lMesg.setMessage(mesg);

		if (logger != null) {
			logger.offsLogInfo(lMesg);
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
			logger.offsLogError(lMesg);
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
			logger.offsLogError(lMesg);
		}
		else {
			System.err.println(lMesg);
		}
	}

	public String getProperty(String propName) {
		return osrProps.getProperty(propName);
	}

	public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
		OffSiteReceiverProcessor osrPrc;

		if (processor instanceof OffSiteReceiverProcessor) {
			osrPrc = (OffSiteReceiverProcessor) processor;
			osrPrc.setReceiver(this);
		}

	}

	public void cleanup(SocketSession userSession) {
		BufferedOutputStream bos = (BufferedOutputStream) userSession.removeUserItem(OffSiteReceiverProcessor.USOBJ_UPLOAD_FILE_HANDLE);

		if (bos != null) {
			try {
				bos.close();
			}
			catch (Exception e) {
				log(e);
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getAbsoluteBackupFilePath(String relPath) {
		String absPath = null;
		StringBuffer sb = new StringBuffer();

		sb.append(backupRootDir);

		relPath = relPath.trim();

		if (!backupRootDir.endsWith("/") && !relPath.startsWith("/")) {
			sb.append("/");
		}

		sb.append(relPath);

		absPath = sb.toString();

		return absPath;
	}

	public static void main(String[] args) {
		int exitCd;
		Properties props;
		FileInputStream fis = null;
		OffSiteReceiver receiver;

		if (args.length != 1) {
			System.err.println("Usage: java " + OffSiteReceiver.class.getName() + " [RECEIVER_PROPERTIES_FILE]");
			exitCd = 1;
		}
		else {
			try {
				PrintWelcome();

				System.out.println("Using " + APP_TITLE + " Properties File: " + args[0]);

				fis = new FileInputStream(args[0]);
				props = new Properties();
				props.load(fis);
				fis.close();
				fis = null;

				receiver = new OffSiteReceiver(props);
				receiver.boot();

				receiver.listen();

				receiver.waitWhileListening();

				exitCd = 0;
			}
			catch (Exception e) {
				exitCd = 1;
				e.printStackTrace();
			}
			finally {
				if (fis != null) {
					try {
						fis.close();
					}
					catch (Exception e) {}
				}
			}
		}

		System.exit(exitCd);
	}

}
