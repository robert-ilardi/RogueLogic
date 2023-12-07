/**
 * Created Oct 31, 2008 
 */
package com.roguelogic.util;

import java.io.IOException;

/**
 * @author Robert C. Ilardi
 * 
 */

public class SimpleXORKeyToArrayCode {

	public SimpleXORKeyToArrayCode() {}

	public String getArrayJavaCode(String keyFile, String varName) throws IOException {
		byte[] data;
		StringBuffer code = new StringBuffer();

		data = SystemUtils.LoadDataFromFile(keyFile);

		code.append("byte[] ");
		code.append(varName);
		code.append(" = new byte[] {");

		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				code.append(", ");
			}

			code.append((int) data[i]);
		}

		code.append("};");

		return code.toString();
	}

	public static void main(String[] args) {
		int exitCd;
		SimpleXORKeyToArrayCode coder;
		String code;

		if (args.length != 2) {
			System.err.println("Usage: java " + SimpleXORKeyToArrayCode.class.getName() + " [KEY_FILE] [ARRAY_NAME]");
			exitCd = 1;
		}
		else {
			try {
				coder = new SimpleXORKeyToArrayCode();

				code = coder.getArrayJavaCode(args[0], args[1]);

				System.out.println(code);

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
