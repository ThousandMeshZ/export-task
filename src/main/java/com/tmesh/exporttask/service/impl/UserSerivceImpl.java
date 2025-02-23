package com.tmesh.exporttask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tmesh.exporttask.common.thread.CommonExportThreadTask;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
 * 导出任务线程，继承自CommonExportThreadTask，这里为了方便写则作为内部类在服务实现类里面。可以单独作为一个任务类提出去
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
        super.run();
    }

    /**
     * 数据库数据查询操作，有其他需求可以重写
     **/
    @Override
    protected List<Map<String, Object>> dbDataHandle(ExportTask exportTask, Sheet sheet, QueryWrapper<UserEntity> newQueryWapper, List<CompletableFuture<Object>> taskList) {
        return super.dbDataHandle(exportTask, sheet, newQueryWapper, taskList);
    }

    /**
     * 数据写入excel实体，有其他需求可以重写
     **/
    @Override
    protected void dataWrite(ExportTask exportTask, Sheet sheet, List<Map<String, Object>>resultList, List<CompletableFuture<Integer>> taskList) throws IOException, InterruptedException {
        super.dataWrite(exportTask, sheet, resultList, taskList);
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
        super.useDbAsynHandle(wb, pageSheet, taskList, taskDbList, dbMap, sheet, exportTask, begTimeLDb);
    }

    /**
     * 异步设置数据处理方法，可以根据需求重写
     **/
    @Override
    protected void useAsynHandle(List<CompletableFuture<Integer>> taskList,
                                 ExportTask exportTask, long begTimeL) {
        super.useAsynHandle(taskList, exportTask, begTimeL);
    }
}
