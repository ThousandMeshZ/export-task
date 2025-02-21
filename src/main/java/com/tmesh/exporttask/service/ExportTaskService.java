package com.tmesh.exporttask.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tmesh.exporttask.entity.ExportTask;

import java.util.Date;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 导出任务服务类
 * @updateUser : TMesh
 */
public interface ExportTaskService extends IService<ExportTask> {
    
    long insertTaskInfo(String module, String fileName, String filePath, String identity, String userName, Date startTime);
    
    long updateTaskInfo(int status, long identity, String reason);

    long deleteByTaskId(String id);
}
