package com.meiren.blockchain.p2p.message;


import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.io.BlockChainOutput;
import com.meiren.blockchain.common.util.RandomUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Build P2P message:
 * https://en.BlockChain.it/wiki/Protocol_documentation#Message_structure
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
		try (BlockChainInput input = new BlockChainInput(new ByteArrayInputStream(payload))) {
			this.nonce = input.readLong();
		}
	}

	public long getNonce() {
		return this.nonce;
	}

	@Override
	protected byte[] getPayload() {
		return new BlockChainOutput().writeLong(this.nonce).toByteArray();
	}

	@Override
	public String toString() {
		return "PingMessage(nonce=" + this.nonce + ")";
	}

}
