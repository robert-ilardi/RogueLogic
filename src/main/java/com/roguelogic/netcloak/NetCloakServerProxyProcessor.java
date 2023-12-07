/*
 * Created on Mar 3, 2006
 */
package com.roguelogic.netcloak;

import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_CLOSE_TUNNEL_RESPONSE;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_TUNNEL_DATA_RESPONSE;

import java.util.HashMap;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.SocketClient;
import com.roguelogic.net.SocketProcessor;
import com.roguelogic.net.SocketSession;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.Packet;
import com.roguelogic.net.rltalk.PacketFactory;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkXorCodec;

public class NetCloakServerProxyProcessor implements SocketProcessor {

	private SocketSession rootSession;

	private int connectionId;

	public NetCloakServerProxyProcessor() {}

	public void setRootSession(SocketSession rootSession) {
		this.rootSession = rootSession;
	}

	public void setConnectionId(int connectionId) {
		this.connectionId = connectionId;
	}

	private void sendEncrypted(CommandDataPair cmDatPair) throws RLTalkException, RLNetException {
		RLTalkXorCodec codec;
		CommandDataPair cipherCmDatPair;

		synchronized (rootSession.getSendLock()) {
			codec = (RLTalkXorCodec) rootSession.getUserItem(NetCloakServerProcessor.USOBJ_NET_CLOAK_USER_CODEC);
			cipherCmDatPair = codec.encrypt(cmDatPair);
			rlTalkSend(cipherCmDatPair);
		}
	}

	private void rlTalkSend(CommandDataPair cmDatPair) throws RLNetException {
		Packet[] packets;

		// Synchronized to make sure a complete message
		// from a single thread will be sent before
		// the next message can be sent!
		synchronized (rootSession.getSendLock()) {
			packets = PacketFactory.GetPackets(cmDatPair); // Convert CDP to Packets

			// Send Packets across the network
			for (int i = 0; i < packets.length; i++) {
				rootSession.send(packets[i].toByteArray());
			}
		}
	}

	public void clearSession() {}

	public void destroyProcessor() {
		HashMap<Integer, SocketClient> sockClientMap;
		SocketClient sockClient;
		CommandDataPair response;

		try {
			sockClientMap = (HashMap<Integer, SocketClient>) rootSession.getUserItem(NetCloakServerProcessor.USOBJ_NET_CLOAK_USER_SOCKET_CLIENT_MAP);
			sockClient = sockClientMap.remove(connectionId);
			if (sockClient != null) {
				sockClient.close();
			}

			if (!rootSession.wasPeerForcedClosed()) {
				response = new CommandDataPair();
				response.setCommand(NCCC_CLOSE_TUNNEL_RESPONSE);
				response.setData(connectionId);

				rlTalkSend(response);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rootSession = null;
			connectionId = -1;
		}
	}

	public void process(SocketSession userSession, byte[] data) throws RLNetException {
		CommandDataPair response;

		response = new CommandDataPair();
		response.setCommand(NCCC_TUNNEL_DATA_RESPONSE);
		response.setData(data);
		response.setMultiplexerIndex(connectionId);

		sendEncrypted(response);
	}

}
