package com.meiren.blockchain;

import com.meiren.blockchain.common.constant.BlockChainConstants;
import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.util.BlockChainFileUtils;
import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.common.util.JsonUtils;
import com.meiren.blockchain.entity.*;
import com.meiren.blockchain.service.*;
import com.meiren.blockchain.service.Impl.BlockServiceImpl;
import com.meiren.blockchain.service.Impl.DiskBlockIndexServiceImpl;
import com.meiren.blockchain.service.Impl.StoreServiceImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Scanner;

public class BlockServiceTest extends BaseServiceTest{
	ClassPathXmlApplicationContext applicationContext = getApplicationContext();

	DiskUTxOIndexService diskUTxOIndexService = (DiskUTxOIndexService) applicationContext.getBean("diskUTxOIndexService");
	TransactionService transactionService = (TransactionService) applicationContext.getBean("transactionService");
	DiskBlockIndexService diskBlockIndexService = (DiskBlockIndexService) applicationContext.getBean("diskBlockIndexService");
	BlockService blockService = (BlockService) applicationContext.getBean("blockService");
	BlockIndexService blockIndexService = (BlockIndexService) applicationContext.getBean("blockIndexService");
	DiskTxIndexService diskTxIndexService = (DiskTxIndexService) applicationContext.getBean("diskTxIndexService");

	@Test
	public void test1() throws IOException {
		TxIn[] txIns = new TxIn[1];
		TxIn txIn = new TxIn(BlockChainConstants.ZERO_HASH_BYTES, 0L);
//		TxIn txIn = new TxIn(HashUtils.toBytesAsLittleEndian("3e074488cfebd5aaf3384013955a5a9f9eb9ee2783a9b8249c5d884b67a5dca2"), 0L);
		txIns[0] = txIn;
		TxOut[] txOuts = new TxOut[1];
		TxOut txOut2 = new TxOut(100L, "adnonstop".getBytes(), "adnonstop".getBytes());
		txOuts[0] = txOut2;
//		for (int i=1; i<1000; i++){
//			TxOut txOut = new TxOut(0L, "adnonstop".getBytes(), "adnonstop".getBytes());
//			txOuts[i] = txOut;
//		}
//		TxOut txOut = new TxOut(40L, "adnonstop".getBytes(), "18868890124".getBytes());
//		TxOut txOut2 = new TxOut(1L, "adnonstop".getBytes(), "adnonstop".getBytes());
//		txOuts[0] = txOut;
//		txOuts[1] = txOut2;
		byte[] operator = "adnonstop".getBytes();
		byte[] transaction = transactionService.buildTransaction(txIns, txOuts, operator);
		BlockChainInput blockChainInput = new BlockChainInput(transaction);
		Transaction transaction1 = new Transaction(blockChainInput);
		Transaction[] transactions = new Transaction[1];
		transactions[0] = transaction1;

		BlockIndex lastestBlockIndex = blockIndexService.getLastestBlockIndex();
		byte[] preHash = BlockChainConstants.ZERO_HASH_BYTES;
		int nHeight = 1;
		if(lastestBlockIndex != null){
			preHash = lastestBlockIndex.getBlockHash();//HashUtils.toBytesAsLittleEndian("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f");
			nHeight = lastestBlockIndex.nHeight + 1;
		}
		Block block = blockService.nextBlock(transactions, preHash);

//		JsonUtils.printJson(block);
//		byte[] blockData = block.toByteArray();
//		BlockChainInput input = new BlockChainInput(blockData);
//		Block block1 = new Block(input);
//		JsonUtils.printJson(block1);
		int nFile = diskBlockIndexService.getMaxnFile();
		int nBlockPos = (int) BlockChainFileUtils.getFileSize("D:\\meiren\\blocks\\blk"+nFile+".dat");
		if(nBlockPos > 1024 * 10){
			nFile++;
			nBlockPos = 0;
		}
		blockService.writeToDisk(block, nFile, Boolean.TRUE);
//		DiskBlockIndexService diskBlockIndexService = new DiskBlockIndexServiceImpl();
		DiskBlockIndex diskBlockIndex = new DiskBlockIndex();
		diskBlockIndex.pHashBlock = block.getBlockHash();
		diskBlockIndex.nBlockPos = nBlockPos;
		diskBlockIndex.nBlockSize = block.toByteArray().length;
		diskBlockIndex.nFile = nFile;
		diskBlockIndex.nHeight = nHeight;
		diskBlockIndex.nextHash = null;
		diskBlockIndex.version = 1;
		diskBlockIndex.prevHash = block.header.prevHash;
		diskBlockIndex.merkleHash = block.calculateMerkleHash();
		diskBlockIndex.timestamp = System.currentTimeMillis();
		diskBlockIndex.bits = block.header.bits;
		diskBlockIndex.nonce = block.header.nonce;
		diskBlockIndexService.writeToDisk(diskBlockIndex);
//		blockService.readFromDisk(1);
		diskTxIndexService.writeToDiskBlock(block, nFile, nBlockPos);

		diskUTxOIndexService.writeToDiskBlock(block);
		diskUTxOIndexService.removeFromBlock(block);
	}

	@Test
	public void test2(){
		Block block = blockService.readFromDiskBySize(0, 0, 167);
		System.out.println(HashUtils.toHexStringAsLittleEndian(block.toByteArray()));
	}

	@Test
	public void test3() throws IOException, InterruptedException {
		blockService.init();
		while (true){
			int i =1;
		}
	}

}
