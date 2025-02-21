package com.tmesh.exporttask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tmesh.exporttask.common.excel.ExportPOIUtils;
import com.tmesh.exporttask.common.thread.AsynSetRowDataThreadTask;
import com.tmesh.exporttask.common.thread.CommonExportThreadTask;
import com.tmesh.exporttask.common.thread.ExportThreadTask;
import com.tmesh.exporttask.common.thread.utils.DataThreadPoolExecutorUtils;
import com.tmesh.exporttask.common.thread.utils.ThreadPoolExecutorUtils;
import com.tmesh.exporttask.controller.UserController;
import com.tmesh.exporttask.dto.UserExportTaskDto;
import com.tmesh.exporttask.entity.ExportTask;
import com.tmesh.exporttask.entity.UserEntity;
import com.tmesh.exporttask.mapper.UserMapper;
import com.tmesh.exporttask.service.UserSerivce;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 用户服务实现类
 * @updateUser : TMesh
 */
@Service("userSerivce")
public class UserSerivceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserSerivce {

    private final Logger logger = LogManager.getLogger(UserController.class);

    @Value("${config.useDbAsyn}")
    private boolean useDbAsyn;
    @Value("${config.useAsyn}")
    private boolean useAsyn;
    @Value("${config.useEconimicMemory}")
    private boolean useEconimicMemory;
    @Value("${config.preGenerateExcel}")
    private boolean preGenerateExcel;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void exportList(UserExportTaskDto userExportTaskDto) {
        QueryWrapper<UserEntity> mapper = new QueryWrapper<>();
        // TODO 组装查询条件
        try {
            // 将任务放入线程池
            ThreadPoolExecutorUtils.ThreadPoolSingleton.execute(new UserExportThreadTask("user", mapper, this.userMapper, this.useDbAsyn, this.useAsyn, this.useEconimicMemory, this.preGenerateExcel));
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.error(e);
        }
    }

    /**
     * 采用注解的方式
     **/
    @Override
    @Async("taskExecutor")
    public void exportListByAnnotation(UserExportTaskDto userExportTaskDto) {
        QueryWrapper<UserEntity> mapper = new QueryWrapper<>();
        // TODO 组装查询条件
        try {
            // 开始任务，这里可以自己定义自己想要功能，这里直接复用代码了。
            new UserExportThreadTask("user", mapper, this.userMapper, this.useDbAsyn, this.useAsyn, this.useEconimicMemory, this.preGenerateExcel)
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.error(e);
        }
    }
}

/**
 * 导出任务线程，继承自CommonExportThreadTask
 **/
class UserExportThreadTask extends CommonExportThreadTask<UserEntity> {

    private static final String MODULE = "USER";
    private static final String[] COLUNM_NAMES = {"ID", "NAME"};
    // map中的key
    private static final String[] KEYS = {"ID", "NAME"};

    private final Logger logger = LogManager.getLogger(this.getClass());

    public UserExportThreadTask(String user, QueryWrapper<UserEntity> queryWapper, BaseMapper<UserEntity> mapper, boolean useDbAsyn, boolean useAsyn, boolean useEconimicMemory, boolean preGenerateExcel) {
        super(MODULE, user, queryWapper, mapper, useDbAsyn, useAsyn, useEconimicMemory, preGenerateExcel, COLUNM_NAMES, KEYS);
    }

