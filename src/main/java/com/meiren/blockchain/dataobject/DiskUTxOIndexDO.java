package com.meiren.blockchain.dataobject;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: DiskUTxOIndexDO
 * @Description: ${todo}
 * @date 2018/4/18 11:32
 */
public class DiskUTxOIndexDO {

	public String blockHash;

	public String txHash;

	public Long txIndex;

	public String receiver;

	public Long outIndex;

	public String getBlockHash() {
		return blockHash;
	}

	public void setBlockHash(String blockHash) {
		this.blockHash = blockHash;
	}

	public String getTxHash() {
		return txHash;
	}

	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}

	public Long getTxIndex() {
		return txIndex;
	}

	public void setTxIndex(Long txIndex) {
		this.txIndex = txIndex;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public Long getOutIndex() {
		return outIndex;
	}

	public void setOutIndex(Long outIndex) {
		this.outIndex = outIndex;
	}
}
