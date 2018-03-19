package com.meiren.blockchain;

import com.meiren.blockchain.p2p.message.MasterIpMessage;
import org.junit.Test;

import java.io.IOException;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: MasterIpMessageTest
 * @Description: ${todo}
 * @date 2018/3/15 17:40
 */
public class MasterIpMessageTest {

	@Test
	public void test() throws IOException {
//		MasterIpMessage masterIpMessage = new MasterIpMessage("192.168.1.1");
////		byte[] a = masterIpMessage.getPayload2();
//		MasterIpMessage masterIpMessage1 = new MasterIpMessage(a);
//		System.out.println(masterIpMessage1.masterIp);
		String[] strings = new String[] {"192.168.4.223", "192.168.4.166"};
		String masterIp = "";
		for(String key : strings) {
			if (masterIp.compareTo(key) > 0) {
				masterIp = key;
			}
		}
		System.out.println(masterIp);
	}





}
