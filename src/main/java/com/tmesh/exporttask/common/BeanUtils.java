package com.tmesh.exporttask.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 一个类实现了ApplicationContextAware接口后，就可以获得ApplicationContext中的所有bean
 *              用于解决某些类因为有被new出来的实例导致@Autowired失效的问题
 * @updateUser : TMesh
 */
@Component
public class BeanUtils implements ApplicationContextAware {

    protected static ApplicationContext applicationContext;

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     * @param arg spring上下文对象
     * @throws BeansException 抛出spring异常
     */
    @Override
    public void setApplicationContext(ApplicationContext arg) throws BeansException {
        if (applicationContext == null) {
            applicationContext = arg;
        }
    }

    /**
     * 获取spring上下文对象
     * @return 上下文对象
     */
    public static ApplicationContext getContext() {
        return applicationContext;
    }

    /**
     * 根据beanName获取bean
     * @param beanName bean的名称
     * @return bean对象
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 根据beanName和类型获取bean
     * @param beanName bean名称
     * @param clazz    bean的Class类型
     * @param <T>      bean的类型
     * @return bean对象
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return applicationContext.getBean(beanName, clazz);
    }

    /**
     * 根据类型获取bean
     * @param clazz bean的Class类型
     * @param <T>   bean的类型
     * @return bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}
