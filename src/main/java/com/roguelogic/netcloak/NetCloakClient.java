package com.roguelogic.netcloak;

import static com.roguelogic.netcloak.LoginStatusConstants.LIS_LOGIN_OK;
import static com.roguelogic.netcloak.LoginStatusConstants.LIS_MESG_NULL_RESPONSE;
import static com.roguelogic.netcloak.LoginStatusConstants.LIS_NULL_RESPONSE;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_CLOSE_TUNNEL_REQUEST;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_LOGIN_REQUEST;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_OPEN_TUNNEL_REQUEST;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_TUNNEL_DATA_REQUEST;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketServer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.Packet;
import com.roguelogic.net.rltalk.PacketFactory;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkXorCodec;

public class NetCloakClient implements SocketProcessorCustomizer {

	public static final String PROP_ADDRESS = "Address";
	public static final String PROP_PORT = "Port";
	public static final String PROP_USERNAME = "Username";
	public static final String PROP_PASSWORD = "Password";
	public static final String PROP_KEY_FILE = "KeyFile";
	public static final String PROP_PORT_MAPPING = "PortMapping";

	public static final String APP_TITLE = "Net Cloak - Client";
	public static final String VERSION = "2.0";
	public static final String ROGUELOGIC = "RogueLogic 3.0";
	public static final String RL_URL = "http://www.roguelogic.com";
	public static final String COPYRIGHT = "Copyright (c) 2002 - 2006 By: Robert C. Ilardi";

	public static final String USOBJ_NET_CLOAK_USER_CODEC = "NetCloakUserCodec";

	public static final String GSOBJ_NET_CLOAK_CLIENT = "TheNetCloakClient";

	private Properties clientProps;

	private String address;
	private int port;
	private String username;
	private String password;
	private String keyFile;

	private byte[] keyData;

	private SocketClient sockClient;

	private Object syncCallLock;

	private boolean loginResponseReceived;
	private int loginStatus;
	private String loginMesg;

	private int[] proxyPorts;
	private HashMap<Integer, Integer> portMapping;
	private SocketServer[] localServers;

	private int[] transactionIdPool;

	private HashMap<Integer, Integer> openTransactConnIdMap;

	private HashMap<Integer, SocketSession> sessionTunnelMap;

	public static final int MAX_TRANSACTIONS = 999;

	public static final int TRANSACTION_ID_NOT_RESERVED = 0;
	public static final int TRANSACTION_ID_RESERVED = 1;
	public static final int TRANSACTION_SUCCEEDED = 2;
	public static final int TRANSACTION_FAILED = 3;

	public NetCloakClient(Properties props) throws NetCloakException {
		if (props == null) {
			throw new NetCloakException("Can NOT create Net Cloak Client with NULL Properties!");
		}

		clientProps = props;

		syncCallLock = new Object();

		transactionIdPool = new int[MAX_TRANSACTIONS];

		openTransactConnIdMap = new HashMap<Integer, Integer>();

		sessionTunnelMap = new HashMap<Integer, SocketSession>();
	}

	public void start() throws NetCloakException {
		loadProperties();
		loadKeyData();
		startClient();
	}

	private void startClient() throws NetCloakException {
		RLTalkXorCodec codec;

		try {
			sockClient = new SocketClient();
			sockClient.setSocketProcessorClass(NetCloakClientProcessor.class);
			sockClient.setSocketProcessorCustomizer(this);
			sockClient.connect(address, port);

			codec = new RLTalkXorCodec();
			codec.setKeyData(keyData);
			sockClient.putUserItem(USOBJ_NET_CLOAK_USER_CODEC, codec);

			System.out.println("Net Cloak Client Connected to Server Running at: " + address + " on Port = " + port);
		}
		catch (Exception e) {
			throw new NetCloakException("Error while attempting to START the Net Cloak Client!", e);
		}
	}

	public void stop() throws NetCloakException {
		try {
			if (sockClient != null) {
				sockClient.close();
			}
		}
		catch (Exception e) {
			throw new NetCloakException("Error while attempting to STOP the Net Cloak Client!", e);
		}
	}

