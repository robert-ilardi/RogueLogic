package com.roguelogic.p2phub.server;

import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_GET_PEER_LIST_REQUEST;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_GET_PEER_LIST_RESPONSE;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_HEART_BEAT_REQUEST;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_LOGIN_REQUEST;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_LOGIN_RESPONSE;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_MESSAGE_RECEIVE_REQUEST;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_SC_FALSE;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_SC_SUCCESS;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_SC_TRUE;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_SEND_MESSAGE_REQUEST;
import static com.roguelogic.p2phub.P2PHubCommandCodes.P2PHUB_SEND_MESSAGE_RESPONSE;

import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.SAXException;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;
import com.roguelogic.net.rltalk.RLTalkUtils;
import com.roguelogic.net.rltalk.RLTalkXorCodec;
import com.roguelogic.p2phub.P2PHubMessage;
import com.roguelogic.p2phub.P2PHubPeer;
import com.roguelogic.p2phub.P2PHubUtils;

public class P2PHubServerProcessor extends RLTalkSocketProcessor {

	public static final String USOBJ_P2P_HUB_SERVER_SESSION_CODEC = "P2PHubServerSessionCodec";
	public static final String USOBJ_P2P_HUB_SERVER_SESSION_PEER_INFO = "P2PHubServerSessionPeerInfo";

	private P2PHubServer server;

	public P2PHubServerProcessor() {
		super();
	}

	public void setServer(P2PHubServer server) {
		this.server = server;
	}

	@Override
	protected void _rlTalkHandshake() throws RLNetException {
		RLTalkXorCodec codec;
		byte[] keyData;

		// Initialize Codec
		keyData = server.getKeyData();
		codec = new RLTalkXorCodec();
		codec.setKeyData(keyData);
		userSession.putUserItem(USOBJ_P2P_HUB_SERVER_SESSION_CODEC, codec);
	}

