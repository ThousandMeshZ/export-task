package com.tmesh.exporttask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tmesh.exporttask.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : UserMapper
 * @updateUser : TMesh
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    
}