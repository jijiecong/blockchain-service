package com.meiren.blockchain.p2p;

import com.meiren.blockchain.p2p.message.Message;

public interface MessageListener {

	void onMessage(MessageSender sender, Message message);

}
