package com.meiren.blockchain.p2p.message;

import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.entity.Store;
import com.meiren.blockchain.entity.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Build P2P message:
 * https://en.BlockChain.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author jijc
 */
public class TransactionMessage extends Message {

	static final Log log = LogFactory.getLog(TransactionMessage.class);

	public Transaction transaction;

	public TransactionMessage() {
		super("transaction");
	}

	public TransactionMessage(byte[] payload) throws IOException {
		super("transaction");
		try (BlockChainInput input = new BlockChainInput(new ByteArrayInputStream(payload))) {
			this.transaction = new Transaction(input);
		}
	}

	@Override
	protected byte[] getPayload() {
		return this.transaction.toByteArray();
	}

	/**
	 * Validate transaction.
	 */
	public boolean validateTransaction() {

		return true;
	}

	@Override
	public String toString() {
		return "TransactionMessage(lockTime=" + this.transaction.lock_time + ")";
	}

}
