/**
 * Created Dec 17, 2008
 */
package com.roguelogic.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @author Robert C. Ilardi
 * 
 */

public class DesEncryptionHelper {

	private Cipher encryptor;
	private Cipher decryptor;

	public DesEncryptionHelper(String passPhrase, byte[] salt, int iterCnt) throws InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchAlgorithmException,
			NoSuchPaddingException {
		// Create the key
		KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterCnt);
		SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
		encryptor = Cipher.getInstance(key.getAlgorithm());
		decryptor = Cipher.getInstance(key.getAlgorithm());

		// Prepare the parameter to the ciphers
		AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterCnt);

		// Create the ciphers
		encryptor.init(Cipher.ENCRYPT_MODE, key, paramSpec);
		decryptor.init(Cipher.DECRYPT_MODE, key, paramSpec);
	}

	public byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
		return encryptor.doFinal(data);
	}

	public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
		return decryptor.doFinal(data);
	}

}
