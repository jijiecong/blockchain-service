package com.meiren.blockchain.dao;

import com.meiren.blockchain.dataobject.DiskTxIndexDO;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: DiskTxIndexDAO
 * @Description: ${todo}
 * @date 2018/4/19 14:55
 */
public interface DiskTxIndexDAO {

	/**
	 * create.
	 * @param diskTxIndexDO {@link DiskTxIndexDO}
	 * @return id
	 */
	public Long create(DiskTxIndexDO diskTxIndexDO);

	/**
	 * findByTxHash.
	 * @param txHash
	 * @return DiskTxIndexDO
	 */
	public DiskTxIndexDO findByTxHash(String txHash);
}
