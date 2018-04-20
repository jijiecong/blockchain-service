package com.meiren.blockchain.p2p.message;

import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.entity.Block;
import com.meiren.blockchain.entity.Store;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Build P2P message:
 * https://en.BlockChain.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class BlockMessage extends Message {

	static final Log log = LogFactory.getLog(BlockMessage.class);

	public Block block;

	public BlockMessage() {
		super("block");
	}

	public BlockMessage(byte[] payload) throws IOException {
		super("block");
		try (BlockChainInput input = new BlockChainInput(new ByteArrayInputStream(payload))) {
			this.block = new Block(input);
		}
	}

	@Override
	protected byte[] getPayload() {
		return this.block.toByteArray();
	}

	/**
	 * Validate block hash.
	 */
	public boolean validateHash() {
		byte[] merkleHash = this.block.calculateMerkleHash();
		if (!Arrays.equals(merkleHash, this.block.header.merkleHash)) {
			log.error("Validate merckle hash failed.");
			return false;
		}
		// TODO: validate bits:
//		Store[] stores = this.block.stores;
//		for(Store store : stores){
//			new String(store.storeScript);
//		}
		return true;
	}

	@Override
	public String toString() {
		return "BlockMessage(txnCount=" + this.block.transactions.length + ")";
	}

}
