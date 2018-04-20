package com.meiren.blockchain.service.Impl;

import com.meiren.blockchain.common.constant.BlockChainConstants;
import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.io.BlockChainOutput;
import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.common.util.JsonUtils;
import com.meiren.blockchain.dataobject.DiskTxIndexDO;
import com.meiren.blockchain.entity.*;
import com.meiren.blockchain.service.DiskTxIndexService;
import com.meiren.blockchain.service.DiskUTxOIndexService;
import com.meiren.blockchain.service.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: TransactionServiceImpl
 * @Description: ${todo}
 * @date 2018/4/18 11:30
 */
public class TransactionServiceImpl implements TransactionService{

	final Log log = LogFactory.getLog(getClass());

	@Value("${blockindex.disk.path}")
	private String pathDisk;

	@Value("${blockfile.prefix}")
	private String blockFilePrefix;

	@Value("${blockfile.extension}")
	private String blockFileExtension;
	@Autowired
	private DiskTxIndexService diskTxIndexService;
	@Autowired
	private DiskUTxOIndexService diskUTxOIndexService;

	@Override
	public byte[] buildTransaction(TxIn[] txIns, TxOut[] txOuts, byte[] operatorHash) {
		if(!verify(txIns, txOuts, operatorHash)){
			log.info("failed to build transaction! ");
			return null;
		}
		BlockChainOutput output = new BlockChainOutput();
		// store version:
		output.writeInt(BlockChainConstants.TX_VERSION);
		output.writeVarInt(txIns.length);
		for (int i = 0; i < txIns.length; i++) {
			output.write(txIns[i].toByteArray());
		}
		output.writeVarInt(txOuts.length);
		for (int i = 0; i < txOuts.length; i++) {
			output.write(txOuts[i].toByteArray());
		}
		output.writeUnsignedInt(Instant.now().getEpochSecond());
		output.writeVarInt(operatorHash.length);
		output.write(operatorHash);
		return output.toByteArray();
	}

	public Boolean verify(TxIn[] txIns, TxOut[] txOuts, byte[] operatorHash) {
		if(txIns.length == 0){
			log.error("No UTxO alloc.");
			return Boolean.FALSE;
		}
		if(txOuts.length == 0){
			log.error("No pay to specified.");
			return Boolean.FALSE;
		}
		if(operatorHash == null){
			log.error("No operator specified.");
			return Boolean.FALSE;
		}
		long total_unspend = 0L;
		long total_spend = 0L;
		for (TxIn txIn: txIns){
			DiskUTxOIndex diskUTxOIndex = diskUTxOIndexService.readFromDisk(txIn);
			if(diskUTxOIndex == null){
				log.error("UTxO has been spent! txHash: " + HashUtils.toHexStringAsLittleEndian(txIn.hash) + ",outIndex: " + txIn.index);
				return Boolean.FALSE;
			}
			Transaction transaction = findByTxHash(HashUtils.toHexStringAsLittleEndian(txIn.hash));
			total_unspend += transaction.tx_outs[(int) txIn.index].value;
		}
		for (TxOut txOut: txOuts){
			total_spend += txOut.value;
		}
		if (total_unspend != total_spend) {
			log.error("Cannot create transaction for inputs: "+ total_unspend + ",outputs: " + total_spend + ".");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public Transaction findByTxHash(String txHash) {
		DiskTxIndex diskTxIndex = diskTxIndexService.findByTxHash(txHash);
		if(diskTxIndex == null){
			return null;
		}
		return readFromDiskBySize(diskTxIndex.nFile, diskTxIndex.nBlockPos+diskTxIndex.nTxOffset, diskTxIndex.txSize);
	}

	public Transaction readFromDiskBySize(int nFile, int begin, int size){
		String pathBlk = pathDisk;
		Transaction transaction = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(pathBlk+ blockFilePrefix +nFile+blockFileExtension);
			fis.skip(begin);
			byte[] result = new byte[size];
			fis.read(result);
			BlockChainInput input = new BlockChainInput(result);
			transaction = new Transaction(input);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(fis != null)
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		JsonUtils.printJson(transaction);
		return transaction;
	}
}
