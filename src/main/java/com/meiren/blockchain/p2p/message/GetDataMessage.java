package com.meiren.blockchain.p2p.message;


import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.io.BlockChainOutput;
import com.meiren.blockchain.entity.InvVect;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Build P2P message:
 * https://en.BlockChain.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class GetDataMessage extends Message {

	InvVect[] inventory; // byte[36]

	public GetDataMessage(int type, byte[]... hashes) {
		super("getdata");
		this.inventory = new InvVect[hashes.length];
		for (int i = 0; i < this.inventory.length; i++) {
			InvVect iv = new InvVect();
			iv.type = type;
			iv.hash = hashes[i];
			this.inventory[i] = iv;
		}
	}

	public GetDataMessage(byte[] payload) throws IOException {
		super("getdata");
		try (BlockChainInput input = new BlockChainInput(new ByteArrayInputStream(payload))) {
			long count = input.readVarInt(); // do not store count
			this.inventory = new InvVect[(int) count];
			for (int i = 0; i < this.inventory.length; i++) {
				this.inventory[i] = new InvVect(input);
			}
		}
	}

	@Override
	protected byte[] getPayload() {
		BlockChainOutput output = new BlockChainOutput().writeVarInt(this.inventory.length);
		for (int i = 0; i < this.inventory.length; i++) {
			output.write(this.inventory[i].toByteArray());
		}
		return output.toByteArray();
	}

	@Override
	public String toString() {
		return "GetDataMessage(count=" + this.inventory.length + ")";
	}

}
