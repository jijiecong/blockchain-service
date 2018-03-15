package com.meiren.blockchain.p2p;

import com.meiren.blockchain.common.constant.BlockChainConstants;
import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.p2p.MessageSender;
import com.meiren.blockchain.p2p.PeerListener;
import com.meiren.blockchain.p2p.message.Message;
import com.meiren.blockchain.p2p.message.VersionMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.net.SocketServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Holds connection to a peer.
 * 
 * @author jijc
 */
public class PeerServer extends Thread implements MessageSender {

	final Log log = LogFactory.getLog(getClass());
	volatile boolean running = false;
	private Socket client ;
	final BlockingQueue<Message> sendingQueue;
	MessageListener listener;

	public PeerServer(ServerSocket serverSocket, MessageListener listener) {
		try {
			this.client = serverSocket.accept();
		} catch (IOException e) {
			this.close();
			log.info(e.getMessage());
		}
		this.listener = listener;
		this.sendingQueue = new ArrayBlockingQueue<>(100);
	}

	@Override
	public void run() {
		try {
			this.running = true;
			InputStream input = client.getInputStream();
			OutputStream output = client.getOutputStream();
			while (running) {
				// try get message to send:
				Message msg = sendingQueue.poll(1, TimeUnit.SECONDS);
				if (this.running && msg != null) {
					// send message:
					log.info("=> " + msg.toString());
					output.write(msg.toByteArray());
				}
				if (this.running && (input.available() > 0)) {
					BlockChainInput in = new BlockChainInput(input);
					Message parsedMsg = Message.Builder.parseMessage(in);
					log.info("<= " + parsedMsg);
					this.listener.onMessage(this, parsedMsg);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.running = false;
		}
	}

	@Override public void sendMessage(Message message) {
		this.sendingQueue.add(message);
	}

	@Override public void setTimeout(long timeoutInMillis) {

	}

	@Override
	public void close(){
		this.running = false;
		this.interrupt();
		try {
			this.join(1000);
		} catch (InterruptedException e) {
			//
		}
	}
}
