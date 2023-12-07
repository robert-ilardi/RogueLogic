package com.roguelogic.netcloak;

import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_CLOSE_TUNNEL_RESPONSE;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_LOGIN_RESPONSE;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_OPEN_TUNNEL_RESPONSE;
import static com.roguelogic.netcloak.NetCloakCommandCodes.NCCC_TUNNEL_DATA_RESPONSE;

import com.roguelogic.net.RLNetException;
import com.roguelogic.net.rltalk.CommandDataPair;
import com.roguelogic.net.rltalk.RLTalkException;
import com.roguelogic.net.rltalk.RLTalkSocketProcessor;
import com.roguelogic.net.rltalk.RLTalkXorCodec;

public class NetCloakClientProcessor extends RLTalkSocketProcessor {

	public static final String USOBJ_NET_CLOAK_USER_CODEC = "NetCloakUserCodec";

	private NetCloakClient client;

	public NetCloakClientProcessor() {
		super();
	}

	protected void setClient(NetCloakClient client) {
		this.client = client;
	}

	@Override
	protected void _rlTalkHandshake() throws RLNetException {}

	@Override
	protected void _rlTalkHandle(CommandDataPair cmDatPair) throws RLNetException {
		switch (cmDatPair.getCommand()) {
			case NCCC_LOGIN_RESPONSE:
				processLogin(cmDatPair);
				break;
			case NCCC_OPEN_TUNNEL_RESPONSE:
				processOpen(cmDatPair);
				break;
			case NCCC_TUNNEL_DATA_RESPONSE:
				processData(cmDatPair);
				break;
			case NCCC_CLOSE_TUNNEL_RESPONSE:
				processClose(cmDatPair);
				break;
			default:
				throw new RLTalkException("Unhandled Command: " + cmDatPair);
		}
	}

	private void processLogin(CommandDataPair cmDatPair) throws RLNetException {
		client.processLoginResponse(cmDatPair);
	}

	private void processOpen(CommandDataPair cmDatPair) throws RLNetException {
		client.processOpenResponse(cmDatPair);
	}

	private void processClose(CommandDataPair cmDatPair) throws RLNetException {
		client.processCloseResponse(cmDatPair);
	}

	private void processData(CommandDataPair cmDatPair) throws RLNetException {
		RLTalkXorCodec codec = (RLTalkXorCodec) userSession.getUserItem(USOBJ_NET_CLOAK_USER_CODEC);
		CommandDataPair plainCmDatPair = codec.decrypt(cmDatPair);
		client.processDataResponse(plainCmDatPair);
	}

}
