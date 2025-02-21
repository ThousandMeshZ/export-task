package com.tmesh.exporttask.common.thread;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tmesh.exporttask.common.BeanUtils;
import com.tmesh.exporttask.common.excel.ExportPOIUtils;
import com.tmesh.exporttask.common.thread.utils.DataThreadPoolExecutorUtils;
import com.tmesh.exporttask.common.thread.utils.ThreadPoolExecutorUtils;
import com.tmesh.exporttask.entity.ExportTask;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import com.tmesh.exporttask.service.ExportTaskService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 通用导出线程任务
 * @updateUser : TMesh
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CommonExportThreadTask<T> implements ExportThreadTask {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public static ExportTaskService exportTaskService = null;

    protected int successCount; // 成功数量
    protected long count; // 总数

    protected Date startData; // 开始日期

    protected String module; // 模块名
    protected String filePath; // 文件路径
    protected String fileName; // 文件名称
    public String user; // 任务使用者
    protected QueryWrapper<T> queryWapper; // 导出条件

    protected BaseMapper<T> mapper; // 导出任务mapper
    protected String query; // 导出sql
    protected long identity; // 任务Id
    protected boolean useDbAsyn; // 是否异步数据查询
    protected boolean useAsyn; // 是否异步写入数据
    protected boolean useEconimicMemory; // 是否使用节省内存
    protected boolean preGenerateExcel; // 是否使用节省内存
    protected String[] columnNames = {"ID", "NAME"}; // 表格标头
    // map中的key
    protected String[] keys = {"ID", "NAME"}; // 数据库对应查询字段名

    protected int limit = 0; // sql limit 条件
    protected int lastNumber = 0; // 当前数据位置


    /**
     * 初始化导出任务的必要信息
     **/
    protected CommonExportThreadTask(String module, String user, QueryWrapper<T> queryWapper, BaseMapper<T> mapper, boolean useDbAsyn, boolean useAsyn, boolean useEconimicMemory, boolean preGenerateExcel, String[] columnNames, String[] keys) {
        // 获取导出任务服务
        if (exportTaskService == null) {
            exportTaskService = BeanUtils.getBean(ExportTaskService.class);
        }
        this.module = module;
        this.startData = new Date();
        // 设置文件信息
        this.setFileInfo();
        this.user = user;
        this.queryWapper = queryWapper;
        this.useEconimicMemory = useEconimicMemory;
        // 如果启用节省内存使用的是SXSSFWorkbook类，该类会分段写入临时文件，此时如果使用异步写入会报这个错误 Attempting to write a row[0] in the range [0,0] that is already written to disk
        // 总体生成将会成为累赘，需要频繁的读取临时文件
        this.useDbAsyn = useDbAsyn;
        if (useEconimicMemory) {
            useAsyn = false;
            preGenerateExcel = false;
        }
        this.preGenerateExcel = preGenerateExcel;
        this.useAsyn = useAsyn;
        this.count = 0;
        if (columnNames != null && columnNames.length > 0) {
            this.columnNames = columnNames;
        }
        if (keys != null && keys.length > 0) {
            this.keys = keys;
        }
        if (mapper != null) {
            this.mapper = mapper;
        }
        // 添加查询字段
        queryWapper.select(keys);
        // 获取查询sql
        this.query = this.getSqlQuery();
        // 添加任务信息
        ThreadPoolExecutorUtils.ThreadPoolSingleton.setThreadRecordMap(this.fileName, this.toJsonObject());
        // 保存导出任务，并返回任务id
        long taskId = this.saveExportTask();
        this.identity = taskId;
    }
    
    /**
     * 获取查询sql
     * 作为简单的展示，需要更好的可以重写
     **/
    private String getSqlQuery() {
        String sql = "select " + this.queryWapper.getSqlSelect() + " from " + this.module;
        if (this.queryWapper.getSqlSegment() != null && !this.queryWapper.getSqlSegment().isEmpty()) {
            sql += " where " + this.queryWapper.getSqlSegment();
        }
        sql = sql.replace("ew.paramNameValuePairs.", "");
        Map<String, Object> paramNameValuePairs = this.queryWapper.getParamNameValuePairs();
        for(String k : paramNameValuePairs.keySet()) {
            if (sql.contains(k)) {
                Object v = paramNameValuePairs.get(k);
                sql = sql.replace(k, String.valueOf(v));
            }
        }
        return sql;
    }
    
    /**
     * 设置文件的基本信息
     **/
    private void setFileInfo() {
        String fileId = UUID.randomUUID().toString().replace("-", "");
        this.fileName = this.module + "-" + fileId + ".xlsx";
        this.filePath = ExportThreadTask.DIR_PATH + "/" + this.module + "/" + this.fileName;
        File file = new File(this.filePath);
        if(!file.exists()){
            //先得到文件的上级目录，并创建上级目录，在创建文件
            file.getParentFile().mkdirs();
        }
    }

    /**
     * 保存导出任务并返回任务id
     **/
    private long saveExportTask() {
        ExportTask exportTask = new ExportTask().
                setModule(this.module).
                setFileName(this.fileName).
                setFilePath(this.filePath).
                setStatus(0).
                setCreateTime(this.startData).
                setUserName(user).
                setQuery(this.query);
        exportTaskService.save(exportTask);
        return exportTask.getId();
    }

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

            QueryWrapper<T> countQueryWapper = this.queryWapper.clone();
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
                    QueryWrapper<T> newQueryWapper = this.queryWapper.clone();
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
     * 数据库数据查询操作，有其他需求可以重写
     **/
    protected List<Map<String, Object>> dbDataHandle(ExportTask exportTask, Sheet sheet, QueryWrapper<T> newQueryWapper, List<CompletableFuture<Object>> taskList) {
        // 添加限制条件
        newQueryWapper.last("limit " + this.limit + "," + ExportThreadTask.OFFSET);
        if (this.useDbAsyn) {
            AsynDbThreadTask task = new AsynDbThreadTask(
                    this.fileName, sheet, this.limit, this.module, this.mapper, newQueryWapper, exportTask, this.lastNumber, ExportThreadTask.OFFSET
            );
            CompletableFuture future = task.runFuture(DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.getInstance());
            this.lastNumber += ExportThreadTask.OFFSET;
            taskList.add(future);
            return null;
        } else {
            long begTimeL = System.currentTimeMillis();
            // 获取结果集
            List<Map<String, Object>> resultList = this.mapper.selectMaps(newQueryWapper);
            long endTime = System.currentTimeMillis();
            exportTask.setDbTimeCost(exportTask.getDbTimeCost() + endTime - begTimeL);
            this.logger.info("{}-线程={},filePath={},导出完成...耗时={} 毫秒,大小={}行", this.module, Thread.currentThread().getName(), this.filePath, endTime - begTimeL, resultList.size());
            return resultList;
        }

    }


    /**
     * 数据写入excel实体，有其他需求可以重写
     **/
    protected void dataWrite(ExportTask exportTask, Sheet sheet, List<Map<String, Object>>resultList, List<CompletableFuture<Integer>> taskList) throws IOException, InterruptedException {
        this.limit += ExportThreadTask.OFFSET;
        // 判断是否开启异步写入
        if (this.useDbAsyn) {
            return;
        }
        if (this.useAsyn) {
            AsynSetRowDataThreadTask task = new AsynSetRowDataThreadTask(
                    this.fileName, sheet, resultList, this.keys, this.lastNumber, this.lastNumber + 1, this.lastNumber + ExportThreadTask.OFFSET, ExportThreadTask.OFFSET
            );
            CompletableFuture future = task.runFuture(DataThreadPoolExecutorUtils.SetDataThreadPoolSingleton.getInstance());
            taskList.add(future);
            this.lastNumber += resultList.size();
        } else {
            long begTimeL = System.currentTimeMillis();
            ExportPOIUtils.setRowData(sheet, resultList, keys, this.lastNumber + 1, false);
            long endTime = System.currentTimeMillis();
            exportTask.setExcelWriteTimeCost(exportTask.getExcelWriteTimeCost() + endTime - begTimeL);
            this.successCount += resultList.size();
            this.lastNumber += resultList.size();
            resultList.clear();
        }
        ThreadPoolExecutorUtils.ThreadPoolSingleton.setThreadRecordMap(this.fileName, this.toJsonObject());
        // TODO 这里可以适当的让线程放弃 cpu，进入等待状态，以防止长时间占用资源
//        Thread.sleep(1000);
    }

    /**
     * 异步数据库查询数据处理方法
     **/
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
     * 异步设置数据处理方法
     **/
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

    /**
     * 返回任务信息，用于记录 
     **/
    protected JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filePath", this.filePath);
        jsonObject.put("successCount", this.successCount);
        jsonObject.put("count", this.count);
        jsonObject.put("identity", this.identity);
        jsonObject.put("task", this);
        return jsonObject;
    }
}

