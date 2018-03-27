package com.meiren.blockchain;

import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.util.JsonUtils;
import com.meiren.blockchain.entity.Store;
import com.meiren.blockchain.service.Impl.StoreServiceImpl;
import com.meiren.blockchain.service.StoreService;
import org.junit.Test;

import java.io.IOException;
import java.util.Scanner;

public class StoreServiceTest {

	@Test
	public void test1() throws IOException {
		StoreService storeService = new StoreServiceImpl();
		byte[] result = storeService.buildStore("http://meiren.pic.1.jpg");
		JsonUtils.printJson(result);
		BlockChainInput input = new BlockChainInput(result);
		Store store = new Store(input);
		JsonUtils.printJson(store);
//		BlockChainFileUtils.createFile("store0001", result);
//		byte[] readFromFile = BlockChainFileUtils.readFiletoByteArray("D:\\meiren\\store0001.dat");
//		try (BlockChainInput input = new BlockChainInput(readFromFile)) {
//			Store store = new Store(input);
//			JsonUtils.printJson(store);
//		}
	}

	public static void main(String[] args){

	}

}
