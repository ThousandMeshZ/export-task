package com.tmesh.exporttask.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : USER实体类
 * @updateUser : TMesh
 */
@TableName("USER")
@Data
@Accessors(chain = true)
public class UserEntity {

    /**
     * ID
     **/
    @TableId
    private long id;

    /**
     * 用户名
     **/
    private String name;
}
