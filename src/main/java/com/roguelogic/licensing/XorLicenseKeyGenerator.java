/**
 * Created Oct 31, 2008 
 */
package com.roguelogic.licensing;

import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Random;

import com.roguelogic.util.Base64Codec;
import com.roguelogic.util.SimpleXORCodec;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class XorLicenseKeyGenerator {

	public static final String GENERATOR_VER = "RL-XOR-LIC-KEY:1.0";

	public XorLicenseKeyGenerator() {
	}

	public static void main(String[] args) {
		int exitCd;
		SimpleXORCodec codec;
		byte[] keyData, licenseKeyData, chkSum;
		StringBuffer fieldBuf;
		String licenseKey;
		FileOutputStream fos = null;
		Random rnd;
		MessageDigest md5;

		if (args.length < 3) {
			System.err.println("Usage: java " + XorLicenseKeyGenerator.class.getName() + " [XOR_ENCRYPTION_KEY_FILE] [OUTPUT_FILE] [FIELD1] <FIELD2> ... <FIELD[N]>");
			exitCd = 1;
		}
		else {
			try {
				fieldBuf = new StringBuffer();

				// Place random length junk in front to confuse people (a little) using brute force.
				System.out.println("Generating Random Character for data shift confusion...");
				rnd = new Random();
				fieldBuf.append(StringUtils.GetRandomChars(rnd.nextInt(32)));
				fieldBuf.append("|");

				System.out.println("Adding Version and Timestamp Info.");

				// Version Information
				fieldBuf.append(GENERATOR_VER);
				fieldBuf.append("|");

				// Generation Time Stamp
				fieldBuf.append(StringUtils.GetTimeStamp());

				// User Defined Fields
				System.out.println("Adding User Defined Fields.");

				for (int i = 2; i < args.length; i++) {
					fieldBuf.append("|");

					fieldBuf.append(args[i]);
				}

				// MD5 Check Sum
				System.out.println("Calculating MD5 Check Sum...");
				md5 = MessageDigest.getInstance("MD5");
				chkSum = md5.digest(fieldBuf.toString().getBytes());

				fieldBuf.append("|");

				for (byte b : chkSum) {
					fieldBuf.append(Integer.toHexString(SystemUtils.GetAsciiFromByte(b)));
				}

				// XOR Encryption of the License String
				System.out.println("Applying XOR Encryption using Key File: " + args[0]);
				codec = new SimpleXORCodec();
				keyData = SystemUtils.LoadDataFromFile(args[0]);
				codec.setKeyData(keyData);

				licenseKeyData = codec.encrypt(fieldBuf.toString().getBytes());

				// Encode the binary data as base 64...
				System.out.println(" >> Encoding Encrypted Data using Base64");
				licenseKey = Base64Codec.Encode(licenseKeyData, false);

				// Write out to output file...
				System.out.println("Writing to License File: " + args[1]);
				fos = new FileOutputStream(args[1]);
				fos.write(licenseKey.getBytes());

				exitCd = 0;
			} // End try block
			catch (Exception e) {
				exitCd = 1;
				e.printStackTrace();
			}
			finally {
				try {
					if (fos != null) {
						fos.close();
					}
				}
				catch (Exception e) {
				}
			}
		}

		System.exit(exitCd);
	}

}
