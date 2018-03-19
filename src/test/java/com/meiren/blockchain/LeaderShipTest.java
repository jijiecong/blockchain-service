package com.meiren.blockchain;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: LeaderShipTest
 * @Description: ${todo}
 * @date 2018/3/19 11:25
 */
public class LeaderShipTest {
	private static final String PATH = "/blockChain/leader";

	public static void main(String[] args) {

		List<LeaderSelector> selectors = new ArrayList<>();
		List<CuratorFramework> clients = new ArrayList<>();
		List<String> ips = new ArrayList<>();
		ips.add("192.168.4.223");
		ips.add("192.168.4.166");
		try {
			for (String ip : ips) {
				CuratorFramework client = getClient(ip);
				clients.add(client);

				final String name = "client#" + ip;
				LeaderSelector leaderSelector = new LeaderSelector(client, PATH, new LeaderSelectorListener() {
					@Override
					public void takeLeadership(CuratorFramework client) throws Exception {
//						client.getZookeeperClient();
						System.out.println(name + ":I am leader.");
						Thread.sleep(2000);
					}

					@Override
					public void stateChanged(CuratorFramework client, ConnectionState newState) {

					}
				});

				leaderSelector.autoRequeue();
				leaderSelector.start();
				selectors.add(leaderSelector);

			}
			Thread.sleep(Integer.MAX_VALUE);
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

	private static CuratorFramework getClient(String ip) {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString(ip + ":2181")
				.retryPolicy(retryPolicy).sessionTimeoutMs(6000).connectionTimeoutMs(3000)
				.namespace("blockChain").build();
		client.start();
		return client;
	}
}
