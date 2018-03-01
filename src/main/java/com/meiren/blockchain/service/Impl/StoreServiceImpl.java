package com.meiren.blockchain.service.Impl;

import com.meiren.blockchain.common.constant.BitcoinConstants;
import com.meiren.blockchain.common.io.BitcoinOutput;
import com.meiren.blockchain.service.StoreService;

/**
 * @author jijiecong
 * @ClassName: StoreServiceImpl
 * @Description: ${todo}
 * @date 2018/2/27 10:58
 */
public class StoreServiceImpl implements StoreService {
	/**
	 * @param url
	 * @Description store url
	 * @return byte[]
	 *
	 * */
	public byte[] buildStore(String url) {
		BitcoinOutput output = new BitcoinOutput();
		// store version:
		output.writeInt(BitcoinConstants.TX_VERSION);
		byte[] storeScript = createStoreScript(url);
		// storeScript length:
		output.writeVarInt(storeScript.length);
		// storeScript
		output.write(storeScript);
		// lock time:
		output.writeInt(0);
		return output.toByteArray();
	}

	private byte[] createStoreScript(String url) {
		return url.getBytes();
	}
}
