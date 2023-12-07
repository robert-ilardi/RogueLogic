package com.roguelogic.util;

public class SimpleXORCodec {

	private byte[] keyData;

	private int sendKeyIndex;
	private int receiveKeyIndex;

	public SimpleXORCodec() {
		sendKeyIndex = 0;
		receiveKeyIndex = 0;
	}

	public void setKeyData(byte[] keyData) {
		this.keyData = keyData;
	}

	private int getNextSendIndex() {
		int nextIndex = sendKeyIndex;

		if ((sendKeyIndex + 1) >= keyData.length) {
			sendKeyIndex = 0;
		}
		else {
			sendKeyIndex++;
		}

		return nextIndex;
	}

	private int getNextReceiveIndex() {
		int nextIndex = receiveKeyIndex;

		if ((receiveKeyIndex + 1) >= keyData.length) {
			receiveKeyIndex = 0;
		}
		else {
			receiveKeyIndex++;
		}

		return nextIndex;
	}

	public synchronized byte[] encrypt(byte[] plainText) {
		byte[] cipherText = new byte[plainText.length];

		for (int i = 0; i < cipherText.length; i++) {
			cipherText[i] = (byte) (plainText[i] ^ keyData[getNextSendIndex()]);
		}

		return cipherText;
	}

	public synchronized byte[] decrypt(byte[] cipherText) {
		byte[] plainText = new byte[cipherText.length];

		for (int i = 0; i < cipherText.length; i++) {
			plainText[i] = (byte) (cipherText[i] ^ keyData[getNextReceiveIndex()]);
		}

		return plainText;
	}

	public void _setKeyIndexes(int keyIndex) {
		sendKeyIndex = keyIndex;
		receiveKeyIndex = keyIndex;
	}

	public void _setKeyIndexes(int sendKeyIndex, int receiveKeyIndex) {
		this.sendKeyIndex = sendKeyIndex;
		this.receiveKeyIndex = receiveKeyIndex;
	}

	public void _resetIndexes() {
		sendKeyIndex = 0;
		receiveKeyIndex = 0;
	}

}
