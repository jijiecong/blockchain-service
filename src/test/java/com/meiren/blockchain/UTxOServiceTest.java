package com.meiren.blockchain;

import com.meiren.blockchain.common.util.JsonUtils;
import com.meiren.blockchain.entity.*;
import com.meiren.blockchain.service.UTxOService;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class UTxOServiceTest extends BaseServiceTest{
	ClassPathXmlApplicationContext applicationContext = getApplicationContext();

	UTxOService uTxOService = (UTxOService) applicationContext.getBean("uTxOService");


	@Test
	public void findByReceiver(){
		List<UTxO> uTxOS = new ArrayList();
		uTxOS = uTxOService.findByReceiver("18868890124");
		JsonUtils.printJson(uTxOS);
	}
}
