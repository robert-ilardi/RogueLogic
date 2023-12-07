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

public class Base64FileEncoder {

	public Base64FileEncoder() {}

	public void encode(String inputFile, String outputFile, boolean useNewLines) throws IOException {
		byte[] inBuf, resizedBuf = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		int len;
		String outStr;

		try {
			inBuf = new byte[1024];

			fis = new FileInputStream(inputFile);
			fos = new FileOutputStream(outputFile);
			bos = new BufferedOutputStream(fos);

			len = fis.read(inBuf);
			while (len != -1) {
				if (inBuf.length != len) {
					// Smaller Buffer Needed
					resizedBuf = new byte[len];
					System.arraycopy(inBuf, 0, resizedBuf, 0, len);
					outStr = Base64Codec.Encode(resizedBuf, useNewLines);
				}
				else {
					outStr = Base64Codec.Encode(inBuf, useNewLines);
				}

				bos.write(outStr.getBytes());
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
		Base64FileEncoder encoder;
		int exitCd;

		if (args.length < 2 || args.length > 3) {
			System.err.println("Usage: java " + Base64FileEncoder.class.getName() + " [INPUT_FILE] [OUTPUT_FILE] <USE_NEW_LINES:TRUE|FALSE>");
			exitCd = 1;
		}
		else {
			try {
				encoder = new Base64FileEncoder();

				encoder.encode(args[0], args[1], (args.length == 3 && "TRUE".equalsIgnoreCase(args[2])));

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