	private void loadProperties() throws NetCloakException {
		String tmp;
		String[] tmpArr1, tmpArr2;
		int local, remote;

		try {
			address = clientProps.getProperty(PROP_ADDRESS);

			tmp = clientProps.getProperty(PROP_PORT);

			port = Integer.parseInt(tmp);

			username = clientProps.getProperty(PROP_USERNAME);

			password = clientProps.getProperty(PROP_PASSWORD);

			keyFile = clientProps.getProperty(PROP_KEY_FILE);

			tmp = clientProps.getProperty(PROP_PORT_MAPPING);

			portMapping = new HashMap<Integer, Integer>();

			tmpArr1 = tmp.split(",");
			proxyPorts = new int[tmpArr1.length];

			for (int i = 0; i < tmpArr1.length; i++) {
				tmpArr2 = tmpArr1[i].split(":");

				local = Integer.parseInt(tmpArr2[0].trim());
				remote = Integer.parseInt(tmpArr2[1].trim());

				portMapping.put(local, remote);
				proxyPorts[i] = local;
			}
		}
		catch (Exception e) {
			throw new NetCloakException("Error while loading Properties!", e);
		}
	}

	private void loadKeyData() throws NetCloakException {
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		byte[] buf;
		int len;

		try {
			fis = new FileInputStream(keyFile);
			buf = new byte[1024];
			baos = new ByteArrayOutputStream();

			len = fis.read(buf);
			while (len > 0) {
				baos.write(buf, 0, len);
				len = fis.read(buf);
			}

			keyData = baos.toByteArray();
		}
		catch (Exception e) {
			throw new NetCloakException("Error while attempting to load Key Data from file: " + keyFile);
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {}
				fis = null;
			}

			if (baos != null) {
				try {
					baos.close();
				}
				catch (Exception e) {}
				baos = null;
			}
		}
	}

	public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
		NetCloakClientProcessor nccProc;
		NetCloakProxyClientProcessor ncpsProc;

		if (processor instanceof NetCloakClientProcessor) {
			nccProc = (NetCloakClientProcessor) processor;
			nccProc.setClient(this);
		}
		else if (processor instanceof NetCloakProxyClientProcessor) {
			ncpsProc = (NetCloakProxyClientProcessor) processor;
			ncpsProc.setClient(this);
		}

	}

	public static void PrintWelcome() {
		StringBuffer sb = new StringBuffer();

		sb.append(APP_TITLE);
		sb.append("\n");

		sb.append("Version: ");
		sb.append(VERSION);
		sb.append("\n");

		sb.append(ROGUELOGIC);
		sb.append("\n");

		sb.append(RL_URL);
		sb.append("\n");

		sb.append(COPYRIGHT);
		sb.append("\n");

		sb.append("\n");
		System.out.print(sb.toString());
	}

	protected synchronized void sendEncrypted(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
		RLTalkXorCodec codec;
		CommandDataPair cipherCmDatPair;

		// Synchronized to make sure a complete message
		// from a single thread will be sent before
		// the next message can be sent!

		codec = (RLTalkXorCodec) sockClient.getUserItem(USOBJ_NET_CLOAK_USER_CODEC);

		cipherCmDatPair = codec.encrypt(cmDatPair);

		send(cipherCmDatPair);
	}

	protected synchronized void send(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
		Packet[] packets;

		// Synchronized to make sure a complete message
		// from a single thread will be sent before
		// the next message can be sent!

		packets = PacketFactory.GetPackets(cmDatPair); // Convert CDP to Packets

		// Send Packets across the network
		for (int i = 0; i < packets.length; i++) {
			sockClient.send(packets[i].toByteArray());
		}
	}

	protected int obtainTransactionId() throws NetCloakException {
		int transId = -1;

		synchronized (transactionIdPool) {
			for (int i = 0; i < transactionIdPool.length; i++) {
				if (transactionIdPool[i] == TRANSACTION_ID_NOT_RESERVED) {
					transactionIdPool[i] = TRANSACTION_ID_RESERVED;
					transId = i;
					break;
				}
			}

			if (transId == -1) {
				throw new NetCloakException("Could NOT reserve Transaction Id! Max number of Simultaneous Transactions in Progress!");
			}
		}

		return transId;
	}

	protected void releaseTransactionId(int transId) {
		if (transId >= 0 && transId < transactionIdPool.length)
			synchronized (transactionIdPool) {
				transactionIdPool[transId] = TRANSACTION_ID_NOT_RESERVED;
			}
	}

	protected boolean transactionComplete(int transId) {
		return (transId >= 0 && transId < transactionIdPool.length) && (transactionIdPool[transId] == TRANSACTION_SUCCEEDED || transactionIdPool[transId] == TRANSACTION_FAILED);
	}

	protected void processLoginResponse(CommandDataPair cmDatPair) {
		synchronized (syncCallLock) {
			if (cmDatPair != null) {
				loginStatus = cmDatPair.getStatusCode();
				loginMesg = cmDatPair.getString();
			}
			else {
				loginStatus = LIS_NULL_RESPONSE;
				loginMesg = LIS_MESG_NULL_RESPONSE;
			}

			loginResponseReceived = true;
			syncCallLock.notifyAll();
		}
	}

	public void processOpenResponse(CommandDataPair cmDatPair) {
		int transactionId, connectionId;

		connectionId = cmDatPair.getInt();
		transactionId = cmDatPair.getMultiplexerIndex();

		synchronized (syncCallLock) {
			openTransactConnIdMap.put(transactionId, connectionId);

			if (connectionId != -1) {
				synchronized (transactionIdPool) {
					transactionIdPool[transactionId] = TRANSACTION_SUCCEEDED;
				}
			}
			else {
				synchronized (transactionIdPool) {
					transactionIdPool[transactionId] = TRANSACTION_FAILED;
				}
			}

			syncCallLock.notifyAll();
		}
	}

	public void processCloseResponse(CommandDataPair cmDatPair) {
		System.out.println(cmDatPair);
	}

	public synchronized void processDataResponse(CommandDataPair cmDatPair) {
		SocketSession userSession = null;

		try {
			userSession = sessionTunnelMap.get(cmDatPair.getMultiplexerIndex());
			userSession.send(cmDatPair.getData());
		}
		catch (Exception e) {
			e.printStackTrace();

			if (userSession != null) {
				userSession.endSession();
			}
		}
	}

	public boolean login() throws NetCloakException {
		CommandDataPair cmDatPair;
		StringBuffer sb;
		boolean loginOk = false;

		try {
			loginResponseReceived = false;

			// Build Login Request
			cmDatPair = new CommandDataPair();
			cmDatPair.setCommand(NCCC_LOGIN_REQUEST);

			sb = new StringBuffer();
			sb.append(username);
			sb.append("|");
			sb.append(password);

			cmDatPair.setData(sb.toString());

			// Send and Wait for Response
			synchronized (syncCallLock) {
				sendEncrypted(cmDatPair);

				while (!loginResponseReceived) {
					syncCallLock.wait();
				}
			} // End Login Request Sync Call

			// Process Response
			switch (loginStatus) {
				case LIS_LOGIN_OK:
					loginOk = true;
					break;
				default:
					System.err.println(loginMesg);
					loginOk = false;
			}

		} // End try block
		catch (Exception e) {
			throw new NetCloakException("Error while attempting to login to Net Cloak Server!", e);
		}

		return loginOk;
	}

	protected int open(int remotePort) throws NetCloakException {
		CommandDataPair cmDatPair;
		int connectionId = -1, transactionId;

		transactionId = obtainTransactionId();

		try {
			// Build Open Request
			cmDatPair = new CommandDataPair();
			cmDatPair.setCommand(NCCC_OPEN_TUNNEL_REQUEST);
			cmDatPair.setMultiplexerIndex(transactionId);

			cmDatPair.setData(remotePort);

			// Send and Wait for Response
			synchronized (syncCallLock) {
				send(cmDatPair);

				while (!transactionComplete(transactionId)) {
					syncCallLock.wait();
				}
			} // End Open Request Sync Call

			// Process Response
			connectionId = getOpenConnectionId(transactionId);
			if (connectionId != -1) {
				System.out.println("Tunnel to Remote Target Port " + remotePort + " established on Multiplexing Channel = " + connectionId);
			}
			else {
				System.out.println("Could NOT establish encrypted tunnel to Remote Target Port " + remotePort);
			}
		} // End try block
		catch (Exception e) {
			throw new NetCloakException("Error while attempting to Open Encrypted Tunnel for Remote Port = " + remotePort, e);
		}
		finally {
			releaseTransactionId(transactionId);
		}

		return connectionId;
	}

	public void close(int connectionId) throws RLTalkException, RLNetException {
		CommandDataPair cmDatPair;

		cmDatPair = new CommandDataPair();
		cmDatPair.setCommand(NCCC_CLOSE_TUNNEL_REQUEST);
		cmDatPair.setMultiplexerIndex(connectionId);

		sendEncrypted(cmDatPair);
	}

	public synchronized void send(int connectionId, byte[] data) throws RLTalkException, RLNetException {
		CommandDataPair cmDatPair;

		cmDatPair = new CommandDataPair();
		cmDatPair.setCommand(NCCC_TUNNEL_DATA_REQUEST);
		cmDatPair.setMultiplexerIndex(connectionId);
		cmDatPair.setData(data);

		sendEncrypted(cmDatPair);
	}

	private int getOpenConnectionId(int transactionId) {
		return openTransactConnIdMap.get(transactionId);
	}

	public void associateSessionWithTunnel(SocketSession userSession, int connectionId) {
		sessionTunnelMap.put(connectionId, userSession);
	}

	public void setupLocalProxyServers() throws NetCloakException {
		int remoteTargetPort;

		try {
			localServers = new SocketServer[proxyPorts.length];

			for (int i = 0; i < localServers.length; i++) {
				remoteTargetPort = portMapping.get(proxyPorts[i]);

				localServers[i] = new SocketServer();

				localServers[i].putGlobalItem(GSOBJ_NET_CLOAK_CLIENT, this);
				localServers[i].setSocketProcessorClass(NetCloakProxyClientProcessor.class);
				localServers[i].setSocketProcessorCustomizer(this);
				localServers[i].setSocketSessionSweeper(new NetCloakProxyClientSweeper());

				localServers[i].putGlobalItem(NetCloakProxyClientProcessor.GSOBJ_REMOTE_TARGET_PORT, remoteTargetPort);

				localServers[i].listen(proxyPorts[i]);
				System.out.println("Tunneling Local Port " + proxyPorts[i] + " to Remote Target Port " + remoteTargetPort);
			}
		}
		catch (Exception e) {
			throw new NetCloakException("An error occurred while attempting to open tunnel!", e);
		}
	}

	public static void main(String[] args) {
		String propsFile;
		FileInputStream fis = null;
		Properties props;
		boolean failed = false;
		NetCloakClient ncClient = null;

		NetCloakClient.PrintWelcome();

		if (args.length != 1) {
			System.err.println("Usage: java " + NetCloakClient.class.getName() + " [CLIENT_PROPERTIES_FILE]");
			System.exit(1);
		}
		else {
			try {
				propsFile = args[0];

				fis = new FileInputStream(propsFile);
				props = new Properties();
				props.load(fis);

				ncClient = new NetCloakClient(props);
				ncClient.start();

				/*
				 * CommandDataPair cmDatPair = new CommandDataPair(); cmDatPair.setCommand(1234); cmDatPair.setMultiplexerIndex(429); cmDatPair.setData("Hello World ~ Net Cloak Style!");
				 * ncClient.send(cmDatPair);
				 */

				if (ncClient.login()) {
					System.out.println("Login OK!");
					ncClient.setupLocalProxyServers();
				}
				else {
					// Login Failed!
					failed = true;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				failed = true;
			}
			finally {
				if (fis != null) {
					try {
						fis.close();
					}
					catch (Exception e) {}
					fis = null;
				}

				if (failed) {
					if (ncClient != null) {
						try {
							ncClient.stop();
						}
						catch (NetCloakException e) {
							e.printStackTrace();
						}
						ncClient = null;
					}
					System.exit(1);
				}
			}
		}
	}

}
