package com.meiren.blockchain.p2p.message;


import com.meiren.blockchain.common.io.BitcoinInput;
import com.meiren.blockchain.common.io.BitcoinOutput;
import com.meiren.blockchain.common.util.RandomUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Build P2P message:
 * https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class PingMessage extends Message {

	long nonce;

	public PingMessage() {
		super("ping");
		this.nonce = RandomUtils.randomLong();
	}

	public PingMessage(byte[] payload) throws IOException {
		super("ping");
		try (BitcoinInput input = new BitcoinInput(new ByteArrayInputStream(payload))) {
			this.nonce = input.readLong();
		}
	}

	public long getNonce() {
		return this.nonce;
	}

	@Override
	protected byte[] getPayload() {
		return new BitcoinOutput().writeLong(this.nonce).toByteArray();
	}

	@Override
	public String toString() {
		return "PingMessage(nonce=" + this.nonce + ")";
	}

}
