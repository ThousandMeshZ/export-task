package com.tmesh.exporttask.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tmesh.exporttask.dto.UserExportTaskDto;
import com.tmesh.exporttask.entity.UserEntity;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 用户服务
 * @updateUser : TMesh
 */
public interface UserSerivce extends IService<UserEntity> {

    void exportList(UserExportTaskDto userExportTaskDto);
    
    void exportListByAnnotation(UserExportTaskDto userExportTaskDto);
}
