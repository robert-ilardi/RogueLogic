/*
 Copyright 2007 Robert C. Ilardi

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.roguelogic.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;

import com.roguelogic.util.SystemUtils;

public class SocketSession {

	private SocketChannel sockCh;

	private SocketServer server;
	private SocketClient client;

	private SocketWriter writer;

	private SocketReadManager readManager;

	private HashMap<String, Object> userData;

	private ArrayList<byte[]> rawDataQueue;

	private boolean virgin;
	private boolean handshook;
	private boolean peerForcedClose;
	private boolean zombie;

	private Object sendLock;

	protected SocketSession(SocketChannel sockCh) throws IOException {
		this.sockCh = sockCh;
		writer = new SocketWriter(sockCh);
		userData = new HashMap<String, Object>();
		rawDataQueue = new ArrayList<byte[]>();
		virgin = true;
		handshook = false;
		peerForcedClose = false;
		zombie = false;
		sendLock = new Object();
	}

	protected void setWriter(SocketWriter writer) {
		this.writer = writer;
	}

	protected void setServer(SocketServer server) {
		this.server = server;
	}

	protected void setClient(SocketClient client) {
		this.client = client;
	}

	protected void setReadManager(SocketReadManager readManager) {
		this.readManager = readManager;
	}

	public HashMap<String, Object> getUserData() {
		return userData;
	}

	public void send(byte[] data) throws RLNetException {
		synchronized (sendLock) {
			try {
				if (writer != null) {
					writer.write(data);
				}
				else {
					throw new RLNetException("Socket Writer NOT Initialized!");
				}
			}
			catch (RLNetException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RLNetException(e);
			}
		}
	}

	public synchronized void destroy() {
		if (writer != null) {
			writer.close();
			writer = null;
		}

		if (userData != null) {
			userData.clear();
			userData = null;
		}
	}

	public Object getUserItem(String key) {
		Object obj = null;

		if (userData != null) {
			synchronized (userData) {
				obj = userData.get(key);
			}
		}

		return obj;
	}

	public Object putUserItem(String key, Object value) {
		Object obj = null;

		if (userData != null) {
			synchronized (userData) {
				obj = userData.put(key, value);
			}
		}

		return obj;
	}

	public Object removeUserItem(String key) {
		Object obj = null;

		if (userData != null) {
			synchronized (userData) {
				obj = userData.remove(key);
			}
		}

		return obj;
	}

	public void clearUserData() {
		if (userData != null) {
			synchronized (userData) {
				userData.clear();
			}
		}
	}

	public Object getGlobalItem(String key) {
		return server.getGlobalSession().get(key);
	}

	public Object putGlobalItem(String key, Object value) {
		return server.getGlobalSession().put(key, value);
	}

	public Object removeGlobalItem(String key) {
		return server.getGlobalSession().remove(key);
	}

	public void endSession() {
		if (server != null) {
			server.close(sockCh);
		}

		if (client != null) {
			client.close();
		}
	}

	public void signalReadReady() {
		if (readManager != null) {
			readManager.enqueueReadySockSession(this);
		}
	}

	public void enqueueRawData(byte[] data) {
		synchronized (rawDataQueue) {
			rawDataQueue.add(data);
		}
	}

	public byte[] dequeueRawData() {
		synchronized (rawDataQueue) {
			byte[] data = null;

			if (!rawDataQueue.isEmpty()) {
				data = rawDataQueue.remove(0);
			}

			return data;
		}
	}

	public boolean hasRawDataQueued() {
		synchronized (rawDataQueue) {
			return !rawDataQueue.isEmpty();
		}
	}

	protected boolean isVirgin() {
		return virgin;
	}

	protected void setVirginStatus(boolean status) {
		this.virgin = status;
	}

	public boolean wasHandshook() {
		return handshook;
	}

	public void setHandshookStatus(boolean status) {
		this.handshook = status;
	}

	public void setPeerForcedClose(boolean peerForceClose) {
		this.peerForcedClose = peerForceClose;
	}

	public boolean wasPeerForcedClosed() {
		return peerForcedClose;
	}

	public boolean isZombie() {
		return zombie;
	}

	public void setZombie(boolean zombie) {
		this.zombie = zombie;
	}

	public Object getSendLock() {
		return sendLock;
	}

	public String getRemoteAddressStr() {
		StringBuffer sb = new StringBuffer();
		InetSocketAddress sockAddr;
		InetAddress addr;

		if (sockCh != null) {
			sockAddr = (InetSocketAddress) sockCh.socket().getRemoteSocketAddress();
			if (sockAddr != null) {
				addr = ((InetSocketAddress) sockAddr).getAddress();

				if (addr != null) {
					sb.append(addr.getHostAddress());
					sb.append(":");
					sb.append(sockAddr.getPort());
				}
				else {
					sb.append("[UNKNOWN]");
				}
			}
			else {
				sb.append("[UNKNOWN]");
			}
		}

		return sb.toString();
	}

	public int getServerPort() {
		int port = -1;

		if (server != null) {
			port = server.getPort();
		}

		return port;
	}

	public String getServerAddress() {
		String address = null;
		InetSocketAddress sockAddr;
		InetAddress addr;

		if (sockCh != null) {
			sockAddr = (InetSocketAddress) sockCh.socket().getLocalSocketAddress();
			if (sockAddr != null) {
				addr = ((InetSocketAddress) sockAddr).getAddress();

				if (addr != null) {
					address = addr.getHostAddress();
				}
			}
		}

		return address;
	}

	public String getRemoteAddress() {
		String address = null;
		InetSocketAddress sockAddr;
		InetAddress addr;

		if (sockCh != null) {
			sockAddr = (InetSocketAddress) sockCh.socket().getRemoteSocketAddress();
			if (sockAddr != null) {
				addr = ((InetSocketAddress) sockAddr).getAddress();

				if (addr != null) {
					address = addr.getHostAddress();
				}
			}
		}

		return address;
	}

	public int getRemotePort() {
		int port = -1;
		InetSocketAddress sockAddr;

		if (sockCh != null) {
			sockAddr = (InetSocketAddress) sockCh.socket().getRemoteSocketAddress();
			if (sockAddr != null) {
				port = sockAddr.getPort();
			}
		}

		return port;
	}

	public void drainRawData() {
		if (hasRawDataQueued()) {
			signalReadReady();

			while (hasRawDataQueued()) {
				SystemUtils.SleepTight(50);
			}
		}
	}

	public void endSessionAsync() {
		Thread t;

		Runnable r = new Runnable() {
			public void run() {
				endSession();
			}
		};

		t = new Thread(r);
		t.start();
	}

}
