package com.meiren.blockchain;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BaseServiceTest {

    public static ClassPathXmlApplicationContext getApplicationContext() {
        String[] xmlFileStrings = new String[]{"classpath:META-INF/spring/blockchain-service.xml"};
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
                xmlFileStrings);
        classPathXmlApplicationContext.start();
        return classPathXmlApplicationContext;
    }
}
