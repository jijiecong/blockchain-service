package com.meiren.blockchain.dataobject;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: DiskTxIndexDO
 * @Description: ${todo}
 * @date 2018/4/19 14:54
 */
public class DiskTxIndexDO {

	public String txHash;
	//nFile
	public int nFile;
	//
	public int nBlockPos;
	//
	public int nTxOffset;
	//
	public int txSize;

	public String getTxHash() {
		return txHash;
	}

	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}

	public int getnFile() {
		return nFile;
	}

	public void setnFile(int nFile) {
		this.nFile = nFile;
	}

	public int getnBlockPos() {
		return nBlockPos;
	}

	public void setnBlockPos(int nBlockPos) {
		this.nBlockPos = nBlockPos;
	}

	public int getnTxOffset() {
		return nTxOffset;
	}

	public void setnTxOffset(int nTxOffset) {
		this.nTxOffset = nTxOffset;
	}

	public int getTxSize() {
		return txSize;
	}

	public void setTxSize(int txSize) {
		this.txSize = txSize;
	}
}
