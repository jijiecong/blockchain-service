package com.meiren.blockchain.service.Impl;


import com.meiren.blockchain.common.constant.BitcoinConstants;
import com.meiren.blockchain.common.io.BitcoinInput;
import com.meiren.blockchain.common.util.BlockChainFileUtils;
import com.meiren.blockchain.common.util.HashUtils;
import com.meiren.blockchain.common.util.JsonUtils;
import com.meiren.blockchain.common.util.LRUCache;
import com.meiren.blockchain.dao.DiskBlockIndexDAO;
import com.meiren.blockchain.dataobject.DiskBlockIndexDO;
import com.meiren.blockchain.entity.Block;
import com.meiren.blockchain.entity.Header;
import com.meiren.blockchain.entity.InvVect;
import com.meiren.blockchain.entity.Store;
import com.meiren.blockchain.p2p.MessageListener;
import com.meiren.blockchain.p2p.MessageSender;
import com.meiren.blockchain.p2p.PeerConnectionPool;
import com.meiren.blockchain.p2p.message.*;
import com.meiren.blockchain.service.BlockService;
import com.meiren.blockchain.service.DiskBlockIndexService;
import com.meiren.blockchain.service.StoreService;
import com.meiren.common.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
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
	private volatile String lastBlockHash = BitcoinConstants.ZERO_HASH;

	/**
	 * To-be-processed BlockMessage objects.
	 */
	private Deque<Block> deque = new LinkedList<>();

	/**
	 * StoreMessage objects.
	 */
	private Map<String, Store> storePool = new LinkedHashMap<>();

	/**
	 * Cached Block data that cannot process now. key=prevHash
	 */
	private Map<String, Block> cache = new LRUCache<>();

	/**
	 * Connection Pool
	 */
	private PeerConnectionPool pool;

	@Autowired
	private DiskBlockIndexService diskBlockIndexService;
	@Autowired
	private StoreService storeService;
	@Autowired
	private DiskBlockIndexDAO diskBlockIndexDAO;

	public void importBlockChain() {

	}

	public Block nextBlock(Store[] stores, byte[] prevHash) {
		Block block = new Block();
		block.stores = stores;
		int version = 1;
		long timestamp = System.currentTimeMillis();
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

	public void writeToDisk(Block block, int nFile) {
		String pathBlk = "D:\\meiren\\blocks\\";
		BlockChainFileUtils.createFile(pathBlk, "blk"+nFile, block.toByteArray());
	}

	public Block readFromDisk(int nFile) {
		String pathBlk = "D:\\meiren\\blocks\\";
		byte[] blockdata = BlockChainFileUtils.readFiletoByteArray(pathBlk+"blk"+nFile+".dat");
		BitcoinInput input = new BitcoinInput(blockdata);
		Block block = null;
		try {
			block = new Block(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JsonUtils.printJson(block);
		return block;

	}

	public void init() throws IOException {
		initBlockData();
		initConnectionPool();
	}

	public void destroy() {
		if (this.pool != null) {
			this.pool.close();
		}
	}

	public void sendStore(Store store){
		StoreMessage storeMessage = null;
		try {
			storeMessage = new StoreMessage(store.toByteArray());
			this.pool.sendMessage(storeMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initBlockData() throws IOException {
		int nFile = diskBlockIndexService.getMaxnFile();
		if (nFile == 0) {
			// add genesis block:
			log.info("Add genesis block...");
//			try (BitcoinInput input = new BitcoinInput(BitcoinConstants.GENESIS_BLOCK_DATA)) {
//				Block gb = new Block(input);
//				processNextBlock(gb);
//			}

			Store[] stores = new Store[1];
			byte[] result = storeService.buildStore("meiren_blockchain_service");
			BitcoinInput input = new BitcoinInput(result);
			Store store = new Store(input);
			stores[1] = store;
			Block block = nextBlock(stores, HashUtils.toBytesAsLittleEndian("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"));
			processNextBlock(block);
		}
		// get last block:
		Block last = readFromDisk(nFile);
		this.lastBlockHash = HashUtils.toHexStringAsLittleEndian(last.getBlockHash());
		log.info("Last block: " + this.lastBlockHash + " created at "
				+ Instant.ofEpochSecond(last.header.timestamp).atZone(ZoneId.systemDefault()).toString());
		long n = (Instant.now().getEpochSecond() - last.header.timestamp) / 600;
		log.info("Needs to synchronize about " + n + " blocks...");
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

//	//定义一个按一定频率执行的定时任务，每隔10分钟执行一次，延迟10秒执行
//	@Scheduled(initialDelay =10*1000 , fixedRate = 10*60*1000)
	public void packStoresIntoBlock() {
		lock.lock();
		try {
			//当前store池有多少待处理的
			int size = this.storePool.size();
			Store[] stores = new Store[size];
			Iterator<Map.Entry<String, Store>> it = this.storePool.entrySet().iterator();
			int index = 0;
			while (it.hasNext()) {
				Map.Entry<String, Store> entry = it.next();
//				System.out.println(entry.getKey() + ":" + entry.getValue());
				stores[index] = entry.getValue();
				it.remove(); //删除元素
				index++;
				if(index == size){
					break;
				}
			}
			Block newBlock = nextBlock(stores, HashUtils.toBytesAsLittleEndian(this.lastBlockHash));
			log.info("pack a new block, blockHash:"+ HashUtils.toHexStringAsLittleEndian(newBlock.getBlockHash()));
			try {
				this.pool.sendMessage(new NewBlockMessage(newBlock.toByteArray()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			lock.unlock();
		}
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
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * handle store which is received from peer to join StorePool
	 */
	private Boolean joinStorePoolFromPeer(Store store) {
		final String hash = HashUtils.toHexStringAsLittleEndian(store.getStoreHash());
		log.info("store join poor " + hash + " from peer...");
		lock.lock();
		try {
			// already exist?
			if(this.storePool.containsKey(hash)){
				log.info("store " + hash + " was already in pool.");
				return false;
			}
			this.storePool.put(hash, store);
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
			if(getBlocksMsg.getHashStop().equals(BitcoinConstants.ZERO_HASH_BYTES)){
				String blockHash = HashUtils.toHexStringAsLittleEndian(getBlocksMsg.getHashes()[0]);
				while(true){
					DiskBlockIndexDO diskBlockIndexDO = diskBlockIndexDAO.findByPrevBlockHash(blockHash);
					if(diskBlockIndexDO!=null){
						Block block = readFromDisk(diskBlockIndexDO.getnFile());
						sender.sendMessage(new BlockMessage(block.toByteArray()));
						if(StringUtils.isBlank(diskBlockIndexDO.getNextHash())){
							break;
						}
					}else {
						break;
					}
				}
			}else {
				for(byte[] hash : getBlocksMsg.getHashes()){
					String blockHash = HashUtils.toHexStringAsLittleEndian(hash);
					DiskBlockIndexDO diskBlockIndexDO = diskBlockIndexDAO.findByPrevBlockHash(blockHash);
					if(diskBlockIndexDO != null){
						Block block = readFromDisk(diskBlockIndexDO.getnFile());
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
				System.exit(1);
				return;
			}
			// check merkle root:
			String actualMerkle = HashUtils.toHexStringAsLittleEndian(block.calculateMerkleHash());
			String expectedMerkle = HashUtils.toHexStringAsLittleEndian(block.header.merkleHash);
			if (!actualMerkle.equals(expectedMerkle)) {
				log.error("Invalid merkle hash: expected = " + expectedMerkle + ", actual = " + actualMerkle);
				// cannot continue process:
				this.deque.clear();
				System.exit(1);
				return;
			}
			// check stores:
			if (!checkStores(block)) {
				log.error("Check stores failed.");
				this.deque.clear();
				System.exit(1);
				return;
			}
			int nFile = diskBlockIndexService.getMaxnFile() + 1;
			writeToDisk(block, nFile);
			this.lastBlockHash = hash;
			log.info("Added block: " + hash);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * check the stores in Block which is received from peer
	 * */
	private boolean checkStores(Block block) {
		return Boolean.TRUE;
	}

	/**
	 * Handle received message from peer.
	 */
	@Override
	public void onMessage(MessageSender sender, Message msg) {

		if (msg instanceof PingMessage) {
			sender.sendMessage(new PongMessage(((PingMessage) msg).getNonce()));
			return;
		}
		if (msg instanceof VersionMessage) {
			sender.sendMessage(new VerAckMessage());
			sender.sendMessage(new GetBlocksMessage(HashUtils.toBytesAsLittleEndian(this.lastBlockHash),
					BitcoinConstants.ZERO_HASH_BYTES));
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
			if(blockMsg.validateHash()){
				processBlockFromPeer(blockMsg.block);
			}
		}
		if (msg instanceof StoreMessage) {
//			sender.setTimeout(60*1000);
			StoreMessage storeMsg = (StoreMessage) msg;
			if(storeMsg.validateStore()){
				if(joinStorePoolFromPeer(storeMsg.store)){
					this.pool.sendMessage(storeMsg);
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
			log.info("Get block data: " + HashUtils.toHexStringAsLittleEndian(newBlockMsg.block.getBlockHash()));
			if(newBlockMsg.validateHash()){
				processBlockFromPeer(newBlockMsg.block);
				this.pool.sendMessage(newBlockMsg);
			}
		}
	}



}
