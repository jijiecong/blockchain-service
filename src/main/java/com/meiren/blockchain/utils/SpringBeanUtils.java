package com.meiren.blockchain.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by sx on 2017/4/27.
 */
public class SpringBeanUtils implements ApplicationContextAware {
    private static ApplicationContext application;
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        application = applicationContext;
    }
    public static <T> T getBeans(String beanName,Class<T> clazz){
         return  application.getBean(beanName,clazz);
    }

}
