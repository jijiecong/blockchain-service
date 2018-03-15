package com.meiren.blockchain.p2p;

import com.meiren.blockchain.common.BlockChainException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Discover full nodes by DNS query:
 * https://BlockChain.org/en/developer-guide#peer-discovery
 * 
 * @author jijc
 */
public class PeerDiscover {

	static final Log log = LogFactory.getLog(PeerDiscover.class);

	// https://en.BlockChain.it/wiki/Satoshi_Client_Node_Discovery#DNS_Addresses
	static final String[] DNS_SEEDS = { "bitseed.xf2.org", "dnsseed.bluematt.me", "seed.BlockChain.sipa.be",
			"dnsseed.BlockChain.dashjr.org", "seed.BlockChainstats.com" };
//	static final String[] DNS_SEEDS = { "ocp.adnonstop.com" };

	/**
	 * Lookup BlockChain peers by DNS seed.
	 * 
	 * @return InetAddress[] contains 1~N peers.
	 * @throws BlockChainException
	 *             If lookup failed.
	 */
	public static String[] lookup() {
		log.info("Lookup peers from DNS seed...");
		String[] ips = Arrays.stream(DNS_SEEDS).parallel().map((host) -> {
			try {
				return InetAddress.getAllByName(host);
			} catch (UnknownHostException e) {
				log.warn("Cannot look up host: " + host);
				return new InetAddress[0];
			}
		}).flatMap(x -> Arrays.stream(x)).filter(addr -> addr instanceof Inet4Address).map(addr -> {
			return addr.getHostAddress();
		}).toArray(String[]::new);
		if (ips.length == 0) {
			throw new BlockChainException("Cannot lookup pears from all DNS seeds.");
		}
		log.info(ips.length + " peers found.");
		return ips;
	}

}
