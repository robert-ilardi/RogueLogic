/**
 * Created Nov 2, 2008 (Refactored) 
 */
package com.roguelogic.net.rltalk;

import com.roguelogic.util.SimpleXORCodec;

/**
 * @author Robert C. Ilardi
 * 
 */
public class RLTalkXorCodec extends SimpleXORCodec {

	public RLTalkXorCodec() {
		super();
	}

	public CommandDataPair decrypt(CommandDataPair cmDatPair) throws RLTalkException {
		CommandDataPair plainCmDatPair;
		byte[] data = null;

		data = cmDatPair.getData();
		data = decrypt(data);

		plainCmDatPair = new CommandDataPair();
		plainCmDatPair.setCommand(cmDatPair.getCommand());
		plainCmDatPair.setMultiplexerIndex(cmDatPair.getMultiplexerIndex());
		plainCmDatPair.setStatusCode(cmDatPair.getStatusCode());

		plainCmDatPair.setData(data, cmDatPair.getDataType());

		return plainCmDatPair;
	}

	public CommandDataPair encrypt(CommandDataPair cmDatPair) throws RLTalkException {
		CommandDataPair cipherCmDatPair;
		byte[] data = null;

		data = cmDatPair.getData();
		data = encrypt(data);

		cipherCmDatPair = new CommandDataPair();
		cipherCmDatPair.setCommand(cmDatPair.getCommand());
		cipherCmDatPair.setMultiplexerIndex(cmDatPair.getMultiplexerIndex());
		cipherCmDatPair.setStatusCode(cmDatPair.getStatusCode());

		cipherCmDatPair.setData(data, cmDatPair.getDataType());

		return cipherCmDatPair;
	}

}
