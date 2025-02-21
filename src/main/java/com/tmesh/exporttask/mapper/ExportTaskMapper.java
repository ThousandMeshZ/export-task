package com.tmesh.exporttask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tmesh.exporttask.entity.ExportTask;
import org.apache.ibatis.annotations.*;

import java.util.Date;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 导出任务Mapper
 * @updateUser : TMesh
 */
@Mapper
public interface ExportTaskMapper extends BaseMapper<ExportTask> {
    @Insert("insert into export_task(MODULE, FILE_NAME, FILE_PATH, IDENTITY, STATUS, TYPE, CREATE_TIME, USER_NAME, UPDATE_TIME ) " +
            "values(#{module}, #{fileName}, #{filePath}, #{identity}, 0, 1, #{startTime}, #{userName}, #{startTime})")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
    long insertTaskInfo(@Param("module") String module, 
                        @Param("fileName") String fileName, 
                        @Param("filePath") String filePath, 
                        @Param("filePath") String identity, 
                        @Param("userName") String userName, 
                        @Param("startTime") Date startTime
    );

    @Update("update export_task set UPDATE_TIME= NOW(), STATUS = #{status}, REASON = #{failReason}  where STATUS = 0 and ID = #{identity}")
    long updateTaskInfo(@Param("status") int status,
                        @Param("identity") long identity,
                        @Param("reason") String reason
    );

    @Update("UPDATE export_task SET STATUS = 3 WHERE ID = #{id}")
    long deleteByTaskId(@Param("id") String id);
}