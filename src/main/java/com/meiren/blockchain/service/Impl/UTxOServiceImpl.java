package com.meiren.blockchain.service.Impl;

import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.entity.DiskUTxOIndex;
import com.meiren.blockchain.entity.Transaction;
import com.meiren.blockchain.entity.UTxO;
import com.meiren.blockchain.service.DiskUTxOIndexService;
import com.meiren.blockchain.service.TransactionService;
import com.meiren.blockchain.service.UTxOService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: UTxOServiceImpl
 * @Description: ${todo}
 * @date 2018/4/23 15:06
 */
public class UTxOServiceImpl implements UTxOService{
	@Autowired
	private DiskUTxOIndexService diskUTxOIndexService;
	@Autowired
	private TransactionService transactionService;

	@Override
	public List<UTxO> findByReceiver(String receiver) {
		List<DiskUTxOIndex> diskUTxOIndices = diskUTxOIndexService.readFromDiskByReceiver(receiver);
		//		JsonUtils.printJson(diskUTxOIndices);
		List<UTxO> uTxOS = new ArrayList();
		for(DiskUTxOIndex diskUTxOIndex: diskUTxOIndices){
			Transaction transaction = transactionService.findByTxHash(HashUtils.toHexStringAsLittleEndian(diskUTxOIndex.txHash));
			UTxO uTxO = new UTxO();
			uTxO.setTxHash(HashUtils.toHexStringAsLittleEndian(transaction.getTxHash()));
			uTxO.setIndex(diskUTxOIndex.outIndex);
			uTxO.setValue(transaction.tx_outs[(int) diskUTxOIndex.outIndex].value);
			uTxO.setMerchant(new String(transaction.tx_outs[(int) diskUTxOIndex.outIndex].merchantHash));
			uTxO.setLockTime(transaction.lock_time);
			uTxOS.add(uTxO);
		}
		return uTxOS;
	}
}
