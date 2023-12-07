package com.roguelogic.netcloak;

import static com.roguelogic.netcloak.LoginStatusConstants.LIS_LOGIN_FAILED;
import static com.roguelogic.netcloak.LoginStatusConstants.LIS_LOGIN_OK;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_CLOSE_TUNNEL_REQUEST;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_CLOSE_TUNNEL_RESPONSE;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_LOGIN_REQUEST;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_LOGIN_RESPONSE;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_OPEN_TUNNEL_REQUEST;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_OPEN_TUNNEL_RESPONSE;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_TUNNEL_DATA_REQUEST;

import java.util.HashMap;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.ProtocolConstants;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;
import com.roguelogic.net.rltalk.RLTalkXorCodec;
import com.roguelogic.util.RLException;
import com.roguelogic.util.UniqueIdPool;

public class NetCloakServerProcessor extends RLTalkSocketProcessor {

	public static final String USOBJ_NET_CLOAK_USER_CODEC = "NetCloakUserCodec";
	public static final String USOBJ_NET_CLOAK_USER_SOCKET_CLIENT_MAP = "NetCloakUserSocketClientMap";
	public static final String USOBJ_NET_CLOAK_USER_ID_POOL = "NetCloakUserIdPool";

	public static final String GSOBJ_NET_CLOAK_KEY_DATA = "NetCloakKeyData";

	public static final String LOCALHOST = "localhost";

	private String username;
	private String password;
	private int[] allowedPorts;

	public NetCloakServerProcessor() {
		super();
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	protected void setPassword(String password) {
		this.password = password;
	}

	public void setAllowedPorts(int[] allowedPorts) {
		this.allowedPorts = allowedPorts;
	}

	@Override
	protected void _rlTalkHandshake() throws RLNetException {
		RLTalkXorCodec codec;
		UniqueIdPool idPool;
		byte[] keyData;
		HashMap<Integer, SocketClient> sockClientMap;

		// Initialize Codec
		keyData = (byte[]) userSession.getGlobalItem(GSOBJ_NET_CLOAK_KEY_DATA);
		codec = new RLTalkXorCodec();
		codec.setKeyData(keyData);
		userSession.putUserItem(USOBJ_NET_CLOAK_USER_CODEC, codec);

		// Initialize Connection Id Pool
		try {
			idPool = new UniqueIdPool("Connection Id Pool", ProtocolConstants.PROTOCOL_MULTIPLEXER_INDEX_LIMIT);
			userSession.putUserItem(USOBJ_NET_CLOAK_USER_ID_POOL, idPool);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Initialize Socket Client Map
		sockClientMap = new HashMap<Integer, SocketClient>();
		userSession.putUserItem(USOBJ_NET_CLOAK_USER_SOCKET_CLIENT_MAP, sockClientMap);
	}

	@Override
	protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
		switch (cmDatPair.getCommand()) {
			case NCCC_LOGIN_REQUEST:
				processLogin(cmDatPair);
				break;
			case NCCC_OPEN_TUNNEL_REQUEST:
				processOpen(cmDatPair);
				break;
			case NCCC_TUNNEL_DATA_REQUEST:
				processData(cmDatPair);
				break;
			case NCCC_CLOSE_TUNNEL_REQUEST:
				processClose(cmDatPair);
				break;
			default:
				// Error or HACK Attempt so disconnect
				userSession.endSession();
		}
	}

	private synchronized int reserveConnectionId() throws RLException {
		UniqueIdPool idPool = (UniqueIdPool) userSession.getUserItem(USOBJ_NET_CLOAK_USER_ID_POOL);
		return idPool.obtainId();
	}

	private synchronized void releaseConnectionId(int connectionId) {
		UniqueIdPool idPool = (UniqueIdPool) userSession.getUserItem(USOBJ_NET_CLOAK_USER_ID_POOL);
		idPool.releaseId(connectionId);
	}

	private boolean portAllowed(int destinationPort) {
		boolean allowed = false;

		for (int i = 0; i < allowedPorts.length; i++) {
			if (allowedPorts[i] == destinationPort) {
				allowed = true;
				break;
			}
		}

		return allowed;
	}

	private void processLogin(CommandDataPair cmDatPair) throws RLNetException {
		String tmp;
		String[] loginTokens;
		CommandDataPair response;
		RLTalkXorCodec codec;
		CommandDataPair plainCmDatPair;

		codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_NET_CLOAK_USER_CODEC);
		plainCmDatPair = codec.decrypt(cmDatPair);

		tmp = plainCmDatPair.getString();
		loginTokens = tmp.split("\\|");

		response = new CommandDataPair();
		response.setCommand(NCCC_LOGIN_RESPONSE);

		if (loginTokens.length == 2 && loginTokens[0].equals(username) && loginTokens[1].equals(password)) {
			// Login OK!
			response.setStatusCode(LIS_LOGIN_OK);
			response.setData("Login OK for " + loginTokens[0]);
		}
		else {
			// Login FAILED!
			response.setStatusCode(LIS_LOGIN_FAILED);
			response.setData("Username and/or Password are invalid!");
		}

		_rlTalkSend(response);
	}

