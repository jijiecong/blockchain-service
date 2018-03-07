package com.meiren.blockchain.p2p.message;


import com.meiren.blockchain.common.constant.BitcoinConstants;
import com.meiren.blockchain.common.io.BitcoinInput;
import com.meiren.blockchain.common.io.BitcoinOutput;
import com.meiren.blockchain.common.util.HashUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Build P2P message:
 * https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class GetBlocksMessage extends Message {

	int version; // uint32
	byte[][] hashes; // byte[32]
	byte[] hashStop; // hash of the last desired block header; set to zero to
						// get as many blocks as possible (2000)

	public GetBlocksMessage(byte[] firstHash, byte[] hashStop) {
		super("getblocks");
		this.version = BitcoinConstants.PROTOCOL_VERSION;
		this.hashes = new byte[][] { firstHash };
		this.hashStop = hashStop;
	}

	public GetBlocksMessage(byte[] payload) throws IOException {
		super("getblocks");
		try (BitcoinInput input = new BitcoinInput(new ByteArrayInputStream(payload))) {
			this.version = input.readInt();
			long hashCount = input.readVarInt(); // do not keep hash count
			this.hashes = new byte[(int) hashCount][];
			for (int i = 0; i < hashCount; i++) {
				this.hashes[i] = input.readBytes(32);
			}
			this.hashStop = input.readBytes(32);
		}
	}

	@Override
	protected byte[] getPayload() {
		BitcoinOutput output = new BitcoinOutput();
		output.writeInt(this.version).writeVarInt(this.hashes.length);
		for (int i = 0; i < this.hashes.length; i++) {
			output.write(this.hashes[i]);
		}
		output.write(this.hashStop);
		return output.toByteArray();
	}

	@Override
	public String toString() {
		return "GetBlocksMessage(" + this.hashes.length + ": ["
				+ String.join(", ", Arrays.stream(this.hashes).map((hash) -> {
					return HashUtils.toHexStringAsLittleEndian(hash);
				}).limit(10).toArray(String[]::new)) + "], hashStop="
				+ HashUtils.toHexStringAsLittleEndian(this.hashStop) + ")";
	}

	public byte[][] getHashes(){
		return this.hashes;
	}

	public byte[] getHashStop(){
		return this.hashStop;
	}
}
