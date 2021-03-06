package com.meiren.blockchain.p2p.message;


import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.io.BlockChainOutput;
import com.meiren.blockchain.entity.TimestampNetworkAddress;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Build P2P message:
 * https://en.BlockChain.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class MasterIpMessage extends Message {

	public String masterIp; // (192.168.1.1 )[]

	public MasterIpMessage(String masterIp) {
		super("masterIp");
		this.masterIp = masterIp;
	}

	public MasterIpMessage(byte[] payload) throws IOException {
		super("masterIp");
		try (BlockChainInput input = new BlockChainInput(new ByteArrayInputStream(payload))) {
			long count = input.readVarInt();
			byte[] masterIpBytes = input.readBytes((int) count);
			this.masterIp = new String(masterIpBytes);
		}
	}

	@Override
	protected byte[] getPayload() {
		BlockChainOutput output = new BlockChainOutput();
		byte[] masterIpBytes = this.masterIp.getBytes();
		output.writeVarInt(masterIpBytes.length);
		output.write(masterIpBytes);
		return output.toByteArray();
	}

	@Override
	public String toString() {
		return "MasterIpMessage(masterIp=" + this.masterIp + ")";
	}

}
