package com.roguelogic.homebeacon;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Robert C. Ilardi
 * 
 */

public class HomeBeaconServer {

	public HomeBeaconServer() {
	}

	public static void main(String[] args) throws Exception {
		DatagramSocket sock = new DatagramSocket();
		DatagramPacket p;
		byte[] bArr;

		sock.connect(InetAddress.getByName("192.168.1.255"), 1979);

		bArr = "Hello World!".getBytes();
		p = new DatagramPacket(bArr, bArr.length);

		sock.send(p);
	}

}
