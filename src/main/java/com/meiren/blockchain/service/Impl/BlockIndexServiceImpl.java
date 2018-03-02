package com.meiren.blockchain.service.Impl;

import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.dao.DiskBlockIndexDAO;
import com.meiren.blockchain.dataobject.DiskBlockIndexDO;
import com.meiren.blockchain.entity.BlockIndex;
import com.meiren.blockchain.entity.DiskBlockIndex;
import com.meiren.blockchain.service.BlockIndexService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: BlockIndexServiceImpl
 * @Description: ${todo}
 * @date 2018/2/27 17:56
 */
public class BlockIndexServiceImpl implements BlockIndexService {

	@Autowired
	private DiskBlockIndexDAO diskBlockIndexDAO;
	public void init() {

	}

	public BlockIndex getLastestBlockIndex() {

		DiskBlockIndexDO diskBlockIndexDO = diskBlockIndexDAO.getLastestBlockIndex();
		DiskBlockIndex diskBlockIndex = new DiskBlockIndex();
		copyFromDO(diskBlockIndex, diskBlockIndexDO);
		BlockIndex blockIndex = diskBlockIndex;


		DiskBlockIndexDO diskBlockIndexDO_prev = diskBlockIndexDAO.findByBlockHash(diskBlockIndexDO.getPrevHash());
		if(diskBlockIndexDO_prev != null){
			DiskBlockIndex diskBlockIndex_prev = new DiskBlockIndex();
			copyFromDO(diskBlockIndex_prev, diskBlockIndexDO_prev);
			BlockIndex blockIndex_pre = new BlockIndex();
			BeanUtils.copyProperties(diskBlockIndex_prev, blockIndex_pre);
			blockIndex.pprev = blockIndex_pre;
		}
		return blockIndex;
	}

	private void copyFromDO(DiskBlockIndex diskBlockIndex, DiskBlockIndexDO diskBlockIndexDO) {
		diskBlockIndex.pHashBlock =HashUtils.toBytesAsLittleEndian(diskBlockIndexDO.getBlockHash());
		diskBlockIndex.nFile = diskBlockIndexDO.getnFile();
		diskBlockIndex.nBlockPos = diskBlockIndexDO.getnBlockPos();
		diskBlockIndex.nHeight = diskBlockIndexDO.getnHeight();
		diskBlockIndex.nextHash = diskBlockIndexDO.getNextHash()==null?null:HashUtils.toBytesAsLittleEndian(diskBlockIndexDO.getNextHash());
		diskBlockIndex.version = diskBlockIndexDO.getVersion();
		diskBlockIndex.prevHash = HashUtils.toBytesAsLittleEndian(diskBlockIndexDO.getPrevHash());
		diskBlockIndex.merkleHash = HashUtils.toBytesAsLittleEndian(diskBlockIndexDO.getMerkleHash());
		diskBlockIndex.timestamp = diskBlockIndexDO.getTimestamp();
		diskBlockIndex.bits = diskBlockIndexDO.getBits();
		diskBlockIndex.nonce = diskBlockIndexDO.getNonce();
	}
}
