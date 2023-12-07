/**
 * Created Apr 2, 2009
 */
package com.roguelogic.toys;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;

import com.roguelogic.util.StringUtils;
import com.roguelogic.util.SystemUtils;

/**
 * @author Robert C. Ilardi
 * 
 */

public class WakeOnLanSender {

	public static final String PROP_BROADCAST_ADDRESS = "BroadcastAddress";
	public static final String PROP_TARGET_MAC_ADDRESS = "TargetMacAddress";
	public static final String PROP_PORT = "Port";
	public static final String PROP_DUMP_FILE = "DumpFile";

	public static final byte ALL_ONES = -1;

	public WakeOnLanSender() {}

	public static void main(String[] args) {
		DatagramSocket sock = null;
		DatagramPacket p;
		byte[] bArr;
		int exitCd;
		String broadcastAddr, targetMacAddr, dumpFile;
		Properties props;
		FileInputStream fis = null;
		String[] tmpArr;
		int ascii, cnt, port;
		FileOutputStream fos = null;

		if (args.length != 1) {
			System.err.println("Usage: java " + WakeOnLanSender.class.getName() + " [TARGET_PROP_FILE]");
			exitCd = 1;
		}
		else {
			try {
				System.out.println("WOL Sender Started -");

				fis = new FileInputStream(args[0]);
				props = new Properties();
				props.load(fis);
				fis.close();
				fis = null;

				broadcastAddr = props.getProperty(PROP_BROADCAST_ADDRESS);
				targetMacAddr = props.getProperty(PROP_TARGET_MAC_ADDRESS);
				port = Integer.parseInt(props.getProperty(PROP_PORT));
				dumpFile = props.getProperty(PROP_DUMP_FILE);

				tmpArr = targetMacAddr.split("\\:");

				bArr = new byte[6 + (tmpArr.length * 16)];

				// Set 6 FF's
				for (int i = 0; i < 6; i++) {
					bArr[i] = ALL_ONES;
				}

				// Set 16 Copies of the MAC Addresses
				cnt = 0;

				for (int i = 6; i < bArr.length; i++) {
					ascii = Integer.parseInt(tmpArr[cnt], 16);
					bArr[i] = SystemUtils.GetByteFromAscii(ascii);

					if (cnt < tmpArr.length - 1) {
						cnt++;
					}
					else {
						cnt = 0;
					}
				}

				if (!StringUtils.IsNVL(dumpFile)) {
					System.out.println("Writing Magic Packet to File: " + dumpFile);
					fos = new FileOutputStream(dumpFile);
					fos.write(bArr);
					fos.close();
					fos = null;
				}

				System.out.println("Sending WakeOnLan Magic Packet to: " + targetMacAddr + " on Broadcast Address: " + broadcastAddr + " ; Port: " + port);

				sock = new DatagramSocket();

				p = new DatagramPacket(bArr, bArr.length, InetAddress.getByName(broadcastAddr), port);

				sock.send(p);

				sock.close();
				sock = null;

				System.out.println("WOL Sender Completed...");
				exitCd = 0;
			} // End try block
			catch (Exception e) {
				exitCd = 1;
				e.printStackTrace();
			}
			finally {
				if (sock != null) {
					try {
						sock.close();
					}
					catch (Exception e) {}
				}

				if (fos != null) {
					try {
						fos.close();
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

		System.exit(exitCd);
	}

}
