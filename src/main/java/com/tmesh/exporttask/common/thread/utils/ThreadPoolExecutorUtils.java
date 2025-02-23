package com.tmesh.exporttask.common.thread.utils;

import com.alibaba.fastjson2.JSONObject;
import com.tmesh.exporttask.common.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 线程池工具
 * @updateUser : TMesh
 */
@EnableAsync
@Configuration
public class ThreadPoolExecutorUtils {
    private static Logger logger = LogManager.getLogger(ThreadPoolExecutorUtils.class);

    private static LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue();   // 导出线程队列

    private static Map<String, JSONObject> threadRecordMap = new LinkedHashMap<>();   // 导出线线程记录Map

    /**
     * 采用注解的方式
     **/
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        int i = Runtime.getRuntime().availableProcessors();
        System.out.println("系统最大线程数  ： " + i);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 3, 5000, TimeUnit.SECONDS, blockingQueue, Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /*
     * @methodName: getDetailInfo
     * @description: 获取任务详细信息
     * @param: []
     * @return: com.alibaba.fastjson.JSONObject
     * @author: TMesh
     * @Version: 1.0
     */
    public static JSONObject getDetailInfo() {
        ThreadPoolExecutor instance = (ThreadPoolExecutor) BeanUtils.getBean("taskExecutor");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("任务总数", instance.getTaskCount());
        jsonObject.put("已完成任务", instance.getCompletedTaskCount());
        jsonObject.put("尚未完成任务", instance.getTaskCount() - instance.getCompletedTaskCount());
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
    public static void setThreadRecordMap(String key, JSONObject value) {
        synchronized (ThreadPoolSingleton.class) {
            threadRecordMap.put(key, value);
        }
    }
    public static JSONObject getThreadRecordMap(String key) {
        synchronized (ThreadPoolSingleton.class) {
            return threadRecordMap.get(key);
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
        synchronized (ThreadPoolSingleton.class) {
            threadRecordMap.remove(key);
        }
    }
    
    /*
     * @methodName: getInstanceBlockingQueue
     * @description: 获取线程队列单例
     * @param: []
     * @return: java.util.concurrent.LinkedBlockingQueue
     * @author: TMesh
     * @Version: 1.0
     */
    private static LinkedBlockingQueue getInstanceBlockingQueue() {
        if (blockingQueue == null) {
            blockingQueue = new LinkedBlockingQueue<>();
        }
        return blockingQueue;
    }
    
    
    /**
     * @author: TMesh
     * @description: 线程池单例
     * @Version: 1.0
     **/
    public static class ThreadPoolSingleton {

        private static ThreadPoolExecutor instance;  // 导出线程池
        private static LinkedBlockingQueue blockingQueue;   // 导出线程队列
        private static Map<String, JSONObject> threadRecordMap = new LinkedHashMap<>();   // 导出线线程记录Map

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
            jsonObject.put("任务总数", getInstance().getTaskCount());
            jsonObject.put("已完成任务", getInstance().getCompletedTaskCount());
            jsonObject.put("尚未完成任务", getInstance().getTaskCount() - getInstance().getCompletedTaskCount());
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
        public static void setThreadRecordMap(String key, JSONObject value) {
            synchronized (ThreadPoolSingleton.class) {
                threadRecordMap.put(key, value);
            }
        }
        public static JSONObject getThreadRecordMap(String key) {
            synchronized (ThreadPoolSingleton.class) {
                return threadRecordMap.get(key);
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
            synchronized (ThreadPoolSingleton.class) {
                threadRecordMap.remove(key);
            }
        }

        /*
         * @methodName: execute
         * @description: 任务进入线程池
         * @param: [command]
         * @return: void
         * @author: TMesh
         * @Version: 1.0
         */
        public static void execute(Runnable command) {
            getInstance().execute(command);
        }


        /*
         * @methodName: getInstance
         * @description: 获取线程池单例
         * @param: []
         * @return: java.util.concurrent.ThreadPoolExecutor
         * @author: TMesh
         * @Version: 1.0
         */
        private static ThreadPoolExecutor getInstance() {
            if (instance == null) {
                instance = new ThreadPoolExecutor(2, 3, 5000, TimeUnit.SECONDS, getInstanceBlockingQueue(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
            }
            return instance;
        }
        

        /*
         * @methodName: getInstanceBlockingQueue
         * @description: 获取线程队列单例
         * @param: []
         * @return: java.util.concurrent.LinkedBlockingQueue
         * @author: TMesh
         * @Version: 1.0
         */
        private static LinkedBlockingQueue getInstanceBlockingQueue() {
            if (blockingQueue == null) {
                blockingQueue = new LinkedBlockingQueue<>();
            }
            return blockingQueue;
        }
        
    }

}