package com.meiren.blockchain.p2p.message;

import java.io.IOException;

/**
 * Build P2P message:
 * https://en.BlockChain.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class GetMasterIpMessage extends Message {

	public GetMasterIpMessage() {
		super("getMasterIp");
	}

	public GetMasterIpMessage(byte[] payload) throws IOException {
		super("getMasterIp");
	}

	@Override
	protected byte[] getPayload() {
		return new byte[0];
	}

	@Override
	public String toString() {
		return "GetMasterIpMessage()";
	}

}
