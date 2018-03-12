package com.meiren.blockchain;

import com.meiren.blockchain.common.io.BitcoinInput;
import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.entity.Block;
import com.meiren.blockchain.entity.BlockIndex;
import com.meiren.blockchain.entity.DiskBlockIndex;
import com.meiren.blockchain.entity.Store;
import com.meiren.blockchain.service.BlockIndexService;
import com.meiren.blockchain.service.BlockService;
import com.meiren.blockchain.service.DiskBlockIndexService;
import com.meiren.blockchain.service.Impl.BlockServiceImpl;
import com.meiren.blockchain.service.Impl.DiskBlockIndexServiceImpl;
import com.meiren.blockchain.service.Impl.StoreServiceImpl;
import com.meiren.blockchain.service.StoreService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Scanner;

public class BlockServiceTest extends BaseServiceTest{
	ClassPathXmlApplicationContext applicationContext = getApplicationContext();

	StoreService storeService = (StoreService) applicationContext.getBean("storeService");
	DiskBlockIndexService diskBlockIndexService = (DiskBlockIndexService) applicationContext.getBean("diskBlockIndexService");
	BlockService blockService = (BlockService) applicationContext.getBean("blockService");
	BlockIndexService blockIndexService = (BlockIndexService) applicationContext.getBean("blockIndexService");
//	@Autowired
//	private BlockServiceImpl blockService;
//	@Autowired
//	private DiskBlockIndexServiceImpl diskBlockIndexService;
//	@Autowired
//	private StoreServiceImpl storeService;

	@Test
	public void test1() throws IOException {
		String[] strArray = new String[]{
				"http://meiren.pic.5.jpg",
				"http://meiren.pic.345.jpg",
				"http://meiren.pic.54.jpg",
				"http://meiren.pic.453.jpg",
				"http://meiren.pic.64.jpg",
				"http://meiren.pic.6.jpg",
				"http://meiren.pic.63.jpg"};
		Store[] stores = new Store[strArray.length];
		for (int i=0; i< strArray.length; i++) {
			byte[] result = storeService.buildStore(strArray[i]);
			BitcoinInput input = new BitcoinInput(result);
			Store store = new Store(input);
			stores[i] = store;
		}
//		BlockService blockService = new BlockServiceImpl();
		BlockIndex lastestBlockIndex = blockIndexService.getLastestBlockIndex();
		byte[] preHash = lastestBlockIndex.getBlockHash();//HashUtils.toBytesAsLittleEndian("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f");
		Block block = blockService.nextBlock(stores, preHash);
//		JsonUtils.printJson(block);
//		byte[] blockData = block.toByteArray();
//		BitcoinInput input = new BitcoinInput(blockData);
//		Block block1 = new Block(input);
//		JsonUtils.printJson(block1);
		int nFile = diskBlockIndexService.getMaxnFile() + 1;
		blockService.writeToDisk(block, nFile);
//		DiskBlockIndexService diskBlockIndexService = new DiskBlockIndexServiceImpl();
		DiskBlockIndex diskBlockIndex = new DiskBlockIndex();
		diskBlockIndex.pHashBlock = block.getBlockHash();
		diskBlockIndex.nFile = nFile;
		diskBlockIndex.nHeight = lastestBlockIndex.nHeight + 1;
		diskBlockIndex.nextHash = null;
		diskBlockIndex.version = 1;
		diskBlockIndex.prevHash = block.header.prevHash;
		diskBlockIndex.merkleHash = block.calculateMerkleHash();
		diskBlockIndex.timestamp = System.currentTimeMillis();
		diskBlockIndex.bits = block.header.bits;
		diskBlockIndex.nonce = block.header.nonce;
		diskBlockIndexService.writeToDisk(diskBlockIndex);
//		blockService.readFromDisk(1);
	}
	@Test
	public void test2(){
		Block block = blockService.readFromDisk(2);
		System.out.println(new String(block.stores[5].storeScript));
	}

	@Test
	public void test3() throws IOException, InterruptedException {
		blockService.init();
		while (true){
			int i =1;
		}
	}
}
