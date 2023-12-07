/**
 * Created Dec 17, 2008
 */
package com.roguelogic.offsite;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.roguelogic.util.FilenameUtils;
import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class DesDecryptor {

	private String passPhrase;
	private byte[] salt;
	private int iterCnt;
	private String inputPath;
	private String outputDir;
	private boolean traverseSubDirs;

	public DesDecryptor(String passPhrase, byte[] salt, int iterCnt, String inputPath, String outputDir, boolean traverseSubDirs) {
		this.passPhrase = passPhrase;
		this.salt = salt;
		this.iterCnt = iterCnt;
		this.inputPath = FilenameUtils.NormalizeToUnix(inputPath);
		this.outputDir = FilenameUtils.NormalizeToUnix(outputDir);
		this.traverseSubDirs = traverseSubDirs;
	}

	public void decryptPathContents() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException {
		File root;
		ArrayList<String> fileLst;

		root = new File(inputPath);
		fileLst = new ArrayList<String>();

		if (root.isFile()) {
			fileLst.add(root.getAbsolutePath());
		}
		else {
			traverseDir(root, fileLst);
		}

		for (String f : fileLst) {
			decrypt(f);
		}
	}

	private void traverseDir(File root, ArrayList<String> fileLst) {
		File[] ls;

		ls = root.listFiles();

		if (ls == null) {
			return;
		}

		for (File f : ls) {
			if (f.isDirectory() && traverseSubDirs) {
				traverseDir(f, fileLst);
			}
			else if (f.isFile()) {
				fileLst.add(f.getAbsolutePath());
			}
		}
	}

	private void decrypt(String srcPath) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException {
		String destPath;
		Cipher decryptor;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		CipherOutputStream cos = null;
		File outDir;

		try {
			srcPath = FilenameUtils.NormalizeToUnix(srcPath);

			destPath = getDecryptedPath(srcPath);

			System.out.println("Decrypting " + srcPath + " -> " + destPath);

			decryptor = createDecryptor();

			fis = new FileInputStream(srcPath);
			bis = new BufferedInputStream(fis);

			outDir = new File(FilenameUtils.GetParentDirectory(destPath));
			outDir.mkdirs();

			fos = new FileOutputStream(destPath);
			cos = new CipherOutputStream(fos, decryptor);

			SystemUtils.Copy(bis, cos);
		} // End try block
		finally {
			if (cos != null) {
				try {
					cos.close();
				}
				catch (Exception e) {}
			}

			if (fos != null) {
				try {
					fos.close();
				}
				catch (Exception e) {}
			}

			if (bis != null) {
				try {
					bis.close();
				}
				catch (Exception e) {}
			}

			if (fis != null) {
				try {
					fis.close();
				}
				catch (Exception e) {}
			}
		}
	}

	private synchronized Cipher createDecryptor() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterCnt);
		SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);

		Cipher decryptor = Cipher.getInstance(key.getAlgorithm());

		AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterCnt);

		// Create the ciphers
		decryptor.init(Cipher.DECRYPT_MODE, key, paramSpec);

		return decryptor;
	}

	private String getDecryptedPath(String filePath) {
		String relPath;
		StringBuffer sb;

		filePath = FilenameUtils.NormalizeToUnix(filePath.trim());

		if (filePath.length() >= 3 && ":/".equals(filePath.substring(1, 3))) {
			// Contains Windows Drive Letter
			relPath = filePath.substring(2);
		}
		else {
			relPath = filePath;
		}

		sb = new StringBuffer();

		sb.append(outputDir);

		if (!outputDir.endsWith("/") && !relPath.startsWith("/")) {
			sb.append("/");
		}

		sb.append(relPath);

		relPath = sb.toString();

		return relPath;
	}

	public static final void main(String[] args) {
		String passPhrase, inputPath, outputDir, tmp;
		String[] tmpArr;
		byte[] salt;
		int exitCd, iterCnt;
		boolean traverseSubDirs;
		DesDecryptor decryptor;

		if (args.length != 6) {
			System.err.println("Usage: java " + DesDecryptor.class.getName() + " [PASS_PHRASE] [SALT] [ITERATION_CNT] [INPUT_PATH] [OUTPUT_DIR] [TRAVERSE_SUB_DIRS:Y|N]");
			exitCd = 1;
		}
		else {
			try {
				passPhrase = args[0];

				tmp = args[1];

				tmpArr = tmp.split(",");
				tmpArr = StringUtils.Trim(tmpArr);

				salt = new byte[tmpArr.length];
				for (int i = 0; i < tmpArr.length; i++) {
					salt[i] = (byte) Integer.parseInt(tmpArr[i]);
				}

				iterCnt = Integer.parseInt(args[2]);

				inputPath = args[3];
				outputDir = args[4];

				traverseSubDirs = "Y".equalsIgnoreCase(args[5]);

				decryptor = new DesDecryptor(passPhrase, salt, iterCnt, inputPath, outputDir, traverseSubDirs);

				decryptor.decryptPathContents();

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
