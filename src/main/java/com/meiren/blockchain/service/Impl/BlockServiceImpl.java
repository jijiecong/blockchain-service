package com.meiren.blockchain.service.Impl;


import com.meiren.blockchain.common.constant.BlockChainConstants;
import com.meiren.blockchain.common.io.BlockChainInput;
import com.meiren.blockchain.common.util.*;
import com.meiren.blockchain.dao.DiskBlockIndexDAO;
import com.meiren.blockchain.dataobject.DiskBlockIndexDO;
import com.meiren.blockchain.entity.*;
import com.meiren.blockchain.p2p.*;
import com.meiren.blockchain.p2p.message.*;
import com.meiren.blockchain.service.*;
import com.meiren.common.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.net.ServerSocket;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: BlockServiceImpl
 * @Description: ${todo}
 * @date 2018/2/27 17:57
 */
public class BlockServiceImpl implements BlockService,MessageListener {

	final Log log = LogFactory.getLog(getClass());
	/**
	 * Global lock. Using hazelcast distributed lock in distributed environment.
	 */
	final Lock lock = new ReentrantLock();

	/**
	 * The latest block hash. Using hazelcast distributed object in distributed
	 * environment.
	 */
	private volatile String lastBlockHash = BlockChainConstants.ZERO_HASH;

	/**
	 * To-be-processed BlockMessage objects.
	 */
	private Deque<Block> deque = new LinkedList<>();

	/**
	 * TransactionMessage objects.
	 */
	private volatile Map<String, Transaction> transactionPool = new LinkedHashMap<>();

	/**
	 * Cached Block data that cannot process now. key=prevHash
	 */
	private Map<String, Block> cache = new LRUCache<>();

	/**
	 * Connection Pool
	 */
	private PeerConnectionPool pool;

	private PeerServer server;

	@Autowired
	private DiskBlockIndexService diskBlockIndexService;
	@Autowired
	private DiskBlockIndexDAO diskBlockIndexDAO;
	@Autowired
	private BlockIndexService blockIndexService;
	@Autowired
	private DiskUTxOIndexService diskUTxOIndexService;
	@Autowired
	private DiskTxIndexService diskTxIndexService;
	@Autowired
	private TransactionService transactionService;

	@Value("${blockindex.disk.path}")
	private String pathDisk;

	@Value("${blockfile.prefix}")
	private String blockFilePrefix;

	@Value("${blockfile.extension}")
	private String blockFileExtension;

	@Value("${takeleadership.port}")
	private String takeLeaderShipPort;

	@Value("${takeleadership.path}")
	private String takeLeaderShipPath;

	@Value("${base.sleep.time}")
	private String baseSleepTime;

	@Value("${max.retries}")
	private String maxRetries;

	@Value("${session.time.out}")
	private String sessionTimeOut;

	@Value("${connection.time.out}")
	private String connectionTimeOut;

	@Value("${name.space}")
	private String nameSpace;

	public void importBlockChain() {

	}

	public Block nextBlock(Transaction[] transactions, byte[] prevHash) {
		Block block = new Block();
		block.transactions = transactions;
		int version = 1;
		long timestamp = Instant.now().getEpochSecond();
		byte[] merkleHash = block.calculateMerkleHash();
		long nbits = getNbits();
		long nNonceFound = scanHash_CrypToPP();
		Header header= new Header();
		header.version = version;
		header.prevHash = prevHash;
		header.merkleHash = merkleHash;
		header.timestamp = timestamp;
		header.bits = nbits;
		header.nonce =nNonceFound;
		block.header = header;
		return block;
	}

	private long getNbits() {
		//难度计算，暂时取默认
		return 486604799L;
	}

	private long scanHash_CrypToPP() {
		//挖矿算法，暂时取默认
		return 123L;
	}

	public void getLastestBlockHash() {

	}

