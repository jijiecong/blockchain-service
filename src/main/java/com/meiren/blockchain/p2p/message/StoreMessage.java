package com.meiren.blockchain.p2p.message;

import com.meiren.blockchain.common.io.BitcoinInput;
import com.meiren.blockchain.entity.Block;
import com.meiren.blockchain.entity.Store;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Build P2P message:
 * https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class StoreMessage extends Message {

	static final Log log = LogFactory.getLog(StoreMessage.class);

	public Store store;

	public StoreMessage() {
		super("block");
	}

	public StoreMessage(byte[] payload) throws IOException {
		super("store");
		try (BitcoinInput input = new BitcoinInput(new ByteArrayInputStream(payload))) {
			this.store = new Store(input);
		}
	}

	@Override
	protected byte[] getPayload() {
		return this.store.toByteArray();
	}

	/**
	 * Validate store.
	 */
	public boolean validateStore() {

		return true;
	}

	@Override
	public String toString() {
		return "StoreMessage(lockTime=" + this.store.lock_time + ")";
	}

}
