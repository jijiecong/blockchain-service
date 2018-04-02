## 使用说明 可信任的有限多节点（联盟链）
leader选举时通过zookeeper实现的，所以需要自行搭建zookeeper集群
 
共识算法√（假设所有节点没人作恶，通过选举得到主节点，区块由主节点生成，使用zookeeper来实现master选举）
 
链同步√定时任务  

广播√已完成socker通信

验证监听到的store有效性
 
验证监听到的block有效性√只验证block的完整性，不对内部的store进行验证
 
block本地缓冲池清理√定时任务每天清除一次
 
store来不及更新导致重复打包√打包store进block方法和移除新增block包含的store方法加锁，限制执行顺序
 
选举leader和socketServer都需要阻塞导致初始化有问题√一开始将socketServer做成只有一个连接，优化后将选举leader另外开一条线程以便socketServer可以同时连接多个