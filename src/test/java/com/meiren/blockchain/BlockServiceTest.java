package com.meiren.blockchain;

import com.meiren.blockchain.common.constant.BlockChainConstants;
import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.common.util.JsonUtils;
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
				"http://meiren.pic.53.jpg",
				"http://meiren.pic.342.jpg",
				"http://meiren.pic.54.jpg",
				"http://meiren.pic.453.jpg",
				"http://meiren.pic.64.jpg",
				"http://meiren.pic.6.jpg",
				"http://meiren.pic.63.jpg"};
		Store[] stores = new Store[strArray.length];
		for (int i=0; i< strArray.length; i++) {
			byte[] result = storeService.buildStore(strArray[i]);
			BlockChainInput input = new BlockChainInput(result);
			Store store = new Store(input);
			stores[i] = store;
		}
//		BlockService blockService = new BlockServiceImpl();
		BlockIndex lastestBlockIndex = blockIndexService.getLastestBlockIndex();
		byte[] preHash = BlockChainConstants.ZERO_HASH_BYTES;
		int nHeight = 1;
		if(lastestBlockIndex != null){
			preHash = lastestBlockIndex.getBlockHash();//HashUtils.toBytesAsLittleEndian("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f");
			nHeight = lastestBlockIndex.nHeight + 1;
		}
		Block block = blockService.nextBlock(stores, preHash);
//		JsonUtils.printJson(block);
//		byte[] blockData = block.toByteArray();
//		BlockChainInput input = new BlockChainInput(blockData);
//		Block block1 = new Block(input);
//		JsonUtils.printJson(block1);
		int nFile = diskBlockIndexService.getMaxnFile() + 1;
		blockService.writeToDisk(block, nFile);
//		DiskBlockIndexService diskBlockIndexService = new DiskBlockIndexServiceImpl();
		DiskBlockIndex diskBlockIndex = new DiskBlockIndex();
		diskBlockIndex.pHashBlock = block.getBlockHash();
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
	}
	@Test
	public void test2(){
		Block block = blockService.readFromDisk(1);
		System.out.println(new String(block.stores[0].storeScript));
		String str = HashUtils.toHexStringAsLittleEndian(block.toByteArray());
		System.out.println(str);
	}

	@Test
	public void test3() throws IOException, InterruptedException {
		blockService.init();
		while (true){
			int i =1;
		}
	}

	@Test
	public void test4() throws IOException {
		Store[] stores = new Store[1];
		byte[] result = storeService.buildStore("meiren_blockchain_service");
		BlockChainInput input = new BlockChainInput(result);
		Store store = new Store(input);
		stores[0] = store;
		Block block = blockService.nextBlock(stores, HashUtils.toBytesAsLittleEndian("0000000000000000000000000000000000000000000000000000000000000000"));
		String str = HashUtils.toHexStringAsLittleEndian(block.toByteArray());
		System.out.println(str);
	}

	@Test
	public void test5() throws IOException {
		String str = "5ab8c2af656369767265735f6e696168636b636f6c625f6e657269656d1900000001010000007b1d00ffff5ab8c2af41d8bd80e94b98775a67adad8c412dea4f0af163b5441c2acb73a640cd99a747000000000000000000000000000000000000000000000000000000000000000000000001";
		BlockChainInput input = new BlockChainInput(HashUtils.toBytesAsLittleEndian(str));
		Block block = new Block(input);
		JsonUtils.printJson(block);
	}
}
