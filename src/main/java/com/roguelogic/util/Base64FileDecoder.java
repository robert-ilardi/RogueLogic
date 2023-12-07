/**
 * Created: Nov 2, 2008 
 */
package com.roguelogic.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Robert C. Ilardi
 * 
 */

public class Base64FileDecoder {

	public Base64FileDecoder() {}

	public void decode(String inputFile, String outputFile, boolean useNewLines) throws IOException {
		byte[] outBuf;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		String line;

		try {
			fis = new FileInputStream(inputFile);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);

			fos = new FileOutputStream(outputFile);
			bos = new BufferedOutputStream(fos);

			line = br.readLine();
			while (line != null) {
				/*if (useNewLines) {
					line = (new StringBuffer()).append(line).append("\n").toString();
				}*/

				outBuf = Base64Codec.Decode(line, useNewLines);

				bos.write(outBuf);
				line = br.readLine();
			}
		} // End try block
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (Exception e) {}
			}

			if (isr != null) {
				try {
					isr.close();
				}
				catch (Exception e) {}
			}

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
		Base64FileDecoder decoder;
		int exitCd;

		if (args.length < 2 || args.length > 3) {
			System.err.println("Usage: java " + Base64FileDecoder.class.getName() + " [INPUT_FILE] [OUTPUT_FILE] <USE_NEW_LINES:TRUE|FALSE>");
			exitCd = 1;
		}
		else {
			try {
				decoder = new Base64FileDecoder();

				decoder.decode(args[0], args[1], (args.length == 3 && "TRUE".equalsIgnoreCase(args[2])));

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
