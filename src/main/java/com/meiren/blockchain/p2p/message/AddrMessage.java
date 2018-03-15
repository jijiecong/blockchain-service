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
public class AddrMessage extends Message {

	TimestampNetworkAddress[] addr_list; // (uint32_t + net_addr)[]

	public AddrMessage() {
		super("addr");
		this.addr_list = new TimestampNetworkAddress[0];
	}

	public AddrMessage(byte[] payload) throws IOException {
		super("addr");
		try (BlockChainInput input = new BlockChainInput(new ByteArrayInputStream(payload))) {
			long count = input.readVarInt(); // do not store count
			this.addr_list = new TimestampNetworkAddress[(int) count];
			for (int i = 0; i < this.addr_list.length; i++) {
				addr_list[i] = new TimestampNetworkAddress(input);
			}
		}
	}

	@Override
	protected byte[] getPayload() {
		BlockChainOutput output = new BlockChainOutput();
		output.writeVarInt(this.addr_list.length);
		for (int i = 0; i < this.addr_list.length; i++) {
			TimestampNetworkAddress taddr = this.addr_list[i];
			output.writeUnsignedInt(taddr.timestamp);
			output.write(taddr.address.toByteArray(false));
		}
		return output.toByteArray();
	}

	@Override
	public String toString() {
		return "AddrMessage(count=" + this.addr_list.length + ")";
	}

}
