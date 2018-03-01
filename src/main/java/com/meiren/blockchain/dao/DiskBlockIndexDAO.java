package com.meiren.blockchain.dao;

import com.meiren.blockchain.dataobject.DiskBlockIndexDO;

/**
 * DiskBlockIndexDAO
 * @author jijc
 */
public interface DiskBlockIndexDAO {
    /**
     * create.
     * @param diskBlockIndexDO {@link DiskBlockIndexDO}
     * @return id
     */
    public Long create(DiskBlockIndexDO diskBlockIndexDO);


    /**
     * findByBlockHash.
     * @param blockHash
     * @return {@link DiskBlockIndexDO}
     */
    public DiskBlockIndexDO findByBlockHash (String blockHash) ;

    /**
     * getLastestBlockIndex.
     * @return {@link DiskBlockIndexDO}
     */
    public DiskBlockIndexDO getLastestBlockIndex () ;
}
