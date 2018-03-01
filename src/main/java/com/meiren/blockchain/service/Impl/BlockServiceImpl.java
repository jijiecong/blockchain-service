package com.meiren.blockchain.service.Impl;


import com.meiren.blockchain.common.io.BitcoinInput;
import com.meiren.blockchain.common.util.BlockChainFileUtils;
import com.meiren.blockchain.common.util.JsonUtils;
import com.meiren.blockchain.entity.Block;
import com.meiren.blockchain.entity.Header;
import com.meiren.blockchain.entity.Store;
import com.meiren.blockchain.service.BlockService;

import java.io.IOException;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: BlockServiceImpl
 * @Description: ${todo}
 * @date 2018/2/27 17:57
 */
public class BlockServiceImpl implements BlockService {
	public void importBlockChain() {

	}

	public Block nextBlock(Store[] stores, byte[] prevHash) {
		Block block = new Block();
		block.stores = stores;
		int version = 1;
		long timestamp = System.currentTimeMillis();
		byte[] merkleHash = block.calculateMerkleHash();
		long nbits = getNbits();
		long nNonceFound = scanHash_CrypToPP();
		Header header= new Header();
		header.version = version;
		header.prevHash = prevHash;
		header.merkleHash = merkleHash;
		header.timestamp = timestamp;
		header.bits = nbits;
		header.nonce =nNonceFound;
		block.header = header;
		return block;
	}

	private long getNbits() {
		//难度计算，暂时取默认
		return 486604799L;
	}

	private long scanHash_CrypToPP() {
		//挖矿算法，暂时取默认
		return 123L;
	}

	public void getLastestBlockHash() {

	}

	public void writeToDisk(Block block, int nFile) {
		String pathBlk = "D:\\meiren\\blocks\\";
		BlockChainFileUtils.createFile(pathBlk, "blk"+nFile, block.toByteArray());
	}

	public Block readFromDisk(int nFile) {
		String pathBlk = "D:\\meiren\\blocks\\";
		byte[] blockdata = BlockChainFileUtils.readFiletoByteArray(pathBlk+"blk"+nFile+".dat");
		BitcoinInput input = new BitcoinInput(blockdata);
		Block block = null;
		try {
			block = new Block(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JsonUtils.printJson(block);
		return block;

	}
}
