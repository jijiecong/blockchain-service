package com.meiren.blockchain;

import com.meiren.blockchain.common.constant.BlockChainConstants;
import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.common.util.JsonUtils;
import com.meiren.blockchain.entity.*;
import com.meiren.blockchain.service.DiskTxIndexService;
import com.meiren.blockchain.service.DiskUTxOIndexService;
import com.meiren.blockchain.service.TransactionService;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;
import java.util.List;

public class TransactionServiceTest extends BaseServiceTest{
	ClassPathXmlApplicationContext applicationContext = getApplicationContext();

	TransactionService transactionService = (TransactionService) applicationContext.getBean("transactionService");
	@Test
	public void buildTransaction() throws IOException {
		TxIn[] txIns = new TxIn[1];
//		TxIn txIn = new TxIn(BlockChainConstants.ZERO_HASH_BYTES, 0L);
		TxIn txIn = new TxIn(HashUtils.toBytesAsLittleEndian("3e074488cfebd5aaf3384013955a5a9f9eb9ee2783a9b8249c5d884b67a5dca2"), 0L);
		txIns[0] = txIn;
		TxOut[] txOuts = new TxOut[1];
		TxOut txOut = new TxOut(200L, "adnonstop".getBytes(), "18868890124".getBytes());
		txOuts[0] = txOut;
		byte[] operator = "adnonstop".getBytes();
		byte[] transaction = transactionService.buildTransaction(txIns, txOuts, operator);
		if(transaction == null){
			return;
		}
		BlockChainInput blockChainInput = new BlockChainInput(transaction);
		Transaction transaction1 = new Transaction(blockChainInput);
		JsonUtils.printJson(transaction1);
	}

	@Test
	public void findByTxHash(){
		transactionService.findByTxHash("3e074488cfebd5aaf3384013955a5a9f9eb9ee2783a9b8249c5d884b67a5dca2");

	}
}
