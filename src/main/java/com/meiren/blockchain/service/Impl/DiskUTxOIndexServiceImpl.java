package com.meiren.blockchain.service.Impl;

import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.dao.DiskUTxOIndexDAO;
import com.meiren.blockchain.dataobject.DiskUTxOIndexDO;
import com.meiren.blockchain.entity.*;
import com.meiren.blockchain.service.DiskUTxOIndexService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: DiskUTxOIndexServiceImpl
 * @Description: ${todo}
 * @date 2018/4/18 11:31
 */
public class DiskUTxOIndexServiceImpl implements DiskUTxOIndexService{

	@Autowired
	private DiskUTxOIndexDAO diskUTxOIndexDAO;

	@Override
	public void writeToDisk(DiskUTxOIndex diskUTxOIndex) {
		DiskUTxOIndexDO diskUTxOIndexDO = new DiskUTxOIndexDO();
		diskUTxOIndexDO.setBlockHash(HashUtils.toHexStringAsLittleEndian(diskUTxOIndex.blockHash));
		diskUTxOIndexDO.setTxHash(HashUtils.toHexStringAsLittleEndian(diskUTxOIndex.txHash));
		diskUTxOIndexDO.setTxIndex(diskUTxOIndex.txIndex);
		diskUTxOIndexDO.setOutIndex(diskUTxOIndex.outIndex);
		diskUTxOIndexDO.setReceiver(new String(diskUTxOIndex.receiver));
		System.out.println("writeToDisk" + diskUTxOIndexDO.getTxHash());
		diskUTxOIndexDAO.create(diskUTxOIndexDO);
	}

	@Override
	public DiskUTxOIndex readFromDisk(TxIn txIn) {
		Map<String, Object> findMap = new HashMap<String, Object>();
		findMap.put("txHash", HashUtils.toHexStringAsLittleEndian(txIn.hash));
		findMap.put("outIndex", txIn.index);
		DiskUTxOIndexDO  diskUTxOIndexDO = diskUTxOIndexDAO.findByTxHashAndOutIndex(findMap);
		if(diskUTxOIndexDO == null){
			return null;
		}
		DiskUTxOIndex diskUTxOIndex = new DiskUTxOIndex();
		diskUTxOIndex.txHash = HashUtils.toBytesAsLittleEndian(diskUTxOIndexDO.getTxHash());
		diskUTxOIndex.blockHash = HashUtils.toBytesAsLittleEndian(diskUTxOIndexDO.getBlockHash());
		diskUTxOIndex.txIndex = diskUTxOIndexDO.getTxIndex();
		diskUTxOIndex.outIndex = diskUTxOIndexDO.getOutIndex();
		diskUTxOIndex.receiver = diskUTxOIndexDO.getReceiver().getBytes();
		return diskUTxOIndex;
	}

	@Override
	public List<DiskUTxOIndex> readFromDiskByReceiver(String receiver) {
		List<DiskUTxOIndexDO>  diskUTxOIndexDOS = diskUTxOIndexDAO.findByReceiver(receiver);
		if(diskUTxOIndexDOS.size() == 0){
			return null;
		}
		List<DiskUTxOIndex> diskUTxOIndices = new ArrayList<>();
		for(DiskUTxOIndexDO diskUTxOIndexDO: diskUTxOIndexDOS){
			DiskUTxOIndex diskUTxOIndex = new DiskUTxOIndex();
			diskUTxOIndex.txHash = HashUtils.toBytesAsLittleEndian(diskUTxOIndexDO.getTxHash());
			diskUTxOIndex.blockHash = HashUtils.toBytesAsLittleEndian(diskUTxOIndexDO.getBlockHash());
			diskUTxOIndex.txIndex = diskUTxOIndexDO.getTxIndex();
			diskUTxOIndex.outIndex = diskUTxOIndexDO.getOutIndex();
			diskUTxOIndex.receiver = diskUTxOIndexDO.getReceiver().getBytes();
			diskUTxOIndices.add(diskUTxOIndex);
		}
		return diskUTxOIndices;
	}

	@Override
	public void writeToDiskBlock(Block block) {
		Transaction[] transactions = block.transactions;
		long txIndex = 0L;
		long outIndex = 0L;
		byte[] blockHash = block.getBlockHash();
		for (Transaction transaction : transactions){
			byte[] txHash = transaction.getTxHash();
			System.out.println("writeToDiskBlock" + HashUtils.toHexStringAsLittleEndian(txHash));
			TxOut[] txOuts = transaction.tx_outs;
			for(TxOut txOut: txOuts){
				DiskUTxOIndex diskUTxOIndex = new DiskUTxOIndex();
				diskUTxOIndex.blockHash = blockHash;
				diskUTxOIndex.txHash = txHash;
				diskUTxOIndex.txIndex = txIndex;
				diskUTxOIndex.outIndex = outIndex;
				diskUTxOIndex.receiver = txOut.receiver;
				outIndex++;
				writeToDisk(diskUTxOIndex);
			}
			txIndex++;
			outIndex = 0L;
		}
	}

	@Override
	public void removeFromDisk(TxIn txIn) {
		Map<String, Object> delMap = new HashMap();
		delMap.put("txHash", HashUtils.toHexStringAsLittleEndian(txIn.hash));
		delMap.put("outIndex", txIn.index);
		diskUTxOIndexDAO.delete(delMap);
	}

	@Override
	public void removeFromBlock(Block block) {
		Transaction[] transactions = block.transactions;
		for (Transaction transaction : transactions){
			TxIn[] txIns = transaction.tx_ins;
			for(TxIn txIn: txIns){
				if(txIn.isbase()){
					continue;
				}
				removeFromDisk(txIn);
			}
		}
	}
}