	public void writeToDisk(Block block, int nFile, Boolean append) {
		String pathBlk = pathDisk;
		BlockChainFileUtils.createFile(pathBlk, blockFilePrefix +nFile, block.toByteArray(), append);
	}

	public Block readFromDisk(int nFile, int begin, int end) {
		String pathBlk = pathDisk;
		byte[] blockdata = BlockChainFileUtils.readFiletoByteArray(pathBlk+ blockFilePrefix +nFile+blockFileExtension);
		byte[] result = new byte[end - begin];
		System.arraycopy(blockdata, begin, result, 0, end - begin);
		BlockChainInput input = new BlockChainInput(result);
		Block block = null;
		try {
			block = new Block(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		JsonUtils.printJson(block);
		return block;

	}

	public Block readFromDiskBySize(int nFile, int begin, int size){
		String pathBlk = pathDisk;
		Block block = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(pathBlk+ blockFilePrefix +nFile+blockFileExtension);
			fis.skip(begin);
			byte[] result = new byte[size];
			fis.read(result);
			BlockChainInput input = new BlockChainInput(result);
			block = new Block(input);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		JsonUtils.printJson(block);
		return block;
	}

	@Override
	public void sendTransaction(Transaction transaction) {
		try {
			this.pool.sendMessage(new TransactionMessage(transaction.toByteArray()));
			log.info("send Transaction successfully! ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void init() throws IOException {
		initBlockData();
		initConnectionPool();
		new Thread(new Runnable(){
			@Override
			public void run() {
				leader();
			}
		}).start();

		initServer();

	}

	private void initServer() throws IOException {
		ServerSocket serverSocket = new ServerSocket(BlockChainConstants.PORT);
		log.info("等待其他节点连接...");
		while(true){
			this.server = new PeerServer(serverSocket, this);
			server.start();
		}
	}

	public void destroy() {
		if (this.pool != null) {
			this.pool.close();
		}
	}

	private void initBlockData() throws IOException {
		BlockIndex blockIndex = blockIndexService.getLastestBlockIndex();
		Block last = null;
		if (blockIndex == null) {
			// add genesis block:
			log.info("Add genesis block...");
			BlockChainInput input = new BlockChainInput(BlockChainConstants.GENESIS_BLOCK_DATA);
			last = new Block(input);
			processNextBlock(last);
		}else {
			int nFile = blockIndex.nFile;
			int begin = blockIndex.nBlockPos;
			int size = blockIndex.nBlockSize;
			// get last block:
			last = readFromDiskBySize(nFile, begin, size);
		}
		this.lastBlockHash = HashUtils.toHexStringAsLittleEndian(last.getBlockHash());
		log.info("Last block: " + this.lastBlockHash + " created at "
				+ Instant.ofEpochSecond(last.header.timestamp).atZone(ZoneId.systemDefault()).toString());
	}

	private void initConnectionPool() {
		this.pool = new PeerConnectionPool(this);
		this.pool.start();
	}

	/**
	 * process next block from queue.
	 */
	//1.initialDelay :初次执行任务之前需要等待的时间
	//2.fixedRate:执行频率，每隔多少时间就启动任务，不管该任务是否启动完成
	@Scheduled(initialDelay = 10_000, fixedRate = 1_000)
	public void processPendingBlock() {
		lock.lock();
		try {
			Block block = this.deque.pollFirst();
			if (block == null) {
				log.info("scheduled process: nothing to do.");
				return;
			}
			this.processNextBlock(block);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * check local blockChain is lastest and synchronizeBlockChain from peers.
	 */
	//1.initialDelay :初次执行任务之前需要等待的时间
	//2.fixedRate:执行频率，每隔多少时间就启动任务，不管该任务是否启动完成
	@Scheduled(initialDelay = 10_000, fixedRate = 10_000)
	public void synchronizeBlockChain() {
		lock.lock();
		log.info("send getBlockMessage to synchronize local blockChain begin...");
		try {
			this.pool.sendMessage(new GetBlocksMessage(HashUtils.toBytesAsLittleEndian(this.lastBlockHash),
										BlockChainConstants.ZERO_HASH_BYTES));
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 每天定时清除一次缓存中无效的block
	 * */
	@Scheduled(initialDelay = 10_000, fixedRate = 24*60*60*1000)
	public void removeInvalidBlockFromCache() {
		lock.lock();
		log.info("remove invalid block from cache begin...");
		try {
			for(String prevHash : cache.keySet()){
				DiskBlockIndexDO diskBlockIndexDO = diskBlockIndexDAO.findByPrevBlockHash(prevHash);
				if (diskBlockIndexDO != null) {
					cache.remove(prevHash);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	//定义一个按一定频率执行的定时任务，每隔10分钟执行一次，延迟10秒执行
//	@Scheduled(initialDelay =10*1000 , fixedRate = 10*60*1000)
	public Block packTransactionsIntoBlock() {
//		if(this.masterIp.equals(NetworkUtils.getLocalInetAddress().getHostAddress())){
			lock.lock();
			try {
				//当前store池有多少待处理的
				int size = this.transactionPool.size();
				log.info("pack "+size+" transactions into block!");
				if(size == 0) {
					return null;
				}
				if(size > 1000){
					size = 1000;
				}
				Transaction[] transactions = new Transaction[size];
				Iterator<Map.Entry<String, Transaction>> it = this.transactionPool.entrySet().iterator();
				int index = 0;
				while (it.hasNext()) {
					Map.Entry<String, Transaction> entry = it.next();
					//				System.out.println(entry.getKey() + ":" + entry.getValue());
					transactions[index] = entry.getValue();
					//				it.remove(); //删除元素

					index++;
					if(index == size){
						break;
					}
				}
				Block newBlock = nextBlock(transactions, HashUtils.toBytesAsLittleEndian(this.lastBlockHash));
				log.info("pack a new block, blockHash:"+ HashUtils.toHexStringAsLittleEndian(newBlock.getBlockHash()));
				try {
					this.pool.sendMessage(new NewBlockMessage(newBlock.toByteArray()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return newBlock;
			} finally {
				lock.unlock();
			}
//		}
	}

	/**
	 * handle block which is received from peer to join blockDeque
	 */

	public Boolean processBlockFromPeer(Block block) {
		final String hash = HashUtils.toHexStringAsLittleEndian(block.getBlockHash());
		log.info("process block " + hash + " from peer...");
		lock.lock();
		try {
			// already processed?
			DiskBlockIndexDO diskBlockIndexDO = diskBlockIndexDAO.findByBlockHash(hash);
			if (diskBlockIndexDO != null) {
				log.info("block " + hash + " was already processed.");
				return false;
			}
//			this.deque.contains(block);
			// add to cache first:
			String prevHash = HashUtils.toHexStringAsLittleEndian(block.header.prevHash);
			cache.put(prevHash, block);
			// get last hash:
			String lastHash = deque.isEmpty() ? this.lastBlockHash
					: HashUtils.toHexStringAsLittleEndian(deque.peekLast().getBlockHash());
			// try get all to queue:
			while (true) {
				Block next = cache.remove(lastHash);
				if (next == null) {
					break;
				}
				this.deque.offerLast(next);
				lastHash = HashUtils.toHexStringAsLittleEndian(next.getBlockHash());
			}
			removeTransactions(block);
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * handle store which is received from peer to join StorePool
	 */
	private Boolean joinTransactionPoolFromPeer(Transaction transaction) {
		final String hash = HashUtils.toHexStringAsLittleEndian(transaction.getTxHash());
		log.info("transaction join poor " + hash + " from peer...");
		lock.lock();
		try {
			// already exist?
			if(this.transactionPool.containsKey(hash)){
				log.info("transaction " + hash + " was already in pool.");
				return false;
			}
			this.transactionPool.put(hash, transaction);
			log.info("transaction " + hash + " was added in pool.");
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * handle getBlock request which is received from peer to send the block which is available
	 * update the blockChain
	 */
	private void handelGetBlockMessageFromPeer(GetBlocksMessage getBlocksMsg, MessageSender sender) {
		try {
//			log.info(Arrays.equals(getBlocksMsg.getHashStop(), BlockChainConstants.ZERO_HASH_BYTES));
			if(Arrays.equals(getBlocksMsg.getHashStop(), BlockChainConstants.ZERO_HASH_BYTES)){
				String blockHash = HashUtils.toHexStringAsLittleEndian(getBlocksMsg.getHashes()[0]);
				while(true){
						DiskBlockIndexDO diskBlockIndexDO = diskBlockIndexDAO.findByPrevBlockHash(blockHash);
					if(diskBlockIndexDO != null){
						Block block = readFromDiskBySize(diskBlockIndexDO.getnFile(), diskBlockIndexDO.getnBlockPos(), diskBlockIndexDO.getnBlockSize());
						sender.sendMessage(new BlockMessage(block.toByteArray()));
						if(StringUtils.isBlank(diskBlockIndexDO.getNextHash())){
							break;
						}
						blockHash = diskBlockIndexDO.getBlockHash();
					}else {
						break;
					}
				}
			}else {
				for(byte[] hash : getBlocksMsg.getHashes()){
					String blockHash = HashUtils.toHexStringAsLittleEndian(hash);
					DiskBlockIndexDO diskBlockIndexDO = diskBlockIndexDAO.findByPrevBlockHash(blockHash);
					if(diskBlockIndexDO != null){
						Block block = readFromDiskBySize(diskBlockIndexDO.getnFile(), diskBlockIndexDO.getnBlockPos(), diskBlockIndexDO.getnBlockSize());
						sender.sendMessage(new BlockMessage(block.toByteArray()));
					}
				}
			}
			log.info("handle the getBlock request successful!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process new block. write into disk
	 *
	 * @param block
	 */
	public void processNextBlock(Block block) {
		String hash = HashUtils.toHexStringAsLittleEndian(block.getBlockHash());
		log.info("Process next block " + hash + "...");
		lock.lock();
		try {
			// check prevHash:
			String prevHash = HashUtils.toHexStringAsLittleEndian(block.header.prevHash);
			if (!this.lastBlockHash.equals(prevHash)) {
				log.warn("Validate block failed: expected prevHash = " + this.lastBlockHash + ", actual = " + prevHash);
				// cannot continue process:
				this.deque.clear();
				return;
			}
			// check merkle root:
			String actualMerkle = HashUtils.toHexStringAsLittleEndian(block.calculateMerkleHash());
			String expectedMerkle = HashUtils.toHexStringAsLittleEndian(block.header.merkleHash);
			if (!actualMerkle.equals(expectedMerkle)) {
				log.error("Invalid merkle hash: expected = " + expectedMerkle + ", actual = " + actualMerkle);
				// cannot continue process:
				this.deque.clear();
				return;
			}
			// check stores:
			if (!checkTransactions(block)) {
				log.error("Check transactions failed.");
				this.deque.clear();
				return;
			}
			int nFile = diskBlockIndexService.getMaxnFile();
			BlockIndex lastestBlockIndex = blockIndexService.getLastestBlockIndex();
			int nHeight = 1;

			if(lastestBlockIndex != null){
				nHeight = lastestBlockIndex.nHeight + 1;
			}
			int nBlockPos = (int) BlockChainFileUtils.getFileSize(pathDisk + blockFilePrefix+nFile+blockFileExtension);

			if(nBlockPos > 1024 * 1024 * 10 || nFile == 0){//如果文件已经大于10M，写入新文件中
				nFile++;
				nBlockPos = 0;
			}
			writeToDisk(block, nFile, Boolean.TRUE);
			this.lastBlockHash = hash;

			DiskBlockIndex diskBlockIndex = new DiskBlockIndex();
			diskBlockIndex.pHashBlock = block.getBlockHash();
			diskBlockIndex.nFile = nFile;
			diskBlockIndex.nHeight = nHeight;
			diskBlockIndex.nBlockPos = nBlockPos;
			diskBlockIndex.nBlockSize = block.toByteArray().length;
			diskBlockIndex.nextHash = null;
			diskBlockIndex.version = 1;
			diskBlockIndex.prevHash = block.header.prevHash;
			diskBlockIndex.merkleHash = block.calculateMerkleHash();
			diskBlockIndex.timestamp = System.currentTimeMillis();
			diskBlockIndex.bits = block.header.bits;
			diskBlockIndex.nonce = block.header.nonce;
			diskBlockIndexService.writeToDisk(diskBlockIndex);
			log.info("Added block: " + hash);
			diskTxIndexService.writeToDiskBlock(block, nFile, nBlockPos);
			log.info("update the tx index in block: " + hash);
			diskUTxOIndexService.writeToDiskBlock(block);
			diskUTxOIndexService.removeFromBlock(block);
			log.info("update the utxo index in block: " + hash);

		} finally {
			lock.unlock();
		}
	}

	/**
	 * check the stores in Block which is received from peer
	 * */
	private boolean checkTransactions(Block block) {
		Transaction[] transactions = block.transactions;
		for (Transaction transaction: transactions){
			if(!transactionService.verify(transaction.tx_ins, transaction.tx_outs, transaction.operatorHash)){
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * remove store from storesPool
	 */
	public boolean removeTransactions(Block block){
		for(Transaction transaction :block.transactions){
			this.transactionPool.remove(HashUtils.toHexStringAsLittleEndian(transaction.getTxHash()));
		}
		return true;
	}

	public String masterIp = "";

	public String localhostIp = NetworkUtils.getLocalInetAddress().getHostAddress();
	/**
	 * Handle received message from peer.
	 */
	@Override
	public void onMessage(MessageSender sender, Message msg) {

		if (msg instanceof PingMessage) {
			sender.sendMessage(new PongMessage(((PingMessage) msg).getNonce()));
			return;
		}
		if (msg instanceof VerAckMessage) {
			return;
		}
		if (msg instanceof VersionMessage) {
			sender.sendMessage(new VerAckMessage());

			return;
		}
		if (msg instanceof InvMessage) {
			InvMessage inv = (InvMessage) msg;
			byte[][] hashes = inv.getBlockHashes();
			if (hashes.length > 0) {
				for (byte[] hash : hashes) {
					log.info("InvMessage::block hash: " + HashUtils.toHexStringAsLittleEndian(hash));
				}
				sender.sendMessage(new GetDataMessage(InvVect.MSG_BLOCK, hashes));
			}
		}
		if (msg instanceof BlockMessage) {
			sender.setTimeout(60*1000);
			BlockMessage blockMsg = (BlockMessage) msg;
			log.info("Get block data: " + HashUtils.toHexStringAsLittleEndian(blockMsg.block.getBlockHash()));
			if(blockMsg.validateHash() ){//&& checkTransactions(blockMsg.block)
				processBlockFromPeer(blockMsg.block);
			}
		}
		if (msg instanceof TransactionMessage) {
			TransactionMessage transactionMsg = (TransactionMessage) msg;
			if(transactionMsg.validateTransaction()){
				if(joinTransactionPoolFromPeer(transactionMsg.transaction)){
					this.pool.sendMessage(transactionMsg);
				}
			}
		}
		if (msg instanceof GetBlocksMessage) {
			//			sender.setTimeout(60*1000);
			GetBlocksMessage getBlocksMsg = (GetBlocksMessage) msg;
			handelGetBlockMessageFromPeer(getBlocksMsg, sender);
		}
		if (msg instanceof NewBlockMessage) {
			sender.setTimeout(60*1000);
			NewBlockMessage newBlockMsg = (NewBlockMessage) msg;
			log.info("Get new block that is waiting to check data: " + HashUtils.toHexStringAsLittleEndian(newBlockMsg.block.getBlockHash()));
			if(newBlockMsg.validateHash() && this.lastBlockHash.equals(HashUtils.toHexStringAsLittleEndian(newBlockMsg.block.header.prevHash))
					&& checkTransactions(newBlockMsg.block)){
				sender.sendMessage(new CheckBlockMessage("success"));
			}else {
				sender.sendMessage(new CheckBlockMessage("failed"));
			}
		}
		if (msg instanceof CheckBlockMessage) {
			CheckBlockMessage checkBlockMsg = (CheckBlockMessage) msg;
			log.info("Get checkBlock data, result: " + checkBlockMsg.result);
			handleCheckBlockMessage(checkBlockMsg);
		}
	}

	private synchronized void handleCheckBlockMessage(CheckBlockMessage checkBlockMsg) {
		if(checkBlockMsg.result.equals("success")){
			this.countSure++;
		}
		this.countAllConn++;
		if(countAllConn == this.pool.getConnectionMap().size()){
			this.countAllConn = 0;
			this.waitCheckBlock = false;
		}
	}

	private volatile boolean waitCheckBlock = true;
	private volatile int countSure = 0;
	private volatile int countAllConn = 0;
	/**
	 * leader节点生成block
	 * */
	public void leader(){

		List<LeaderSelector> selectors = new ArrayList<>();
		List<CuratorFramework> clients = new ArrayList<>();
		List<String> ips = new ArrayList<>();
		ips.add("192.168.4.223");
		String ip = "192.168.4.223";
		try {
//			initServer();
			Thread.sleep(3000);

			//			for (String ip : ips) {
			CuratorFramework client = getClient(ip);
			clients.add(client);

			final String name = "client#" + ip;
			LeaderSelector leaderSelector = new LeaderSelector(client, takeLeaderShipPath, new LeaderSelectorListener() {
				@Override
				public void takeLeadership(CuratorFramework client) throws Exception {
					System.out.println(name + ":I am leader.");
					waitCheckBlock = true;
					Block block = packTransactionsIntoBlock();
					if(block == null){
						Thread.sleep(10000);
						return;
					}
//					System.out.println(waitCheckBlock+ "============"+countSure);
					//睡一段时间，否则出现waitCheckBlock或者countSure获取不到最新值，用volatile关键字修饰
//					Thread.sleep(3000);
//					System.out.println(waitCheckBlock+ "============"+countSure);
					while (waitCheckBlock){
					}//阻塞，直到其他节点返回结果
					System.out.println("go on ........countSure:"+countSure);
					if (countSure >= (pool.getConnectionMap().size()/2 + 1)){
						countSure = 0;
						pool.sendMessage(new BlockMessage(block.toByteArray()));

						processNextBlock(block);
						removeTransactions(block);
						/**
						 * 这里可以处理保存每个图片对应的blockHash和在block对应的index，用于查询
						 * */
					}
					Thread.sleep(10000);
				}

				@Override
				public void stateChanged(CuratorFramework client, ConnectionState newState) {

				}
			});

			leaderSelector.autoRequeue();
			leaderSelector.start();
			selectors.add(leaderSelector);

			//			}
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			for(CuratorFramework client : clients){
				CloseableUtils.closeQuietly(client);
			}

			for(LeaderSelector selector : selectors){
				CloseableUtils.closeQuietly(selector);
			}

		}
	}

	private CuratorFramework getClient(String ip) {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(Integer.parseInt(baseSleepTime), Integer.parseInt(maxRetries));
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString(ip + ":" + takeLeaderShipPort)
				.retryPolicy(retryPolicy).sessionTimeoutMs(Integer.parseInt(sessionTimeOut)).connectionTimeoutMs(Integer.parseInt(connectionTimeOut))
				.namespace(nameSpace).build();
		client.start();
		return client;
	}
}
