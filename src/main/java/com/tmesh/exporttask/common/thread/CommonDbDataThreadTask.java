package com.tmesh.exporttask.common.thread;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tmesh.exporttask.common.BeanUtils;
import com.tmesh.exporttask.common.thread.utils.DataThreadPoolExecutorUtils;
import com.tmesh.exporttask.entity.ExportTask;
import com.tmesh.exporttask.service.ExportTaskService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 通用异步查询任务
 * @updateUser : TMesh
 */
@Component
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonDbDataThreadTask<T> extends CompletableFuture<Map<String, Object>> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public static ExportTaskService exportTaskService = null;

    protected String fileName; // 文件名称
    protected long count; // 数据总数
    protected int startRow; // 开始行号
    protected int endRow; // 结束行号
    protected int offset; // 偏移量
    protected Sheet sheet; // sheet
    protected int limit; // sheet
    protected String module; // sheet
    protected BaseMapper<T> mapper; // sheet
    protected QueryWrapper<T> queryWapper; // 当前数据位置
    protected ExportTask exportTask; // sheet
    protected int lastNumber; // 当前数据位置

    protected CommonDbDataThreadTask() {

    }

    /**
     * 必要数据初始化
     **/
    protected CommonDbDataThreadTask(String fileName, Sheet sheet, int limit, String module, BaseMapper<T> mapper,
                                     QueryWrapper<T> queryWapper, ExportTask exportTask, int lastNumber, int offset) {
        if (exportTaskService == null) {
            exportTaskService = BeanUtils.getBean(ExportTaskService.class);
        }
        this.fileName = fileName;
        this.sheet = sheet;
        this.limit = limit;
        this.module = module;
        this.mapper = mapper;
        this.queryWapper = queryWapper;
        this.exportTask = exportTask;
        this.lastNumber = lastNumber;
        this.offset = offset;
    }
    
    /**
     * 任务执行，没有指定线程池
     **/
    public CompletableFuture<Object> runFuture() {
        DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.setThreadRecordMap(this.fileName, this);

        return CompletableFuture.supplyAsync(() -> {
            logger.info("fileName={},线程开始运行", this.fileName);
            Map<String, List<Map<String, Object>>> map = new HashMap<>();
            try {
                // 添加限制条件
                long begTimeL = System.currentTimeMillis();
                // 获取结果集
                List<Map<String, Object>> resultList = this.mapper.selectMaps(this.queryWapper);
                long endTime = System.currentTimeMillis();
                this.logger.info("{}-线程={},fileName={},导出完成...耗时={} 毫秒,大小={}行", this.module, Thread.currentThread().getName(), this.fileName, endTime - begTimeL, resultList.size());
                map.put(String.valueOf(this.limit), resultList);
                return map;
            } catch (Exception e) {
                e.printStackTrace();
                this.logger.error(e.getMessage(), e);
            }
            return map;
        });
    }

    /**
     * 任务执行，指定线程池
     **/
    public CompletableFuture<Object> runFuture(ThreadPoolTaskExecutor executor) {
        DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.setThreadRecordMap(this.fileName, this);

        return CompletableFuture.supplyAsync(() -> {
            logger.info("fileName={},线程开始运行", this.fileName);
            Map<String, List<Map<String, Object>>> map = new HashMap<>();
            try {
                // 添加限制条件
                long begTimeL = System.currentTimeMillis();
                // 获取结果集
                List<Map<String, Object>> resultList = this.mapper.selectMaps(this.queryWapper);
                long endTime = System.currentTimeMillis();
                this.logger.info("{}-线程={},fileName={},导出完成...耗时={} 毫秒,大小={}行", this.module, Thread.currentThread().getName(), this.fileName, endTime - begTimeL, resultList.size());
                map.put(String.valueOf(this.limit), resultList);
                return map;
            } catch (Exception e) {
                e.printStackTrace();
                this.logger.error(e.getMessage(), e);
            }
            return map;
        }, executor);
    }

}
