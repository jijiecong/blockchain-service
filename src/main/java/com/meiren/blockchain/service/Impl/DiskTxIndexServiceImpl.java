package com.meiren.blockchain.service.Impl;

import com.meiren.blockchain.common.io.BlockChainOutput;
import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.dao.DiskTxIndexDAO;
import com.meiren.blockchain.dataobject.DiskTxIndexDO;
import com.meiren.blockchain.entity.Block;
import com.meiren.blockchain.entity.DiskTxIndex;
import com.meiren.blockchain.entity.Transaction;
import com.meiren.blockchain.service.DiskTxIndexService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: DiskTxIndexServiceImpl
 * @Description: ${todo}
 * @date 2018/4/19 14:43
 */
public class DiskTxIndexServiceImpl implements DiskTxIndexService {
	@Autowired
	private DiskTxIndexDAO diskTxIndexDAO;

	@Override
	public void writeToDiskBlock(Block block, int nFile, int nBlockPos) {
		BlockChainOutput output = new BlockChainOutput();
		Transaction[] transactions = block.transactions;
		output.writeVarInt(transactions.length);
		System.out.println("transactions length: " + output.toByteArray().length);
		int nTxOffset = block.header.toByteArray().length + output.toByteArray().length;
		for (Transaction transaction : transactions){
			int txSize = transaction.toByteArray().length;
			DiskTxIndex diskTxIndex = new DiskTxIndex();
			diskTxIndex.txHash = transaction.getTxHash();
			diskTxIndex.nFile = nFile;
			diskTxIndex.nBlockPos = nBlockPos;
			diskTxIndex.nTxOffset = nTxOffset;
			diskTxIndex.txSize = txSize;
			writeToDisk(diskTxIndex);
			nTxOffset = nTxOffset + txSize;
		}
	}

	@Override
	public void writeToDisk(DiskTxIndex diskTxIndex) {
		DiskTxIndexDO diskTxIndexDO = new DiskTxIndexDO();
		diskTxIndexDO.setTxHash(HashUtils.toHexStringAsLittleEndian(diskTxIndex.txHash));
		diskTxIndexDO.setnFile(diskTxIndex.nFile);
		diskTxIndexDO.setnBlockPos(diskTxIndex.nBlockPos);
		diskTxIndexDO.setnTxOffset(diskTxIndex.nTxOffset);
		diskTxIndexDO.setTxSize(diskTxIndex.txSize);
		diskTxIndexDAO.create(diskTxIndexDO);
	}

	@Override
	public DiskTxIndex findByTxHash(String txHash) {
		DiskTxIndexDO diskTxIndexDO = diskTxIndexDAO.findByTxHash(txHash);
		if(diskTxIndexDO == null){
			return null;
		}
		DiskTxIndex diskTxIndex = new DiskTxIndex();
		diskTxIndex.nFile = diskTxIndexDO.getnFile();
		diskTxIndex.nBlockPos = diskTxIndexDO.getnBlockPos();
		diskTxIndex.nTxOffset = diskTxIndexDO.getnTxOffset();
		diskTxIndex.txSize = diskTxIndexDO.getTxSize();
		return diskTxIndex;
	}
}
