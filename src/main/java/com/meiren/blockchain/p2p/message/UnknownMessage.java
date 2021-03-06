package com.meiren.blockchain.p2p.message;

/**
 * Build P2P message:
 * https://en.BlockChain.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class UnknownMessage extends Message {

	private byte[] payload;

	public UnknownMessage(String command, byte[] payload) {
		super(command);
	}

	@Override
	protected byte[] getPayload() {
		return this.payload;
	}

}
