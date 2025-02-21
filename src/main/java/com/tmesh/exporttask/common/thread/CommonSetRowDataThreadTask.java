package com.tmesh.exporttask.common.thread;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.tmesh.exporttask.common.excel.ExportPOIUtils;
import com.tmesh.exporttask.common.thread.utils.DataThreadPoolExecutorUtils;
import com.tmesh.exporttask.common.thread.utils.ThreadPoolExecutorUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 通用异步设置数据任务
 * @updateUser : TMesh
 */
@Component
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonSetRowDataThreadTask<T> extends CompletableFuture<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());
    
    protected String fileName; // 文件名称
    protected long count; // 数据总数
    protected int startRow; // 开始行号
    protected int endRow; // 结束行号
    protected int offset; // 偏移量
    protected Sheet sheet; // sheet
    protected List<Map<String, Object>> list; // 数据列表
    protected String[] keys; // 数据库对应字段
    protected int lastNumber; // 当前数据位置
    protected boolean useAsyn = true; // 标志

    protected CommonSetRowDataThreadTask() {

    }

    /**
     * 必要数据初始化
     **/
    protected CommonSetRowDataThreadTask(String fileName, Sheet sheet, List<Map<String, Object>> list,
                                         String[] keys, int lastNumber, int startRow, int endRow, int offset) {
        this.fileName = fileName;
        this.sheet = sheet;
        this.list = list;
        this.keys = keys;
        this.lastNumber = lastNumber;
        this.startRow = startRow;
        this.endRow = endRow;
        this.offset = offset;
    }
    
    /**
     * 任务执行，没有指定线程池
     **/
    public CompletableFuture<Integer> runFuture() {
        DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.setThreadRecordMap(this.fileName, this);
        return CompletableFuture.supplyAsync(() -> {
            long begTime = System.currentTimeMillis();
            this.logger.info("fileName={},线程开始运行", this.fileName);
            try {
                // excel设置数据
                ExportPOIUtils.setRowData(this.sheet, this.list, this.keys, this.lastNumber + 1, this.useAsyn);
                long endTime = System.currentTimeMillis();
                JSONObject threadRecordMap = ThreadPoolExecutorUtils.ThreadPoolSingleton.getThreadRecordMap(this.fileName);
                CommonExportThreadTask task = threadRecordMap.getObject("task", new TypeReference<CommonExportThreadTask>(){});
                // 任务数量统计
                task.setSuccessCount(task.getSuccessCount() + this.offset);
                DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.removeThreadRecordMap(this.fileName);
                logger.info("fileName={},数据全部导出至excel...耗时={} 毫秒,大小={} 行={}-{}", this.fileName, endTime - begTime, this.list.size(), this.lastNumber + 1, this.lastNumber + this.list.size());
                // 清空已使用完的数据，节省内存
                int size = this.list.size();
                this.list.clear();
                return size;
            } catch (Exception e) {
                e.printStackTrace();
                this.logger.error(e.getMessage(), e);
            } finally {

            }
            return 0;
        });
    }

    /**
     * 任务执行，指定线程池
     **/
    public CompletableFuture<Integer> runFuture(ThreadPoolTaskExecutor executor) {
        DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.setThreadRecordMap(this.fileName, this);
        
        return CompletableFuture.supplyAsync(() -> {
            long begTime = System.currentTimeMillis();
            this.logger.info("fileName={},线程开始运行", this.fileName);
            try {
                // excel设置数据
                ExportPOIUtils.setRowData(this.sheet, this.list, this.keys, this.lastNumber + 1, this.useAsyn);
                long endTime = System.currentTimeMillis();
                JSONObject threadRecordMap = ThreadPoolExecutorUtils.ThreadPoolSingleton.getThreadRecordMap(this.fileName);
                CommonExportThreadTask task = threadRecordMap.getObject("task", new TypeReference<CommonExportThreadTask>(){});
                // 任务数量统计
                task.setSuccessCount(task.getSuccessCount() + this.offset);
                DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.removeThreadRecordMap(this.fileName);
                logger.info("fileName={},数据全部导出至excel...耗时={} 毫秒,大小={} 行={}-{}", this.fileName, endTime - begTime, this.list.size(), this.lastNumber + 1, this.lastNumber + this.list.size());
                // 清空已使用完的数据，节省内存
                int size = this.list.size();
                this.list.clear();
                return size;
            } catch (Exception e) {
                e.printStackTrace();
                this.logger.error(e.getMessage(), e);
            } finally {

            }
            return 0;
        }, executor);
    }
    
}
