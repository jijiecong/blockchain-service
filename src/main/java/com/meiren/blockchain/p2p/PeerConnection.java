package com.meiren.blockchain.p2p;

import com.meiren.blockchain.common.constant.BlockChainConstants;
import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.p2p.message.Message;
import com.meiren.blockchain.p2p.message.VersionMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Holds connection to a peer.
 * 
 * @author jijc
 */
public class PeerConnection extends Thread implements MessageSender {

	final Log log = LogFactory.getLog(getClass());

	final String nodeIp;
	final PeerListener listener;
	final BlockingQueue<Message> sendingQueue;

	volatile boolean running = false;
	volatile long timeout = 0;

	public PeerConnection(String nodeIp, PeerListener listener) {
		this.nodeIp = nodeIp;
		this.listener = listener;
		this.sendingQueue = new ArrayBlockingQueue<>(100);
		this.setDaemon(true);
	}

	@Override
	public void run() {
		this.running = true;
		// connect:
		try (Socket sock = new Socket()) {
			sock.connect(new InetSocketAddress(nodeIp, BlockChainConstants.PORT), 100000);
			sock.setSoTimeout(10000);
			try (InputStream input = sock.getInputStream()) {
				try (OutputStream output = sock.getOutputStream()) {
					listener.connected(this.nodeIp);
					setTimeout(60_000);
					// add version message to send automatically:
//					this.sendMessage(new VersionMessage(0, sock.getInetAddress()));
					// loop:
					while (this.running) {
						if (isTimeout()) {
							log.info("Timeout!");
							break;
						}
						// try get message to send:
						Message msg = sendingQueue.poll(1, TimeUnit.SECONDS);
						if (this.running && msg != null) {
							// send message:
							log.info("=> " + msg.toString());
							output.write(msg.toByteArray());
						}
						// try receive message:
						if (this.running && (input.available() > 0)) {
							BlockChainInput in = new BlockChainInput(input);
							Message parsedMsg = Message.Builder.parseMessage(in);
							log.info("<= " + parsedMsg);
							this.listener.onMessage(this, parsedMsg);
						}
					}
				}
			}
			listener.disconnected(this.nodeIp, null);
		} catch (SocketTimeoutException | ConnectException e) {
			log.warn("Connect exception: " + e.getMessage(), e);
			listener.disconnected(this.nodeIp, e);
		} catch (IOException e) {
			log.warn("IOException: " + e.getMessage(), e);
			listener.disconnected(this.nodeIp, e);
		} catch (InterruptedException e) {
			log.warn("Peer connection thread interrupted.");
			listener.disconnected(this.nodeIp, null);
		} catch (Exception e) {
			log.warn("Peer connection exception.", e);
			listener.disconnected(this.nodeIp, null);
		} finally {
			this.running = false;
		}
	}

	public void close() {
		this.running = false;
		try {
			this.join(1000);
		} catch (InterruptedException e) {
			//
		}
	}

	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void sendMessage(Message message) {
		this.sendingQueue.add(message);
	}

	@Override
	public void setTimeout(long timeoutInMillis) {
		this.timeout = System.currentTimeMillis() + timeoutInMillis;
	}

	boolean isTimeout() {
		return System.currentTimeMillis() > this.timeout;
	}
}
