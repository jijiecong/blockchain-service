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
public class CheckBlockMessage extends Message {

	public String result; // (192.168.1.1 )[]

	public CheckBlockMessage(String result) {
		super("checkBlock");
		this.result = result;
	}

	public CheckBlockMessage(byte[] payload) throws IOException {
		super("checkBlock");
		try (BlockChainInput input = new BlockChainInput(new ByteArrayInputStream(payload))) {
			long count = input.readVarInt();
			byte[] resultBytes = input.readBytes((int) count);
			this.result = new String(resultBytes);
		}
	}

	@Override
	protected byte[] getPayload() {
		BlockChainOutput output = new BlockChainOutput();
		byte[] resultBytes = this.result.getBytes();
		output.writeVarInt(resultBytes.length);
		output.write(resultBytes);
		return output.toByteArray();
	}

	@Override
	public String toString() {
		return "CheckBlockMessage(result=" + this.result + ")";
	}

}
