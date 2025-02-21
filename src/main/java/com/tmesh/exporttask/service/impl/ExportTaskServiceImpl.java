package com.tmesh.exporttask.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tmesh.exporttask.entity.ExportTask;
import com.tmesh.exporttask.mapper.ExportTaskMapper;
import com.tmesh.exporttask.service.ExportTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 导出任务服务实现类
 * @updateUser : TMesh
 */
@Service("exportTaskService")
public class ExportTaskServiceImpl extends ServiceImpl<ExportTaskMapper, ExportTask> implements ExportTaskService {

    @Autowired
    private ExportTaskMapper exportTaskMapper;

    @Override
    public long insertTaskInfo(String module, String fileName, String filePath, String identity, String userName, Date startTime) {
        return this.exportTaskMapper.insertTaskInfo(module, fileName, filePath, identity, userName, startTime);
    }
    
    @Override
    public long updateTaskInfo(int status, long identity, String reason) {
        return this.exportTaskMapper.updateTaskInfo(status, identity, reason);
    }

    @Override
    public long deleteByTaskId(String id) {
        return this.exportTaskMapper.deleteByTaskId(id);
    }
}
