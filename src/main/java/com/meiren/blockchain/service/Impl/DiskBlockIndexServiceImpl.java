package com.meiren.blockchain.service.Impl;

import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.dao.DiskBlockIndexDAO;
import com.meiren.blockchain.dataobject.DiskBlockIndexDO;
import com.meiren.blockchain.entity.DiskBlockIndex;
import com.meiren.blockchain.service.DiskBlockIndexService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: DiskBlockIndexServiceImpl
 * @Description: ${todo}
 * @date 2018/2/27 17:58
 */
public class DiskBlockIndexServiceImpl implements DiskBlockIndexService {
	@Autowired
	private DiskBlockIndexDAO diskBlockIndexDAO;

	public void writeToDisk(DiskBlockIndex diskBlockIndex) {
		//这里用写入mysql替代
		DiskBlockIndexDO diskBlockIndexDO = new DiskBlockIndexDO();
		copyToDO(diskBlockIndex, diskBlockIndexDO);
		diskBlockIndexDAO.create(diskBlockIndexDO);

		Map modifyMap = new HashMap();
		modifyMap.put("blockHash", diskBlockIndexDO.getPrevHash());
		modifyMap.put("nextHash", diskBlockIndexDO.getBlockHash());
		diskBlockIndexDAO.modifyByBlockHash(modifyMap);
	}

	private void copyToDO(DiskBlockIndex diskBlockIndex, DiskBlockIndexDO diskBlockIndexDO) {
		diskBlockIndexDO.setBlockHash(HashUtils.toHexStringAsLittleEndian(diskBlockIndex.pHashBlock));
		diskBlockIndexDO.setnFile(diskBlockIndex.nFile);
		diskBlockIndexDO.setnBlockPos(diskBlockIndex.nBlockPos);
		diskBlockIndexDO.setnHeight(diskBlockIndex.nHeight);
		diskBlockIndexDO.setnBlockSize(diskBlockIndex.nBlockSize);
		diskBlockIndexDO.setNextHash(diskBlockIndex.nextHash == null?null:HashUtils.toHexStringAsLittleEndian(diskBlockIndex.nextHash));
		diskBlockIndexDO.setVersion(diskBlockIndex.version);
		diskBlockIndexDO.setPrevHash(HashUtils.toHexStringAsLittleEndian(diskBlockIndex.prevHash));
		diskBlockIndexDO.setMerkleHash(HashUtils.toHexStringAsLittleEndian(diskBlockIndex.merkleHash));
		diskBlockIndexDO.setTimestamp(diskBlockIndex.timestamp);
		diskBlockIndexDO.setBits(diskBlockIndex.bits);
		diskBlockIndexDO.setNonce(diskBlockIndex.nonce);
	}

	public DiskBlockIndex readFromDisk() {
		//这里用读取mysql替代
		return null;
	}

	@Override
	public int getMaxnFile() {
		return diskBlockIndexDAO.getMaxnFile();
	}
}