	@Override
	protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
		switch (cmDatPair.getCommand()) {
			case P2PHUB_LOGIN_REQUEST:
				processLogin(cmDatPair);
				break;
			case P2PHUB_SEND_MESSAGE_REQUEST:
				if (ensureLogin()) {
					processSendMessage(cmDatPair);
				}
				break;
			case P2PHUB_GET_PEER_LIST_REQUEST:
				if (ensureLogin()) {
					processGetPeerList(cmDatPair);
				}
				break;
			case P2PHUB_HEART_BEAT_REQUEST:
				processHeartBeat(cmDatPair);
				break;
			default:
				// Error or HACK Attempt so disconnect
				userSession.endSession();
		}
	}

	private boolean ensureLogin() {
		boolean loggedIn = (userSession.getUserItem(USOBJ_P2P_HUB_SERVER_SESSION_PEER_INFO) != null);

		if (!loggedIn) {
			userSession.endSession();
		}

		return loggedIn;
	}

	private void processLogin(CommandDataPair cmDatPair) throws RLNetException {
		String tmp;
		String[] loginTokens;
		CommandDataPair response;
		RLTalkXorCodec codec;
		CommandDataPair plainCmDatPair;
		P2PHubPeer peer;

		codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_P2P_HUB_SERVER_SESSION_CODEC);
		plainCmDatPair = codec.decrypt(cmDatPair);

		tmp = plainCmDatPair.getString();
		loginTokens = tmp.split("\\|");

		response = new CommandDataPair();
		response.setCommand(P2PHUB_LOGIN_RESPONSE);

		if (loginTokens != null && loginTokens.length == 2) {
			peer = findPeerByUsername(loginTokens[0]);

			if (peer != null && peer.getPassword().equals(loginTokens[1])) {
				// Login OK, but check if this user is already logged in...
				synchronized (peer) {
					if (!server.isLoggedIn(peer)) {
						// Login REALLY OK!
						server.setSessionToken(peer);

						response.setStatusCode(P2PHUB_SC_TRUE);
						response.setData(peer.getSessionToken());

						userSession.putUserItem(USOBJ_P2P_HUB_SERVER_SESSION_PEER_INFO, peer);
						server.associateSessionWithPeer(userSession, peer);
						server.updateHeartBeatEntry(peer, System.currentTimeMillis()); // Default Heart Beat TS to avoid multi-login potential
					}
					else {
						// User already logged in...
						response.setStatusCode(P2PHUB_SC_FALSE);
						response.setData("User already logged in on this server!");
					}
				}
			} // End peer password check if block
			else {
				// Login FAILED!
				response.setStatusCode(P2PHUB_SC_FALSE);
				response.setData("Username and/or Password are invalid!");
			}
		}
		else {
			// Login FAILED!
			response.setStatusCode(P2PHUB_SC_FALSE);
			response.setData("Username and/or Password are invalid!");
		}

		_rlTalkSend(response);

		if (response.getStatusCode() == P2PHUB_SC_FALSE) {
			userSession.endSession();
		}
	}

	private P2PHubPeer findPeerByUsername(String username) {
		ArrayList<P2PHubPeer> peers = server.getPeers();
		P2PHubPeer peer = null;

		for (int i = 0; i < peers.size(); i++) {
			peer = peers.get(i);
			if (peer.getUsername().equals(username)) {
				break;
			}
			else {
				peer = null;
			}
		}

		return peer;
	}

	private P2PHubPeer findPeerBySessionToken(String sessionToken) {
		ArrayList<P2PHubPeer> peers = server.getPeers();
		P2PHubPeer peer = null;

		for (int i = 0; i < peers.size(); i++) {
			peer = peers.get(i);
			if (peer.getSessionToken() != null && peer.getSessionToken().equals(sessionToken)) {
				break;
			}
			else {
				peer = null;
			}
		}

		return peer;
	}

	private void processSendMessage(CommandDataPair cmDatPair) throws RLNetException {
		String xml;
		CommandDataPair response;
		RLTalkXorCodec codec;
		CommandDataPair plainCmDatPair;
		P2PHubMessage mesg;
		P2PHubPeer self;

		try {
			codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_P2P_HUB_SERVER_SESSION_CODEC);
			plainCmDatPair = codec.decrypt(cmDatPair);

			xml = plainCmDatPair.getString();
			// System.out.println(xml);
			mesg = P2PHubUtils.ReadMessageXML(xml);

			// Force setting the Sender
			self = (P2PHubPeer) userSession.getUserItem(USOBJ_P2P_HUB_SERVER_SESSION_PEER_INFO);
			mesg.setSender(self.getSessionToken());

			response = new CommandDataPair();
			response.setCommand(P2PHUB_SEND_MESSAGE_RESPONSE);

			if (mesg != null) {
				if (sendMessageToPeers(mesg)) {
					// Send Message OK!
					response.setStatusCode(P2PHUB_SC_TRUE);
					response.setData("Send Message OK for message id = " + mesg.getMessageId());
				}
				else {
					// Send Message FAILED!
					response.setStatusCode(P2PHUB_SC_FALSE);
					response.setData("Send Message FAILED for message id = " + mesg.getMessageId());
				}
			}
			else {
				// Send Message FAILED!
				response.setStatusCode(P2PHUB_SC_FALSE);
				response.setData("Send Message FAILED for message id = " + mesg.getMessageId());
			}

			_rlTalkSend(response);
		} // End try block
		catch (SAXException e) {
			throw new RLNetException("An error occurred while attempting to process Send Message Request!", e);
		}
		catch (IOException e) {
			throw new RLNetException("An error occurred while attempting to process Send Message Request!", e);
		}
	}

	private boolean sendMessageToPeers(P2PHubMessage mesg) throws IOException, RLNetException {
		boolean sent = false;
		P2PHubPeer peer;
		SocketSession peerSession;
		CommandDataPair plainCmDatPair, cipherCmDatPair;
		RLTalkXorCodec codec;
		String[] recipients;

		recipients = mesg.getRecipients();

		if (recipients != null) {
			for (int i = 0; i < recipients.length; i++) {
				peer = findPeerBySessionToken(recipients[i]);
				peerSession = server.getSessionAssociatedWithPeer(peer);

				if (peerSession != null) {
					plainCmDatPair = new CommandDataPair();
					plainCmDatPair.setCommand(P2PHUB_MESSAGE_RECEIVE_REQUEST);
					plainCmDatPair.setData(mesg.toXML());

					codec = (RLTalkXorCodec) peerSession.getUserItem(USOBJ_P2P_HUB_SERVER_SESSION_CODEC);

					// We have to make sure we synchronized on the target peer's send lock, since we do not
					// know who else is trying to encrypt a message to a particular peer at the same time...
					synchronized (peerSession.getSendLock()) {
						cipherCmDatPair = codec.encrypt(plainCmDatPair);
						RLTalkUtils.RLTalkSend(peerSession, cipherCmDatPair);
					}

					sent = true;
				} // End null peerSession check
			} // End for i loop through recipients
		} // End null recipients check

		return sent;
	}

	private void processGetPeerList(CommandDataPair cmDatPair) throws RLNetException {
		P2PHubPeer[] peers = server.getLoggedInPeers();
		StringBuffer sessionTokenList = new StringBuffer();
		CommandDataPair plainResponse, cipherResponse;
		RLTalkXorCodec codec;

		if (peers != null) {
			for (int i = 0; i < peers.length; i++) {
				if (i > 0) {
					sessionTokenList.append(",");
				}
				sessionTokenList.append(peers[i].getSessionToken());
				sessionTokenList.append(":");
				sessionTokenList.append(peers[i].getUsername());
			}
		}

		plainResponse = new CommandDataPair();
		plainResponse.setCommand(P2PHUB_GET_PEER_LIST_RESPONSE);
		plainResponse.setData(sessionTokenList.toString());
		plainResponse.setStatusCode(P2PHUB_SC_SUCCESS);

		// We have to make sure we synchronized on the target peer's send lock, since we do not
		// know who else is trying to encrypt a CommandDataPair using the same peer's codec at the same time...
		synchronized (userSession.getSendLock()) {
			codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_P2P_HUB_SERVER_SESSION_CODEC);

			cipherResponse = codec.encrypt(plainResponse);

			_rlTalkSend(cipherResponse);
		}
	}

	private void processHeartBeat(CommandDataPair cmDatPair) {
		P2PHubPeer self;
		long hbTs;

		self = (P2PHubPeer) userSession.getUserItem(USOBJ_P2P_HUB_SERVER_SESSION_PEER_INFO);
		hbTs = cmDatPair.getLong();

		server.updateHeartBeatEntry(self, hbTs);
	}

}
