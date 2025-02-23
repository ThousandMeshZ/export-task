package com.tmesh.exporttask.common.thread.utils;

import com.alibaba.fastjson2.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 异步设置数据线程池工具
 * @updateUser : TMesh
 */
public class DataThreadPoolExecutorUtils {
    private static Logger logger = LogManager.getLogger(DataThreadPoolExecutorUtils.class);

    /**
     * @author: TMesh
     * @description: 线程池单例
     * @Version: 1.0
     **/
    public static class SetDataThreadPoolSingleton {

        private static ThreadPoolTaskExecutor instance;  // 导出线程池
        private static final Map<String, CompletableFuture> threadRecordMap = new LinkedHashMap<>();   // 导出线线程记录Map

        /*
         * @methodName: getDetailInfo
         * @description: 获取任务详细信息
         * @param: []
         * @return: com.alibaba.fastjson.JSONObject
         * @author: TMesh
         * @Version: 1.0
         */
        public static JSONObject getDetailInfo() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("任务总数", getInstance().getThreadPoolExecutor().getTaskCount());
            jsonObject.put("已完成任务", getInstance().getThreadPoolExecutor().getCompletedTaskCount());
            jsonObject.put("尚未完成任务", getInstance().getThreadPoolExecutor().getTaskCount() - getInstance().getThreadPoolExecutor().getCompletedTaskCount());
            jsonObject.put("尚未完成任务信息", threadRecordMap);
            return jsonObject;
        }


        /*
         * @methodName: setThreadRecordMap
         * @description: 添加/修改 任务信息
         * @param: [key, value]
         * @return: void
         * @author: TMesh
         * @Version: 1.0
         */
        public static void setThreadRecordMap(String key, CompletableFuture threadTask) {
            synchronized (SetDataThreadPoolSingleton.class) {
                threadRecordMap.put(key, threadTask);
            }
        }

        /*
         * @methodName: removeThreadRecordMap
         * @description: 删除已完成任务信息
         * @param: [key]
         * @return: void
         * @author: TMesh
         * @Version: 1.0
         */
        public static void removeThreadRecordMap(String key) {
            synchronized (SetDataThreadPoolSingleton.class) {
                threadRecordMap.remove(key);
            }
        }

        /*
         * @methodName: getInstance
         * @description: 获取线程池单例
         * @param: []
         * @return: java.util.concurrent.ThreadPoolExecutor
         * @author: TMesh
         * @Version: 1.0
         */
        /**
         * ThreadPoolTaskExecutor 用法：
         * corePoolSize：线程池维护线程最小的数量，默认为1
         * maxPoolSize：线程池维护线程最大数量，默认为Integer.MAX_VALUE
         * keepAliveSeconds：(maxPoolSize-corePoolSize)部分线程空闲最大存活时间，默认存活时间是60s
         * queueCapacity：阻塞任务队列的大小，默认为Integer.MAX_VALUE，默认使用LinkedBlockingQueue
         * allowCoreThreadTimeOut：设置为true的话，keepAliveSeconds 参数设置的有效时间对 corePoolSize 线程也有效，默认是flase
         * threadFactory：：用于设置创建线程的工厂，可以通过线程工厂给每个创建出来的线程设置更有意义的名字。使用开源框架 guava 提供的 ThreadFactoryBuilder 可以快速给线程池里的线程设置有意义的名字
         * rejectedExecutionHandler：拒绝策略，当队列 workQueue 和线程池 maxPoolSize 都满了，说明线程池处于饱和状态，那么必须采取一种策略处理提交的新任务。这个策略默认情况下是 AbortPolicy，表示无法处理新任务时抛出异常，有以下四种策略，当然也可以根据实际业务需求类实现 RejectedExecutionHandler 接口实现自己的处理策略
         * 1.AbortPolicy：丢弃任务，并且抛出 RejectedExecutionException 异常
         * 2.DiscardPolicy：丢弃任务，不处理，不抛出异常
         * 3.CallerRunsPolicy：直接在 execute 方法的调用线程中运行被拒绝的任务
         * 4.DiscardOldestPolicy：丢弃队列中最前面的任务，然后重新尝试执行任务
         */
        public static ThreadPoolTaskExecutor getInstance() {
            if (instance == null) {
                instance = new ThreadPoolTaskExecutor();
                // 设置核心线程数
                instance.setCorePoolSize(2);
                // 设置最大线程数
                instance.setMaxPoolSize(3);
                // 设置队列容量
                instance.setQueueCapacity(1000);
                // 设置线程活跃时间（秒）
                instance.setKeepAliveSeconds(5000);
                // 设置默认线程名称
                instance.setThreadNamePrefix("data-pool");
                // 设置拒绝策略
                instance.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                // 等待所有任务结束后再关闭线程池
                instance.setWaitForTasksToCompleteOnShutdown(true);
                // 初始化线程池
                instance.initialize();
            }
            return instance;
        }

        public static void execute(Runnable runnable) {
            getInstance().execute(runnable);
        }
        
        
        /*
         * @methodName: getInstanceBlockingQueue
         * @description: 获取线程队列单例
         * @param: []
         * @return: java.util.concurrent.BlockingQueue<Runnable>
         * @author: TMesh
         * @Version: 1.0
         */
        public static BlockingQueue<Runnable> getInstanceBlockingQueue() {
            if (instance == null) {
                return null;
            }
            return instance.getThreadPoolExecutor().getQueue();
        }
        
    }

}