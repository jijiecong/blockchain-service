package com.meiren.blockchain.p2p;

import com.meiren.blockchain.common.util.NetworkUtils;
import com.meiren.blockchain.p2p.message.GetMasterIpMessage;
import com.meiren.blockchain.p2p.message.Message;
import com.meiren.common.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds connection pool.
 * 
 * @author jijc
 */
public class PeerConnectionPool extends Thread implements PeerListener {

	final Log log = LogFactory.getLog(getClass());

	final PeerManager peerManager;
	final int poolSize;
	final MessageListener messageListener;
	final Map<String, PeerConnection> connectionMap = new ConcurrentHashMap<>();
	final String localhost = "192.168.4.223";

	volatile boolean running;
	public PeerConnectionPool(MessageListener messageListener) {
		this(messageListener, 3);
	}

	public PeerConnectionPool(MessageListener messageListener, int poolSize) {
		this.messageListener = messageListener;
		this.poolSize = poolSize;
		this.peerManager = new PeerManager(new File(".peercache2.json"));
	}

	@Override
	public void run() {
		this.running = true;
		while (this.running) {
			for(Peer p : this.peerManager.getPeers()){
				if(p.ip.equals(localhost)){
					continue;
				}
				if (p.ip != null && !connectionMap.containsKey(p.ip)) {
					log.info("Try open new peer connection to " + p.ip + "...");
					PeerConnection conn = new PeerConnection(p.ip, this);
					connectionMap.put(p.ip, conn);
					conn.start();
				}
			}
//			if (connectionMap.size() < this.poolSize) {
//				log.info("Try open new peer connection...");
//				String ip = this.peerManager.getPeer();
//				if (ip != null) {
//					log.info("Try open new peer connection to " + ip + "...");
//					PeerConnection conn = new PeerConnection(ip, this);
//					connectionMap.put(ip, conn);
//					conn.start();
//				} else {
//					log.info("No peers found yet.");
//				}
//			}
			try {
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				break;
			}
		}
		log.info("Closing all peer connections...");
		for (PeerConnection conn : this.connectionMap.values()) {
			conn.close();
		}
	}

	public void close() {
		this.running = false;
		this.interrupt();
		try {
			this.join(5000);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Send message to all connected peers.
	 * 
	 * @param message
	 *            Bitcoin message object.
	 * @return Number of peers sent.
	 */
	public int sendMessage(Message message) {
		int n = 0;
		for (MessageSender sender : connectionMap.values()) {
			sender.sendMessage(message);
			n++;
		}
		return n;
	}

	@Override
	public void onMessage(MessageSender sender, Message message) {
		this.messageListener.onMessage(sender, message);
	}

	@Override
	public void connected(String ip) {
		log.info("Peer " + ip + " connected.");
	}

	@Override
	public void disconnected(String ip, Exception e) {
		if (e == null) {
			log.info("Peer " + ip + " disconnected.");
		} else {
			log.warn("Peer " + ip + " disconnected with error.", e);
		}
		this.connectionMap.remove(ip);
//		this.peerManager.releasePeer(ip, e == null ? 3 : -1);
	}

	public Map<String, PeerConnection> getConnectionMap(){
		return this.connectionMap;
	}
}
