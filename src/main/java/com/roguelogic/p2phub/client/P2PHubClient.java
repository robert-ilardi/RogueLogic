/**
 * Created Sep 16, 2006
 */
package com.roguelogic.p2phub.client;

import java.util.HashMap;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketProcessorCustomizer;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.SocketSessionSweeper;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkAsyncHelper;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkUtils;
import com.roguelogic.net.rltalk.RLTalkXorCodec;
import com.roguelogic.p2phub.P2PHubCommandCodes;
import com.roguelogic.p2phub.P2PHubException;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.p2phub.P2PHubUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class P2PHubClient implements SocketProcessorCustomizer, SocketSessionSweeper {

	public static final String USOBJ_P2P_HUB_CLIENT_USER_CODEC = "P2PHubClientUserCodec";

	public static final int LOGIN_TIMEOUT = 60;
	public static final int SEND_MESG_TIMEOUT = 60;
	public static final int GET_PEER_LIST_TIMEOUT = 60;

	private String address;
	private int port;
	private P2PHubPeer self;
	private byte[] keyData;

	private SocketClient sockClient;

	private RLTalkAsyncHelper rltAsyncHelper;

	private P2PHubMessageObserver observer;

	private HashMap<Integer, Boolean> boolFlagTransactionMap;
	private HashMap<Integer, String> strDataTransactionMap;

	private HeartBeater hBeater = null;

	public P2PHubClient(String address, int port, P2PHubPeer self, byte[] keyData, P2PHubMessageObserver observer) {
		this.address = address;
		this.port = port;

		this.self = self;

		this.keyData = keyData;

		this.observer = observer;

		rltAsyncHelper = new RLTalkAsyncHelper();
		boolFlagTransactionMap = new HashMap<Integer, Boolean>();
		strDataTransactionMap = new HashMap<Integer, String>();
	}

	public void enableHeartBeating(int hbIntervalSecs) {
		if (hbIntervalSecs > 0) {
			hBeater = new HeartBeater(this);
			hBeater.setHeartBeatInterval(hbIntervalSecs);
		}
	}

	public void start() throws P2PHubException {
		startClient();
	}

	private void startClient() throws P2PHubException {
		RLTalkXorCodec codec;

		try {
			sockClient = new SocketClient();
			sockClient.setSocketProcessorClass(P2PHubClientProcessor.class);
			sockClient.setSocketProcessorCustomizer(this);
			sockClient.setSocketSessionSweeper(this);
			sockClient.connect(address, port);

			rltAsyncHelper.resetAllTransactions();

			codec = new RLTalkXorCodec();
			codec.setKeyData(keyData);
			sockClient.putUserItem(USOBJ_P2P_HUB_CLIENT_USER_CODEC, codec);

			System.out.println(Version.APP_TITLE + " Connected to Server Running at: " + address + " on Port = " + port);
		}
		catch (Exception e) {
			throw new P2PHubException("Error while attempting to START the " + Version.APP_TITLE + "!", e);
		}
	}

	public void stop() throws P2PHubException {
		try {
			if (sockClient != null) {
				sockClient.close();
			}

			if (hBeater != null) {
				hBeater.stop();
			}
		}
		catch (Exception e) {
			throw new P2PHubException("Error while attempting to STOP the " + Version.APP_TITLE + "!", e);
		}
	}

	public void initSocketProcessor(SocketProcessor processor) throws RLNetException {
		P2PHubClientProcessor p2phcProcessor;

		try {
			p2phcProcessor = (P2PHubClientProcessor) processor;
			p2phcProcessor.setClient(this);
		}
		catch (Exception e) {
			throw new RLNetException("Could NOT Initialize " + Version.APP_TITLE + " Processor!", e);
		}
	}

	public void cleanup(SocketSession userSession) {
		rltAsyncHelper.killAllTransactions();
	}

	public boolean isConnected() {
		return (sockClient != null ? sockClient.isConnected() : false);
	}

	@SuppressWarnings("unused")
	private synchronized void sendEncrypted(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
		RLTalkXorCodec codec;
		CommandDataPair cipherCmDatPair;

		// Synchronized to make sure a complete message
		// from a single thread will be sent before
		// the next message can be sent!

		codec = (RLTalkXorCodec) sockClient.getUserItem(USOBJ_P2P_HUB_CLIENT_USER_CODEC);

		cipherCmDatPair = codec.encrypt(cmDatPair);

		send(cipherCmDatPair);
	}

	private synchronized void send(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
		RLTalkUtils.RLTalkSend(sockClient.getUserSession(), cmDatPair);
	}

	private synchronized void sendAndWait(CommandDataPair cmDatPair, int transactionId, int timeout) throws RLTalkException, RLNetException, InterruptedException {
		rltAsyncHelper.sendAndWait(sockClient.getUserSession(), cmDatPair, transactionId, timeout);
	}

	private synchronized void sendEncryptedAndWait(CommandDataPair cmDatPair, int transactionId, int timeout) throws RLTalkException, RLNetException, InterruptedException {
		RLTalkXorCodec codec;
		CommandDataPair cipherCmDatPair;

		// Synchronized to make sure a complete message
		// from a single thread will be sent before
		// the next message can be sent!

		codec = (RLTalkXorCodec) sockClient.getUserItem(USOBJ_P2P_HUB_CLIENT_USER_CODEC);

		cipherCmDatPair = codec.encrypt(cmDatPair);

		rltAsyncHelper.sendAndWait(sockClient.getUserSession(), cipherCmDatPair, transactionId, timeout);
	}

	protected void processLoginResponse(CommandDataPair cmDatPair) {
		int transactionId;
		boolean loginOK;

		loginOK = (cmDatPair.getStatusCode() == P2PHubCommandCodes.P2PHUB_SC_TRUE);
		transactionId = cmDatPair.getMultiplexerIndex();

		if (loginOK) {
			self.setSessionToken(cmDatPair.getString());
		}

		boolFlagTransactionMap.put(transactionId, loginOK);

		if (cmDatPair.getStatusCode() == P2PHubCommandCodes.P2PHUB_SC_TRUE || cmDatPair.getStatusCode() == P2PHubCommandCodes.P2PHUB_SC_FALSE) {
			rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
		}
		else {
			rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
		}
	}

	protected void processSendMessageResponse(CommandDataPair cmDatPair) {
		int transactionId;
		boolean sent;

		sent = (cmDatPair.getStatusCode() == P2PHubCommandCodes.P2PHUB_SC_TRUE);
		transactionId = cmDatPair.getMultiplexerIndex();

		boolFlagTransactionMap.put(transactionId, sent);

		if (cmDatPair.getStatusCode() == P2PHubCommandCodes.P2PHUB_SC_TRUE || cmDatPair.getStatusCode() == P2PHubCommandCodes.P2PHUB_SC_FALSE) {
			rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
		}
		else {
			rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
		}
	}

	protected void processMessageReceiveRequest(CommandDataPair cipherCmDatPair) throws RLTalkException {
		RLTalkXorCodec codec;
		CommandDataPair plainCmDatPair;
		P2PHubMessage mesg;
		String xml;

		// We don't need to make this method synchronized
		// because message queuing and flow control
		// is managed by the RL-Talk Protocol and Socket Wrapper...

		try {
			codec = (RLTalkXorCodec) sockClient.getUserItem(USOBJ_P2P_HUB_CLIENT_USER_CODEC);

			plainCmDatPair = codec.decrypt(cipherCmDatPair);

			xml = plainCmDatPair.getString();
			// System.out.println(xml);
			mesg = P2PHubUtils.ReadMessageXML(xml);

			if (observer != null) {
				// This can be optimized, by using a queue and a separate queue consumer thread...
				observer.onMessage(mesg);
			}
			else {
				System.out.println("Observer is NULL - Message Received:\n" + mesg.toXML());
			}
		} // End try block
		catch (RLTalkException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RLTalkException("An error occurred while attempting to process a received message request!", e);
		}
	}

	public void processGetPeerList(CommandDataPair cipherCmDatPair) throws RLTalkException {
		RLTalkXorCodec codec;
		CommandDataPair plainCmDatPair;
		String delimitedList;
		int transactionId;

		// We don't need to make this method synchronized
		// because message queuing and flow control
		// is managed by the RL-Talk Protocol and Socket Wrapper...

		codec = (RLTalkXorCodec) sockClient.getUserItem(USOBJ_P2P_HUB_CLIENT_USER_CODEC);

		plainCmDatPair = codec.decrypt(cipherCmDatPair);

		transactionId = plainCmDatPair.getMultiplexerIndex();

		delimitedList = plainCmDatPair.getString();

		strDataTransactionMap.put(transactionId, delimitedList);

		if (plainCmDatPair.getStatusCode() == P2PHubCommandCodes.P2PHUB_SC_SUCCESS || plainCmDatPair.getStatusCode() == P2PHubCommandCodes.P2PHUB_SC_FAILURE) {
			rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_SUCCEEDED);
		}
		else {
			rltAsyncHelper.setTransactionState(transactionId, RLTalkAsyncHelper.TRANSACTION_FAILED);
		}
	}

	public boolean login() throws P2PHubException {
		boolean loginOk = false;
		CommandDataPair cmDatPair;
		int transactionId = -1;
		StringBuffer sb;

		try {
			transactionId = rltAsyncHelper.obtainTransactionId();

			cmDatPair = new CommandDataPair();
			cmDatPair.setCommand(P2PHubCommandCodes.P2PHUB_LOGIN_REQUEST);
			cmDatPair.setMultiplexerIndex(transactionId);

			sb = new StringBuffer();
			sb.append(self.getUsername());
			sb.append("|");
			sb.append(self.getPassword());

			cmDatPair.setData(sb.toString());

			// Send and Wait for Response
			sendEncryptedAndWait(cmDatPair, transactionId, LOGIN_TIMEOUT);

			if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
				throw new P2PHubException("Login operation FAILED!");
			}

			loginOk = boolFlagTransactionMap.get(transactionId);

			// If login OK, then start the heart beat mechanism
			if (loginOk) {
				if (hBeater != null) {
					hBeater.start();
				}
			}
		}
		catch (Exception e) {
			throw new P2PHubException("An error occurred while attempting to Perform Login for username = " + self.getUsername(), e);
		}
		finally {
			rltAsyncHelper.releaseTransactionId(transactionId);
		}

		return loginOk;
	}

	public boolean sendMessage(P2PHubMessage mesg) throws P2PHubException {
		boolean sent = false;
		CommandDataPair cmDatPair;
		int transactionId = -1;

		try {
			transactionId = rltAsyncHelper.obtainTransactionId();

			cmDatPair = new CommandDataPair();
			cmDatPair.setCommand(P2PHubCommandCodes.P2PHUB_SEND_MESSAGE_REQUEST);
			cmDatPair.setMultiplexerIndex(transactionId);

			cmDatPair.setData(mesg.toXML()); // XML so we can support non-Java Clients

			// Send and Wait for Response
			sendEncryptedAndWait(cmDatPair, transactionId, SEND_MESG_TIMEOUT);

			if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
				throw new P2PHubException("Send Message operation FAILED!");
			}

			sent = boolFlagTransactionMap.get(transactionId);
		}
		catch (Exception e) {
			throw new P2PHubException("An error occurred while attempting to Send Message = " + mesg, e);
		}
		finally {
			rltAsyncHelper.releaseTransactionId(transactionId);
		}

		return sent;
	}

	public P2PHubPeer getSelf() {
		return self;
	}

	public P2PHubPeer[] getPeerList() throws P2PHubException {
		P2PHubPeer[] peers = null;
		String[] peerPairs, peerTokens;
		String delimitedList;
		CommandDataPair cmDatPair;
		int transactionId = -1;

		try {
			transactionId = rltAsyncHelper.obtainTransactionId();

			cmDatPair = new CommandDataPair();
			cmDatPair.setCommand(P2PHubCommandCodes.P2PHUB_GET_PEER_LIST_REQUEST);
			cmDatPair.setMultiplexerIndex(transactionId);

			// Send and Wait for Response
			sendAndWait(cmDatPair, transactionId, GET_PEER_LIST_TIMEOUT);

			if (rltAsyncHelper.getTransactionFlag(transactionId) != RLTalkAsyncHelper.TRANSACTION_SUCCEEDED) {
				throw new P2PHubException("Get Peer List operation FAILED!");
			}

			delimitedList = strDataTransactionMap.get(transactionId);
			if (delimitedList != null) {
				peerPairs = delimitedList.trim().split(",");

				if (peerPairs != null) {
					peers = new P2PHubPeer[peerPairs.length];
					for (int i = 0; i < peerPairs.length; i++) {
						peers[i] = new P2PHubPeer();
						peerTokens = peerPairs[i].split(":");

						if (peerTokens != null && peerTokens.length == 2) {
							peers[i].setSessionToken(peerTokens[0]);
							peers[i].setUsername(peerTokens[1]);
						}
					}
				}
			}
		}
		catch (Exception e) {
			throw new P2PHubException("An error occurred while attempting to obtain remote peer list!", e);
		}
		finally {
			rltAsyncHelper.releaseTransactionId(transactionId);
		}

		return peers;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public void sendHeartBeat() throws P2PHubException {
		CommandDataPair cmDatPair;

		try {
			cmDatPair = new CommandDataPair();

			cmDatPair.setCommand(P2PHubCommandCodes.P2PHUB_HEART_BEAT_REQUEST);
			cmDatPair.setData(System.currentTimeMillis());

			send(cmDatPair);
		}
		catch (Exception e) {
			throw new P2PHubException("An error occurred while sending Heart Beat!", e);
		}
	}

}
