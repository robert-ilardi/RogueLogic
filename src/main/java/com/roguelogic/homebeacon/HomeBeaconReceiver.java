package com.roguelogic.homebeacon;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Robert C. Ilardi
 * 
 */
public class HomeBeaconReceiver {

	public HomeBeaconReceiver() {
	}

	public static void main(String[] args) throws Exception {
		DatagramSocket sock = new DatagramSocket(1979);
		DatagramPacket p;
		byte[] bArr;

		bArr = new byte[1024];

		p = new DatagramPacket(bArr, bArr.length);

		sock.receive(p);

		System.out.println(new String(p.getData(), 0, p.getLength()));

		sock.close();
	}

}
