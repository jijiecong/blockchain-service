package com.meiren.blockchain.dataobject;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: DiskBlockIndexDO
 * @Description: ${todo}
 * @date 2018/2/28 16:47
 */
public class DiskBlockIndexDO {

	//current block hash
	public String blockHash;
	//nFile
	public int nFile;
	//
	public int nBlockPos;
	// block height
	public int nHeight;
	public int nBlockSize;

	//next block hash
	public String nextHash;

	// block header
	public int version;

	//pre block hash
	public String prevHash;

	public String merkleHash;

	public long timestamp;

	public long bits;

	public long nonce;

	public String getBlockHash() {
		return blockHash;
	}

	public void setBlockHash(String blockHash) {
		this.blockHash = blockHash;
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

	public int getnHeight() {
		return nHeight;
	}

	public void setnHeight(int nHeight) {
		this.nHeight = nHeight;
	}

	public int getnBlockSize() {
		return nBlockSize;
	}

	public void setnBlockSize(int nBlockSize) {
		this.nBlockSize = nBlockSize;
	}

	public String getNextHash() {
		return nextHash;
	}

	public void setNextHash(String nextHash) {
		this.nextHash = nextHash;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getPrevHash() {
		return prevHash;
	}

	public void setPrevHash(String prevHash) {
		this.prevHash = prevHash;
	}

	public String getMerkleHash() {
		return merkleHash;
	}

	public void setMerkleHash(String merkleHash) {
		this.merkleHash = merkleHash;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getBits() {
		return bits;
	}

	public void setBits(long bits) {
		this.bits = bits;
	}

	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}
}