    /**
     * 任务执行内容。可以重写 run 方法，实现需要的功能
     **/
    @Override
    public void run() {
        this.logger.info("filePath={},任务={}开始运行", this.filePath, this.identity);
        FileOutputStream out = null;
        ByteArrayOutputStream outputStream = null;
        Workbook wb = null;
        Sheet sheet = null;
        Map<String, List<Map<String, Object>>> dbMap = new ConcurrentHashMap<>();
        List<CompletableFuture<Object>> taskDbList = new ArrayList<>();
        List<CompletableFuture<Integer>> taskList = new ArrayList<>();
        ExportTask exportTask = exportTaskService.getById(this.identity);
        exportTask.setStatus(1);
        exportTaskService.updateById(exportTask);

        try {
            out = new FileOutputStream(new File(this.filePath));
            // 获取数据总数
            outputStream = new ByteArrayOutputStream();

            QueryWrapper<UserEntity> countQueryWapper = this.queryWapper.clone();
            countQueryWapper.select("1");
            this.count = this.mapper.selectCount(countQueryWapper);
            // sheet数
            int pageSheet = new BigDecimal(this.count).divide(new BigDecimal(ExportThreadTask.SHEETLIMIT), 0, RoundingMode.UP).setScale(0, RoundingMode.UP).intValue();
            // 总限制次数
            int limitSheet = new BigDecimal(this.count).divide(new BigDecimal(ExportThreadTask.OFFSET), 0,RoundingMode.UP).setScale(0, RoundingMode.UP).intValue();
            // 每个sheet最大循环次数
            int sheetNumberLimit = new BigDecimal(ExportThreadTask.SHEETLIMIT).divide(new BigDecimal(ExportThreadTask.OFFSET)).setScale(0, RoundingMode.UP).intValue();;

            if (this.useEconimicMemory) {
                wb = ExportPOIUtils.createSXlsxWorkBook();
            } else {
                wb = ExportPOIUtils.createXlsxWorkBook();
            }
            List<Sheet> sheetList = null;
            if (this.preGenerateExcel) {
                sheetList = ExportPOIUtils.createSheetList(wb, pageSheet, this.count, this.columnNames);
            }

            exportTask.setStartTime(new Date());
            exportTask.setDbStartDate(new Date());
            exportTask.setExcelWriteStartDate(new Date());
            exportTaskService.updateById(exportTask);

            long begTimeLDb = System.currentTimeMillis();
            for (int j = 1; j < pageSheet + 1; j++) {
                // 创建新的 sheet
                if (this.preGenerateExcel) {
                    sheet = sheetList.get(j - 1);
                } else {
                    sheet = ExportPOIUtils.createSheet(wb, this.columnNames);
                }

                // 修改最后一次循环次数，最后一次不一定能循环满
                if (limitSheet < sheetNumberLimit) {
                    sheetNumberLimit = limitSheet;
                }
                // 获取新的wapper用于添加分段信息
                for (int k = 1; k < sheetNumberLimit + 1; k++) {
                    QueryWrapper<UserEntity> newQueryWapper = this.queryWapper.clone();
                    // 数据库操作，返回结果集
                    List<Map<String, Object>> resultList = this.dbDataHandle(exportTask, sheet, newQueryWapper, taskDbList);
                    // 数据写入excel实体
                    this.dataWrite(exportTask, sheet, resultList, taskList);
                }
                limitSheet -= sheetNumberLimit;
            }
            exportTask.setDbEndDate(new Date());

            long begTimeL = System.currentTimeMillis();
            if (this.useDbAsyn) {
                this.useDbAsynHandle(wb, pageSheet, taskList, taskDbList, dbMap, sheet, exportTask, begTimeLDb);
            }

            if (this.useAsyn) {
                this.useAsynHandle(taskList, exportTask, begTimeL);
            }
            taskList.clear();
            taskDbList.clear();
            long writeTime = System.currentTimeMillis();
            // 写入文件
            wb.write(outputStream);

            // 记录结束信息
            exportTask.setExcelWriteEndDate(new Date());
            exportTask.setExcelWriteTimeCost(exportTask.getExcelWriteTimeCost()  + exportTask.getExcelWriteEndDate().getTime() - writeTime);
            exportTask.setEndTime(new Date());
            exportTask.setTimeCost(exportTask.getEndTime().getTime() - exportTask.getStartTime().getTime());

            ThreadPoolExecutorUtils.ThreadPoolSingleton.removeThreadRecordMap(this.fileName);

            this.logger.info("{}-filePath={},数据全部导出至excel文件...耗时={} 毫秒,大小:{} 行：", this.module, this.filePath, exportTask.getTimeCost(), this.successCount);
            exportTask.setStatus(2);
            exportTask.setReason("成功");
        } catch (Exception e) {
            e.printStackTrace();
            String failReason = e.toString();
            exportTask.setStatus(3);
            exportTask.setReason(failReason);
            this.logger.error(e.getMessage(), e);
        } finally {
            // 进行任务更新，文件流，workbook的关闭
            try {
                exportTaskService.updateById(exportTask);
                if (wb != null) {
                    // 关闭Workbook
                    if (wb instanceof SXSSFWorkbook) {
                        // dispose of temporary files backing this workbook on disk -> 处理SXSSFWorkbook导出excel时，产生的临时文件
                        ((SXSSFWorkbook) wb).dispose();
                    }
                    wb.close();
                }
                if (outputStream != null) {
                    // 刷新缓存，并关闭流
                    outputStream.flush();
                    outputStream.close();
                }
                if (out != null) {
                    // 刷新缓存，并关闭流
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 异步数据库查询数据处理方法，可以根据需求重写
     **/
    @Override
    protected void useDbAsynHandle(Workbook wb, int pageSheet,
                                   List<CompletableFuture<Integer>> taskList,
                                    List<CompletableFuture<Object>> taskDbList,
                                   Map<String, List<Map<String, Object>>> dbMap,
                                   Sheet sheet, ExportTask exportTask, long begTimeLDb) throws IOException {
        CompletableFuture[] taskDbArray = new CompletableFuture[taskDbList.size()];
        Sheet finalSheet = sheet;

        CompletableFuture.allOf(taskDbList.toArray(taskDbArray)).join();
        exportTask.setDbEndDate(new Date());
        exportTask.setDbTimeCost(exportTask.getDbTimeCost() + System.currentTimeMillis() - begTimeLDb);

        CompletableFuture.allOf(taskDbList.toArray(taskDbArray)).thenApply(v -> {
            for(CompletableFuture future: taskDbList) {
                try {
                    long s = System.currentTimeMillis();;
                    Map<String, List<Map<String, Object>>> map = (Map<String, List<Map<String, Object>>>) future.get();
                    if (this.useAsyn) {
                        String key = (String) map.keySet().toArray()[0];
                        List<Map<String, Object>> mapList = map.get(key);
                        int rowNum = Integer.parseInt(key);
                        AsynSetRowDataThreadTask task = new AsynSetRowDataThreadTask(
                                this.fileName, finalSheet, mapList, this.keys, rowNum, rowNum, rowNum + ExportThreadTask.OFFSET, ExportThreadTask.OFFSET
                        );
                        CompletableFuture futureExcel = task.runFuture(DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.getInstance());
                        this.lastNumber += ExportThreadTask.OFFSET;
                        taskList.add(futureExcel);
                    } else {
                        String key = (String) map.keySet().toArray()[0];
                        this.successCount += map.get(key).size();
                        dbMap.putAll(map);
                        long e = System.currentTimeMillis();;
                        exportTask.setExcelWriteTimeCost(exportTask.getExcelWriteTimeCost() + e - s);
                        exportTaskService.updateById(exportTask);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }).thenAccept(result -> {
            System.out.println("最终结果是: " + result); // 打印最终结果
        }).join();
        if (!this.useAsyn) {
            long s = System.currentTimeMillis();;
            ExportPOIUtils.mergeDbData(wb, pageSheet, dbMap, this.keys, this.module, this.fileName, this.useAsyn);
            exportTask.setExcelWriteTimeCost(exportTask.getExcelWriteTimeCost() + System.currentTimeMillis() - s);
            dbMap.clear();
        }
    }

    /**
     * 异步设置数据处理方法，可以根据需求重写
     **/
    @Override
    protected void useAsynHandle(List<CompletableFuture<Integer>> taskList,
                                 ExportTask exportTask, long begTimeL) {
        CompletableFuture[] taskArray = new CompletableFuture[taskList.size()];
        CompletableFuture.allOf(taskList.toArray(taskArray)).join();
        exportTask.setExcelWriteTimeCost(exportTask.getExcelWriteTimeCost() + System.currentTimeMillis() - begTimeL);
        exportTaskService.updateById(exportTask);
        CompletableFuture.allOf(taskList.toArray(taskArray)).thenApply(v -> {
            for(CompletableFuture future: taskList) {
                try {
                    Integer sum = (Integer) future.get();
                    this.successCount += sum;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }).thenAccept(result -> {
            System.out.println("最终结果是: " + result); // 打印最终结果
        }).join();;
    }
}
