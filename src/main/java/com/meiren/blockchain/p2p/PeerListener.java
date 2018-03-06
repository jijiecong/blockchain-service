package com.meiren.blockchain.p2p;

import com.meiren.blockchain.p2p.message.Message;

public interface PeerListener {

	void onMessage(MessageSender sender, Message message);

	void connected(String ip);

	void disconnected(String ip, Exception e);
}
