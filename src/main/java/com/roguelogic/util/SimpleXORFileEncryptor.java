/**
 * Created: Nov 2, 2008 
 */
package com.roguelogic.util;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * @author Robert C. Ilardi
 * 
 */

public class SimpleXORFileEncryptor {

	public SimpleXORFileEncryptor() {}

	public void encrypt(String keyFile, String inputFile, String outputFile) throws IOException {
		SimpleXORCodec codec;
		byte[] keyData, inBuf, outBuf;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		int len;

		try {
			keyData = SystemUtils.LoadDataFromFile(keyFile);

			codec = new SimpleXORCodec();
			codec.setKeyData(keyData);

			inBuf = new byte[1024];

			fis = new FileInputStream(inputFile);
			fos = new FileOutputStream(outputFile);
			bos = new BufferedOutputStream(fos);

			len = fis.read(inBuf);
			while (len != -1) {
				outBuf = codec.encrypt(inBuf);
				bos.write(outBuf, 0, len);
				len = fis.read(inBuf);
			}
		} // End try block
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {}
			}

			if (bos != null) {
				try {
					bos.close();
				}
				catch (Exception e) {}
			}

			if (fos != null) {
				try {
					fos.close();
				}
				catch (Exception e) {}
			}
		}
	}

	public static void main(String[] args) {
		SimpleXORFileEncryptor encryptor;
		int exitCd;

		if (args.length != 3) {
			System.err.println("Usage: java " + SimpleXORFileEncryptor.class.getName() + " [KEY_FILE] [INPUT_FILE] [OUTPUT_FILE]");
			exitCd = 1;
		}
		else {
			try {
				encryptor = new SimpleXORFileEncryptor();

				encryptor.encrypt(args[0], args[1], args[2]);

				exitCd = 0;
			} // End try block
			catch (Exception e) {
				exitCd = 1;
				e.printStackTrace();
			}
		}

		System.exit(exitCd);
	}

}
