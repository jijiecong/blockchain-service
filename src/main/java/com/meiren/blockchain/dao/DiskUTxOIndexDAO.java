package com.meiren.blockchain.dao;

import com.meiren.blockchain.dataobject.DiskUTxOIndexDO;

import java.util.Map;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: DiskUTxOIndexDAO
 * @Description: ${todo}
 * @date 2018/4/18 11:32
 */
public interface DiskUTxOIndexDAO {

	/**
	 * create.
	 * @param diskUTxOIndexDO {@link DiskUTxOIndexDO}
	 * @return id
	 */
	public Long create(DiskUTxOIndexDO diskUTxOIndexDO);

	/**
	 * del.
	 * @param delMap
	 */
	public Integer delete(Map<String, Object> delMap);

	/**
	 * findByTxHashAndOutIndex.
	 * @param findMap
	 */
	public DiskUTxOIndexDO findByTxHashAndOutIndex(Map<String, Object> findMap);
}
