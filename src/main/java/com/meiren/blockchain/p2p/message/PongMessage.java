package com.meiren.blockchain.p2p.message;


import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.io.BlockChainOutput;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Build P2P message:
 * https://en.BlockChain.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class PongMessage extends Message {

	long nonce;

	public PongMessage(long nonce) {
		super("pong");
		this.nonce = nonce;
	}

	public PongMessage(byte[] payload) throws IOException {
		super("pong");
		try (BlockChainInput input = new BlockChainInput(new ByteArrayInputStream(payload))) {
			this.nonce = input.readLong();
		}
	}

	@Override
	protected byte[] getPayload() {
		return new BlockChainOutput().writeLong(this.nonce).toByteArray();
	}

	@Override
	public String toString() {
		return "PongMessage(nonce=" + this.nonce + ")";
	}

}
