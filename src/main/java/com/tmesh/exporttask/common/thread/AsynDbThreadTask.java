package com.tmesh.exporttask.common.thread;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tmesh.exporttask.common.thread.utils.DataThreadPoolExecutorUtils;
import com.tmesh.exporttask.entity.ExportTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 异步查询任务
 * @updateUser : TMesh
 */
public class AsynDbThreadTask<T> extends CommonDbDataThreadTask<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * 初始化数据
     **/
    public AsynDbThreadTask(String fileName, Sheet sheet, int limit, String module, BaseMapper<T> mapper,
                            QueryWrapper<T> queryWapper, ExportTask exportTask, int lastNumber, int offset) {
        super(fileName, sheet, limit, module, mapper, queryWapper, exportTask, lastNumber, offset);
    }


    /**
     * 任务执行，没有指定线程池，可以重写该方法，实现需要的功能 
     **/
    @Override
    public CompletableFuture<Object> runFuture() {
        return super.runFuture();
    }

    /**
     * 任务执行，指定线程池，可以重写该方法，实现需要的功能 
     **/
    @Override
    public CompletableFuture<Object> runFuture(ThreadPoolTaskExecutor executor) {
        return super.runFuture(executor);
    }
}