	private void processOpen(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
		HashMap<Integer, SocketClient> sockClientMap;
		SocketClient sockClient;
		int port, connectionId, transactionId;
		CommandDataPair response;
		ServerProxyCustomizer customizer;

		port = cmDatPair.getInt();
		transactionId = cmDatPair.getMultiplexerIndex();

		response = new CommandDataPair();
		response.setCommand(NCCC_OPEN_TUNNEL_RESPONSE);
		response.setMultiplexerIndex(transactionId);

		try {
			if (portAllowed(port)) {
				sockClientMap = (HashMap<Integer, SocketClient>) userSession.getUserItem(USOBJ_NET_CLOAK_USER_SOCKET_CLIENT_MAP);

				try {
					connectionId = reserveConnectionId();
				}
				catch (RLException e) {
					throw new RLNetException("Could NOT obtain free Connection Id!", e);
				}

				customizer = new ServerProxyCustomizer();
				customizer.setConnectionId(connectionId);
				customizer.setUserSession(userSession);

				sockClient = new SocketClient();
				sockClient.setSocketProcessorClass(NetCloakServerProxyProcessor.class);
				sockClient.setSocketProcessorCustomizer(customizer);
				sockClient.connect(LOCALHOST, port);

				sockClientMap.put(connectionId, sockClient);

				response.setData(connectionId);
			}
			else {
				response.setData(-1);
			}
		} // End try block
		catch (Exception e) {
			e.printStackTrace();

			response.setData(-1);
		}

		_rlTalkSend(response);
	}

	private void processClose(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
		HashMap<Integer, SocketClient> sockClientMap;
		SocketClient sockClient;
		int connectionId;
		CommandDataPair response;

		connectionId = cmDatPair.getMultiplexerIndex();

		sockClientMap = (HashMap<Integer, SocketClient>) userSession.getUserItem(USOBJ_NET_CLOAK_USER_SOCKET_CLIENT_MAP);

		releaseConnectionId(connectionId);

		sockClient = sockClientMap.remove(connectionId);
		if (sockClient != null) {
			sockClient.close();
		}

		response = new CommandDataPair();
		response.setCommand(NCCC_CLOSE_TUNNEL_RESPONSE);
		response.setData(connectionId);

		_rlTalkSend(response);
	}

	private void processData(CommandDataPair cmDatPair) throws RLNetException {
		HashMap<Integer, SocketClient> sockClientMap;
		SocketClient sockClient;
		int connectionId;
		RLTalkXorCodec codec;
		CommandDataPair plainCmDatPair;

		codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_NET_CLOAK_USER_CODEC);
		plainCmDatPair = codec.decrypt(cmDatPair);

		connectionId = cmDatPair.getMultiplexerIndex();
		sockClientMap = (HashMap<Integer, SocketClient>) userSession.getUserItem(USOBJ_NET_CLOAK_USER_SOCKET_CLIENT_MAP);

		sockClient = sockClientMap.get(connectionId);

		if (sockClient != null) {
			sockClient.send(plainCmDatPair.getData());
		}
	}

}
