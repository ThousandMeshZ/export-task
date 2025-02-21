package com.tmesh.exporttask.common.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 异步设置数据任务
 * @updateUser : TMesh
 */

public class AsynSetRowDataThreadTask extends CommonSetRowDataThreadTask {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * 初始化数据
     **/
    public AsynSetRowDataThreadTask(String fileName, Sheet sheet, List<Map<String, Object>> list, String[] keys, int lastNumber, int startRow, int endRow, int offset) {
        super(fileName, sheet, list, keys, lastNumber, startRow, endRow, offset);
    }

    /**
     * 任务执行，没有指定线程池，可以重写该方法，实现需要的功能 
     **/
    @Override
    public CompletableFuture<Integer> runFuture() {
        return super.runFuture();
    }

    /**
     * 任务执行，指定线程池，可以重写该方法，实现需要的功能 
     **/
    @Override
    public CompletableFuture<Integer> runFuture(ThreadPoolTaskExecutor executor) {
        return super.runFuture(executor);
    }
}