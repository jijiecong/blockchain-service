# blockchain-service
可信任的有限多节点（联盟链） 
    共识算法√（假设所有节点没人作恶，通过选举得到主节点，区块由主节点生成，使用zookeeper来实现master选举） 
    链同步√ 
    广播√已完成socker通信 
    验证监听到的store有效性 
    验证监听到的block有效性√只验证block的完整性，不对内部的store进行验证 
    block本地缓冲池清理 
    store去重 
